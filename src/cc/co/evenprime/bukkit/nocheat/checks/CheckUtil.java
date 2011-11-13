package cc.co.evenprime.bukkit.nocheat.checks;

import net.minecraft.server.Block;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import cc.co.evenprime.bukkit.nocheat.NoCheatPlayer;
import cc.co.evenprime.bukkit.nocheat.data.PreciseLocation;

/**
 * Some stuff that's used by different checks
 * 
 */
public class CheckUtil {

    /**
     * Check if a player looks at a target of a specific size, with a specific
     * precision value (roughly)
     */
    public static final double directionCheck(final NoCheatPlayer player, final double targetX, final double targetY, final double targetZ, final double targetWidth, final double targetHeight, final double precision) {

        // Eye location of the player
        final Location eyes = player.getPlayer().getEyeLocation();

        final double factor = Math.sqrt(Math.pow(eyes.getX() - targetX, 2) + Math.pow(eyes.getY() - targetY, 2) + Math.pow(eyes.getZ() - targetZ, 2));

        // View direction of the player
        final Vector direction = eyes.getDirection();

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

    public static final double reachCheck(final NoCheatPlayer player, final double targetX, final double targetY, final double targetZ, final double limit) {

        final Location eyes = player.getPlayer().getEyeLocation();

        final double distance = Math.sqrt(Math.pow(eyes.getX() - targetX, 2) + Math.pow(eyes.getY() - targetY, 2) + Math.pow(eyes.getZ() - targetZ, 2));

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

    public static final boolean isSprinting(final Player player) {

        return !(player.isSprinting() && player.getFoodLevel() > 5);
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
    public static final int isLocationOnGround(final World world, final PreciseLocation location) {

        final int lowerX = lowerBorder(location.x);
        final int upperX = upperBorder(location.x);
        final int Y = (int) Math.floor(location.y);
        final int lowerZ = lowerBorder(location.z);
        final int upperZ = upperBorder(location.z);

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
            if(isSolid(types[world.getBlockTypeIdAt(Location.locToBlock(location.x), Location.locToBlock(location.y), Location.locToBlock(location.z))])) {
                result |= INGROUND;
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
    private static final int canStand(final World world, final int x, final int y, final int z) {

        final int standingIn = types[world.getBlockTypeIdAt(x, y, z)];
        final int headIn = types[world.getBlockTypeIdAt(x, y + 1, z)];

        int result = 0;

        // It's either liquid, or something else
        if(isLiquid(standingIn) || isLiquid(headIn)) {
            return LIQUID;
        }

        if(isLadder(standingIn) || isLadder(headIn)) {
            return LADDER;
        }

        final int standingOn = types[world.getBlockTypeIdAt(x, y - 1, z)];

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

    public static final boolean isSolid(final int value) {
        return (value & SOLID) == SOLID;
    }

    public static final boolean isLiquid(final int value) {
        return (value & LIQUID) == LIQUID;
    }

    private static final boolean isNonSolid(final int value) {
        return((value & NONSOLID) == NONSOLID);
    }

    private static final boolean isLadder(final int value) {
        return((value & LADDER) == LADDER);
    }

    public static final boolean isOnGround(final int fromType) {
        return isLadder(fromType) || (fromType & ONGROUND) == ONGROUND;
    }

    public static final boolean isInGround(final int fromType) {
        return isLadder(fromType) || isLiquid(fromType) || (fromType & INGROUND) == INGROUND;
    }

    /**
     * Personal Rounding function to determine if a player is still touching a
     * block or not
     * 
     * @param d1
     * @return
     */
    private static final int lowerBorder(final double d1) {

        final double floor = Math.floor(d1);

        if(floor + magic <= d1)
            return (int) (floor);
        else
            return (int) (floor - 1);
    }

    /**
     * Personal Rounding function to determine if a player is still touching a
     * block or not
     * 
     * @param d1
     * @return
     */
    private static final int upperBorder(final double d1) {

        final double floor = Math.floor(d1);

        if(floor + magic2 < d1)
            return (int) (floor + 1);
        else
            return (int) floor;
    }

    public static int getType(final int typeId) {
        return types[typeId];
    }

}
