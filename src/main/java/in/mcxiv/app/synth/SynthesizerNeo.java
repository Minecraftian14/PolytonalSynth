package in.mcxiv.app.synth;

import in.mcxiv.app.synth.env.ScalarEnvironment;
import in.mcxiv.app.util.i.WaveFunction;
import in.mcxiv.old.synth.tone.Tone;
import in.mcxiv.app.pool.Pool;
import in.mcxiv.app.pool.Poolable;

import java.util.Iterator;

/**
 * An object to manage sounds of a single type.
 * <p>
 * Sounds as in having multiple inputs at any time and having them played simultaneously.
 */
public class SynthesizerNeo implements WaveFunction {

    Tone tone;
    Pool<Pair> pool;

    public SynthesizerNeo(Tone tone, ScalarEnvironment reference) {
        this.tone = tone;

        this.pool = new Pool<>(() -> new Pair(reference.clone()));
    }

    public void submit(Input input) {
        Pair pair = pool.acquire();
        pair.input = input;
        pair.environment.trigger();
    }

    @Override
    public double calculate(double t) {
        double addend = 0;
        int counter = 0;

        Iterator<Pair> iterator = pool.iterator();
        while (iterator.hasNext()) {
            Pair next = iterator.next();
            Input input = next.input;
            if (input == null) continue;
            ScalarEnvironment environment = next.environment;
            addend += environment.calculate(t) * tone.calculate(t, input.frequency);
            counter++;
            if (!input.active) {
                environment.de_trigger();
                input.active = true; // so that we dont set it to de trigerred again
            }
        }

        addend /= counter;
        return addend;
    }

    private static class Pair extends Poolable {
        Input input;
        ScalarEnvironment environment;

        public Pair() {
        }

        public Pair(ScalarEnvironment environment) {
            this.environment = environment;
        }

        @Override
        public boolean canBeReset() {
            return environment.canBeReset();
        }

        @Override
        public void reset() {
            environment.reset();
            input = null;
        }
    }

}
