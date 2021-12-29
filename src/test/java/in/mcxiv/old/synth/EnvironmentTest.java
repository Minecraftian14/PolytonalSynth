package in.mcxiv.old.synth;

import in.mcxiv.old.synth.io.SoundSpecialization;
import in.mcxiv.old.synth.tone.Tone;

import javax.sound.sampled.LineUnavailableException;
import java.util.concurrent.atomic.AtomicInteger;

class EnvironmentTest {
    public static void main(String[] args) throws LineUnavailableException, InterruptedException {
        AtomicInteger i = new AtomicInteger(440);
        Environment environment = new Environment(new Tone() {
            @Override
            public double calculate(double t) {
                return Math.sin(i.get() * t);
            }
        });
        new Thread(() -> {
            try {
                Thread.sleep(1000);
                environment.trigger();
                Thread.sleep(5000);
                environment.de_trigger();
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
        try (SoundSpecialization sp = new SoundSpecialization(environment)) {
            sp.init();
            long cur = System.currentTimeMillis();
            while (System.currentTimeMillis() - cur < 10000)
                sp.playStep();
        }
    }
}