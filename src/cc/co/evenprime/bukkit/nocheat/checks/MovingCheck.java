package cc.co.evenprime.bukkit.nocheat.checks;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

import cc.co.evenprime.bukkit.nocheat.NoCheatData;
import cc.co.evenprime.bukkit.nocheat.NoCheatPlugin;
import cc.co.evenprime.bukkit.nocheat.actions.Action;
import cc.co.evenprime.bukkit.nocheat.actions.CancelAction;
import cc.co.evenprime.bukkit.nocheat.actions.LogAction;

/**
 * Check if the player should be allowed to make that move, e.g. is he allowed to jump here or move that far in one step
 * 
 * @author Evenprime
 *
 */
public class MovingCheck extends Check {

	public MovingCheck(NoCheatPlugin plugin) {
		super(plugin);
		setActive(true);
	}

	// How many move events can a player have in air before he is expected to lose altitude (or land somewhere)
	private final int jumpingLimit = 4;

	// How high may a player get compared to his last location with ground contact
	private final double jumpHeight = 1.3D;

	// How high may a player move in one event on ground
	private final double stepHeight = 0.501D;

	// Limits
	public final double moveLimits[] = { 0.0D, 0.5D, 2.0D };

	public final double heightLimits[] = { 0.0D, 0.5D, 2.0D };

	public int ticksBeforeSummary = 100;

	// How should moving violations be treated?
	public final Action actions[][] = { 
			{ LogAction.logLow, CancelAction.reset },
			{ LogAction.logMed, CancelAction.reset },
			{ LogAction.logHigh, CancelAction.reset } };

	private static final double magic =  0.30000001192092896D;
	private static final double magic2 = 0.69999998807907103D;

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


