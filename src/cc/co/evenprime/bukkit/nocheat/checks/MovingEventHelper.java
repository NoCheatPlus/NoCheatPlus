package cc.co.evenprime.bukkit.nocheat.checks;

import net.minecraft.server.Block;

import org.bukkit.Material;
import org.bukkit.World;

/**
 * A collection of stuff to process data of move events
 * @author Evenprime
 *
 */
public class MovingEventHelper {

	private final double magic =  0.30000001192092896D;
	private final double magic2 = 0.69999998807907103D;
	
	// Block types that may need to be treated specially
	public static final int NONSOLID = 0;     // 0x00000000
	public static final int SOLID = 1;        // 0x00000001
	public static final int LIQUID = 2;       // 0x00000010
	public static final int LADDER = 4;       // 0x00000100
	public static final int FENCE = 8;        // 0x00001000
	
	// Until I can think of a better way to determine if a block is solid or not, this is what I'll do
	private final int types[] = new int[256];

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
		types[Material.LADDER.getId()]= LADDER | SOLID;
		types[Material.FENCE.getId()]= FENCE | SOLID;
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
		final int higherZ = upperBorder(z);


		int result;

		// check in what kind of block the player is standing "in"
		result = types[world.getBlockTypeIdAt(lowerX, Y, lowerZ)] | types[world.getBlockTypeIdAt(upperX, Y, lowerZ)] |
		types[world.getBlockTypeIdAt(lowerX, Y, higherZ)] | types[world.getBlockTypeIdAt(upperX, Y, higherZ)];

		if((result & SOLID) != 0) {
			// return standing
			return SOLID;
		}
		else if((result & LIQUID) != 0) {
			// return swimming
			return LIQUID;
		}

		// Check the four borders of the players hitbox for something he could be standing on
		result = types[world.getBlockTypeIdAt(lowerX, Y-1, lowerZ)] | types[world.getBlockTypeIdAt(upperX, Y-1, lowerZ)] |
		types[world.getBlockTypeIdAt(lowerX, Y-1, higherZ)] | types[world.getBlockTypeIdAt(upperX, Y-1, higherZ)];

		if((result & SOLID) != 0) {
			// return standing
			return SOLID;
		}


		// check if his head is "stuck" in an block
		result = types[world.getBlockTypeIdAt(lowerX, Y+1, lowerZ)] | types[world.getBlockTypeIdAt(upperX, Y+1, lowerZ)] |
		types[world.getBlockTypeIdAt(lowerX, Y+1, higherZ)] | types[world.getBlockTypeIdAt(upperX, Y+1, higherZ)];

		if((result & SOLID) != 0) {
			// return standing
			return  SOLID;
		}
		else if((result & LIQUID) != 0) {
			// return swimming
			return LIQUID;
		}

		// Running on fences causes problems if not treated specially
		result = types[world.getBlockTypeIdAt(lowerX, Y-2, lowerZ)] | types[world.getBlockTypeIdAt(upperX, Y-2, lowerZ)] |
		types[world.getBlockTypeIdAt(lowerX, Y-2, higherZ)] | types[world.getBlockTypeIdAt(upperX, Y-2, higherZ)];

		if((result & FENCE) != 0) {
			// return standing
			return SOLID;
		}

		// Water elevators - optional "feature"
		if(waterElevatorsAllowed) {
			result = types[world.getBlockTypeIdAt(lowerX+1, Y+1, lowerZ+1)] |
			types[world.getBlockTypeIdAt(lowerX+1, Y  , lowerZ+1)] |
			types[world.getBlockTypeIdAt(lowerX,   Y+1, lowerZ+1)] |
			types[world.getBlockTypeIdAt(lowerX  , Y  , lowerZ+1)] |
			types[world.getBlockTypeIdAt(lowerX+1, Y+1, lowerZ  )] |
			types[world.getBlockTypeIdAt(lowerX+1, Y  , lowerZ  )] ;

			if((result & LIQUID) != 0) {
				return SOLID; // Solid? Why that? Because that's closer to what the bug actually does than liquid
			}
		}
		// If nothing matches, he is somewhere in the air
		return NONSOLID;
	}
	
	
	
	/**
	 * Personal Rounding function to determine if a player is still touching a block or not
	 * @param d1
	 * @return
	 */
	private int lowerBorder(double d1) {

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
	private int upperBorder(double d1) {

		double floor = Math.floor(d1);
		double d4 = floor + magic2;

		if(d4 < d1)
			d4 = -1;
		else
			d4 = 0;

		return (int) (floor - d4);
	}
}
