package cc.co.evenprime.bukkit.nocheat;


import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 * Check if the player should be allowed to make that move, e.g. is he allowed to jump here or move that far in one step
 * 
 * @author Evenprime
 *
 */
public class MovingCheck {

	// previously-calculated upper bound values for jumps. Minecraft is very deterministic when it comes to jumps
    // Each entry represents the maximum gain in height per move event.
    private static double jumpingPhases[] = new double[]{ 0.501D, 0.34D, 0.26D, 0.17D, 0.09D, 0.02D, 0.00D, -0.07D, -0.15D, -0.22D, -0.29D, -0.36D, -0.43D, -0.50D };
    
    // Violation levels
    private static final int HEAVY = 3;
    private static final int NORMAL = 2;
    private static final int MINOR = 1;
    private static final int NONE = 0;
    
    // Every player gets freebee moves to prevent them from getting stuck to easy
    private static final int freeIllegalMoves = 1;
    
    // Block types that may be treated specially
    private enum BlockType {
		SOLID, NONSOLID, LADDER, LIQUID, UNKNOWN;
	}
    
    // Until I can think of a better way to determine if a block is solid or not, this is what I'll do
    private static BlockType types[] = new BlockType[256];
    static {
    	
    	for(int i = 0; i < types.length; i++) {
    		types[i] = BlockType.UNKNOWN;
    	}
    	
    	types[Material.AIR.getId()] = BlockType.NONSOLID;
    	types[Material.STONE.getId()] = BlockType.SOLID;
    	types[Material.GRASS.getId()] = BlockType.SOLID;
    	types[Material.DIRT.getId()] = BlockType.SOLID;
    	types[Material.COBBLESTONE.getId()] = BlockType.SOLID;
    	types[Material.WOOD.getId()] = BlockType.SOLID;
    	types[Material.SAPLING.getId()] = BlockType.NONSOLID;
    	types[Material.BEDROCK.getId()] = BlockType.SOLID;
    	types[Material.WATER.getId()] = BlockType.LIQUID;
    	types[Material.STATIONARY_WATER.getId()] = BlockType.LIQUID;
    	types[Material.LAVA.getId()] = BlockType.LIQUID;
    	types[Material.STATIONARY_LAVA.getId()] = BlockType.LIQUID;
    	types[Material.SAND.getId()] = BlockType.SOLID;
    	types[Material.GRAVEL.getId()] = BlockType.SOLID;
    	types[Material.GOLD_ORE.getId()] = BlockType.SOLID;
    	types[Material.IRON_ORE.getId()] = BlockType.SOLID;
    	types[Material.COAL_ORE.getId()] = BlockType.SOLID;
    	types[Material.LOG.getId()] = BlockType.SOLID;
    	types[Material.LEAVES.getId()] = BlockType.SOLID;
    	types[Material.SPONGE.getId()] = BlockType.SOLID;
    	types[Material.GLASS.getId()] = BlockType.SOLID;
    	types[Material.LAPIS_ORE.getId()] = BlockType.SOLID;
    	types[Material.LAPIS_BLOCK.getId()] = BlockType.SOLID;
    	types[Material.DISPENSER.getId()] = BlockType.SOLID;
    	types[Material.SANDSTONE.getId()] = BlockType.SOLID;
    	types[Material.NOTE_BLOCK.getId()]= BlockType.SOLID;
    	types[Material.WOOL.getId()]= BlockType.SOLID;
    	types[Material.YELLOW_FLOWER.getId()]= BlockType.NONSOLID;
    	types[Material.RED_ROSE.getId()]= BlockType.NONSOLID;
    	types[Material.BROWN_MUSHROOM.getId()]= BlockType.NONSOLID;
    	types[Material.RED_MUSHROOM.getId()]= BlockType.NONSOLID;
    	types[Material.GOLD_BLOCK.getId()]= BlockType.SOLID;
    	types[Material.IRON_BLOCK.getId()]= BlockType.SOLID;
    	types[Material.DOUBLE_STEP.getId()]= BlockType.UNKNOWN;
    	types[Material.STEP.getId()]= BlockType.UNKNOWN;
    	types[Material.BRICK.getId()]= BlockType.SOLID;
    	types[Material.TNT.getId()]= BlockType.SOLID;
    	types[Material.BOOKSHELF.getId()]= BlockType.SOLID;
    	types[Material.MOSSY_COBBLESTONE.getId()]  = BlockType.SOLID;  	                                                                                    
    	types[Material.OBSIDIAN.getId()]= BlockType.SOLID;
    	types[Material.TORCH.getId()]= BlockType.NONSOLID;
    	types[Material.FIRE.getId()]= BlockType.NONSOLID;
    	types[Material.MOB_SPAWNER.getId()]= BlockType.SOLID;
    	types[Material.WOOD_STAIRS.getId()]= BlockType.UNKNOWN;
    	types[Material.CHEST.getId()]= BlockType.SOLID;
    	types[Material.REDSTONE_WIRE.getId()]= BlockType.NONSOLID;
    	types[Material.DIAMOND_ORE.getId()]= BlockType.SOLID;
    	types[Material.DIAMOND_BLOCK.getId()]= BlockType.SOLID;
    	types[Material.WORKBENCH.getId()]= BlockType.SOLID;
    	types[Material.CROPS.getId()]= BlockType.NONSOLID;
    	types[Material.SOIL.getId()]= BlockType.SOLID;
    	types[Material.FURNACE.getId()]= BlockType.SOLID;
    	types[Material.BURNING_FURNACE.getId()]= BlockType.SOLID;
    	types[Material.SIGN_POST.getId()]= BlockType.NONSOLID;
    	types[Material.WOODEN_DOOR.getId()]= BlockType.NONSOLID;
    	types[Material.LADDER.getId()]= BlockType.LADDER;
    	types[Material.RAILS.getId()]= BlockType.NONSOLID;
    	types[Material.COBBLESTONE_STAIRS.getId()]= BlockType.UNKNOWN;
    	types[Material.WALL_SIGN.getId()]= BlockType.NONSOLID;
    	types[Material.LEVER.getId()]= BlockType.NONSOLID;
    	types[Material.STONE_PLATE.getId()]= BlockType.UNKNOWN;
    	types[Material.IRON_DOOR_BLOCK.getId()]= BlockType.NONSOLID;
    	types[Material.WOOD_PLATE.getId()]= BlockType.NONSOLID;
    	types[Material.REDSTONE_ORE.getId()]= BlockType.SOLID;
    	types[Material.GLOWING_REDSTONE_ORE.getId()]= BlockType.SOLID;
    	types[Material.REDSTONE_TORCH_OFF.getId()]= BlockType.NONSOLID;
    	types[Material.REDSTONE_TORCH_ON.getId()]= BlockType.NONSOLID;
    	types[Material.STONE_BUTTON.getId()]= BlockType.NONSOLID;
    	types[Material.SNOW.getId()]= BlockType.UNKNOWN;
    	types[Material.ICE.getId()]= BlockType.UNKNOWN;
    	types[Material.SNOW_BLOCK.getId()]= BlockType.SOLID;
    	types[Material.CACTUS.getId()]= BlockType.SOLID;
    	types[Material.CLAY.getId()]= BlockType.SOLID;
    	types[Material.SUGAR_CANE_BLOCK.getId()]= BlockType.NONSOLID;
    	types[Material.JUKEBOX.getId()]= BlockType.SOLID;
    	types[Material.FENCE.getId()]= BlockType.UNKNOWN;
    	types[Material.PUMPKIN.getId()]= BlockType.SOLID;
    	types[Material.NETHERRACK.getId()]= BlockType.SOLID;
    	types[Material.SOUL_SAND.getId()]= BlockType.UNKNOWN;
    	types[Material.GLOWSTONE.getId()]= BlockType.SOLID;
    	types[Material.PORTAL.getId()]= BlockType.NONSOLID;
    	types[Material.JACK_O_LANTERN.getId()]= BlockType.SOLID;
    	types[Material.CAKE_BLOCK.getId()]= BlockType.UNKNOWN;
    }
    

