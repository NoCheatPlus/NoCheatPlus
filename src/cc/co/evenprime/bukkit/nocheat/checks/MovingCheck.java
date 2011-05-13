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

import cc.co.evenprime.bukkit.nocheat.ConfigurationException;
import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.actions.Action;
import cc.co.evenprime.bukkit.nocheat.actions.CancelAction;
import cc.co.evenprime.bukkit.nocheat.actions.CustomAction;
import cc.co.evenprime.bukkit.nocheat.actions.LogAction;
import cc.co.evenprime.bukkit.nocheat.config.NoCheatConfiguration;
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

	public MovingCheck(NoCheat plugin, NoCheatConfiguration config) {
		super(plugin, "moving", PermissionData.PERMISSION_MOVING, config);

	}

	// How many move events can a player have in air before he is expected to lose altitude (or land somewhere)
	private final static int jumpingLimit = 4;

	// How high may a player get compared to his last location with ground contact
	private final static double jumpHeight = 1.3D;

	// How high may a player move in one event on ground
	private final static double stepHeight = 0.501D;

	private final static double stepWidth = 0.6D;
	private final static double sneakStepWidth = 0.25D;

	private int ticksBeforeSummary = 100;

	public long statisticElapsedTimeNano = 0;

	public boolean allowFlying;
	public boolean allowFakeSneak;

	private String logMessage;
	private String summaryMessage;

	// How should moving violations be treated?
	private Action actions[][];

	public long statisticTotalEvents = 1; // Prevent accidental division by 0 at some point

	private static final double magic =  0.30000001192092896D;
	private static final double magic2 = 0.69999998807907103D;

	/**
	 * The actual check.
	 * First find out if the event needs to be handled at all
	 * Second check if the player moved too far horizontally
	 * Third check if the player moved too high vertically
	 * Fourth treat any occured violations as configured
	 * @param event
	 */
	public void check(final PlayerMoveEvent event) {

		final Player player = event.getPlayer();

		// Should we check at all
		if(skipCheck(player)) {	return;	}

		final long startTime = System.nanoTime();

		// Get the player-specific data
		final MovingData data = MovingData.get(player);

		// Get the two locations of the event
		final Location to = event.getTo();
		Location from = event.getFrom();

		// The use of event.getFrom() is intentional
		if(shouldBeIgnored(player, data, from, to)) {
			statisticElapsedTimeNano += System.nanoTime() - startTime;
			statisticTotalEvents++;
			return;
		}

		// WORKAROUND for changed PLAYER_MOVE logic
		if(data.teleportTo != null) {
			from = data.teleportTo;
			data.teleportTo = null;
		}


		// First check the distance the player has moved horizontally
		final double xDistance = from.getX()-to.getX();
		final double zDistance = from.getZ()-to.getZ();

		double combined = Math.sqrt((xDistance*xDistance + zDistance*zDistance));


		// If the target is a bed and distance not too big, allow it
		// Bukkit prevents using blocks behind walls already, so I don't have to check for that
		if(to.getWorld().getBlockTypeIdAt(to) == Material.BED_BLOCK.getId() && combined < 8.0D) {
			statisticElapsedTimeNano += System.nanoTime() - startTime;
			statisticTotalEvents++;
			return;
		}


		final boolean onGroundFrom = playerIsOnGround(from, 0.0D);		

		final boolean canFly;
		if(allowFlying || plugin.hasPermission(player, PermissionData.PERMISSION_FLYING)) {
			canFly = true;
			data.jumpPhase = 0;
		}
		else
			canFly = false;

		final boolean canFakeSneak;
		if(allowFakeSneak || plugin.hasPermission(player, PermissionData.PERMISSION_FAKESNEAK)) {
			canFakeSneak = true;
		}
		else
			canFakeSneak = false;

		/**** Horizontal movement check START ****/

		int violationLevelSneaking = -1;

		if(!canFakeSneak && player.isSneaking()) {
			violationLevelSneaking = limitCheck(combined - (data.horizFreedom + sneakStepWidth));
			if(violationLevelSneaking >= 0) {
				if(combined >= data.sneakingLastDistance)
					data.sneakingFreedomCounter -= 2;
				else
				{
					violationLevelSneaking = -1;
				}
			}

			data.sneakingLastDistance = combined;
		}

		if(violationLevelSneaking >= 0 && data.sneakingFreedomCounter > 0) {
			violationLevelSneaking = -1;
		}
		else if(violationLevelSneaking < 0 && data.sneakingFreedomCounter < 10){
			data.sneakingFreedomCounter += 1;
		}

		int violationLevelHorizontal = limitCheck(combined - (data.horizFreedom + stepWidth));

		violationLevelHorizontal = violationLevelHorizontal > violationLevelSneaking ? violationLevelHorizontal : violationLevelSneaking;

		// Reduce horiz moving freedom with each event
		data.horizFreedom *= 0.9;

		/**** Horizontal movement check END ****/

		/**** Vertical movement check START ****/

		int violationLevelVertical = -1;

		// The location we'd use as a new setback if there are no violations
		Location newSetBack = null;

		double limit = calculateVerticalLimit(data, onGroundFrom);

		// Handle 4 distinct cases: Walk, Jump, Land, Fly

		// Walk or start Jump
		if(onGroundFrom)
		{
			limit += jumpHeight;
			final double distance = to.getY() - from.getY();

			violationLevelVertical = limitCheck(distance - limit);

			if(violationLevelVertical < 0)
			{				
				// reset jumping
				data.jumpPhase = 0;

				newSetBack = from;
			}
		}
		// Land or Fly/Fall
		else
		{
			final Location l;

			if(data.setBackPoint == null || canFly)
				l = from;
			else
				l = data.setBackPoint;

			if(!canFly && data.jumpPhase > jumpingLimit)
				limit += jumpHeight - (data.jumpPhase-jumpingLimit) * 0.2D;
			else limit += jumpHeight;

			final boolean onGroundTo = playerIsOnGround(to, 0.5D);

			if(onGroundTo) limit += stepHeight;

			final double distance = to.getY() - l.getY();

			// Check if player isn't jumping too high
			violationLevelVertical = limitCheck(distance - limit);

			if(violationLevelVertical < 0) {
				if(onGroundTo) { // Land
					data.jumpPhase = 0; // He is on ground now, so reset the jump
					newSetBack = to;
				}
				else { // Fly
					data.jumpPhase++; // Enter next phase of the flight
					// If we have no setback point, create one now
					if(data.setBackPoint == null) { 
						newSetBack = from;
					}
				}
			}
		}

		/**** Vertical movement check END ****/

		/****** Violation Handling START *****/
		int violationLevel = violationLevelHorizontal > violationLevelVertical ? violationLevelHorizontal : violationLevelVertical;

		if(violationLevel < 0 && newSetBack != null) {
			data.setBackPoint = newSetBack;
		}

		// If we haven't already got a setback point by now, make this location the new setback point
		if(data.setBackPoint == null) {
			data.setBackPoint = from;
		}

		if(violationLevel >= 0) {
			setupSummaryTask(event.getPlayer(), data);

			data.violationsInARow[violationLevel]++;

			action(event, event.getPlayer(), from, to, actions[violationLevel], data.violationsInARow[violationLevel], data);
		}

		/****** Violation Handling END *****/

		statisticElapsedTimeNano += System.nanoTime() - startTime;
		statisticTotalEvents++;
	}

	private double calculateVerticalLimit(final MovingData data, final boolean onGroundFrom) {

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

		final double limit = data.vertFreedom;

		// If the event counter has been consumed, remove the vertical movement limit increase when landing the next time
		if(onGroundFrom && data.vertFreedomCounter <= 0) {
			data.vertFreedom = 0.0D;
		}

		return limit;
	}

	/**
	 * Various corner cases that would cause this check to fail or require special treatment
	 * @param player
	 * @param data
	 * @param from
	 * @param to
	 * @return
	 */
	private boolean shouldBeIgnored(final Player player, final MovingData data, final Location from, final Location to) {

		// Identical locations - just ignore the event
		final double x = from.getX();
		final double y = from.getY();
		final double z = from.getZ();
		final Location l = data.lastLocation;

		if(x == to.getX() && z == to.getZ() && y == to.getY() ) {
			return true;
		}
		// Something or someone moved the player without causing a move event - Can't do much with that
		else if(!(x == l.getX() && z == l.getZ() && y == l.getY())){
			resetData(data, to);
			return true;
		}
		// Player was respawned just before, this causes all kinds of weirdness - better ignore it
		else if(data.respawned) {
			data.respawned = false;
			return true;
		}
		// Player changed the world before, which makes any location information basically useless
		else if(data.worldChanged) {
			data.worldChanged = false;
			return true;
		}
		// Player is inside a vehicle, this causes all kinds of weirdness - better ignore it
		else if(player.isInsideVehicle()) {
			return true;
		}
		return false;
	}


	/**
	 * Register a task with bukkit that will be run a short time from now, displaying how many
	 * violations happened in that timeframe
	 * @param p
	 * @param data
	 */
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

	/**
	 * Set a flag to declare that the player recently respawned
	 * @param event
	 */
	public void respawned(PlayerRespawnEvent event) {
		MovingData data = MovingData.get(event.getPlayer());

		data.respawned = true;

	}

	/**
	 * Update the cached values for players velocity to be prepared to
	 * give them additional movement freedom in their next move events
	 * @param v
	 * @param data
	 */
	public void updateVelocity(Vector v, MovingData data) {

		// Compare the velocity vector to the existing movement freedom that we've from previous events
		double tmp = (Math.abs(v.getX()) + Math.abs(v.getZ())) * 3D;
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
	private void action(PlayerMoveEvent event, Player player, Location from, Location to, Action[] actions, int violations, MovingData data) {

		if(actions == null) return;
		boolean cancelled = false;

		
		for(Action a : actions) {
			if(a.firstAfter <= violations) {
				if(a.firstAfter == violations || a.repeat) {
					if(a instanceof LogAction)  {
						// prepare log message if necessary
						String log = String.format(logMessage, player.getName(), from.getWorld().getName(), to.getWorld().getName(), from.getX(), from.getY(), from.getZ(), to.getX(), to.getY(), to.getZ());

						plugin.log(((LogAction)a).level, log);
						
						// Remember the highest log level we encountered to determine what level the summary log message should have
						if(data.highestLogLevel == null) data.highestLogLevel = Level.ALL;
						if(data.highestLogLevel.intValue() < ((LogAction)a).level.intValue()) data.highestLogLevel = ((LogAction)a).level;
					}
					else if(!cancelled && a instanceof CancelAction) {
						resetPlayer(event, from);
						cancelled = true;
					}
					else if(a instanceof CustomAction)
						plugin.handleCustomAction((CustomAction)a, player);
				}
			}
		}
	}

	/**
	 * Check a value against an array of sorted values to find out
	 * where it fits in
	 * @param value
	 * @param limits
	 * @return
	 */
	private static int limitCheck(final double value) {

		if(value > 0.0D) {
			if(value > 0.5D) {
				if(value > 2.0D) 
					return 2;
				return 1; }
			return 0; }
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

		if(data.setBackPoint == null) data.setBackPoint = from;

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
	 * Check if certain coordinates are considered "on ground"
	 * 
	 * @param w	The world the coordinates belong to
	 * @param values The coordinates [lowerX, higherX, Y, lowerZ, higherZ] to be checked
	 * @param l The precise location that was used for calculation of "values"
	 * @return
	 */
	private static boolean playerIsOnGround(final Location l, final double ymod) {

		final int types[] = MovingData.types;

		final World w = l.getWorld();

		final int lowerX = lowerBorder(l.getX());
		final int upperX = upperBorder(l.getX());
		final int Y = (int)Math.floor(l.getY() + ymod);
		final int lowerZ = lowerBorder(l.getZ());
		final int higherZ = upperBorder(l.getZ());


		// Check the four borders of the players hitbox for something he could be standing on
		if(types[w.getBlockTypeIdAt(lowerX, Y-1, lowerZ)] != MovingData.NONSOLID ||
				types[w.getBlockTypeIdAt(upperX, Y-1, lowerZ)] != MovingData.NONSOLID ||
				types[w.getBlockTypeIdAt(lowerX, Y-1, higherZ)] != MovingData.NONSOLID ||
				types[w.getBlockTypeIdAt(upperX, Y-1, higherZ)] != MovingData.NONSOLID )
			return true;
		// Check if he is hanging onto a ladder
		else if(types[w.getBlockTypeIdAt(l.getBlockX(), Y, l.getBlockZ())] == MovingData.LADDER || 
				types[w.getBlockTypeIdAt(l.getBlockX(), Y+1, l.getBlockZ())] == MovingData.LADDER)
			return true;
		// check if he is standing "in" a block that's potentially solid (we give him the benefit of a doubt and see that as a legit move)
		// If it is not legit, the MC server already has a safeguard against that (You'll get "xy moved wrongly" on the console in that case)
		else if(types[w.getBlockTypeIdAt(lowerX, Y, lowerZ)] != MovingData.NONSOLID ||
				types[w.getBlockTypeIdAt(upperX, Y, lowerZ)] != MovingData.NONSOLID||
				types[w.getBlockTypeIdAt(lowerX, Y, higherZ)] != MovingData.NONSOLID ||
				types[w.getBlockTypeIdAt(upperX, Y, higherZ)] != MovingData.NONSOLID)
			return true;
		// check if his head is "stuck" in an block that's potentially solid (we give him the benefit of a doubt and see that as a legit move)
		// If it is not legit, the MC server already has a safeguard against that (You'll get "xy moved wrongly" on the console in that case)
		else if(types[w.getBlockTypeIdAt(lowerX, Y+1, lowerZ)] != MovingData.NONSOLID ||
				types[w.getBlockTypeIdAt(upperX, Y+1, lowerZ)] != MovingData.NONSOLID ||
				types[w.getBlockTypeIdAt(lowerX, Y+1, higherZ)] != MovingData.NONSOLID ||
				types[w.getBlockTypeIdAt(upperX, Y+1, higherZ)] != MovingData.NONSOLID)
			return true;
		// Allow using a bug called "water elevator" by checking northwest of the players location for liquids
		else if(types[w.getBlockTypeIdAt(lowerX+1, Y-1, lowerZ+1)] == MovingData.LIQUID ||
				types[w.getBlockTypeIdAt(lowerX+1, Y, lowerZ+1)] == MovingData.LIQUID ||
				types[w.getBlockTypeIdAt(lowerX+1, Y+1, lowerZ+1)] == MovingData.LIQUID ||
				types[w.getBlockTypeIdAt(lowerX+1, Y-1, lowerZ)] == MovingData.LIQUID ||
				types[w.getBlockTypeIdAt(lowerX+1, Y, lowerZ)] == MovingData.LIQUID ||
				types[w.getBlockTypeIdAt(lowerX+1, Y+1, lowerZ)] == MovingData.LIQUID ||
				types[w.getBlockTypeIdAt(lowerX, Y-1, lowerZ+1)] == MovingData.LIQUID ||
				types[w.getBlockTypeIdAt(lowerX, Y, lowerZ+1)] == MovingData.LIQUID ||
				types[w.getBlockTypeIdAt(lowerX, Y+1, lowerZ+1)] == MovingData.LIQUID)
			return true;
		// Running on fences
		else if(types[w.getBlockTypeIdAt(lowerX, Y-2, lowerZ)] == MovingData.FENCE ||
				types[w.getBlockTypeIdAt(upperX, Y-2, lowerZ)] == MovingData.FENCE ||
				types[w.getBlockTypeIdAt(lowerX, Y-2, higherZ)] == MovingData.FENCE ||
				types[w.getBlockTypeIdAt(upperX, Y-2, higherZ)] == MovingData.FENCE )
			return true;
		else
			return false;
	}


	/**
	 * Personal Rounding function to determine if a player is still touching a block or not
	 * @param d1
	 * @return
	 */
	private static int lowerBorder(double d1) {

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
	private static int upperBorder(double d1) {

		double floor = Math.floor(d1);
		double d4 = floor + magic2;

		if(d4 < d1)
			d4 = -1;
		else
			d4 = 0;

		return (int) (floor - d4);
	}

	/**
	 * Reset all temporary information of this check
	 * @param data
	 * @param l
	 */
	private void resetData(MovingData data, Location l) {

		data.setBackPoint = l;
		data.jumpPhase = 0;
		data.teleportTo = null;
	}

	@Override
	public void configure(NoCheatConfiguration config) {

		try {
			allowFlying = config.getBooleanValue("moving.allowflying");
			allowFakeSneak = config.getBooleanValue("moving.allowfakesneak");

			logMessage = config.getStringValue("moving.logmessage");
			summaryMessage = config.getStringValue("moving.summarymessage");

			actions = new Action[3][];
			
			actions[0] = config.getActionValue("moving.action.low");
			actions[1] = config.getActionValue("moving.action.med");
			actions[2] = config.getActionValue("moving.action.high");
			
			setActive(config.getBooleanValue("active.moving"));
		} catch (ConfigurationException e) {
			setActive(false);
			e.printStackTrace();
		}

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
