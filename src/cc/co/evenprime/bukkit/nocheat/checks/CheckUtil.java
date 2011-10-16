package cc.co.evenprime.bukkit.nocheat.checks;

import net.minecraft.server.Block;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

/**
 * Some stuff that's used by different checks
 * 
 * @author Evenprime
 * 
 */
public class CheckUtil {

    /**
     * Check if a player looks at a target of a specific size, with a specific
     * precision value (roughly)
     */
    public static double directionCheck(Player player, double targetX, double targetY, double targetZ, double targetWidth, double targetHeight, double precision) {

        // Eye location of the player
        Location eyes = player.getEyeLocation();

        double factor = Math.sqrt(Math.pow(eyes.getX() - targetX, 2) + Math.pow(eyes.getY() - targetY, 2) + Math.pow(eyes.getZ() - targetZ, 2));

        // View direction of the player
        Vector direction = player.getEyeLocation().getDirection();

        final double x = ((double) targetX) - eyes.getX();
        final double y = ((double) targetY) - eyes.getY();
        final double z = ((double) targetZ) - eyes.getZ();

        final double xPrediction = factor * direction.getX();
        final double yPrediction = factor * direction.getY();
        final double zPrediction = factor * direction.getZ();

        double off = 0.0D;

        off += Math.max(Math.abs(x - xPrediction) - (targetWidth / 2 + precision), 0.0D);
        off += Math.max(Math.abs(z - zPrediction) - (targetWidth / 2 + precision), 0.0D);
        off += Math.max(Math.abs(y - yPrediction) - (targetHeight / 2 + precision), 0.0D);

        if(off > 1) {
            off = Math.sqrt(off);
        }

        return off;
    }

    public static double reachCheck(Player player, double targetX, double targetY, double targetZ, double limit) {

        Location eyes = player.getEyeLocation();

        double distance = Math.sqrt(Math.pow(eyes.getX() - targetX, 2) + Math.pow(eyes.getY() - targetY, 2) + Math.pow(eyes.getZ() - targetZ, 2));

        return Math.max(distance - limit, 0.0D);
    }

    private final static double magic    = 0.45D;
    private final static double magic2   = 0.55D;

    // Block types that may need to be treated specially
    private static final int    NONSOLID = 1;                   // 0x00000001
    private static final int    SOLID    = 2;                   // 0x00000010
    private static final int    LIQUID   = 4 | NONSOLID;        // 0x00000101
    private static final int    LADDER   = 8 | NONSOLID | SOLID; // 0x00001011
    private static final int    FENCE    = 16 | SOLID;          // 0x00010000
    private static final int    INGROUND = 128;
    private static final int    ONGROUND = 256;
    // Until I can think of a better way to determine if a block is solid or
    // not, this is what I'll do
    private static final int    types[];

    static {
        types = new int[256];
        // Find and define properties of all blocks
        for(int i = 0; i < types.length; i++) {

            // Everything is considered nonsolid at first
            types[i] = NONSOLID;

            if(Block.byId[i] != null) {
                if(Block.byId[i].material.isSolid()) {
                    // solid blocks like STONE, CAKE, TRAPDOORS
                    types[i] = SOLID;
                } else if(Block.byId[i].material.isLiquid()) {
                    // WATER, LAVA
                    types[i] = LIQUID;
                }
            }
        }

        // Some exceptions
        types[Material.LADDER.getId()] = LADDER;
        types[Material.FENCE.getId()] = FENCE;
        types[Material.WALL_SIGN.getId()] = NONSOLID;
        types[Material.DIODE_BLOCK_ON.getId()] |= SOLID | NONSOLID;
        types[Material.DIODE_BLOCK_OFF.getId()] |= SOLID | NONSOLID;
        types[Material.WOODEN_DOOR.getId()] |= SOLID | NONSOLID;
        types[Material.IRON_DOOR_BLOCK.getId()] |= SOLID | NONSOLID;
        types[Material.PISTON_EXTENSION.getId()] |= SOLID | NONSOLID;
        types[Material.PISTON_MOVING_PIECE.getId()] |= SOLID | NONSOLID;
        types[Material.TRAP_DOOR.getId()] |= SOLID | NONSOLID;
    }