	public static void check(NoCheatPluginData data, PlayerMoveEvent event) {
			
		// Should we check at all
    	if(NoCheatPlugin.Permissions != null && NoCheatPlugin.Permissions.has(event.getPlayer(), "nocheat.moving")) {
			return;
		}
		else if(NoCheatPlugin.Permissions == null && event.getPlayer().isOp() ) {
			return;
		}
    	
    	// Get the two locations of the event
		Location from = event.getFrom();
		Location to = event.getTo();
		
		int vl = NONE; // The violation level (none, minor, normal, heavy)

    	// First check the distance the player has moved horizontally
    	// TODO: Make this check much more precise
   		double xDistance = Math.abs(from.getX() - to.getX());
    	double zDistance = Math.abs(from.getZ() - to.getZ());
    		
    	// How far are we off?
    	if(xDistance > NoCheatConfiguration.movingDistanceHigh || zDistance > NoCheatConfiguration.movingDistanceHigh) {
    		vl = vl > HEAVY ? vl : HEAVY;
    	}
    	else if(xDistance > NoCheatConfiguration.movingDistanceMed || zDistance > NoCheatConfiguration.movingDistanceMed) {
    		vl = vl > NORMAL ? vl : NORMAL;
    	}
    	else if(xDistance > NoCheatConfiguration.movingDistanceLow || zDistance > NoCheatConfiguration.movingDistanceLow) {
    		vl = vl > MINOR ? vl : MINOR;
    	}


    	// pre-calculate boundary values that are needed multiple times in the following checks
    	// the array each contains [lowerX, higherX, Y, lowerZ, higherZ]
    	int fromValues[] = {floor_double(from.getX() - 0.3D), (int)Math.floor(from.getX() + 0.3D), from.getBlockY(), floor_double(from.getZ() - 0.3D),(int)Math.floor(from.getZ() + 0.3D) };
    	int toValues[] = {floor_double(to.getX() - 0.3D), (int)Math.floor(to.getX() + 0.3D), to.getBlockY(), floor_double(to.getZ() - 0.3D), (int)Math.floor(to.getZ() + 0.3D) };

    	// compare locations to the world to guess if the player is standing on the ground, a half-block or next to a ladder
    	boolean onGroundFrom = playerIsOnGround(from.getWorld(), fromValues, from);
    	boolean onGroundTo = playerIsOnGround(from.getWorld(), toValues, to);

    	// Both locations seem to be on solid ground or at a ladder
    	if(onGroundFrom && onGroundTo)
    	{
    		// reset jumping
    		data.movingJumpPhase = 0;

    		// Check if the player isn't 'walking' up unrealistically far in one step
    		// Finally found out why this can happen:
    		// If a player runs into a wall at an angle from above, the game tries to
    		// place him above the block he bumped into, by placing him 0.5 m above
    		// the target block
    		if(!(to.getY() - from.getY() < jumpingPhases[data.movingJumpPhase])) {

    			double offset = (to.getY() - from.getY()) - jumpingPhases[data.movingJumpPhase];

    			if(offset > 2D)        vl = vl > HEAVY ? vl : HEAVY;
    			else if(offset > 0.6D) vl = vl > NORMAL ? vl : NORMAL;
    			else                   vl = vl > MINOR ? vl : MINOR;
    		}
    	}
    	// player is starting to jump (or starting to fall down somewhere)
    	else if(onGroundFrom && !onGroundTo)
    	{	
    		// reset jumping
    		data.movingJumpPhase = 0;

    		// Check if player isn't jumping too high
    		if(!(to.getY() - from.getY() < jumpingPhases[data.movingJumpPhase])) {

    			double offset = (to.getY() - from.getY()) - jumpingPhases[data.movingJumpPhase];

    			if(offset > 2D)        vl = vl > HEAVY ? vl : HEAVY;
    			else if(offset > 0.6D) vl = vl > NORMAL ? vl : NORMAL;
    			else                   vl = vl > MINOR ? vl : MINOR;
    		}
    		else if(to.getY() <= from.getY()) {
    			// Very special case if running over a cliff and then immediately jumping. 
    			// Some sort of "air jump", MC allows it, so we have to do so too.
    		}
    		else data.movingJumpPhase++; // Setup next phase of the jump
    	}
    	// player is probably landing somewhere
    	else if(!onGroundFrom && onGroundTo)
    	{
    		// Check if player isn't landing to high (sounds weird, but has its use)
    		if(!(to.getY() - from.getY() < jumpingPhases[data.movingJumpPhase])) {

    			double offset = (to.getY() - from.getY()) - jumpingPhases[data.movingJumpPhase];

    			if(offset > 2D)        vl = vl > HEAVY ? vl : HEAVY;
    			else if(offset > 0.6D) vl = vl > NORMAL ? vl : NORMAL;
    			else                   vl = vl > MINOR ? vl : MINOR;
    		}
    		else {
    			data.movingJumpPhase = 0; // He is on ground now, so reset the jump
    		}
    	}
    	// Player is moving through air (during jumping, falling)
    	else {
    		// May also be at the very edge of a platform (I seem to not be able to reliably tell if that's the case)
    		if(!(to.getY() - from.getY() < jumpingPhases[data.movingJumpPhase])) {

    			double offset = (to.getY() - from.getY()) - jumpingPhases[data.movingJumpPhase];

    			if(offset > 2D)        vl = vl > HEAVY ? vl : HEAVY;
    			else if(offset > 0.6D) vl = vl > NORMAL ? vl : NORMAL;
    			else                   vl = vl > MINOR ? vl : MINOR;
    		}

    		data.movingJumpPhase++; // Enter next phase of the flight
    	}

    	// do a security check on the jumping phase, such that we don't get 
    	// OutOfArrayBoundsExceptions at long air times (falling off high places)
    	if(!(data.movingJumpPhase < jumpingPhases.length)) {
    		data.movingJumpPhase = jumpingPhases.length - 1;
    	}

    	// Treat the violation(s) according to their level
    	switch(vl) {
    	case MINOR:
    		minorViolation(data, event);
    		break;
    	case NORMAL:
    		normalViolation(data, event);
    		break;
    	case HEAVY:
    		heavyViolation(data, event);
    		break;
    	default:
    		legitimateMove(data, event);
    	}
	}
	
	
	protected static void minorViolation(NoCheatPluginData data, PlayerMoveEvent event) {

		// If it is the first violation, store the "from" location for potential later use
		if(data.movingMinorViolationsInARow == 0) {
			data.movingSetBackPoint = event.getFrom().clone();
		}
		
		// Start logging only after the freebee illegal moves have all been used
		if(data.movingMinorViolationsInARow == freeIllegalMoves + 1) {
			NoCheatPlugin.logMinor("NoCheatPlugin: Moving violation: "+event.getPlayer().getName()+" from " + String.format("(%.5f, %.5f, %.5f) to (%.5f, %.5f, %.5f)", event.getFrom().getX(), event.getFrom().getY(), event.getFrom().getZ(), event.getTo().getX(), event.getTo().getY(), event.getTo().getZ()));
			
		}
		
		data.movingMinorViolationsInARow++;
		
		// 16 minor violations in a row count as one normal violation
		if(data.movingMinorViolationsInARow % 16 == 0) {
			normalViolation(data, event);
		}
		// Each time the freebee moves are all used up, we may reset the player to his old location
		else if(data.movingMinorViolationsInARow % (freeIllegalMoves + 1) == 0) {
			resetPlayer(data, event);
		}
	}
	
