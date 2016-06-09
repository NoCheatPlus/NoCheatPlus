package fr.neatmonster.nocheatplus.utilities.collision;

/**
 * A collision checker much like RayTracing or AxisTracing.
 * 
 * @author asofold
 *
 */
public interface ICollide {

    /**
     * Set the maximum steps to be done during loop(). Setting to 0 should
     * disable the upper limit. (Integer.MAX_VALUE will be on the safe side as
     * well.)
     * 
     * @param maxSteps
     */
    public void setMaxSteps(int maxSteps);
    public int getMaxSteps();

    /**
     * Call before loop to set the coordinates of a move.
     * 
     * @param x0
     * @param y0
     * @param z0
     * @param x1
     * @param y1
     * @param z1
     */
    public void set(double x0, double y0, double z0, double x1, double y1, double z1);

    /**
     * Run the collision checking.
     */
    public void loop();

    /**
     * Get the (primary) steps done during loop(). In case of aborting due to
     * the max steps limit, the result should be greater or equal to
     * getMaxSteps().
     * 
     * @return
     */
    public int getStepsDone();

    /**
     * Test if the testing found a collision during loop().
     * @return
     */
    public boolean collides();

}
