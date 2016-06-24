package fr.neatmonster.nocheatplus.utilities.collision;

public class CollideRayVsAABB implements ICollideRayVsAABB {

    private boolean findNearestPointIfNotCollide = false;
    /** Start point and direction. */
    private double startX, startY, startZ, dirX, dirY, dirZ;
    /** Target box (AABB). */
    private double minX, minY, minZ, maxX, maxY, maxZ;

    /** Collision or closest point. */
    private double closestX, closestY, closestZ, closestDistanceSquared, closestTime;
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
        return this.setAABB(targetX - boxMarginHorizontal, targetY, targetZ - boxMarginHorizontal,
                targetX + boxMarginHorizontal, targetY + boxMarginVertical, targetZ + boxMarginHorizontal);
    }

    @Override
    public ICollideRayVsAABB setAABB(int targetX, int targetY, int targetZ, double margin) {
        return setAABB(-margin + targetX, -margin + targetY, -margin + targetZ, 
                1.0 + margin + targetX, 1.0 + margin + targetY, 1.0 + margin + targetZ);
    }

    @Override
    public ICollideRayVsAABB setAABB(double minX, double minY, double minZ, 
            double maxX, double maxY, double maxZ) {
        this.minX = minX;
        this.minY = minY;
        this.minZ = minZ;
        this.maxX = maxX;
        this.maxY = maxY;
        this.maxZ = maxZ;
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
        closestDistanceSquared = 0.0; // Not applicable by default.
        closestTime = 0.0;
        // Determine basic orientation and timing.
        final double tMinX = CollisionUtil.getMinTimeIncludeEdges(startX, dirX, minX, maxX);
        final double tMinY = CollisionUtil.getMinTimeIncludeEdges(startY, dirY, minY, maxY);
        final double tMinZ = CollisionUtil.getMinTimeIncludeEdges(startZ, dirZ, minZ, maxZ);
        final double tMaxX = CollisionUtil.getMaxTimeIncludeEdges(startX, dirX, minX, maxX, tMinX);
        final double tMaxY = CollisionUtil.getMaxTimeIncludeEdges(startY, dirY, minY, maxY, tMinY);
        final double tMaxZ = CollisionUtil.getMaxTimeIncludeEdges(startZ, dirZ, minZ, maxZ, tMinZ);
        //System.out.println("TIMING: " + tMinX + " " + tMinY + " " + tMinZ + " " + tMaxX + " " + tMaxY + " " + tMaxZ);
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
                closestTime = tMin;
            }
            else if (findNearestPointIfNotCollide) {
                findNearestPoint(tMinX, tMinY, tMinZ, tMaxX, tMaxY, tMaxZ);
            }
        }
        else if (findNearestPointIfNotCollide) {
            findNearestPoint(tMinX, tMinY, tMinZ, tMaxX, tMaxY, tMaxZ);
        }
        return this;
    }

    /**
     * Estimate the nearest point for the case of at least one of the
     * coordinates not being possible to match at all. Asserts closestX|Y|Z to
     * be set to the start coordinates.
     * 
     * @param tMinX
     * @param tMinY
     * @param tMinZ
     * @param tMaxX
     * @param tMaxY
     * @param tMaxZ
     */
    private void findNearestPoint(final double... timeValues) {
        // TODO: Squared vs. Manhattan vs. maxAxis.
        // Update squared distance to 'actual'.
        closestDistanceSquared = CollisionUtil.getSquaredDistAABB(this.startX, this.startY, this.startZ, 
                minX, minY, minZ, maxX, maxY, maxZ);
        // Find the closest point using set time values.
        for (int i = 0; i < timeValues.length; i++) {
            final double time = timeValues[i];
            if (time == Double.NaN || time == Double.POSITIVE_INFINITY) {
                // Note that Double.POSITIVE_INFINITY could mean that we are either colliding forever, or never.
                continue;
            }
            final double x = startX + dirX * time;
            final double y = startY + dirY * time;
            final double z = startZ + dirZ * time;
            final double distanceSquared = CollisionUtil.getSquaredDistAABB(x, y, z, 
                    minX, minY, minZ, maxX, maxY, maxZ);
            if (distanceSquared < closestDistanceSquared) {
                closestX = x;
                closestY = y;
                closestZ = z;
                closestDistanceSquared = distanceSquared;
                closestTime = time;
            }
        }
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
    public double getClosestDistanceSquared() {
        return closestDistanceSquared;
    }

    @Override
    public double getTime() {
        return closestTime;
    }

}