	protected static void normalViolation(NoCheatPluginData data, PlayerMoveEvent event) {
		// Log the first violation in a row
		if(data.movingNormalViolationsInARow == 0)
			NoCheatPlugin.logNormal("NoCheatPlugin: Moving violation: "+event.getPlayer().getName()+" from " + String.format("(%.5f, %.5f, %.5f) to (%.5f, %.5f, %.5f)", event.getFrom().getX(), event.getFrom().getY(), event.getFrom().getZ(), event.getTo().getX(), event.getTo().getY(), event.getTo().getZ()));
	
		resetPlayer(data, event);
		
		data.movingNormalViolationsInARow++;
	}
	
	protected static void heavyViolation(NoCheatPluginData data, PlayerMoveEvent event) {
		
		// Log the first violation in a row
		if(data.movingHeavyViolationsInARow == 0)
			NoCheatPlugin.logHeavy("NoCheatPlugin: Moving violation: "+event.getPlayer().getName()+" from " + String.format("(%.5f, %.5f, %.5f) to (%.5f, %.5f, %.5f)", event.getFrom().getX(), event.getFrom().getY(), event.getFrom().getZ(), event.getTo().getX(), event.getTo().getY(), event.getTo().getZ()));
	
		resetPlayer(data, event);
		
		data.movingHeavyViolationsInARow++;
	}
	
