package fr.neatmonster.nocheatplus.checks.moving;

/**
 * One entry of velocity for a player. Might be used per axis or for horizontal/vertical.
 * @author mc_dev
 *
 */
public class Velocity {
	
	/** The amount of velocity, decreasing with use. */
	public double value;
	/** "Some sum" for general purpose. 
	 * <li> For vertical entries this is used to alter the allowed y-distance to the set-back point. </li>
	 */
	public double sum;
	
	
	///////////////////////////
	// Activation conditions.
	///////////////////////////
	
	// TODO: Add more conditions (ticks, ?real time)
	
	/**
	 * Maximum count before activation.
	 */
	public int actCount;
	
	///////////////////////////
	// Invalidation conditions.
	///////////////////////////
	
	// TODO: Add more conditions (ticks, ?real time)
	
	/**
	 * Count .
	 */
	public int valCount;
	
	
	

	
	public Velocity(double value, int actCount, int valCount){
		
		this.value = value;
		
		this.actCount = actCount;
		
		this.valCount = valCount;
	}
}
