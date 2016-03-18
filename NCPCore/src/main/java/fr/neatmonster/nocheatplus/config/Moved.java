package fr.neatmonster.nocheatplus.config;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A configuration path that has been relocated. newPath may be empty (relocated "somewhere").<br>
 * Note that only individual entries can be relocated at present, not sections.
 * @author mc_dev
 *
 */
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Moved {

    /**
     * The new path where the content has moved to.
     * @return
     */
    public String newPath() default "";

    /**
     * Only added to be able to explicitly deny moving configuration sections.
     * Moving configuration sections might not actually be supported.
     * 
     * @return If to allow moving configuration sections.
     */
    public boolean configurationSection() default false;
}
