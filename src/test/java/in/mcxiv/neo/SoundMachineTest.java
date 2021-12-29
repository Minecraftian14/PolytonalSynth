package in.mcxiv.neo;

import in.mcxiv.app.synth.SoundMachine;

import javax.sound.sampled.LineUnavailableException;

class SoundMachineTest {
    public static void main(String[] args) throws LineUnavailableException, InterruptedException {
        try (SoundMachine machine = new SoundMachine() {
            @Override
            public double getSample(double t) {
                return Math.sin(440*t);
            }
        }) {
            long ct = System.currentTimeMillis();
            while (System.currentTimeMillis() - ct < 10000) {
                machine.playStepAndWait();
    //            Thread.sleep(100);
            }
        }
    }
}