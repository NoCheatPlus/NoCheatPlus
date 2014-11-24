package fr.neatmonster.nocheatplus.checks.fight;

import org.bukkit.util.Vector;

/**
 * Context data for the direction check, for repeated use within a loop.
 * @author mc_dev
 *
 */
public class DirectionContext {

    public boolean damagedComplex;
    public double damagedWidth;
    public double damagedHeight;
    public Vector direction = null;
    public double lengthDirection;

    /** Minimum value for the distance that was a violation. */
    public double minViolation = Double.MAX_VALUE;
    /** Minimum value for off. */
    public double minResult = Double.MAX_VALUE;

}
