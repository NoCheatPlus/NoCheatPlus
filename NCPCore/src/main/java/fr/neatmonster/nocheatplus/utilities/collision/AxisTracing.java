package fr.neatmonster.nocheatplus.utilities.collision;

import org.bukkit.Location;

/**
 * Similar to ray-tracing, attempt to model how the client processes move vs.
 * block collision via y-x-z or similar (TODO: xz vs zx). Rough orientation is
 * the RayTracing classes or a thinkable interface, to be able to use similar
 * test cases later on.
 * 
 * @author asofold
 *
 */
public abstract class AxisTracing implements ICollide, ISetMargins {

    // TODO: Consider an extra loop(coordinates + margins...) for convenience.

    /** The order of axis to be checked. */
    private final Axis[] axisOrder = new Axis[3];

    /** Start coordinates (center). */
    private double x0, y0, z0;
    /** End coordinates (center). */
    private double x1, y1, z1;
    /** Margins for the bounding box, seen from center / start coordinates. Positive values. */
    private double marginXpos, marginXneg, marginYpos, marginYneg, marginZpos, marginZneg;

    /** Result returned with collides() and reset to false on set/loop. */
    protected boolean collides;

    /** */
    protected Axis collidesAxis;

    /**
     * Number of steps, counting advancing on one axis for all axes. Does not
     * count the number of processed blocks. The step is increased before
     * calling step.
     */
    protected int step = 0;

    /**
     * Number of steps along one axis. Resets with each axis. Incremented before
     * calling step.
     */
    protected int axisStep = 0;

    private int maxSteps = 0;

    // TODO: maxBlocks (...).

    // TODO: ignoreFirst -> Should be ignore a specific block or blocks within the initial bounds or ...?

    // TODO: Margin only for iteration, not for the bounds (for use-cases like fences).

    public AxisTracing() {
        setDefaultAxisOrder();
    }

    public int getMaxSteps() {
        return maxSteps;
    }

    public void setMaxSteps(int maxSteps) {
        this.maxSteps = maxSteps;
    }

    public int getStepsDone() {
        return step;
    }

    public int getAxisStepsDone() {
        return axisStep;
    }

    /**
     * Indicate if a collision appeared during loop().
     * @return 
     */
    public boolean collides() {
        return collides;
    }

    @Override
    public Axis getCollidingAxis() {
        return collidesAxis;
    }

    public void setDefaultAxisOrder() {
        setAxisOrder(Axis.Y_AXIS, Axis.X_AXIS, Axis.Z_AXIS);
    }

    public void setAxisOrder(Axis first, Axis second, Axis third) {
        axisOrder[0] = first;
        axisOrder[1] = second;
        axisOrder[2] = third;
    }

    @Override
    public void setMargins(final double height, final double xzMargin) {
        this.marginXneg = this.marginXpos = this.marginZneg = this.marginZpos = xzMargin;
        this.marginYneg = 0.0;
        this.marginYpos = height;
    }

    public void set(double x0, double y0, double z0, double x1, double y1, double z1) {
        collides = false;
        step = 0;
        this.x0 = x0;
        this.y0 = y0;
        this.z0 = z0;
        this.x1 = x1;
        this.y1 = y1;
        this.z1 = z1;
    }

    /**
     * Loop through blocks.
     */
    public void loop() {
        // Current start coordinates.
        double x = this.x0;
        double y = this.y0;
        double z = this.z0;
        for (int i = 0; i < 3; i++) {
            final Axis axis = axisOrder[i];
            collidesAxis = axis; // Ensure here, to get it on max steps.
            if (axis == Axis.Y_AXIS) {
                runAxisY(x, y, z);
                y = this.y1;
            }
            else if (axis == Axis.X_AXIS) {
                runAxisX(x, y, z);
                x = this.x1;
            }
            else if (axis == Axis.Z_AXIS) {
                runAxisZ(x, y, z);
                z = this.z1;
            }
            else if (axis != Axis.NONE) {
                // TODO: Might still just skip these.
                // TODO: Should throw IllegalArgumentException with setting to this.
                throw new IllegalStateException("Can not ");
            }
            // NONE = skip
            if (collides) {
                break;
            }
        }
    }


    private void runAxisY(final double xIn, final double yIn, final double zIn) {
        // Skip if there is nothing to iterate.
        if (yIn == this.y1) {
            return;
        }
        // Iterate over axis, applying margins.
        final int increment;
        final double xMin = xIn - marginXneg;
        final double xMax = xIn + marginXpos;
        final double zMin = zIn - marginZneg;
        final double zMax = zIn + marginZpos;
        final double yStart, yEnd;
        final int iEndY;
        if (yIn < this.y1) {
            increment = 1;
            yStart = yIn - marginYneg;
            yEnd = this.y1 + marginYpos;
            iEndY = Location.locToBlock(yEnd) + 1;
        }
        else {
            increment = -1;
            yStart = yIn + marginYpos;
            yEnd = this.y1 - marginYneg;
            iEndY = Location.locToBlock(yEnd) - 1;
        }
        final int iMinX = Location.locToBlock(xMin);
        final int iMaxX = Location.locToBlock(xMax);
        final int iMinZ = Location.locToBlock(zMin);
        final int iMaxZ = Location.locToBlock(zMax);
        final int iStartY = Location.locToBlock(yStart);
        axisStep = 0;
        for (int y = iStartY; y != iEndY; y += increment) {
            ++step;
            ++axisStep;
            if (step > maxSteps) {
                return;
            }
            // TODO: Ignore first setting?
            for (int x = iMinX; x <= iMaxX; x++) {
                for (int z = iMinZ; z <= iMaxZ; z++) {
                    if (!step(x, y, z, xMin, increment == 1 ? yStart : yEnd, zMin, xMax, increment == 1 ? yEnd : yStart, zMax, Axis.Y_AXIS, increment)) {
                        collides = true;
                        return;
                    }
                }
            }
        }
        // No collision.
        return;
    }

