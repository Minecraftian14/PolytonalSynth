package in.mcxiv.app.pool;

public abstract class Poolable {
    public abstract boolean canBeReset();

    public abstract void reset();
}
