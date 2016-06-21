package fr.neatmonster.nocheatplus.utilities.collision;

import fr.neatmonster.nocheatplus.components.location.IGetPosition;

/**
 * Collide a ray with an axis aligned bounding box (AABB). Allow fetching the
 * point of time, when the ray is closest to the AABB. The methods getX|Y|Z from
 * IGetPosition will fetch the coordinates of collision or the nearest point (if
 * set so).
 * 
 * @author asofold
 *
 */
public interface ICollideRayVsAABB extends IGetPosition {

    // TODO: Convenience methods to retrieve distances (especially in case of not colliding).
    // TODO: Convenience methods for other argument types (vector, IGet..., double[], setAABB(block/ints).
    // TODO: Implement fight.visible, use in BlockBreak.direction/visible.

    /**
     * Set the start and direction of the ray. This can be called independently
     * of other calls to setAABB and loop (etc.), in order to reiterate with an
     * altered ray to start with.
     * 
     * @param startX
     *            Starting point.
     * @param startY
     * @param startZ
     * @param dirX
     *            Direction of the ray. One time unit matches the length of this
     *            vector.
     * @param dirY
     * @param dirZ
     * @return The same instance for chaining.
     */
    public ICollideRayVsAABB setRay(double startX, double startY, double startZ,
            double dirX, double dirY, double dirZ);

    /**
     * Set the properties of the AABB. This can be called independently of
     * setRay and loop, in order to reiterate with a different AABB to check
     * against.
     * 
     * @param targetX
     *            Bottom center coordinates of the AABB, e.g. the foot location
     *            of a player.
     * @param targetY
     * @param targetZ
     * @param boxMarginHorizontal
     * @param boxMArginVertical
     * @return The same instance for chaining.
     */
    public ICollideRayVsAABB setAABB(double targetX, double targetY, double targetZ,
            double boxMarginHorizontal, double boxMArginVertical);

    /**
     * Set if the nearest point is to be estimated instead, in case the ray does
     * not collide.
     * 
     * @param findNearestPointIfNotCollide
     *            If set to true, getTime/getX/getY/getZ will return the point
     *            on the ray that is closest to the AABB (Implementation may
     *            allow some imprecision, as well as starting from 0 always).
     * @return The same instance for chaining.
     */
    public ICollideRayVsAABB setFindNearestPointIfNotCollide(boolean findNearestPointIfNotCollide);

    /**
     * Test if the nearest point is to be estimated instead, in case the ray
     * does not collide.
     * 
     * @return
     */
    public boolean getFindNearestPointIfNotCollide();

    /**
     * Run the actual checking once. This can be run multiple times with varying
     * arguments independently of setRay and setAABB (etc.).
     * 
     * @return The same instance for chaining.
     */
    public ICollideRayVsAABB loop();

    /**
     * Just fetch the result after calling loop.
     * 
     * @return If the ray collides the AABB
     */
    public boolean collides();

    /**
     * Time of collision if collides() returns true, or of the nearest point,
     * counted in times of applying the direction.
     * 
     * @return Time in multiples of the initial direction vector. In case it's
     *         not possible at all, 0.0 might be returned.
     */
    public double getTime();

}
