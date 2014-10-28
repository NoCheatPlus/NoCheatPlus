package fr.neatmonster.nocheatplus.utilities;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

/**
 * Auxiliary methods for trigonometry related tasks, such as distances, directions, angles.
 * @author mc_dev
 *
 */
public class TrigUtil {

    /** Used for internal calculations, no passing on, beware of nested calls. */
    private static final Vector vec1 = new Vector();
    /** Used for internal calculations, no passing on, beware of nested calls. */
    private static final Vector vec2 = new Vector();
    /** Multiply to get grad from rad. */
    public static final double fRadToGrad = 360.0 / (2.0 * Math.PI);
    /** Some default precision value for the directionCheck method. */
    public static final double DIRECTION_PRECISION = 2.6;

    private static final Location useLoc = new Location(null, 0, 0, 0);

    /**
     * Check if a player looks at a target of a specific size, with a specific
     * precision value (roughly).
     * 
     * @param player
     *            the player
     * @param targetX
     *            the target x
     * @param targetY
     *            the target y
     * @param targetZ
     *            the target z
     * @param targetWidth
     *            the target width
     * @param targetHeight
     *            the target height
     * @param precision
     *            the precision
     * @return
     */
    public static double directionCheck(final Player player, final double targetX, final double targetY, final double targetZ, final double targetWidth, final double targetHeight, final double precision)
    {
        final Location loc = player.getLocation(useLoc);
        final Vector dir = loc.getDirection();
        final double res = directionCheck(loc.getX(), loc.getY() + player.getEyeHeight(), loc.getZ(), dir.getX(), dir.getY(), dir.getZ(), targetX, targetY, targetZ, targetWidth, targetHeight, precision);
        useLoc.setWorld(null);
        return res;
    }

    /**
     * Convenience method.
     * @param sourceFoot
     * @param eyeHeight
     * @param dir
     * @param target
     * @param precision (width/height are set to 1)
     * @return
     */
    public static double directionCheck(final Location sourceFoot, final double eyeHeight, final Vector dir, final Block target, final double precision)
    {
        return directionCheck(sourceFoot.getX(), sourceFoot.getY() + eyeHeight, sourceFoot.getZ(), dir.getX(), dir.getY(), dir.getZ(), target.getX(), target.getY(), target.getZ(), 1, 1, precision);
    }

    /**
     * Convenience method.
     * @param sourceFoot
     * @param eyeHeight
     * @param dir
     * @param targetX
     * @param targetY
     * @param targetZ
     * @param targetWidth
     * @param targetHeight
     * @param precision
     * @return
     */
    public static double directionCheck(final Location sourceFoot, final double eyeHeight, final Vector dir, final double targetX, final double targetY, final double targetZ, final double targetWidth, final double targetHeight, final double precision)
    {
        return directionCheck(sourceFoot.getX(), sourceFoot.getY() + eyeHeight, sourceFoot.getZ(), dir.getX(), dir.getY(), dir.getZ(), targetX, targetY, targetZ, targetWidth, targetHeight, precision);					
    }

    /**
     * Check how far the looking direction is off the target.
     * @param sourceX Source location of looking direction.
     * @param sourceY
     * @param sourceZ
     * @param dirX Looking direction.
     * @param dirY
     * @param dirZ
     * @param targetX Location that should be looked towards.
     * @param targetY
     * @param targetZ
     * @param targetWidth xz extent
     * @param targetHeight y extent
     * @param precision
     * @return Some offset.
     */
    public static double directionCheck(final double sourceX, final double sourceY, final double sourceZ, final double dirX, final double dirY, final double dirZ, final double targetX, final double targetY, final double targetZ, final double targetWidth, final double targetHeight, final double precision)
    {

        //		// TODO: Here we have 0.x vs. 2.x, sometimes !
        //		System.out.println("COMBINED: " + combinedDirectionCheck(sourceX, sourceY, sourceZ, dirX, dirY, dirZ, targetX, targetY, targetZ, targetWidth, targetHeight, precision, 60));

        // TODO: rework / standardize.

        double dirLength = Math.sqrt(dirX * dirX + dirY * dirY + dirZ * dirZ);
        if (dirLength == 0.0) dirLength = 1.0; // ...

        final double dX = targetX - sourceX;
        final double dY = targetY - sourceY;
        final double dZ = targetZ - sourceZ;

        final double targetDist = Math.sqrt(dX * dX + dY * dY + dZ * dZ);

        final double xPrediction = targetDist * dirX / dirLength;
        final double yPrediction = targetDist * dirY / dirLength;
        final double zPrediction = targetDist * dirZ / dirLength;

        double off = 0.0D;

        off += Math.max(Math.abs(dX - xPrediction) - (targetWidth / 2 + precision), 0.0D);
        off += Math.max(Math.abs(dZ - zPrediction) - (targetWidth / 2 + precision), 0.0D);
        off += Math.max(Math.abs(dY - yPrediction) - (targetHeight / 2 + precision), 0.0D);

        if (off > 1) off = Math.sqrt(off);

        return off;
    }