	public void check(final PlayerMoveEvent event) {

		// Should we check at all
		if(plugin.hasPermission(event.getPlayer(), "nocheat.moving"))
			return;

		// Get the player-specific data
		final NoCheatData data = plugin.getPlayerData(event.getPlayer());

		// Get the two locations of the event
		final Location to = event.getTo();

		// WORKAROUND for changed PLAYER_MOVE logic
		final Location from = data.movingTeleportTo == null ? event.getFrom() : data.movingTeleportTo;
		data.movingTeleportTo = null;

		// Notice to myself: How world changes with e.g. command /world work:
		// 1. TeleportEvent from the players current position to another position in the _same_ world
		// 2. MoveEvent(s) (yes, multiple events can be triggered) from that position in the _new_ world 
		//    to the actual target position in the new world
		// strange...

		// I've no real way to get informed about a world change, therefore I have to
		// store the "lastWorld" and compare it to the world of the next event
		if(data.movingLastWorld != to.getWorld()) {

			data.movingLastWorld = to.getWorld();
			// "Forget" previous setback points
			data.movingSetBackPoint = null;
			data.speedhackSetBackPoint = null;

			// Store the destination that this move goes to for later use
			data.movingLocation = to.clone();

			// the world changed since our last check, therefore I can't check anything
			// for this event (reliably)
			return;
		}

		if(data.movingLocation != null && data.movingLocation.equals(to)) {
			// If we are still trying to reach that location, accept the move
			return;
		}
		else if(data.movingLocation != null) {
			// If we try to go somewhere else, delete the location. It is no longer needed
			data.movingLocation = null;
		}

		// Ignore vehicles
		if(event.getPlayer().isInsideVehicle()) {
			data.movingSetBackPoint = null;
			data.speedhackSetBackPoint = null;
			return;
		}

		// The actual movingCheck starts here

		// First check the distance the player has moved horizontally
		double xDistance = from.getX()-to.getX();
		double zDistance = from.getZ()-to.getZ();

		// If the target is a bed and distance not too big, allow it
		if(to.getWorld().getBlockTypeIdAt(to) == Material.BED_BLOCK.getId() && xDistance < 8.0D && zDistance < 8.0D) {
			return; // players are allowed to "teleport" into a bed over "short" distances
		}

		// Calculate the horizontal distance
		double combined = Math.sqrt((xDistance*xDistance + zDistance*zDistance)) - 0.6D ;

		// Give additional movement based on velocity (not too precise, but still better than false positives)
		Vector v = event.getPlayer().getVelocity();

		// Compare the velocity vector to the existing movement freedom that we've from previous events
		double tmp = Math.abs(v.getX()*5D) + Math.abs(v.getZ()*5D);
		data.movingHorizFreedom = tmp > data.movingHorizFreedom * 0.9D ? tmp : data.movingHorizFreedom * 0.9D;

		// subtract the additional freedom
		combined -= data.movingHorizFreedom;

		// Violation level
		int vl1 = -1;

		// How far are we off?
		if(combined > moveLimits[2]) {
			vl1 = max(vl1, 2);
		}
		else if(combined > moveLimits[1]) {
			vl1 = max(vl1, 1);
		}
		else if(combined > moveLimits[0]) {
			vl1 =  max(vl1, 0);
		}


		// pre-calculate boundary values that are needed multiple times in the following checks
		// the array each contains [lowerX, higherX, Y, lowerZ, higherZ]
		int fromValues[] = {lowerBorder(from.getX()), upperBorder(from.getX()), (int)Math.floor(from.getY()+0.5D), lowerBorder(from.getZ()),upperBorder(from.getZ()) };
		int toValues[] = {lowerBorder(to.getX()), upperBorder(to.getX()), (int)Math.floor(to.getY()+0.5D), lowerBorder(to.getZ()), upperBorder(to.getZ()) };

		// compare locations to the world to guess if the player is standing on the ground, a half-block or next to a ladder
		final boolean onGroundFrom = playerIsOnGround(from.getWorld(), fromValues, from);
		final boolean onGroundTo = playerIsOnGround(to.getWorld(), toValues, to);


		// Handle 4 distinct cases: Walk, Jump, Land, Fly

		int vl2 = -1;

		// A halfway lag-resistant method of allowing vertical acceleration without allowing blatant cheating
		
		// FACT: Minecraft server sends player "velocity" to the client and lets the client calculate the movement
		// PROBLEM: There may be an arbitrary amount of other move events between the server sending the data
		//          and the client accepting it/reacting to it. The server can't know when the client starts to
		//          consider the sent "velocity" in its movement.
		// SOLUTION: Give the client at least 10 events after sending "velocity" to actually use the velocity for
		//           its movement, plus additional events if the "velocity" was big and can cause longer flights
		
		// The server sent the player a "velocity" a short time ago
		if(v.getY() > 0.0D) {
			if(data.movingVertFreedomCounter < 10)
				data.movingVertFreedomCounter = 10;

			// Be generous with the height limit for the client
			data.movingVertFreedom += v.getY()*3D;

			// Set a top limit for how many events a client has to "consume" the sent velocity
			if(data.movingVertFreedomCounter < 30)
				data.movingVertFreedomCounter++;
		}
		// If the server no longer has a positive velocity, start consuming the event counter for this client
		else if(data.movingVertFreedomCounter > 0) {
			data.movingVertFreedomCounter--;
		}

		// If the event counter has been consumed, remove the vertical movement limit increase
		if(data.movingVertFreedomCounter <= 0) {
			data.movingVertFreedom = 0.0D;
		}

		double limit = data.movingVertFreedom;

		// Walk
		if(onGroundFrom && onGroundTo)
		{
			limit += jumpHeight;
			double distance = to.getY() - from.getY();

			vl2 = heightLimitCheck(limit, distance);

			if(vl1 < 0 && vl2 < 0)
			{
				// reset jumping
				data.movingJumpPhase = 0;
				data.movingSetBackPoint = from.clone();
			}
		}
		// Jump
		else if(onGroundFrom && !onGroundTo)
		{	
			limit += jumpHeight;
			double distance = to.getY() - from.getY();

			// Check if player isn't jumping too high
			vl2 = heightLimitCheck(limit, distance);

			if(vl1 < 0 && vl2 < 0) {
				// Setup next phase of the jump
				data.movingJumpPhase = 1;
				data.movingSetBackPoint = from.clone();
			}
		}
		// Land
		else if(!onGroundFrom && onGroundTo)
		{
			Location l = data.movingSetBackPoint == null ? from : data.movingSetBackPoint;

			if(data.movingJumpPhase > jumpingLimit)
				limit += jumpHeight + stepHeight - (data.movingJumpPhase-jumpingLimit) * 0.2D;
			else limit += jumpHeight;

			double distance = to.getY() - l.getY();

			// Check if player isn't jumping too high
			vl2 = heightLimitCheck(limit, distance);

			if(vl1 < 0 && vl2 < 0) {
				data.movingJumpPhase = 0; // He is on ground now, so reset the jump
				data.movingSetBackPoint = to.clone();
			}
		}
		// Player is moving through air (during jumping, falling)
		else {
			Location l = data.movingSetBackPoint == null ? from : data.movingSetBackPoint;

			if(data.movingJumpPhase > jumpingLimit)
				limit += jumpHeight - (data.movingJumpPhase-jumpingLimit) * 0.2D;
			else limit += jumpHeight;

			double distance = to.getY() - l.getY();

			// Check if player isn't jumping too high
			vl2 = heightLimitCheck(limit, distance);

			if(vl1 < 0 && vl2 < 0) {
				data.movingJumpPhase++; // Enter next phase of the flight
				// Setback point stays the same. If we don't have one, take the "from" location as a setback point for now
				if(data.movingSetBackPoint == null) {
					data.movingSetBackPoint = from.clone();
				}
			}
		}

		int vl = max(vl1, vl2);

		if(vl >= 0) {
			final Player p = event.getPlayer();
			final NoCheatData d = data;

			// Setup task to display summary later
			if(data.movingRunnable == null) {
				data.movingRunnable = new Runnable() {

					@Override
					public void run() {
						summary(p, d);
						// deleting its own reference
						d.movingRunnable = null;
					}
				};

				// Give a summary in x ticks. 20 ticks ~ 1 second
				plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, data.movingRunnable, ticksBeforeSummary);
			}

			// If we haven't already got a setback point, make this location the new setback point
			if(data.movingSetBackPoint == null) {
				data.movingSetBackPoint = event.getFrom().clone();
			}

			Action action[] = null;
			boolean log = true;

			// Find out with what actions to treat the violation(s)
			if(data.movingViolationsInARow[vl] > 0) log = false; // only log the first violation of that level
			data.movingViolationsInARow[vl]++;
			action = actions[vl];	

			action(event, event.getPlayer(), from, to, action, log);
		}
	}


	public void teleported(PlayerMoveEvent event) {
		NoCheatData data = plugin.getPlayerData(event.getPlayer());

		if(data.reset) { // My plugin requested this teleport, so we don't do anything
			data.reset = false;
		}
		else {
			if(!event.isCancelled()) {
				// If it wasn't our plugin that ordered the teleport, forget (almost) all our information and start from scratch
				// Setback points are created automatically the next time a move event is handled
				data.speedhackSetBackPoint = event.getTo().clone();
				data.movingSetBackPoint = event.getTo().clone();
				data.speedhackEventsSinceLastCheck = 0;
				data.movingJumpPhase = 0;
			}
		}

		// WORKAROUND for changed PLAYER_MOVE logic
		data.movingTeleportTo = event.getTo();
	}

	/**
	 * Perform actions that were specified by the admin
	 * @param event
	 * @param action
	 */
	private void action(PlayerMoveEvent event, Player player, Location from, Location to, Action[] actions, boolean log) {

		if(actions == null) return;

		for(Action a : actions) {
			if(a instanceof LogAction) 
				plugin.log(((LogAction)a).getLevel(), "Moving violation: "+player.getName()+" from " + String.format("%s (%.5f, %.5f, %.5f) to %s (%.5f, %.5f, %.5f)", from.getWorld().getName(), from.getX(), from.getY(), from.getZ(), to.getWorld().getName(), to.getX(), to.getY(), to.getZ()));
			else if(a.equals(CancelAction.reset))
				resetPlayer(event, from);
		}
	}

	private void summary(Player p, NoCheatData data) {

		LogAction log = null;

		String logString =  "Moving summary of last ~" + (ticksBeforeSummary/20) + " seconds: "+p.getName() + " total Violations: ("+ data.movingViolationsInARow[0] + "," + data.movingViolationsInARow[1] + "," + data.movingViolationsInARow[2] + ")";
		// Find out the biggest applicable log level
		for(int i = 0; i < data.movingViolationsInARow.length; i++) {
			if(data.movingViolationsInARow[i] > 0)
				for(Action a : actions[i]) 
					if(a instanceof LogAction)
						if(log == null || ((LogAction)a).getIndex() > log.getIndex())
							log = (LogAction)a;
			data.movingViolationsInARow[i] = 0;
		}

		if(log != null)
			plugin.log(log.getLevel(), logString);
	}

	private int heightLimitCheck(double limit, double value) {

		double offset = value - limit;

		for(int i = heightLimits.length - 1; i >= 0; i--) {
			if(offset > heightLimits[i]) {
				return i;
			}
		}

		return -1;
	}

	private static int max(int a, int b) {
		if(a > b) {
			return a;
		}
		return b;
	}
	/** 
	 * Return the player to a stored location or if that is not available,
	 * the previous location.
	 * @param data
	 * @param event
	 */
	private void resetPlayer(PlayerMoveEvent event, Location from) {

		NoCheatData data = plugin.getPlayerData(event.getPlayer());

		// Reset the jumpphase. We choose the setback-point such that it should be
		// on solid ground, but in case it isn't (maybe the ground is gone now) we
		// still have to allow the player some freedom with vertical movement due
		// to lost vertical momentum to prevent him from getting stuck
		data.movingJumpPhase = 0;
		data.movingVertFreedom = 0.0D;

		Location l = data.movingSetBackPoint;

		data.reset = true;
		// If we have stored a location for the player, we put him back there
		if(l != null) {
			// Lets try it that way. Maybe now people don't "disappear" any longer
			event.setFrom(l.clone());
			event.setTo(l.clone());
			event.getPlayer().teleport(l.clone());
			event.setCancelled(true);
		}
		else {
			// If we don't have a setback point, we'll have to use the from location
			event.setFrom(from.clone());
			event.setTo(from.clone());
			event.getPlayer().teleport(from.clone());
			event.setCancelled(true);
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
		// Allow using a bug called "water elevator"
		else if(types[w.getBlockTypeIdAt(values[0]+1, values[2]-1, values[3]+1)] == BlockType.LIQUID ||
				types[w.getBlockTypeIdAt(values[0]+1, values[2], values[3]+1)] == BlockType.LIQUID ||
				types[w.getBlockTypeIdAt(values[0]+1, values[2]+1, values[3]+1)] == BlockType.LIQUID ||
				types[w.getBlockTypeIdAt(values[0]+1, values[2]-1, values[3])] == BlockType.LIQUID ||
				types[w.getBlockTypeIdAt(values[0]+1, values[2], values[3])] == BlockType.LIQUID ||
				types[w.getBlockTypeIdAt(values[0]+1, values[2]+1, values[3])] == BlockType.LIQUID ||
				types[w.getBlockTypeIdAt(values[0], values[2]-1, values[3]+1)] == BlockType.LIQUID ||
				types[w.getBlockTypeIdAt(values[0], values[2], values[3]+1)] == BlockType.LIQUID ||
				types[w.getBlockTypeIdAt(values[0], values[2]+1, values[3]+1)] == BlockType.LIQUID)
			return true;
		else
			return false;
	}

	private static int lowerBorder(double d1) {
		double floor = Math.floor(d1);
		double d4 = floor + magic;

		if(d4 <= d1)
			d4 = 0;
		else
			d4 = 1;

		return (int) (floor - d4);
	}

	private static int upperBorder(double d1) {
		double floor = Math.floor(d1);
		double d4 = floor + magic2;

		if(d4 < d1)
			d4 = -1;
		else
			d4 = 0;

		return (int) (floor - d4);
	}

	@Override
	public String getName() {
		return "moving";
	}
}
