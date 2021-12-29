package in.mcxiv.app.util;

/**
 * To mark values or constants that cannot be changed
 * or should not be changed in development point of
 * view.
 */
public @interface Unconfigurable {
    String value() default "null";
}