    public static double combinedDirectionCheck(final Location sourceFoot, final double eyeHeight, final Vector dir, final double targetX, final double targetY, final double targetZ, final double targetWidth, final double targetHeight, final double precision, final double anglePrecision)
    {
        return combinedDirectionCheck(sourceFoot.getX(), sourceFoot.getY() + eyeHeight, sourceFoot.getZ(), dir.getX(), dir.getY(), dir.getZ(), targetX, targetY, targetZ, targetWidth, targetHeight, precision, anglePrecision);					
    }

    public static double combinedDirectionCheck(final Location sourceFoot, final double eyeHeight, final Vector dir, final Block target, final double precision, final double anglePrecision)
    {
        return combinedDirectionCheck(sourceFoot.getX(), sourceFoot.getY() + eyeHeight, sourceFoot.getZ(), dir.getX(), dir.getY(), dir.getZ(), target.getX(), target.getY(), target.getZ(), 1, 1, precision, anglePrecision);
    }

    /**
     * Combine directionCheck with angle, in order to prevent low-distance abuse.
     * @param sourceX
     * @param sourceY
     * @param sourceZ
     * @param dirX
     * @param dirY
     * @param dirZ
     * @param targetX
     * @param targetY
     * @param targetZ
     * @param targetWidth
     * @param targetHeight
     * @param blockPrecision
     * @param anglePrecision Precision in grad.
     * @return
     */
    public static double combinedDirectionCheck(final double sourceX, final double sourceY, final double sourceZ, final double dirX, final double dirY, final double dirZ, final double targetX, final double targetY, final double targetZ, final double targetWidth, final double targetHeight, final double blockPrecision, final double anglePrecision)
    {
        double dirLength = Math.sqrt(dirX * dirX + dirY * dirY + dirZ * dirZ);
        if (dirLength == 0.0) dirLength = 1.0; // ...

        final double dX = targetX - sourceX;
        final double dY = targetY - sourceY;
        final double dZ = targetZ - sourceZ;

        final double targetDist = Math.sqrt(dX * dX + dY * dY + dZ * dZ);

        if (targetDist > Math.max(targetHeight, targetWidth) / 2.0 && angle(sourceX, sourceY, sourceZ, dirX, dirY, dirZ, targetX, targetY, targetZ) * fRadToGrad > anglePrecision){
            return targetDist - Math.max(targetHeight, targetWidth) / 2.0;
        }

        final double xPrediction = targetDist * dirX / dirLength;
        final double yPrediction = targetDist * dirY / dirLength;
        final double zPrediction = targetDist * dirZ / dirLength;

        double off = 0.0D;

        off += Math.max(Math.abs(dX - xPrediction) - (targetWidth / 2 + blockPrecision), 0.0D);
        off += Math.max(Math.abs(dZ - zPrediction) - (targetWidth / 2 + blockPrecision), 0.0D);
        off += Math.max(Math.abs(dY - yPrediction) - (targetHeight / 2 + blockPrecision), 0.0D);

        if (off > 1) off = Math.sqrt(off);

        return off;
    }

    /**
     * 3D-distance of two locations. This is obsolete, since it has been fixed. To ignore world checks it might be "useful".
     * 
     * @param location1
     *            the location1
     * @param location2
     *            the location2
     * @return The distance between the locations.
     */
    public static final double distance(final Location location1, final Location location2)
    {
        return distance(location1.getX(), location1.getY(), location1.getZ(), location2.getX(), location2.getY(), location2.getZ());
    }

    /**
     * 3D-distance of two locations.
     * 
     * @param location1
     *            the location1
     * @param location2
     *            the location2
     * @return The distance between the locations.
     */
    public static final double distance(final PlayerLocation location1, final Location location2)
    {
        return distance(location1.getX(), location1.getY(), location1.getZ(), location2.getX(), location2.getY(), location2.getZ());
    }