    private void runAxisX(final double xIn, final double yIn, final double zIn) {
        // Skip if there is nothing to iterate.
        if (xIn == this.x1) {
            return;
        }
        // Iterate over axis, applying margins.
        final int increment;
        final double yMin = yIn - marginYneg;
        final double yMax = yIn + marginYpos;
        final double zMin = zIn - marginZneg;
        final double zMax = zIn + marginZpos;
        final double xStart, xEnd;
        final int iEndX;
        if (xIn < this.x1) {
            increment = 1;
            xStart = xIn - marginXneg;
            xEnd = this.x1 + marginXpos;
            iEndX = Location.locToBlock(xEnd) + 1;
        }
        else {
            increment = -1;
            xStart = xIn + marginXpos;
            xEnd = this.x1 - marginXneg;
            iEndX = Location.locToBlock(xEnd) - 1;
        }
        final int iMinY = Location.locToBlock(yMin);
        final int iMaxY = Location.locToBlock(yMax);
        final int iMinZ = Location.locToBlock(zMin);
        final int iMaxZ = Location.locToBlock(zMax);
        final int iStartX = Location.locToBlock(xStart);
        axisStep = 0;
        for (int x = iStartX; x != iEndX; x += increment) {
            ++step;
            ++axisStep;
            if (step > maxSteps) {
                return;
            }
            // TODO: Ignore first setting?
            for (int y = iMinY; y <= iMaxY; y++) {
                for (int z = iMinZ; z <= iMaxZ; z++) {
                    if (!step(x, y, z, increment == 1 ? xStart : xEnd, yMin, zMin, increment == 1 ? xEnd : xStart, yMax, zMax, Axis.X_AXIS, increment)) {
                        collides = true;
                        return;
                    }
                }
            }
        }
        // No collision.
        return;
    }

    private void runAxisZ(final double xIn, final double yIn, final double zIn) {
        // Skip if there is nothing to iterate.
        if (zIn == this.z1) {
            return;
        }
        // Iterate over axis, applying margins.
        final int increment;
        final double yMin = yIn - marginYneg;
        final double yMax = yIn + marginYpos;
        final double xMin = xIn - marginXneg;
        final double xMax = xIn + marginXpos;
        final double zStart, zEnd;
        final int iEndZ;
        if (zIn < this.z1) {
            increment = 1;
            zStart = zIn - marginZneg;
            zEnd = this.z1 + marginZpos;
            iEndZ = Location.locToBlock(zEnd + 1);
        }
        else {
            increment = -1;
            zStart = zIn + marginZpos;
            zEnd = this.z1 - marginZneg;
            iEndZ = Location.locToBlock(zEnd - 1);
        }
        final int iMinY = Location.locToBlock(yMin);
        final int iMaxY = Location.locToBlock(yMax);
        final int iMinX = Location.locToBlock(xMin);
        final int iMaxX = Location.locToBlock(xMax);
        final int iStartZ = Location.locToBlock(zStart);
        axisStep = 0;
        for (int z = iStartZ; z != iEndZ; z += increment) {
            ++step;
            ++axisStep;
            if (step > maxSteps) {
                return;
            }
            // TODO: Ignore first setting?
            for (int y = iMinY; y <= iMaxY; y++) {
                for (int x = iMinX; x <= iMaxX; x++) {
                    if (!step(x, y, z, xMin, yMin, increment == 1 ? zStart : zEnd, xMax, yMax, increment == 1 ? zEnd : zStart, Axis.Z_AXIS, increment)) {
                        collides = true;
                        return;
                    }
                }
            }
        }
        // No collision.
        return;
    }

    /**
     * Check a block position for collision with the ordered bounds of a move
     * along one axis.
     * 
     * @param blockX
     *            Position of the block that might be colliding.
     * @param blockY
     * @param blockZ
     * @param minX
     *            Minimum coordinates of the move along one axis, including
     *            bounds.
     * @param minY
     * @param minZ
     * @param maxX
     *            Maximum coordinates of the move along one axis, including
     *            bounds.
     * @param maxY
     * @param maxZ
     * @param axis
     *            The axis along which the bounds are stretched.
     * @param increment
     *            Direction of the move concerning the given axis (1 means from
     *            min to max, -1 means from max to min).
     * @return
     */
    protected abstract boolean step(int blockX, int blockY, int blockZ, 
            double minX, double minY, double minZ, double maxX, double maxY, double maxZ,
            Axis axis, int increment);

}
