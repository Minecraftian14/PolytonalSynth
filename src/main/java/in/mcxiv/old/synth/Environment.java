package in.mcxiv.old.synth;

import in.mcxiv.app.util.i.WaveFunction;
import in.mcxiv.old.synth.tone.Tone;
import in.mcxiv.old.synth.tone.TonePressState;
import in.mcxiv.app.time.TimeEvent;

public class Environment implements TimeEvent, WaveFunction, Cloneable {

    private final Tone[] tones;
    TonePressState state = TonePressState.INACTIVE;

    private double len_delay = 44100;
    private double len_attack = 44100;
    private double len_hold = 44100;
    private double len_decay = 44100;
    private double len_release = 44100;

    private long life;
    private double velocity = 0;
    private double decay_velocity = 0.5;
    private double release_velocity_dummy;

    public Environment(Tone... tones) {
        this.tones = tones;
    }

    public Environment(Tone[] tones, double len_delay, double len_attack, double len_hold, double len_decay, double len_release, double decay_velocity) {
        this.tones = tones;
        this.len_delay = len_delay;
        this.len_attack = len_attack;
        this.len_hold = len_hold;
        this.len_decay = len_decay;
        this.len_release = len_release;
        this.decay_velocity = decay_velocity;
    }

    @Override
    public void tick() {
        switch (state) {
            case UNDEFINED -> {
            }
            case INACTIVE -> {
                // Awaiting for new trigger
            }
            case TRIGGERED -> {
                life = 0;
                velocity = 0;
                state = len_delay != 0 ? TonePressState.DELAY
                        : len_attack != 0 ? TonePressState.ATTACK
                        : len_hold != 0 ? TonePressState.HOLD
                        : len_decay != 0 ? TonePressState.DECAY
                        : TonePressState.RELEASE;
                if (state != TonePressState.DELAY && state != TonePressState.ATTACK) velocity = 1;
            }
            case DELAY -> {
                if (life > len_delay) {
                    state = TonePressState.ATTACK;
                    life = 0;
                }
            }
            case ATTACK -> {
                if (life > len_attack) {
                    state = TonePressState.HOLD;
                    life = 0;
                } else {
                    velocity = life * 1d / len_attack;
                }
            }
            case HOLD -> {
                if (life > len_hold) {
                    state = TonePressState.DECAY;
                    life = 0;
                }
            }
            case DECAY -> {
                if (life > len_decay) {
                    // Do nothing until release event takes place.
                } else {
                    velocity = 1 + (decay_velocity - 1) * life * 1d / len_decay;
                }
            }
            case DE_TRIGGERED -> {
                release_velocity_dummy = velocity;
                state = TonePressState.RELEASE;
                life = 0;
            }
            case RELEASE -> {
                if (life > len_release) {
                    state = TonePressState.INACTIVE;
                    velocity = 0; // hard reset to zero.
                } else {
                    velocity = release_velocity_dummy - release_velocity_dummy * life * 1d / len_release;
                }
            }
        }
        life++;
    }

    public void trigger() {
        state = TonePressState.TRIGGERED;
    }

    public void de_trigger() {
        state = TonePressState.DE_TRIGGERED;
    }

    @Override
    public double calculate(double t) {
        tick();
        if (velocity == 0) return 0;
        double addend = 0;
        for (int i = 0; i < tones.length; i++) addend += tones[i].calculate(t);
        addend *= velocity / tones.length;
        return addend;
    }

    @Override
    public Environment clone() {
        return new Environment(tones, len_delay, len_attack, len_hold, len_decay, len_release, decay_velocity);
    }
}
