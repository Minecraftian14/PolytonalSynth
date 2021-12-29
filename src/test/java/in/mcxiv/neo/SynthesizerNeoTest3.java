package in.mcxiv.neo;

import in.mcxiv.app.synth.SoundMachine;
import in.mcxiv.app.synth.SynthesizerNeo;
import in.mcxiv.app.synth.env.ScalarEnvironment;
import in.mcxiv.app.synth.Input;
import in.mcxiv.old.synth.tone.Tone;
import net.jafama.FastMath;

import javax.sound.sampled.LineUnavailableException;

class SynthesizerNeoTest3 {

    public static void main(String[] args) throws LineUnavailableException, InterruptedException {

        ScalarEnvironment environment = new ScalarEnvironment();
        Tone sinTone = new Tone() {
            @Override
            public double calculate(double t, double f) {
                return FastMath.sin(f * t);
            }
        };
        SynthesizerNeo synthesizer = new SynthesizerNeo(sinTone, environment);

        try (SoundMachine sp = new SoundMachine() {
            @Override
            public double getSample(double t) {
                return synthesizer.calculate(t);
            }
        }) {
            for (int i = 0; i < 100; i++) {
                int finalI = i;
                synthesizer.submit(new Input() {{
                    frequency = 440 * FastMath.pow(2, (finalI + 65 - 80) / 12d);
                }});
            }
            while (true)
                sp.playStepAndWait();
        }
    }


}