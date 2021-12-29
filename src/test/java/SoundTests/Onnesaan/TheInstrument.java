package SoundTests.Onnesaan;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;
import java.nio.ByteBuffer;

public class TheInstrument implements AutoCloseable {

    public static final int SAMPLING_RATE = 44100;
    public static final double SAMPLE_LENGTH = 1d / SAMPLING_RATE;
    public static final int SAMPLE_SIZE = 2;
    public static final double PI2 = 2 * Math.PI;
    public static final double TROT = Math.pow(2d, 1d / 12d);

    private static final AudioFormat AUDIO_FORMAT = new AudioFormat(SAMPLING_RATE, 16, 1, true, true);

    // The instantaneous frequency, which is used in the sin function.
    private double frequency = 440;
    // The target freq, towards which frequency interpolates to.
    private double target = 440;

    // Amplitude modifier [0,1]
    private double velocity = 0;

    // Position in the sin graph (offset) (0 means start of the curve, 1 means after one full cycle)
    private double phase = 0;

    // in seconds
    private double glide_time = 2;

    private double glide_tart = 1 - 0.0125 / SAMPLING_RATE;

    private long samples_taken = 0;
    private double t = 0;

    private boolean started = true;

    DataLine.Info info;
    SourceDataLine line;
    ByteBuffer buffer;

    public TheInstrument() throws Exception {
        info = new DataLine.Info(SourceDataLine.class, AUDIO_FORMAT);

        line = (SourceDataLine) AudioSystem.getLine(info);
        line.open(AUDIO_FORMAT);
        line.start();

        buffer = ByteBuffer.allocate(line.getBufferSize());
    }

    public short getSample() {
        if (started) {
            if (samples_taken < glide_time * SAMPLING_RATE) if (velocity < 1) velocity += SAMPLE_LENGTH / glide_time;
        } else if (velocity > 0) velocity -= SAMPLE_LENGTH / glide_time;
        samples_taken++;
        t += SAMPLE_LENGTH;
        frequency = target + (frequency - target) * glide_tart;
        return (short) (Short.MAX_VALUE * velocity * Math.sin(PI2 * frequency * t));
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
//        while (line.getBufferSize() / 2 < line.available())
//            Thread.sleep(100);
    }

    public static void main(String[] args) throws Exception {
        try (TheInstrument instrument = new TheInstrument()) {
            instrument.init();
            long cur = System.currentTimeMillis();
            while (System.currentTimeMillis() - cur < 5000) instrument.playStep();
        }
    }

    @Override
    public void close() throws InterruptedException {
        started = false;
        while (velocity > 0)
            playStep();
        line.drain();
        line.close();
    }

    public void incTarget() {
        target *= TROT;
    }

    public void decTarget() {
        target /= TROT;
    }
}
