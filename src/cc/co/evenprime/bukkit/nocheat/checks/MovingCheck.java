package cc.co.evenprime.bukkit.nocheat.checks;

import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.util.Vector;

import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.actions.Action;
import cc.co.evenprime.bukkit.nocheat.actions.CancelAction;
import cc.co.evenprime.bukkit.nocheat.actions.CustomAction;
import cc.co.evenprime.bukkit.nocheat.actions.LogAction;
import cc.co.evenprime.bukkit.nocheat.data.MovingData;
import cc.co.evenprime.bukkit.nocheat.data.PermissionData;
import cc.co.evenprime.bukkit.nocheat.listeners.MovingEntityListener;
import cc.co.evenprime.bukkit.nocheat.listeners.MovingPlayerListener;
import cc.co.evenprime.bukkit.nocheat.listeners.MovingPlayerMonitor;

/**
 * Check if the player should be allowed to make that move, e.g. is he allowed to jump here or move that far in one step
 * 
 * @author Evenprime
 *
 */
public class MovingCheck extends Check {

	public MovingCheck(NoCheat plugin) {
		super(plugin, "moving", PermissionData.PERMISSION_MOVING);
	}

	// How many move events can a player have in air before he is expected to lose altitude (or land somewhere)
	private final int jumpingLimit = 4;

	// How high may a player get compared to his last location with ground contact
	private final double jumpHeight = 1.3D;

	// How high may a player move in one event on ground
	private final double stepHeight = 0.501D;

	private final double stepWidth = 0.6D;
	private final double sneakStepWidth = 0.25D;

	// Limits
	public final double moveLimits[] =   { 0.0D, 0.5D, 2.0D };
	public final double heightLimits[] = { 0.0D, 0.5D, 2.0D };

	public int ticksBeforeSummary = 100;

	public long statisticElapsedTimeNano = 0;

	public boolean allowFlying = false;

	// How should moving violations be treated?
	public final Action actions[][] = { 
			{ LogAction.loglow,  CancelAction.cancel },
			{ LogAction.logmed,  CancelAction.cancel },
			{ LogAction.loghigh, CancelAction.cancel } };

	public String logMessage = "Moving violation: %1$s from %2$s (%4$.5f, %5$.5f, %6$.5f) to %3$s (%7$.5f, %8$.5f, %9$.5f)";
	public String summaryMessage = "Moving summary of last ~%2$d seconds: %1$s total Violations: (%3$d,%4$d,%5$d)";

	public long statisticTotalEvents = 0;

	private static final double magic =  0.30000001192092896D;
	private static final double magic2 = 0.69999998807907103D;

	// Block types that may be treated specially
	private enum BlockType {
		SOLID, NONSOLID, LADDER, LIQUID, UNKNOWN, FENCE;
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
		types[Material.FENCE.getId()]= BlockType.FENCE;
		types[Material.PUMPKIN.getId()]= BlockType.SOLID;
		types[Material.NETHERRACK.getId()]= BlockType.SOLID;
		types[Material.SOUL_SAND.getId()]= BlockType.UNKNOWN;
		types[Material.GLOWSTONE.getId()]= BlockType.SOLID;
		types[Material.PORTAL.getId()]= BlockType.NONSOLID;
		types[Material.JACK_O_LANTERN.getId()]= BlockType.SOLID;
		types[Material.CAKE_BLOCK.getId()]= BlockType.UNKNOWN;
	}


