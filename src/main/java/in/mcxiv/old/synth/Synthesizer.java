package in.mcxiv.old.synth;

import in.mcxiv.app.synth.Input;
import in.mcxiv.app.util.i.WaveFunction;
import in.mcxiv.old.synth.tone.Tone;
import in.mcxiv.old.synth.tone.TonePressState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * An object to manage sounds of a single type.
 * <p>
 * Sounds as in having multiple inputs at any time and having them played simultaneously.
 */
public class Synthesizer implements WaveFunction {

    Tone tone;
    EnvironmentNeo reference;

    HashMap<Input, EnvironmentNeo> inputs = new HashMap<>();

    public Synthesizer(Tone tone, EnvironmentNeo reference) {
        this.tone = tone;
        this.reference = reference;
    }

    public void submit(Input input) {
        EnvironmentNeo environment = reference.clone();
        environment.trigger();
        inputs.put(input, environment);
    }

    List<Input> tbr = new ArrayList<>();

    @Override
    public double calculate(double t) {
        double addend = 0;
        Set<Input> keys = inputs.keySet();
        int size = keys.size();

        for (Input key : keys) {
            EnvironmentNeo environment = inputs.get(key);
            addend += environment.calculate(t) * tone.calculate(t, key.frequency);
            if (environment.getState() == TonePressState.INACTIVE) tbr.add(key);
            else if (!key.active)
                if (environment.getState() != TonePressState.DE_TRIGGERED && environment.getState() != TonePressState.RELEASE)
                    environment.de_trigger();
        }

        tbr.forEach(key -> inputs.remove(key));
        tbr.clear();

        addend /= size;
        return addend;
    }
}
