package in.mcxiv.old.synth.io;

import javax.sound.sampled.LineUnavailableException;
import java.util.concurrent.atomic.AtomicInteger;

class SoundSpecializationTest {
    public static void main(String[] args) throws LineUnavailableException, InterruptedException {
        AtomicInteger i = new AtomicInteger(440);
        try (SoundSpecialization sp = new SoundSpecialization(t -> Math.sin(i.get() * t))) {

            sp.init();
            long cur = System.currentTimeMillis();
            while (System.currentTimeMillis() - cur < 5000)
                sp.playStep();
        }
    }
}