	public void check(final PlayerMoveEvent event) {

		long startTime = System.nanoTime();

		final Player player = event.getPlayer();

		// Should we check at all
		if(hasPermission(player)) {
			statisticElapsedTimeNano += System.nanoTime() - startTime;
			statisticTotalEvents++;
			return;
		}

		final boolean canFly;
		if(allowFlying || plugin.hasPermission(player, PermissionData.PERMISSION_FLYING))
			canFly = true;
		else
			canFly = false;

		// Get the player-specific data
		final MovingData data = MovingData.get(player);

		// Get the two locations of the event
		final Location to = event.getTo();
		Location from = event.getFrom();

		// WORKAROUND for changed PLAYER_MOVE logic
		if(data.teleportTo != null) {
			from = data.teleportTo;
			data.teleportTo = null;
		}

		// The use of event.getFrom() is intentional
		if(shouldBeIgnored(player, data, event.getFrom(), to)) {
			statisticElapsedTimeNano += System.nanoTime() - startTime;
			statisticTotalEvents++;
			return;
		}

		// The actual movingCheck starts here
		updateVelocity(player.getVelocity(), data);

		// First check the distance the player has moved horizontally
		final double xDistance = Math.abs(from.getX()-to.getX());
		final double zDistance = Math.abs(from.getZ()-to.getZ());

		final double combined = Math.sqrt((xDistance*xDistance + zDistance*zDistance));

		// If the target is a bed and distance not too big, allow it
		// Bukkit prevents using blocks behind walls already, so I don't have to check for that
		if(to.getWorld().getBlockTypeIdAt(to) == Material.BED_BLOCK.getId() && combined < 8.0D) {
			statisticElapsedTimeNano += System.nanoTime() - startTime;
			statisticTotalEvents++;
			return;
		}

		/**** Horizontal movement check START ****/

		int vl1 = -1;

		//if(player.isSneaking())
		if(false) // Currently disabled, still needs some additional work
			vl1 = limitCheck(combined - (data.horizFreedom + sneakStepWidth), moveLimits);
		else
			vl1 = limitCheck(combined - (data.horizFreedom + stepWidth), moveLimits);

		// Reduce horiz moving freedom with each event
		data.horizFreedom *= 0.9;

		/**** Horizontal movement check END ****/

		/**** Vertical movement check START ****/
		// pre-calculate boundary values that are needed multiple times in the following checks
		// the array each contains [lowerX, higherX, Y, lowerZ, higherZ]
		int fromValues[] = {lowerBorder(from.getX()), upperBorder(from.getX()), (int)Math.floor(from.getY()), lowerBorder(from.getZ()),upperBorder(from.getZ()) };
		int toValues[] = {lowerBorder(to.getX()), upperBorder(to.getX()), (int)Math.floor(to.getY()+0.5D), lowerBorder(to.getZ()), upperBorder(to.getZ()) };

		// compare locations to the world to guess if the player is standing on the ground, a half-block or next to a ladder
		final boolean onGroundFrom = playerIsOnGround(from.getWorld(), fromValues, from);
		final boolean onGroundTo = playerIsOnGround(to.getWorld(), toValues, to);

		int vl2 = -1;

		// A halfway lag-resistant method of allowing vertical acceleration without allowing blatant cheating

		// FACT: Minecraft server sends player "velocity" to the client and lets the client calculate the movement
		// PROBLEM: There may be an arbitrary amount of other move events between the server sending the data
		//          and the client accepting it/reacting to it. The server can't know when the client starts to
		//          consider the sent "velocity" in its movement.
		// SOLUTION: Give the client at least 10 events after sending "velocity" to actually use the velocity for
		//           its movement, plus additional events if the "velocity" was big and can cause longer flights

		// The server sent the player a "velocity" packet a short time ago
		if(data.maxYVelocity > 0.0D) {
			data.vertFreedomCounter = 30;

			// Be generous with the height limit for the client
			data.vertFreedom += data.maxYVelocity*2D;
			data.maxYVelocity = 0.0D;
		}

		// consume a counter for this client
		if(data.vertFreedomCounter > 0) {
			data.vertFreedomCounter--;
		}

		double limit = data.vertFreedom;

		// If the event counter has been consumed, remove the vertical movement limit increase when landing the next time
		if(data.vertFreedomCounter <= 0 && (onGroundFrom || onGroundTo)) {
			data.vertFreedom = 0.0D;
		}

		// The location we'd use as a new setback if there are no violations
		Location newSetBack = null;

		// there's no use for counting this
		if(canFly) data.jumpPhase = 0;

		// Handle 4 distinct cases: Walk, Jump, Land, Fly

		// Walk or start Jump
		if(onGroundFrom)
		{
			limit += jumpHeight;
			double distance = to.getY() - from.getY();

			vl2 = limitCheck(distance - limit, heightLimits);

			if(vl2 < 0)
			{
				// reset jumping
				if(onGroundTo)
					data.jumpPhase = 0; // Walk
				else
					data.jumpPhase = 1; // Jump

				newSetBack = from.clone();
			}
		}
		// Land or Fly/Fall
		else
		{
			Location l = null;

			if(data.setBackPoint == null || canFly)
				l = from;
			else
				l = data.setBackPoint;

			if(!canFly && data.jumpPhase > jumpingLimit)
				limit += jumpHeight - (data.jumpPhase-jumpingLimit) * 0.2D;
			else limit += jumpHeight;

			if(onGroundTo) limit += stepHeight;

			double distance = to.getY() - l.getY();

			// Check if player isn't jumping too high
			vl2 = limitCheck(distance - limit, heightLimits);

			if(vl2 < 0) {
				if(onGroundTo) { // Land
					data.jumpPhase = 0; // He is on ground now, so reset the jump
					newSetBack = to.clone();
				}
				else { // Fly
					data.jumpPhase++; // Enter next phase of the flight
					// If we have no setback point, create one now
					if(data.setBackPoint == null) { 
						newSetBack = from.clone();
					}
				}
			}
		}

		int vl = vl1 > vl2 ? vl1 : vl2;

		if(vl < 0 && newSetBack != null) {
			data.setBackPoint = newSetBack;
		}

		// If we haven't already got a setback point by now, make this location the new setback point
		if(data.setBackPoint == null) {
			data.setBackPoint = from.clone();
		}

		if(vl >= 0) {
			setupSummaryTask(event.getPlayer(), data);

			boolean log = !(data.violationsInARow[vl] > 0);
			data.violationsInARow[vl]++;

			action(event, event.getPlayer(), from, to, actions[vl], log, data);
		}

		statisticElapsedTimeNano += System.nanoTime() - startTime;
		statisticTotalEvents++;
	}

