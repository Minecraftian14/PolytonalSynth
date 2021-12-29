package in.mcxiv.app.util;

/**
 * To mark values or constants that can be changed
 * in development point of view.
 */
public @interface Configurable {
    String value() default "null";
}
