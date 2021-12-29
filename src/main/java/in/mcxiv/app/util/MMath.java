package in.mcxiv.app.util;

public class MMath {
    /**
     * Calculates the linear interpolation from source to target by a factor.
     *
     * @param target Any number desired when the factor must be 1.
     * @param source Any number desired when the factor must be 0.
     * @param factor A number in the range [0, 1].
     * @return The interpolated result.
     */
    public static double lerp(double target, double source, double factor) {
        return source + factor * (target - source);
    }

    /**
     * Calculates the linear interpolation from source to target by a factor of unit / max.
     *
     * @param target Any number desired when the factor must be 1.
     * @param source Any number desired when the factor must be 0.
     * @param unit A number in the range [0, max].
     * @param max Any positive number.
     * @return The interpolated result.
     */
    public static double lerp(double target, double source, int unit, int max) {
        return lerp(target, source, unit * 1d / max);
    }
}