	private boolean shouldBeIgnored(Player player, MovingData data, Location from, Location to) {
		if(from.equals(to)) // Both locations are perfectly identical
			return true;
		else if(!from.equals(data.lastLocation)) { // The player was moved somehow without causing a move event
			resetData(data, to);
			return true;
		}
		else if(data.respawned) {
			data.respawned = false;
			return true;
		}
		else if(data.worldChanged) {
			data.worldChanged = false;
			return true;
		}
		// vehicles are a special case, I ignore them because the server controls them
		else if(player.isInsideVehicle()) {
			return true;
		}
		return false;
	}


	private void setupSummaryTask(final Player p, final MovingData data) {
		// Setup task to display summary later
		if(data.summaryTask == null) {
			data.summaryTask = new Runnable() {

				@Override
				public void run() {
					if(data.highestLogLevel != null) {
						String logString =  String.format(summaryMessage, p.getName(), ticksBeforeSummary/20, data.violationsInARow[0], data.violationsInARow[1],data.violationsInARow[2]);
						plugin.log(data.highestLogLevel, logString);
					}
					// deleting its own reference
					data.summaryTask = null;

					data.violationsInARow[0] = 0;
					data.violationsInARow[1] = 0;
					data.violationsInARow[2] = 0;
				}
			};

			// Give a summary in x ticks. 20 ticks ~ 1 second
			plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, data.summaryTask, ticksBeforeSummary);
		}
	}

	/**
	 * Call this when a player got successfully teleported with the corresponding event to set new "setback" points
	 * and reset data (if necessary)
	 * 
	 * @param event
	 */
	public void teleported(PlayerTeleportEvent event) {

		MovingData data = MovingData.get(event.getPlayer());

		if(event.getTo().equals(data.teleportInitializedByMe)) { // My plugin requested this teleport while handling another event

			// DANGEROUS, but I have no real choice on that one thanks to Essentials jail simply blocking ALL kinds of teleports
			// even the respawn teleport, the player moved wrongly teleport, the "get player out of the void" teleport", ...

			// TODO: Make this optional OR detect Essentials and make this dependent on essential
			event.setCancelled(false);
			data.teleportInitializedByMe = null;
			//data.movingTeleportTo = event.getTo();
		}
		else if(!event.isCancelled()) {
			// If it wasn't our plugin that ordered the teleport, forget (almost) all our information and start from scratch
			resetData(data, event.getTo());

			if(event.getFrom().getWorld().equals(event.getTo().getWorld())) {
				// WORKAROUND for changed PLAYER_MOVE logic - I need to remember the "to" location of teleports and use it as a from-Location
				// for the move event that comes next
				data.teleportTo = event.getTo();
			}
			else
				data.worldChanged = true;
		}
	}

	public void respawned(PlayerRespawnEvent event) {
		MovingData data = MovingData.get(event.getPlayer());

		data.respawned = true;

	}

	public void updateVelocity(Vector v, MovingData data) {

		// Compare the velocity vector to the existing movement freedom that we've from previous events
		double tmp = (Math.abs(v.getX()) + Math.abs(v.getZ())) * 2D;
		if(tmp > data.horizFreedom)
			data.horizFreedom = tmp;

		if(v.getY() > data.maxYVelocity) {
			data.maxYVelocity = v.getY();
		}
	}
	/**
	 * Perform actions that were specified in the config file
	 * @param event
	 * @param action
	 */
	private void action(PlayerMoveEvent event, Player player, Location from, Location to, Action[] actions, boolean loggingAllowed, MovingData data) {

		if(actions == null) return;
		boolean cancelled = false;

		// prepare log message if necessary
		String log = null;

		if(loggingAllowed) {
			log = String.format(logMessage, player.getName(), from.getWorld().getName(), to.getWorld().getName(), from.getX(), from.getY(), from.getZ(), to.getX(), to.getY(), to.getZ());
		}

		for(Action a : actions) {
			if(loggingAllowed && a instanceof LogAction)  {
				plugin.log(((LogAction)a).level, log);
				if(data.highestLogLevel == null) data.highestLogLevel = Level.ALL;
				if(data.highestLogLevel.intValue() < ((LogAction)a).level.intValue()) data.highestLogLevel = ((LogAction)a).level;
			}
			else if(!cancelled && a instanceof CancelAction) {
				resetPlayer(event, from);
				cancelled = true;
			}
			else if(a instanceof CustomAction)
				plugin.handleCustomAction(a, player);
		}
	}

	private int limitCheck(double value, double limits[]) {

		for(int i = limits.length - 1; i >= 0; i--) {
			if(value > limits[i]) {
				return i;
			}
		}

		return -1;
	}

	/** 
	 * Return the player to a stored location or if that is not available,
	 * the previous location.
	 * @param data
	 * @param event
	 */
	private void resetPlayer(PlayerMoveEvent event, Location from) {

		MovingData data = MovingData.get(event.getPlayer());

		// Reset the jumpphase. We choose the setback-point such that it should be
		// on solid ground, but in case it isn't (maybe the ground is gone now) we
		// still have to allow the player some freedom with vertical movement due
		// to lost vertical momentum to prevent him from getting stuck

		if(data.setBackPoint == null) data.setBackPoint = from.clone();

		// Set a flag that gets used while handling teleport events (to determine if
		// it was my teleport or someone else'
		Location t = data.setBackPoint;

		t = new Location(t.getWorld(), t.getX(),  t.getY(), t.getZ(), event.getTo().getYaw(), event.getTo().getPitch());
		data.teleportInitializedByMe = t;

		resetData(data, t);

		// Only reset player and cancel event if teleport is successful
		if(event.getPlayer().teleport(t)) {

			// Put the player back to the chosen location
			event.setFrom(t);
			event.setTo(t);

			event.setCancelled(true);
		}
	}


	/**
	 * Check if certain coordinates are considered "on ground" or in air
	 * 
	 * @param w	The world the coordinates belong to
	 * @param values The coordinates [lowerX, higherX, Y, lowerZ, higherZ]
	 * @param l The location that was used for calculation of "values"
	 * @return
	 */
	private static boolean playerIsOnGround(World w, int values[], Location l) {

		// Check the four borders of the players hitbox for something he could be standing on
		if(types[w.getBlockTypeIdAt(values[0], values[2]-1, values[3])] != BlockType.NONSOLID ||
				types[w.getBlockTypeIdAt(values[1], values[2]-1, values[3])] != BlockType.NONSOLID ||
				types[w.getBlockTypeIdAt(values[0], values[2]-1, values[4])] != BlockType.NONSOLID ||
				types[w.getBlockTypeIdAt(values[1], values[2]-1, values[4])] != BlockType.NONSOLID )
			return true;
		// Check if he is hanging onto a ladder
		else if(types[w.getBlockTypeIdAt(l.getBlockX(), values[2], l.getBlockZ())] == BlockType.LADDER || 
				types[w.getBlockTypeIdAt(l.getBlockX(), values[2]+1, l.getBlockZ())] == BlockType.LADDER)
			return true;
		// check if he is standing "in" a block that's potentially solid (we give him the benefit of a doubt and see that as a legit move)
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
		// Allow using a bug called "water elevator" by checking northwest of the players location for liquids
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
		// Running on fences
		else if(types[w.getBlockTypeIdAt(values[0], values[2]-2, values[3])] == BlockType.FENCE ||
				types[w.getBlockTypeIdAt(values[1], values[2]-2, values[3])] == BlockType.FENCE ||
				types[w.getBlockTypeIdAt(values[0], values[2]-2, values[4])] == BlockType.FENCE ||
				types[w.getBlockTypeIdAt(values[1], values[2]-2, values[4])] == BlockType.FENCE )
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

	private void resetData(MovingData data, Location l) {
		// If it wasn't our plugin that ordered the teleport, forget (almost) all our information and start from scratch
		data.setBackPoint = l;
		data.jumpPhase = 0;
		data.teleportTo = null;
	}

	@Override
	protected void registerListeners() {
		PluginManager pm = Bukkit.getServer().getPluginManager();

		Listener movingPlayerMonitor = new MovingPlayerMonitor(this);

		// Register listeners for moving check
		pm.registerEvent(Event.Type.PLAYER_MOVE, new MovingPlayerListener(this), Priority.Lowest, plugin);
		pm.registerEvent(Event.Type.PLAYER_INTERACT, movingPlayerMonitor, Priority.Monitor, plugin);
		pm.registerEvent(Event.Type.PLAYER_MOVE, movingPlayerMonitor, Priority.Monitor, plugin);
		pm.registerEvent(Event.Type.PLAYER_RESPAWN, movingPlayerMonitor, Priority.Monitor, plugin);
		pm.registerEvent(Event.Type.ENTITY_DAMAGE, new MovingEntityListener(this), Priority.Monitor, plugin);
		pm.registerEvent(Event.Type.PLAYER_TELEPORT, new MovingPlayerMonitor(this), Priority.Monitor, plugin);		
	}
}
