package in.mcxiv.app.synth.env;

import static in.mcxiv.app.synth.SoundMachine.SAMPLES_PER_SECOND;

public record EnvironmentMeta(int len_delay, int len_attack, int len_hold, int len_decay, int len_release,
                              double decay_velocity) {
    public EnvironmentMeta() {
        this(0, SAMPLES_PER_SECOND / 10, SAMPLES_PER_SECOND, SAMPLES_PER_SECOND, SAMPLES_PER_SECOND, 0.25);
    }

    public EnvironmentMeta(double len_delay, double len_attack, double len_hold, double len_decay, double len_release, double decay_velocity) {
        this(
                (int) (len_delay * SAMPLES_PER_SECOND),
                (int) (len_attack * SAMPLES_PER_SECOND),
                (int) (len_hold * SAMPLES_PER_SECOND),
                (int) (len_decay * SAMPLES_PER_SECOND),
                (int) (len_release * SAMPLES_PER_SECOND),
                decay_velocity);
    }
}
