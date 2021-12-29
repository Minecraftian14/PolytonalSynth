package SoundTests.Twoengua;

import in.mcxiv.old.synth.Environment;
import in.mcxiv.old.synth.tone.Tone;
import in.mcxiv.app.time.Clock;

public class GettingSometihingUp {

    public static void main(String[] args) {
        Environment env = new Environment(new SinTone());
        Clock clock = new Clock(env);
        clock.register();


    }

    public static final class SinTone extends Tone {
        int t = 0;

        public double next() {
            if (t > 44100) t -= 44100;
            return Math.sin(2 * Math.PI * 440 * t++ / 44100);
        }
    }
}