    /**
     * 3D-distance of two locations.
     * 
     * @param location1
     *            the location1
     * @param location2
     *            the location2
     * @return The distance between the locations.
     */
    public static final double distance(final PlayerLocation location1, final PlayerLocation location2)
    {
        return distance(location1.getX(), location1.getY(), location1.getZ(), location2.getX(), location2.getY(), location2.getZ());
    }

    /**
     * 3d-distance from location (exact) to block middle.
     * @param location
     * @param block
     * @return
     */
    public static final double distance(final Location location, final Block block)
    {
        return distance(location.getX(), location.getY(), location.getZ(), 0.5 + block.getX(), 0.5 + block.getY(), 0.5 + block.getZ());
    }

    /**
     * 3D-distance.
     * @param x1
     * @param y1
     * @param z1
     * @param x2
     * @param y2
     * @param z2
     * @return
     */
    public static final double distance(final double x1, final double y1, final double z1, final double x2, final double y2, final double z2) {
        final double dx = Math.abs(x1 - x2);
        final double dy = Math.abs(y1 - y2);
        final double dz = Math.abs(z1 - z2);
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    public static final double distanceSquared(final Location location1, final Location location2)
    {
        return distanceSquared(location1.getX(), location1.getY(), location1.getZ(), location2.getX(), location2.getY(), location2.getZ());
    }

    public static final double distanceSquared(final PlayerLocation location1, final PlayerLocation location2)
    {
        return distanceSquared(location1.getX(), location1.getY(), location1.getZ(), location2.getX(), location2.getY(), location2.getZ());
    }

    public static final double distanceSquared(final PlayerLocation location1, final Location location2)
    {
        return distanceSquared(location1.getX(), location1.getY(), location1.getZ(), location2.getX(), location2.getY(), location2.getZ());
    }

    public static final double distanceSquared(final double x1, final double y1, final double z1, final double x2, final double y2, final double z2) {
        final double dx = Math.abs(x1 - x2);
        final double dy = Math.abs(y1 - y2);
        final double dz = Math.abs(z1 - z2);
        return dx * dx + dy * dy + dz * dz;
    }

    public static final double distanceSquared(final double x1, final double z1, final double x2, final double z2) {
        final double dx = Math.abs(x1 - x2);
        final double dz = Math.abs(z1 - z2);
        return dx * dx + dz * dz;
    }

    /**
     * 2D-distance in x-z plane.
     * @param location1
     * @param location2
     * @return
     */
    public static final double xzDistance(final Location location1, final Location location2)
    {
        return distance(location1.getX(), location1.getZ(), location2.getX(), location2.getZ());
    }

    /**
     * 2D-distance.
     * @param x1
     * @param z1
     * @param x2
     * @param z2
     * @return
     */
    public static final double distance(final double x1, final double z1, final double x2, final double z2) {
        final double dx = Math.abs(x1 - x2);
        final double dz = Math.abs(z1 - z2);
        return Math.sqrt(dx * dx + dz * dz);
    }

    /**
     * Positive angle between vector from source to target and the vector for the given direction [0...PI].
     * @param sourceX
     * @param sourceY
     * @param sourceZ
     * @param dirX
     * @param dirY
     * @param dirZ
     * @param targetX
     * @param targetY
     * @param targetZ
     * @return  Positive angle between vector from source to target and the vector for the given direction [0...PI].
     */
    public static float angle(final double sourceX, final double sourceY, final double sourceZ, final double dirX, final double dirY, final double dirZ, final double targetX, final double targetY, final double targetZ) {
        double dirLength = Math.sqrt(dirX * dirX + dirY * dirY + dirZ * dirZ);
        if (dirLength == 0.0) dirLength = 1.0; // ...

        final double dX = targetX - sourceX;
        final double dY = targetY - sourceY;
        final double dZ = targetZ - sourceZ;

        vec1.setX(dX);
        vec1.setY(dY);
        vec1.setZ(dZ);
        vec2.setX(dirX);
        vec2.setY(dirY);
        vec2.setZ(dirZ);
        return vec2.angle(vec1);
    }

    /**
     * Angle of a 2d vector, x being the side at the angle. (radians).
     * @param x
     * @param z
     * @return
     */
    public static final double angle(final double x, final double z){
        final double a;
        if (x > 0.0) a = Math.atan(z / x);
        else if  (x < 0.0) a = Math.atan(z / x) + Math.PI;
        else{
            if (z < 0.0) a=3.0 * Math.PI / 2.0;
            else if (z > 0.0) a = Math.PI / 2.0;
            else return Double.NaN;
        }
        if (a < 0.0) return a + 2.0 * Math.PI;
        else return a;
    }

    /**
     * Get the difference of angles (radians) as given from angle(x,z), from a1 to a2, i.e. rather a2 - a1 in principle.
     * @param a1
     * @param a2
     * @return Difference of angle from -pi to pi
     */
    public static final double angleDiff(final double a1, final double a2){
        if (Double.isNaN(a1) || Double.isNaN(a1)) return Double.NaN;
        final double diff = a2 - a1;
        if (diff < -Math.PI) return diff + 2.0 * Math.PI;
        else if (diff > Math.PI) return diff - 2.0 * Math.PI;
        else return diff;
    }

    /**
     * Yaw (angle in grad) difference. This ensures inputs are interpreted correctly (for 360 degree offsets).
     * @param fromYaw
     * @param toYaw
     * @return Angle difference to get from fromYaw to toYaw. Result is in [-180, 180].
     */
    public static final float yawDiff(float fromYaw, float toYaw){
        if (fromYaw <= -360f) fromYaw = -((-fromYaw) % 360f);
        else if (fromYaw >= 360f) fromYaw = fromYaw % 360f;
        if (toYaw <= -360f) toYaw = -((-toYaw) % 360f);
        else if (toYaw >= 360f) toYaw = toYaw % 360f;
        float yawDiff = toYaw - fromYaw;
        if (yawDiff < -180f) yawDiff += 360f;
        else if (yawDiff > 180f) yawDiff -= 360f;
        return yawDiff;
    }
    
    /**
     * Manhattan distance.
     * @param loc1
     * @param loc2
     * @return
     */
    public static int manhattan(final Location loc1, final Location loc2) {
        return manhattan(loc1.getBlockX(), loc1.getBlockY(), loc1.getBlockZ(), loc2.getBlockX(), loc2.getBlockY(), loc2.getBlockZ());
    }

    /**
     * Manhattan distance.
     * @param x1
     * @param y1
     * @param z1
     * @param block
     * @return
     */
    public static int manhattan(final int x1, final int y1, final int  z1, final Block block) {
        return manhattan(x1, y1, z1, block.getX(), block.getY(), block.getZ());
    }

    public static double manhattan(final double x1,final double  z1, final double x2, final double z2){
        return manhattan(Location.locToBlock(x1), Location.locToBlock(z1), Location.locToBlock(x2), Location.locToBlock(z2));
    }

    /**
     * Manhattan distance (steps along the sides of an orthogonal grid).
     * @param x1
     * @param y1
     * @param z1
     * @param x2
     * @param y2
     * @param z2
     * @return
     */
    public static int manhattan(final int x1, final int y1, final int  z1, final int x2, final int y2, final int z2){
        return Math.abs(x1 - x2) + Math.abs(y1 - y2) + Math.abs(z1 - z2);
    }

    public static double manhattan(final double x1, final double y1, final double  z1, final double x2, final double y2, final double z2){
        return manhattan(Location.locToBlock(x1), Location.locToBlock(y1), Location.locToBlock(z1), Location.locToBlock(x2), Location.locToBlock(y2), Location.locToBlock(z2));
    }

    /**
     * Maximum distance comparing dx, dy, dz.
     * @param x1
     * @param y1
     * @param z1
     * @param x2
     * @param y2
     * @param z2
     * @return
     */
    public static int maxDistance(final int x1, final int y1, final int  z1, final int x2, final int y2, final int z2){
        return Math.max(Math.max(Math.abs(x1 - x2), Math.abs(y1 - y2)), Math.abs(z1 - z2));
    }

    /**
     * Maximum distance comparing dx, dy, dz.
     * @param x1
     * @param y1
     * @param z1
     * @param x2
     * @param y2
     * @param z2
     * @return
     */
    public static double maxDistance(final double x1, final double y1, final double  z1, final double x2, final double y2, final double z2){
        return Math.max(Math.max(Math.abs(x1 - x2), Math.abs(y1 - y2)), Math.abs(z1 - z2));
    }

    /**
     * Check if the x-z plane move is "any backwards"  regarding the yaw direction.
     * @param xDistance
     * @param zDistance
     * @param yaw
     * @return
     */
    public static boolean isMovingBackwards(final double xDistance, final double zDistance, final float yaw) {
        return xDistance < 0D && zDistance > 0D && yaw > 180F && yaw < 270F
                || xDistance < 0D && zDistance < 0D && yaw > 270F && yaw < 360F 
                || xDistance > 0D && zDistance < 0D && yaw > 0F && yaw < 90F 
                || xDistance > 0D && zDistance > 0D && yaw > 90F && yaw < 180F;
    }

}
