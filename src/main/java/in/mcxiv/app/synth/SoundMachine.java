package in.mcxiv.app.synth;

import in.mcxiv.app.util.Automatic;
import in.mcxiv.app.util.Configurable;
import in.mcxiv.app.util.Unconfigurable;

import javax.sound.sampled.*;
import java.io.Closeable;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * This class has an abstract method, which should implemented to
 * be able to give th t^th sound sample. t is just the time elapsed
 * since it's creation/start.
 * <p>
 * This class contains the constants and other resources readily
 * waiting for a buffer to be filled up of samples, after which,
 * it instantly starts playing it, repeating it to produce a
 * continuous effect.
 */
public abstract class SoundMachine implements AutoCloseable, Closeable {

    /**
     * Specifically there are a bunch of constants to keep in mind.
     * <ol>
     *     <li>Sample Data Type = Short</li>
     *     <li>Sample Bit Size = 16</li>
     *     <li>Sample Byte Size = 2</li>
     * </ol>
     * <p>
     * It's kinda configurable, one may increase it to a higher data type such as int or even long.
     * But that will require a lot of modifying like the return types everywhere.
     */
    @Unconfigurable("Requires a lot of refactoring.")
    public static final int SAMPLE_DATATYPE_BIT_SIZE = Short.SIZE;

    /**
     * Number of samples required to represent a single second of sound.
     * This also means, that getSample is called ~SAMPLES_PER_SECOND times
     * in one second xD.
     */
    @Configurable("32000/44100/48000/88200/96000/192000")
    public static final int SAMPLES_PER_SECOND = 44100;

    /**
     * The time elapsed between two samples.
     */
    @Automatic
    public static final double SAMPLE_TIME_PERIOD = 1d / SAMPLES_PER_SECOND;

    /**
     * refer SAMPLE_DATATYPE_BIT_SIZE doc.
     */
    @Automatic
    public static final int BITS_PER_SAMPLE = SAMPLE_DATATYPE_BIT_SIZE;

    /**
     * TODO
     */
    @Configurable
    public static final int CHANNELS = 1;

    /**
     * refer SAMPLE_DATATYPE_BIT_SIZE doc.
     */
    @Automatic
    public static final int SAMPLE_BYTE_SIZE = SAMPLE_DATATYPE_BIT_SIZE / 8;

    /**
     * A collection of constants defined above, it's required
     * to tell the java audio api what kind of connection we want.
     */
    public static final AudioFormat AUDIO_FORMAT = new AudioFormat(SAMPLES_PER_SECOND, BITS_PER_SAMPLE, CHANNELS, true, true);

    /**
     * Amount of time we wait while buffer is half emptied.
     * <p>
     * The smallest packet of time such that the amount of
     * time spent waiting can only exist as a natural multiple
     * of this quantum!
     * <p>
     * It's in milliseconds.
     */
    public static final long BUFFER_CONSUMPTION_WAIT_LENGTH = 100;

    public static final double PI2 = 2 * Math.PI;
    public static final short zero = 0; // Why am I doing this?

    /**
     * Poor fella, we just never used it...
     * Counts the number of samples created so far.
     */
    private long samples_created = 0;

    /**
     * This variable doesn't exactly keep track of time,
     * rather it's mathematically equal to samples_created * SAMPLE_TIME_PERIOD
     */
    private double time_spent = 0;

    /**
     * A var to know that we haven't closed the line yet.
     */
    private boolean isClosed = true;

    /**
     * A buffer to easily store short values in a byte array.
     */
    private final ByteBuffer buffer;

    /**
     * Just a packet of what kind of data line and the
     * format of information we require.
     */
    private final DataLine.Info info;

    /**
     * The main channel where we feed the bytes to be played.
     * <p>
     * Note that `line.available()` gives the number of bytes
     * which can be fed to it.
     */
    private final SourceDataLine line;

    public SoundMachine() throws LineUnavailableException {

        info = new DataLine.Info(SourceDataLine.class, AUDIO_FORMAT);

        line = (SourceDataLine) AudioSystem.getLine(info);
        System.out.println("line.getClass().getSimpleName() = " + line.getClass().getSimpleName()); // -> DirectSDL

        line.open(AUDIO_FORMAT);
        line.start();

        int bufferSize = line.getBufferSize();
        System.out.println("bufferSize = " + bufferSize);
        buffer = ByteBuffer.allocate(bufferSize);

        isClosed = false;
    }

    /**
     * @param t total time spent since the start.
     * @return the amplitude [-1,1] at time t.
     */
    public abstract double getSample(double t);

    /**
     * @return calculates and returns one sample, such that the next time it's called, it gives the next sample.
     */
    public short getSample() {
        samples_created++;
        time_spent += SAMPLE_TIME_PERIOD;
        return (short) (Short.MAX_VALUE * getSample(PI2 * time_spent));
    }

    /**
     * Clears and fills the buffer just enough to feed all the new values to the data line.
     */
    public void fillBuffer() {
        buffer.clear();
        int length = line.available() / SAMPLE_BYTE_SIZE;
        for (int i = 0; i < length; i++) buffer.putShort(getSample());
    }

    /**
     * Fill up the buffer and play it.
     *
     * @throws InterruptedException
     */
    public void playStep() throws InterruptedException {
        if (isClosed) return;
        fillBuffer();
        line.write(buffer.array(), 0, buffer.position());
    }

    /**
     * Fill up the buffer, play it and wait while at least half the buffer is used up.
     * <p>
     * FIXME: Is audio played on another thread? Aren't we just interfering by pausing the app midway?
     *
     * @throws InterruptedException
     */
    public void playStepAndWait() throws InterruptedException {
        if (isClosed) return;
        fillBuffer();
        line.write(buffer.array(), 0, buffer.position());
        // The buffer size is 44100, contains 22050 samples, which is half a second.
        while (line.available() < line.getBufferSize() / 2) Thread.sleep(BUFFER_CONSUMPTION_WAIT_LENGTH);
    }

    /**
     * Essentially stops all other method (and itself) for being
     * responsive again. Then it fades away whatever is playing
     * for 2 second to 0. Finally, closing everything.
     */
    @Override
    public void close() {
        if (isClosed) return;

        isClosed = true;

        final double fadeTimePeriod = SAMPLE_TIME_PERIOD / 2; // divide SAMPLE_TIME_PERIOD by 2 to extend the fade for 2 seconds

        double fadeCoefficient = 1; // aka master velocity

        try {
            while (fadeCoefficient > 0) {

                buffer.clear();
                int length = line.available() / SAMPLE_BYTE_SIZE;
                for (int i = 0; i < length; i++) {
                    if (fadeCoefficient > 0) buffer.putShort((short) (getSample() * fadeCoefficient));
                    else buffer.putShort(zero);
                    fadeCoefficient -= fadeTimePeriod;
                }

                line.write(buffer.array(), 0, buffer.position());
                while (line.available() < line.getBufferSize() / 2) Thread.sleep(BUFFER_CONSUMPTION_WAIT_LENGTH);

            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        line.drain();
        line.stop();
        line.close();
    }

    /**
     * Creates two threads
     */
    public void activitise() {

        final Runnable playStep = () -> {
            try {
                playStep();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };

        final ExecutorService sound_playing_thread = Executors.newSingleThreadExecutor();

        Thread sleep_wait_thread = new Thread(() -> {
            try {
                while (!isClosed) {
                    sound_playing_thread.submit(playStep);
                    while (line.available() < line.getBufferSize() / 2)
                        Thread.sleep(BUFFER_CONSUMPTION_WAIT_LENGTH);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        sleep_wait_thread.start();
    }

}
