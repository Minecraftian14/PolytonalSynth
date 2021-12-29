package in.mcxiv.old.synth.io;

import in.mcxiv.app.util.i.WaveFunction;
import in.mcxiv.app.time.TimeEvent;

import javax.sound.sampled.*;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicLong;

public class SoundSpecialization extends CallbackBaseFeeder implements TimeEvent, AutoCloseable {

    public static final int SAMPLING_RATE = 44100;
    public static final double SAMPLE_LENGTH = 1d / SAMPLING_RATE;
    public static final int BITS_PER_SAMPLE = 16; // yup, definitely a short
    public static final int CHANNELS = 1;
    public static final int SAMPLE_SIZE = 2; // 2 bytes for short? IDK
    public static final double PI2 = 2 * Math.PI;

    private static final AudioFormat AUDIO_FORMAT = new AudioFormat(SAMPLING_RATE, BITS_PER_SAMPLE, CHANNELS, true, true);

    private WaveFunction function;

    private long samples_taken = 0;
    private double t = 0;

    private boolean started = true;

    DataLine.Info info;
    SourceDataLine line;
    ByteBuffer buffer;

    public SoundSpecialization(WaveFunction function) throws LineUnavailableException {
        this.function = function;
        info = new DataLine.Info(SourceDataLine.class, AUDIO_FORMAT);

        line = (SourceDataLine) AudioSystem.getLine(info);
        line.open(AUDIO_FORMAT);
        line.start();

        buffer = ByteBuffer.allocate(line.getBufferSize());
    }

    public short getSample() {
        samples_taken++;
        t += SAMPLE_LENGTH;
        return (short) (Short.MAX_VALUE * function.calculate(PI2 * t));
    }

    public ByteBuffer getBuffer() {
        buffer.clear();
        int samples = line.available() / SAMPLE_SIZE;
        for (int i = 0; i < samples; i++) buffer.putShort(getSample());
        return buffer;
    }

    public void init() {
        samples_taken = 0;
    }

    public void playStep() throws InterruptedException {
        ByteBuffer buffer = getBuffer();
        line.write(buffer.array(), 0, buffer.position());

        //Wait until the buffer is at least half empty  before we add more
        while (line.getBufferSize() / 2 < line.available())
            Thread.sleep(100);
    }

    @Override
    public void tick() {
        try {
            playStep();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() throws InterruptedException {
        started = false;
        WaveFunction _function = function;
        AtomicLong drain = new AtomicLong(SAMPLING_RATE * 2);
        function = t -> _function.calculate(t) * drain.getAndDecrement() / (SAMPLING_RATE * 2d);
        while (drain.get() > 0)
            playStep();
        line.drain();
        line.close();
    }

}
