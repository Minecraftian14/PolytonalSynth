package in.mcxiv.old.synth.tone;

public enum TonePressState {
    // Not used at all
    UNDEFINED,
    // Defined but currently not playing
    INACTIVE,
    // Have to start now
    TRIGGERED,
    // Being played, but on a lag
    DELAY,
    // Being played, just getting into volume
    ATTACK,
    // Being played, maintaining the volume
    HOLD,
    // Being played, reducing current volume to certain value
    DECAY,
    // Being played at decay velocity. Awaiting release
    PLAYING,
    // Being played, have to release now
    DE_TRIGGERED,
    // Being played, reducing current volume to zero
    RELEASE
}
