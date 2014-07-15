package fr.neatmonster.nocheatplus.checks.fight;

/**
 * Context data for the reach check, for repeated use within a loop.
 * @author mc_dev
 *
 */
public class ReachContext {
	
	public double distanceLimit;
	public double distanceMin;
	public double damagedHeight;
	/** Attacking player. */
	public double eyeHeight;
	/** Eye location y of the attacking player. */
	public double pY;
	/** Minimum value of lenpRel that was a violation. */
	public double minViolation = Double.MAX_VALUE;
	/** Minimum value of lenpRel. */
	public double minResult = Double.MAX_VALUE;

}