    /**
     * Check if certain coordinates are considered "on ground"
     * 
     * @param w
     *            The world the coordinates belong to
     * @param values
     *            The coordinates [lowerX, higherX, Y, lowerZ, higherZ] to be
     *            checked
     * @param l
     *            The precise location that was used for calculation of "values"
     * @return
     */
    public static int isLocationOnGround(final World world, final double x, final double y, final double z, boolean waterElevatorsAllowed) {

        final int lowerX = lowerBorder(x);
        final int upperX = upperBorder(x);
        final int Y = (int) Math.floor(y);
        final int lowerZ = lowerBorder(z);
        final int upperZ = upperBorder(z);

        // Check the four borders of the players hitbox for something he could
        // be standing on
        // Four seperate corners to check
        // First border: lowerX, lowerZ
        int result = 0;

        result |= canStand(world, lowerX, Y, lowerZ);
        result |= canStand(world, upperX, Y, lowerZ);
        result |= canStand(world, upperX, Y, upperZ);
        result |= canStand(world, lowerX, Y, upperZ);

        if(!isInGround(result)) {
            // Original location: X, Z (allow standing in walls this time)
            if(isSolid(types[world.getBlockTypeIdAt(Location.locToBlock(x), Location.locToBlock(y), Location.locToBlock(z))])) {
                result |= INGROUND;
            }
        }

        // Water elevators - optional "feature"
        if(waterElevatorsAllowed && result == 0) {
            result = types[world.getBlockTypeIdAt(lowerX + 1, Y + 1, lowerZ + 1)] | types[world.getBlockTypeIdAt(lowerX, Y + 1, lowerZ + 1)] | types[world.getBlockTypeIdAt(lowerX + 1, Y + 1, lowerZ)];

            if((result & LIQUID) != 0) {
                return INGROUND | ONGROUND; // WaterElevators don't really count
                                            // as "water"
            }
        }
        return result;
    }

    /**
     * Potential results are: "LIQUID", "ONGROUND", "INGROUND", mixture or 0
     * 
     * @param world
     * @param x
     * @param y
     * @param z
     * @return
     */
    private static final int canStand(World world, int x, int y, int z) {

        int standingIn = types[world.getBlockTypeIdAt(x, y, z)];
        int headIn = types[world.getBlockTypeIdAt(x, y + 1, z)];

        int result = 0;

        // It's either liquid, or something else
        if(isLiquid(standingIn) || isLiquid(headIn)) {
            return LIQUID;
        }

        if(isLadder(standingIn) || isLadder(headIn)) {
            return LADDER;
        }

        int standingOn = types[world.getBlockTypeIdAt(x, y - 1, z)];

        // Player standing with his feet in a (half) block?
        if((isSolid(standingIn) || standingOn == FENCE) && isNonSolid(headIn) && standingIn != FENCE) {
            result = INGROUND;
        }

        // Player standing on a block?
        if((isLadder(headIn) || isLadder(standingIn)) || ((isSolid(standingOn) || types[world.getBlockTypeIdAt(x, y - 2, z)] == FENCE) && isNonSolid(standingIn) && standingOn != FENCE)) {
            result |= ONGROUND;
        }

        return result;
    }

    public static final boolean isSolid(int value) {
        return (value & SOLID) == SOLID;
    }

    public static final boolean isLiquid(int value) {
        return (value & LIQUID) == LIQUID;
    }

    private static final boolean isNonSolid(int value) {
        return((value & NONSOLID) == NONSOLID);
    }

    private static final boolean isLadder(int value) {
        return((value & LADDER) == LADDER);
    }

    public static boolean isOnGround(int fromType) {
        return isLadder(fromType) || (fromType & ONGROUND) == ONGROUND;
    }

    public static boolean isInGround(int fromType) {
        return isLadder(fromType) || isLiquid(fromType) || (fromType & INGROUND) == INGROUND;
    }

    /**
     * Personal Rounding function to determine if a player is still touching a
     * block or not
     * 
     * @param d1
     * @return
     */
    private static final int lowerBorder(double d1) {

        double floor = Math.floor(d1);
        double d4 = floor + magic;

        if(d4 <= d1)
            d4 = 0;
        else
            d4 = 1;

        return (int) (floor - d4);
    }

    /**
     * Personal Rounding function to determine if a player is still touching a
     * block or not
     * 
     * @param d1
     * @return
     */
    private static final int upperBorder(double d1) {

        double floor = Math.floor(d1);
        double d4 = floor + magic2;

        if(d4 < d1)
            d4 = -1;
        else
            d4 = 0;

        return (int) (floor - d4);
    }

    public static int getType(int typeId) {
        return types[typeId];
    }

}
