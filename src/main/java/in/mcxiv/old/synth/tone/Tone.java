package in.mcxiv.old.synth.tone;

import in.mcxiv.app.util.i.WaveFunction;

public abstract class Tone implements WaveFunction {
    /**
     * Override this function if time and frequency are not multiplied together in the wave equation.
     *
     * @param t time
     * @param f frequency
     * @return amplitude
     */
    public double calculate(double t, double f) {
        return calculate(t * f);
    }

    @Override
    public double calculate(double t) {
        return 0;
    }
}
