package fr.neatmonster.nocheatplus.event;

/**
 * Annotation to allow per-method method-order. Empty strings are regarded as "not set".
 * @author mc_dev
 *
 */
public @interface MethodOrder {
	public String tag() default "";
	public String beforeTag() default "";
}
