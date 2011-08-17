package cc.co.evenprime.bukkit.nocheat.checks;

import net.minecraft.server.Block;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

/**
 * A collection of stuff to process data of move events
 * @author Evenprime
 *
 */
public class MovingEventHelper {

    //private final double magic =  0.30000001192092896D;
    //private final double magic2 = 0.69999998807907103D;
    private final double magic =  0.45D;
    private final double magic2 = 0.55D;

    // Block types that may need to be treated specially
    private static final int NONSOLID = 1;     // 0x00000001
    private static final int SOLID = 2;        // 0x00000010
    private static final int LIQUID = 4 | NONSOLID | SOLID;       // 0x00000101
    private static final int LADDER = 8 | NONSOLID | SOLID;       // 0x00001001
    private static final int FENCE = 16 | SOLID;       // 0x00010000
    private static final int INGROUND = 32;
    private static final int ONGROUND = 64;
    // Until I can think of a better way to determine if a block is solid or not, this is what I'll do
    public final int types[] = new int[256];
    
    public MovingEventHelper() { 
        // Find and define properties of all blocks
        for(int i = 0; i < types.length; i++) {

            // Everything is considered nonsolid at first
            types[i] = NONSOLID;

            if(Block.byId[i] != null) {
                if(Block.byId[i].material.isSolid()) {
                    // solid blocks like STONE, CAKE, TRAPDOORS
                    types[i] = SOLID;
                }
                else if(Block.byId[i].material.isLiquid()){
                    // WATER, LAVA
                    types[i] = LIQUID;
                }
            }
        }

        // Special types just for me
        types[Material.LADDER.getId()]= LADDER;
        types[Material.FENCE.getId()]= FENCE;
        
        // Some exceptions
        types[Material.WOODEN_DOOR.getId()] |= SOLID | NONSOLID;
        types[Material.IRON_DOOR_BLOCK.getId()] |= SOLID | NONSOLID;
        types[Material.PISTON_EXTENSION.getId()] |= SOLID | NONSOLID;
        types[Material.PISTON_MOVING_PIECE.getId()] |= SOLID | NONSOLID;
        types[Material.TRAP_DOOR.getId()] |= SOLID | NONSOLID;
    }

