package fr.neatmonster.nocheatplus.components.registry.order;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Empty string counts as null.
 * @author asofold
 *
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface RegisterWithOrder {
    /** Crude workaround for an Integer that may be null. */
    public String basePriority() default "";
    public String tag() default "";
    public String beforeTag() default "";
    public String afterTag() default "";
}
