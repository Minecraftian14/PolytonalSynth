package in.mcxiv.app.synth.env;

import in.mcxiv.app.util.MMath;
import in.mcxiv.app.util.i.WaveFunction;
import in.mcxiv.old.synth.tone.TonePressState;
import in.mcxiv.app.time.TimeEvent;
import in.mcxiv.app.pool.Poolable;

public class ScalarEnvironment extends Poolable implements TimeEvent, WaveFunction, Cloneable {

    private final EnvironmentMeta meta;

    private TonePressState state = TonePressState.INACTIVE;
    private int life = 0;
    private double velocity = 0;
    private double release_velocity_dummy;

    public ScalarEnvironment() {
        meta = new EnvironmentMeta();
    }

    public ScalarEnvironment(EnvironmentMeta meta) {
        this.meta = meta;
    }

    @Override
    public void tick() {
        switch (state) {
            case UNDEFINED -> throw new IllegalStateException("Not applicable for this object " + getClass());
            case INACTIVE -> inactiveTick();
            case TRIGGERED -> triggeringTick();
            case DELAY -> delayingTick();
            case ATTACK -> attackingTick();
            case HOLD -> holdingTick();
            case DECAY -> decayingTick();
            case PLAYING -> playingTick();
            case DE_TRIGGERED -> deTriggeringTick();
            case RELEASE -> releasingTick();
        }
    }

    private void inactiveTick() {
        // Awaiting a trigger.
        // May ensure if velocity and life are zero.
    }

    private void triggeringTick() {
        // Assuming that velocity and life is zero.
        state = TonePressState.DELAY;
        velocity = 0; // setting velocity for the sake of being processed by delayingTick()
        delayingTick(); // transforming current tick to a delay tick, so that this tick doesn't go wasted/idle.
    }

    private void delayingTick() {
        // Assuming velocity is zero
        if (meta.len_delay() == 0) /* if there is no delay required. */ {
            state = TonePressState.ATTACK;
            attackingTick(); // transform the current tick to attack.
        } else if (life < meta.len_delay()) /* if this event is still going on */ {
            life++;
        } else /* if this event is over */ {
            state = TonePressState.ATTACK;
            life = 0;
            attackingTick();
        }
    }

    private void attackingTick() {
        // Assuming velocity was zero when the event was started.
        if (meta.len_attack() == 0) /* if there is no attack required. */ {
            state = TonePressState.HOLD;
            velocity = 1; // setting velocity sharply to 1 (as required by hold)
            holdingTick(); // transform the current tick to hold.
        } else if (life < meta.len_attack()) /* if this event is still going on */ {
            velocity = MMath.lerp(1, 0, life++, meta.len_attack());
        } else /* if this event is over */ {
            state = TonePressState.HOLD;
            life = 0;
            velocity = 1; // perfect setting of velocity to 1
            holdingTick(); // transform the current tick to hold.
        }
    }

    private void holdingTick() {
        // Assuming velocity is 1
        if (meta.len_hold() == 0) /* if there is no hold required. */ {
            state = TonePressState.DECAY;
            decayingTick(); // transform the current tick to decay.
        } else if (life < meta.len_hold()) /* if this event is still going on */ {
            life++;
        } else /* if this event is over */ {
            state = TonePressState.DECAY;
            life = 0;
            decayingTick();
        }
    }

    private void decayingTick() {
        // Assuming velocity was 1 when the event was started.
        if (meta.len_decay() == 0) /* if there is no decay required. */ {
            state = TonePressState.PLAYING;
            velocity = meta.decay_velocity(); // setting velocity sharply to decay_velocity (as required by playingTick)
            playingTick(); // transform the current tick to play.
        } else if (life < meta.len_decay()) /* if this event is still going on */ {
            velocity = MMath.lerp(meta.decay_velocity(), 1, life++, meta.len_decay());
        } else /* if this event is over */ {
            state = TonePressState.PLAYING;
            life = 0;
            velocity = meta.decay_velocity(); // perfect setting of velocity to decay_velocity
            playingTick(); // transform the current tick to play.
        }
    }

    private void playingTick() {
        // Assuming velocity is decay_velocity
        /* well, this is an infinitely playing stage. */
    }

    private void deTriggeringTick() {
        // A de_trigger can happen at any stage of any state
        // of the environment. Therefore, we manage a separate
        // dummy variable to store whatever the current
        // velocity for easier interpolation.
        release_velocity_dummy = velocity;
        state = TonePressState.RELEASE;
        life = 0;
        releasingTick(); // transforming current tick to a release tick
    }

    private void releasingTick() {
        if (meta.len_release() == 0) /* if there is no release required. */ {
            state = TonePressState.INACTIVE;
            velocity = 0; // setting velocity sharply to 0 (default/ready state)
        } else if (life < meta.len_decay()) /* if this event is still going on */ {
            velocity = MMath.lerp(0, release_velocity_dummy, life++, meta.len_release());
        } else /* if this event is over */ {
            state = TonePressState.INACTIVE;
            life = 0;
            velocity = 0; // perfect setting of velocity to 0
        }
    }

    public void trigger() {
        if (state != TonePressState.INACTIVE)
            throw new IllegalStateException("The environment is still running!");
        state = TonePressState.TRIGGERED;
    }

    public void de_trigger() {
        if(state == TonePressState.DE_TRIGGERED || state == TonePressState.RELEASE)
            throw new IllegalStateException("The enironment is already closing!");
        state = TonePressState.DE_TRIGGERED;
    }

    @Override
    public double calculate(double t) {
        tick();
        return velocity;
    }

    @Override
    public boolean canBeReset() {
        return state == TonePressState.INACTIVE;
    }

    public TonePressState getState() {
        return state;
    }

    @Override
    public void reset() {
//        state = TonePressState.INACTIVE;
//        velocity = 0;
//        life = 0;
//        like really? If it can be reset, state is already INACTIVE,
//        and if state update system (tick()) is working
//        correctly, life == velocity == 0
    }

    @Override
    public ScalarEnvironment clone() {
        return new ScalarEnvironment(meta);
    }

}
