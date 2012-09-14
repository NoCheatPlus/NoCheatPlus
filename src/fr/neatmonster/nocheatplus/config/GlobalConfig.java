package fr.neatmonster.nocheatplus.config;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to indicate that a config path can only be set in the global configuration file. 
 * This can be added to parent fields, all other fields whose name starts with the name of the parent field will automatically be global only.
 * @author asofold
 *
 */
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface GlobalConfig {
}
