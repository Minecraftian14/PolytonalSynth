package in.mcxiv.app.synth;

/**
 * A class to represent the event when a key is pressed.
 * <p>
 * Note, that it's construction doesn't play it directly!
 * Gotta feed it to a synthesizer too.
 */
public class Input {

    // [0-inf)  in Hertz
    public double frequency = 440;

    // [0-1]
    public double pressure = 1;

    // [-1, 1]
    public double joystick_a = 0, joystick_b = 0, joystick_c = 0;

    // true --> just pressed (just constructed), false --> just released (to be finished playing and be destroyed)
    public boolean active = true;

}
