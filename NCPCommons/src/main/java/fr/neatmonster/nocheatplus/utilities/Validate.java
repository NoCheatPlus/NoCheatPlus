package fr.neatmonster.nocheatplus.utilities;

/**
 * Simple parameter/thing validation.
 * 
 * @author asofold
 *
 */
public class Validate {

    /**
     * Throw a NullPointerException if any given object is null.
     * 
     * @param objects
     * @throws NullPointerException
     *             If any object is null.
     */
    public static void validateNotNull(final Object...objects) {
        for (int i = 0; i < objects.length; i++) {
            if (objects[i] == null) {
                throw new NullPointerException("Object at index " + i + " is null.");
            }
        }
    }

}
