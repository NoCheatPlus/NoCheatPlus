package fr.neatmonster.nocheatplus.utilities.collision;

public class CollideRayVsAABB implements ICollideRayVsAABB {

    private boolean findNearestPointIfNotCollide = false;
    /** Start point and direction. */
    private double startX, startY, startZ, dirX, dirY, dirZ;
    /** Target box (AABB). */
    private double minX, minY, minZ, maxX, maxY, maxZ;

    /** Collision or closest point. */
    private double closestX, closestY, closestZ, closestTime;
    /**
     * Indicate a collision occurred. Reset with calling loop only.
     */
    private boolean collides;

    @Override
    public ICollideRayVsAABB setRay(double startX, double startY, double startZ, 
            double dirX, double dirY, double dirZ) {
        // Set from parameters.
        this.startX = startX;
        this.startY = startY;
        this.startZ = startZ;
        this.dirX = dirX;
        this.dirY = dirY;
        this.dirZ = dirZ;
        return this;
    }

    @Override
    public ICollideRayVsAABB setAABB(double targetX, double targetY, double targetZ, 
            double boxMarginHorizontal, double boxMarginVertical) {
        // Set from parameters.
        this.minX = targetX - boxMarginHorizontal;
        this.minY = targetY;
        this.minZ = targetZ - boxMarginHorizontal;
        this.maxX = targetX + boxMarginHorizontal;
        this.maxY = targetY + boxMarginVertical;
        this.maxZ = targetZ + boxMarginHorizontal;
        return this;
    }

    @Override
    public ICollideRayVsAABB setFindNearestPointIfNotCollide(boolean findNearestPointIfNotCollide) {
        this.findNearestPointIfNotCollide = findNearestPointIfNotCollide;
        return this;
    }

    @Override
    public boolean getFindNearestPointIfNotCollide() {
        return findNearestPointIfNotCollide;
    }

    @Override
    public ICollideRayVsAABB loop() {
        // Reset results.
        collides = false;
        closestX = startX;
        closestY = startY;
        closestZ = startZ;
        closestTime = 0.0;
        // Determine basic orientation and timing.
        final double tMinX = CollisionUtil.getMinTimeIncludeEdges(startX, dirX, minX, maxX);
        final double tMinY = CollisionUtil.getMinTimeIncludeEdges(startY, dirY, minY, maxY);
        final double tMinZ = CollisionUtil.getMinTimeIncludeEdges(startZ, dirZ, minZ, maxZ);
        final double tMaxX = CollisionUtil.getMaxTimeIncludeEdges(startX, dirX, minX, maxX, tMinX);
        final double tMaxY = CollisionUtil.getMaxTimeIncludeEdges(startY, dirY, minY, maxY, tMinY);
        final double tMaxZ = CollisionUtil.getMaxTimeIncludeEdges(startZ, dirZ, minZ, maxZ, tMinZ);
        if (tMaxX != Double.NaN && tMaxY != Double.NaN && tMaxZ != Double.NaN) {
            // (Excludes any tMin value to be Double.MAX_VALUE.)
            // Determine if there is overlapping intervals.
            final double tMin = Math.max(tMinX, Math.max(tMinY, tMinZ));
            final double tMax = Math.min(tMaxX, Math.min(tMaxY, tMaxZ));
            if (tMin <= tMax) {
                collides = true;
                closestX = startX + dirX * tMin;
                closestY = startY + dirY * tMin;
                closestZ = startZ + dirZ * tMin;
            }
            else if (findNearestPointIfNotCollide) {
                findNearestPoint(tMinX, tMinY, tMinZ, tMaxX, tMaxY, tMaxZ, tMin, tMax);
            }
        }
        else if (findNearestPointIfNotCollide) {
            findNearestPoint(tMinX, tMinY, tMinZ, tMaxX, tMaxY, tMaxZ);
        }
        return this;
    }

    /**
     * Find the nearest point for the case of not hitting the box, but with
     * hitting min-max coordinates per axis independently.
     * 
     * @param tMinX
     * @param tMinY
     * @param tMinZ
     * @param tMaxX
     * @param tMaxY
     * @param tMaxZ
     * @param tMin
     * @param tMax
     */
    private void findNearestPoint(final double tMinX, final double tMinY, final double tMinZ, 
            final double tMaxX, final double tMaxY, final double tMaxZ,
            final double tMin, final double tMax) {
        // TODO: Implement.
    }

    /**
     * Estimate the nearest point for the case of at least one of the
     * coordinates not being possible to match at all.
     * 
     * @param tMinX
     * @param tMinY
     * @param tMinZ
     * @param tMaxX
     * @param tMaxY
     * @param tMaxZ
     */
    private void findNearestPoint(final double tMinX, final double tMinY, final double tMinZ, 
            final double tMaxX, final double tMaxY, final double tMaxZ) {
        // TODO: Implement.
    }

    @Override
    public boolean collides() {
        return collides;
    }

    @Override
    public double getX() {
        return closestX;
    }

    @Override
    public double getY() {
        return closestY;
    }

    @Override
    public double getZ() {
        return closestZ;
    }

    @Override
    public double getTime() {
        return closestTime;
    }

}