    /**
     * Check if certain coordinates are considered "on ground"
     * 
     * @param w	The world the coordinates belong to
     * @param values The coordinates [lowerX, higherX, Y, lowerZ, higherZ] to be checked
     * @param l The precise location that was used for calculation of "values"
     * @return
     */
    public int isLocationOnGround(final World world, final double x, final double y, final double z, boolean waterElevatorsAllowed) {


        final int lowerX = lowerBorder(x);
        final int upperX = upperBorder(x);
        final int Y = (int)Math.floor(y);
        final int lowerZ = lowerBorder(z);
        final int upperZ = upperBorder(z);


        int result = NONSOLID;
        int standingOn;
        int standingIn;
        int headIn;
        
        // Check the four borders of the players hitbox for something he could be standing on
        // Four seperate corners to check
        // First border: lowerX, lowerZ
        standingOn = types[world.getBlockTypeIdAt(lowerX, Y-1, lowerZ)];
        standingIn = types[world.getBlockTypeIdAt(lowerX, Y, lowerZ)];
        headIn = types[world.getBlockTypeIdAt(lowerX, Y+1, lowerZ)];
        
        if(isSolid(standingIn) && isNonSolid(headIn)) {
            result |= SOLID | INGROUND; // Already the "best" result we can get
        }
        else if(isSolid(standingOn) && isNonSolid(standingIn)) {
            result |= SOLID | ONGROUND; // 
        }
        if(isLiquid(standingIn)) {
            result |= LIQUID | INGROUND | ONGROUND; // May get better
        }
        
        // Second border: upperX, lowerZ
        standingOn = types[world.getBlockTypeIdAt(upperX, Y-1, lowerZ)];
        standingIn = types[world.getBlockTypeIdAt(upperX, Y, lowerZ)];
        headIn = types[world.getBlockTypeIdAt(upperX, Y+1, lowerZ)];
        
        if(isSolid(standingIn) && isNonSolid(headIn)) {
            result |= SOLID | INGROUND; // Already the "best" result we can get
        }
        else if(isSolid(standingOn) && isNonSolid(standingIn)) {
            result |= SOLID | ONGROUND; // 
        }
        if(isLiquid(standingIn)) {
            result |= LIQUID | INGROUND | ONGROUND; // May get better
        }
        
        // Third border: lowerX, upperZ
        standingOn = types[world.getBlockTypeIdAt(lowerX, Y-1, upperZ)];
        standingIn = types[world.getBlockTypeIdAt(lowerX, Y, upperZ)];
        headIn = types[world.getBlockTypeIdAt(lowerX, Y+1, upperZ)];
        
        if(isSolid(standingIn) && isNonSolid(headIn)) {
            result |= SOLID | INGROUND; // Already the "best" result we can get
        }
        else if(isSolid(standingOn) && isNonSolid(standingIn)) {
            result |= SOLID | ONGROUND; // 
        }
        if(isLiquid(standingIn)) {
            result |= LIQUID | INGROUND | ONGROUND; // May get better
        }
        
        // Fourth border: upperX, upperZ
        standingOn = types[world.getBlockTypeIdAt(upperX, Y-1, upperZ)];
        standingIn = types[world.getBlockTypeIdAt(upperX, Y, upperZ)];
        headIn = types[world.getBlockTypeIdAt(upperX, Y+1, upperZ)];
        
        if(isSolid(standingIn) && isNonSolid(headIn)) {
            result |= SOLID | INGROUND; // Already the "best" result we can get
        }
        else if(isSolid(standingOn) && isNonSolid(standingIn)) {
            result |= SOLID | ONGROUND; // 
        }
        if(isLiquid(standingIn)) {
            result |= LIQUID | INGROUND  | ONGROUND; // May get better
        }
        
        // Original location: X, Z (allow standing in walls this time
        standingIn = types[world.getBlockTypeIdAt(Location.locToBlock(x),Location.locToBlock(y),Location.locToBlock(z))];
        
        if(isSolid(standingIn)) {
            return SOLID | INGROUND | ONGROUND; // Already the "best" result we can get
        }
        if(isLiquid(standingIn)) {
            result |= LIQUID | INGROUND | ONGROUND; // May get better
        }
        
        // Water elevators - optional "feature"
        if(waterElevatorsAllowed) {
            result = types[world.getBlockTypeIdAt(lowerX + 1, Y + 1, lowerZ + 1)] | types[world.getBlockTypeIdAt(lowerX + 1, Y, lowerZ + 1)] | types[world.getBlockTypeIdAt(lowerX, Y + 1, lowerZ + 1)] | types[world.getBlockTypeIdAt(lowerX, Y, lowerZ + 1)] | types[world.getBlockTypeIdAt(lowerX + 1, Y + 1, lowerZ)] | types[world.getBlockTypeIdAt(lowerX + 1, Y, lowerZ)];

            if((result & LIQUID) != 0) {
                return SOLID | INGROUND | ONGROUND;
            }
        }
        
        return result;
    }

    public final boolean isSolid(int value) {
        return (value & SOLID) == SOLID;
    }

    public final boolean isLiquid(int value) {
        return (value & LIQUID) == LIQUID;
    }
    
    public final boolean isInGround(int value) {
        return (value & INGROUND) == INGROUND;
    }
    
    public final boolean isOnGround(int value) {
        return (value & ONGROUND) == ONGROUND;
    }

    public final boolean isSolidOrLiquid(int value) {
        return isSolid(value) || isLiquid(value);
    }
    
    public final boolean isNonSolid(int value) {
        return ((value & NONSOLID) == NONSOLID);
    }
    
    /**
     * Personal Rounding function to determine if a player is still touching a block or not
     * @param d1
     * @return
     */
    private final int lowerBorder(double d1) {

        double floor = Math.floor(d1);
        double d4 = floor + magic;

        if(d4 <= d1)
            d4 = 0;
        else
            d4 = 1;

        return (int) (floor - d4);
    }

    /**
     * Personal Rounding function to determine if a player is still touching a block or not
     * @param d1
     * @return
     */
    private final int upperBorder(double d1) {

        double floor = Math.floor(d1);
        double d4 = floor + magic2;

        if(d4 < d1)
            d4 = -1;
        else
            d4 = 0;

        return (int) (floor - d4);
    }

}