	protected static void legitimateMove(NoCheatPluginData data, PlayerMoveEvent event) {
		// Give some additional logs about now ending violations
		if(data.movingHeavyViolationsInARow > 0) {
			NoCheatPlugin.logHeavy("NoCheatPlugin: Moving violation stopped: "+event.getPlayer().getName() + " total Events: "+ data.movingHeavyViolationsInARow);
			data.movingHeavyViolationsInARow = 0;
		}
		else if(data.movingNormalViolationsInARow > 0) {
			NoCheatPlugin.logNormal("NoCheatPlugin: Moving violation stopped: "+event.getPlayer().getName()+ " total Events: "+ data.movingNormalViolationsInARow);
			data.movingNormalViolationsInARow = 0;
		}
		else if(data.movingMinorViolationsInARow > freeIllegalMoves) {
			NoCheatPlugin.logMinor("NoCheatPlugin: Moving violation stopped: "+event.getPlayer().getName()+ " total Events: "+ data.movingMinorViolationsInARow);
			data.movingMinorViolationsInARow = 0;
		}
		else {
			data.movingMinorViolationsInARow = 0;
		}
		
		// Setback point isn't needed anymore
		data.movingSetBackPoint = null;
	}
		
	/** 
	 * Return the player to a stored location or if that is not available,
	 * the previous location.
	 * @param data
	 * @param event
	 */
	private static void resetPlayer(NoCheatPluginData data, PlayerMoveEvent event) {
		
		// If we only log, we never reset the player to his original location and can end here already
		if(NoCheatConfiguration.movingLogOnly) return;
		
		// Still not a perfect solution. After resetting a player his vertical momentum gets lost
		// Therefore we can't expect him to fall big distances in his next move, therefore we have to
		// set his flying phase to something he can work with.
		if(data.movingJumpPhase > 7) {
			data.movingJumpPhase = 7;
		}

		// If we have stored a location for the player, we put him back there
		if(data.movingSetBackPoint != null) {
			
			// Lets try it that way. Maybe now people don't "disappear" any longer
			event.setFrom(data.movingSetBackPoint);
			event.setTo(data.movingSetBackPoint);
			event.getPlayer().teleportTo(data.movingSetBackPoint);
		}
		else {
			// Lets try it that way. Maybe now people don't "disappear" any longer
			event.setFrom(event.getFrom());
			event.setTo(event.getFrom().clone());
			event.getPlayer().teleportTo(event.getFrom());
		}
	}

	  
    /**
     * Check the four edges of the player's approximated Bounding Box for blocks or ladders, 
     * at his own height (values[2]) and below his feet (values[2]-1). Also, check at his "head"
     * for ladders.
     * 
     * If there is one, the player is considered as standing on it/hanging to it.
     * 
     * Not perfect at all and will produce some false negatives. Probably will be refined 
     * later.
     * 
     * @param w	The world the coordinates belong to
     * @param values The coordinates [lowerX, higherX, Y, lowerZ, higherZ]
     * @return
     */
    private static boolean playerIsOnGround(World w, int values[], Location l) {
 
    	// Completely revamped collision detection
    	// What it does:
    	// Check the blocks below the player. If they aren't not solid (sic!) and the blocks directly above
    	// them aren't solid, The player is considered to be standing on the lower block
    	// Plus the player can hang onto a ladder that is one field above him
    	
    	// Check the four borders of the players hitbox for something he could be standing on
    	if(types[w.getBlockTypeIdAt(values[0], values[2]-1, values[3])] != BlockType.NONSOLID ||
    	   types[w.getBlockTypeIdAt(values[1], values[2]-1, values[3])] != BlockType.NONSOLID ||
    	   types[w.getBlockTypeIdAt(values[0], values[2]-1, values[4])] != BlockType.NONSOLID ||
    	   types[w.getBlockTypeIdAt(values[1], values[2]-1, values[4])] != BlockType.NONSOLID )
    		return true;
    	// Check if he is hanging onto a ladder
    	else if(types[w.getBlockTypeIdAt(l.getBlockX(), l.getBlockY(), l.getBlockZ())] == BlockType.LADDER || 
    			types[w.getBlockTypeIdAt(l.getBlockX(), l.getBlockY()+1, l.getBlockZ())] == BlockType.LADDER)
    		return true;
    	// check if he is standing "in" an block that's potentially solid (we give him the benefit of a doubt and see that as a legit move)
    	// If it is not legit, the MC server already has a safeguard against that (You'll get "xy moved wrongly" on the console in that case)
    	else if(types[w.getBlockTypeIdAt(values[0], values[2], values[3])] != BlockType.NONSOLID ||
    		 types[w.getBlockTypeIdAt(values[1], values[2], values[3])] != BlockType.NONSOLID||
    		 types[w.getBlockTypeIdAt(values[0], values[2], values[4])] != BlockType.NONSOLID ||
    		 types[w.getBlockTypeIdAt(values[1], values[2], values[4])] != BlockType.NONSOLID)
    		return true;
    	// check if his head is "stuck" in an block that's potentially solid (we give him the benefit of a doubt and see that as a legit move)
    	// If it is not legit, the MC server already has a safeguard against that (You'll get "xy moved wrongly" on the console in that case)
    	else if(types[w.getBlockTypeIdAt(values[0], values[2]+1, values[3])] != BlockType.NONSOLID ||
    		 types[w.getBlockTypeIdAt(values[1], values[2]+1, values[3])] != BlockType.NONSOLID ||
    		 types[w.getBlockTypeIdAt(values[0], values[2]+1, values[4])] != BlockType.NONSOLID ||
    		 types[w.getBlockTypeIdAt(values[1], values[2]+1, values[4])] != BlockType.NONSOLID)
    		return true;
    	else
    		return false;
    }
    
    public static int floor_double(double d)
    {
        int i = (int)d;
        return d > (double)i ? i : i - 1;
    }
}
