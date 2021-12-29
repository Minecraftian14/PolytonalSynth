package in.mcxiv.neo;

import in.mcxiv.UIj;
import in.mcxiv.app.synth.SoundMachine;
import in.mcxiv.app.synth.SynthesizerNeo;
import in.mcxiv.app.synth.env.ScalarEnvironment;
import in.mcxiv.app.synth.Input;
import in.mcxiv.old.synth.tone.Tone;
import net.jafama.FastMath;

import javax.sound.sampled.LineUnavailableException;
import java.util.HashMap;

class SynthesizerNeoTest2 {

    public static void main(String[] args) throws LineUnavailableException, InterruptedException {

        ScalarEnvironment environment = new ScalarEnvironment();
        Tone sinTone = new Tone() {
            @Override
            public double calculate(double t, double f) {
                return FastMath.sin(f * t);
            }
        };
        SynthesizerNeo synthesizer = new SynthesizerNeo(sinTone, environment);

        HashMap<Integer, Input> map = new HashMap<>();
        new UIj(new UIj.CallBackDevice() {
            @Override
            public void down(int id) {

                Input input = new Input() {{
                    frequency = 440 * FastMath.pow(2, (id - 80) / 12d);
                }};
                synthesizer.submit(input);
                map.put(id, input);
                System.out.println("id d = " + id);

            }

            @Override
            public void up(int id) {
                Input input = map.get(id);
                if (input != null) input.active = false;
                map.remove(id);
                System.out.println("id u = " + id);
            }
        });


        try (SoundMachine sp = new SoundMachine() {
            @Override
            public double getSample(double t) {
                return synthesizer.calculate(t);
            }
        }) {
            Runtime.getRuntime().addShutdownHook(new Thread(sp::close));
            while (true)
                sp.playStepAndWait();
        }
    }


}