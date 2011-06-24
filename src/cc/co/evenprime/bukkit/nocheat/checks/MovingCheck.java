package cc.co.evenprime.bukkit.nocheat.checks;

import java.util.Locale;
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
	private final static double sneakWidth = 0.25D;
	private final static double swimWidth = 0.4D;

	private int ticksBeforeSummary = 100;

	public long statisticElapsedTimeNano = 0;

	public boolean allowFlying;
	public boolean allowFakeSneak;
	public boolean allowFastSwim;
	public boolean checkOPs;

	private boolean waterElevators;

	private String logMessage;
	private String summaryMessage;

	// How should moving violations be treated?
	private Action actions[][];

	public long statisticTotalEvents = 1; // Prevent accidental division by 0 at some point

	private boolean enforceTeleport;



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

		// use our self-defined from-location, instead of the one from the event
		Location from = data.lastLocation;

		updateVelocity(player.getVelocity(), data);

		// event.getFrom() is intentional here
		if(shouldBeIgnored(player, data, event.getFrom(), to)) {
			statisticElapsedTimeNano += System.nanoTime() - startTime;
			statisticTotalEvents++;
			return;
		}

		/**** Horizontal movement check START ****/

		// First check the distance the player has moved horizontally
		final double xDistance = from.getX()-to.getX();
		final double zDistance = from.getZ()-to.getZ();

		double combined = Math.sqrt((xDistance*xDistance + zDistance*zDistance));

		// If the target is a bed and distance not too big, allow it always
		// Bukkit prevents using blocks behind walls already, so I don't have to check for that
		if(to.getWorld().getBlockTypeIdAt(to) == Material.BED_BLOCK.getId() && combined < 8.0D) {
			statisticElapsedTimeNano += System.nanoTime() - startTime;
			statisticTotalEvents++;
			return;
		}

		final int onGroundFrom = playerIsOnGround(from, 0.0D);


		// Do various checks on the players horizontal movement
		int sn = getSneakingViolationLevel(combined, data, player);
		int sw = getSwimmingViolationLevel(combined, data, onGroundFrom == MovingData.LIQUID, player);
		int s = limitCheck(combined - (data.horizFreedom + stepWidth));

		// The maximum of the three values is the biggest violation measured
		int violationLevelHorizontal = sn > sw && sn > s ? sn : (sw > s ? sw : s);

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
		if(onGroundFrom != MovingData.NONSOLID)
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

			final boolean canFly = allowFlying || plugin.hasPermission(player, PermissionData.PERMISSION_FLYING, checkOPs);

			if(data.setBackPoint == null || canFly)
				l = from;
			else
				l = data.setBackPoint;

			if(!canFly && data.jumpPhase > jumpingLimit)
				limit += jumpHeight - (data.jumpPhase-jumpingLimit) * 0.2D;
			else limit += jumpHeight;

			final int onGroundTo = playerIsOnGround(to, 0.5D);

			if(onGroundTo != MovingData.NONSOLID) limit += stepHeight;

			final double distance = to.getY() - l.getY();

			// Check if player isn't jumping too high
			violationLevelVertical = limitCheck(distance - limit);

			if(violationLevelVertical < 0) {
				if(onGroundTo != MovingData.NONSOLID) { // Land
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


	/**
	 * Run a check if a sneaking player is moving too fast
	 *  
	 * @param combined Distance moved
	 * @param data the players data
	 * @param player the player
	 * @return violation level
	 */
	private int getSneakingViolationLevel(final double combined, final MovingData data, final Player player) {

		int violationLevelSneaking = -1;

		// Maybe the player is allowed to sneak faster than usual?
		final boolean canFakeSneak = allowFakeSneak || plugin.hasPermission(player, PermissionData.PERMISSION_FAKESNEAK, checkOPs);

		if(!canFakeSneak) {

			// Explaination blob:
			// When a player starts to sneak, he may have a phase where he is still moving faster than he
			// should be, e.g. because he is in air, on slippery ground, ...
			// Therefore he gets a counter that gets reduced everytime he is too fast and slowly incremented
			// every time he is slow enough
			// If the counter reaches zero, his movement is considered a violation.
			if(player.isSneaking()) {
				violationLevelSneaking = limitCheck(combined - (data.horizFreedom + sneakWidth));
				if(violationLevelSneaking >= 0) {
					if(combined >= data.sneakingLastDistance * 0.9)
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
		}

		return violationLevelSneaking;		
	}

	private int getSwimmingViolationLevel( final double combined, final MovingData data, final boolean isSwimming, final Player player) {

		int violationLevelSwimming = -1;

		// Maybe the player is allowed to swim faster than usual?
		final boolean canFastSwim = allowFastSwim || plugin.hasPermission(player, PermissionData.PERMISSION_FASTSWIM, checkOPs);

		if(!canFastSwim) {

			final double limit = data.horizFreedom + swimWidth;


			// Explaination blob:
			// When a player starts to swim, he may have a phase where he is still moving faster than he
			// should be, e.g. because he jumped into the water ...
			// Therefore he gets a counter that gets reduced everytime he is too fast and slowly incremented
			// every time he is slow enough
			// If the counter reaches zero, his movement is considered a violation.
			if(isSwimming) {
				violationLevelSwimming = limitCheck(combined - limit);
				if(violationLevelSwimming >= 0) {
					if(combined >= data.swimmingLastDistance * 0.9)
						data.swimmingFreedomCounter -= 2;
					else
					{
						violationLevelSwimming = -1;
					}
				}

				data.swimmingLastDistance = combined;
			}

			if(violationLevelSwimming >= 0 && data.swimmingFreedomCounter > 0) {
				violationLevelSwimming = -1;
			}
			else if(violationLevelSwimming < 0 && data.swimmingFreedomCounter < 10){
				data.swimmingFreedomCounter += 1;
			}

		}

		return violationLevelSwimming;		
	}

	private double calculateVerticalLimit(final MovingData data, final int onGroundFrom) {

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
		if(onGroundFrom != MovingData.NONSOLID && data.vertFreedomCounter <= 0) {
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

		// First the simple yes/no checks
		if(data.insideVehicle || player.isInsideVehicle()) {
			return true;
		}

		// More sophisticated checks
		final Location l = data.lastLocation;

		// Player is currently changing worlds
		if(l.getWorld() != from.getWorld()) {
			return true;
		}

		final double x = from.getX();
		final double y = from.getY();
		final double z = from.getZ();

		// Player didn't move at all
		if(x == to.getX() && z == to.getZ() && y == to.getY() ) {
			return true;
		}
		// Something or someone moved the player without causing a move event - Can't do much with that
		else if(!(x == l.getX() && z == l.getZ() && y == l.getY())){
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
		if(data.summaryTask == -1) {
			Runnable r = new Runnable() {

				@Override
				public void run() {
					if(data.highestLogLevel != null) {
						String logString =  String.format(summaryMessage, p.getName(), ticksBeforeSummary/20, data.violationsInARow[0], data.violationsInARow[1],data.violationsInARow[2]);
						plugin.log(data.highestLogLevel, logString);
					}
					// deleting its own reference
					data.summaryTask = -1;

					data.violationsInARow[0] = 0;
					data.violationsInARow[1] = 0;
					data.violationsInARow[2] = 0;
				}
			};

			// Give a summary in x ticks. 20 ticks ~ 1 second
			data.summaryTask = plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, r, ticksBeforeSummary);
		}
	}

	/**
	 * Call this when a player got successfully teleported with the corresponding event to adjust stored
	 * data to the new situation
	 * 
	 * @param event
	 */
	public void teleported(PlayerTeleportEvent event) {

		MovingData data = MovingData.get(event.getPlayer());

		// We can enforce a teleport, if that flag is explicitly set
		if(event.isCancelled() && enforceTeleport && event.getTo().equals(data.teleportTo)) {
			event.setCancelled(false);
		}

		if(!event.isCancelled()) {
			data.lastLocation = event.getTo();
			data.jumpPhase = 0;
			data.setBackPoint = event.getTo();
		}
	}

	/**
	 * Set a flag to declare that the player recently respawned
	 * @param event
	 */
	public void respawned(PlayerRespawnEvent event) {
		MovingData data = MovingData.get(event.getPlayer());
		data.lastLocation = event.getRespawnLocation(); // We expect the player to be there next
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
						String log = String.format(Locale.US, logMessage, player.getName(), from.getWorld().getName(), to.getWorld().getName(), from.getX(), from.getY(), from.getZ(), to.getX(), to.getY(), to.getZ(), Math.abs(from.getX()-to.getX()),to.getY()-from.getY(), Math.abs(from.getZ()-to.getZ()));

						plugin.log(((LogAction)a).level, log);

						// Remember the highest log level we encountered to determine what level the summary log message should have
						if(data.highestLogLevel == null) data.highestLogLevel = Level.ALL;
						if(data.highestLogLevel.intValue() < ((LogAction)a).level.intValue()) data.highestLogLevel = ((LogAction)a).level;
					}
					else if(!cancelled && a instanceof CancelAction) {
						// Make a modified copy of the setBackPoint to prevent other plugins from accidentally modifying it
						// and keep the current pitch and yaw (setbacks "feel" better that way). Plus try to adapt the Y-coord
						// to place the player close to ground

						double y = data.setBackPoint.getY();

						// search for the first solid block up to 5 blocks below the setbackpoint and teleport the player there
						for(int i = 0; i < 20; i++) {
							if(playerIsOnGround(data.setBackPoint, -0.5*i) != MovingData.NONSOLID) {
								y -= 0.5*i;
								break;
							}
						}	

						// Remember the location we send the player to, to identify teleports that were started by us
						data.teleportTo = new Location(data.setBackPoint.getWorld(), data.setBackPoint.getX(), y, data.setBackPoint.getZ(), event.getTo().getYaw(), event.getTo().getPitch());

						event.setTo(data.teleportTo);

						cancelled = true; // just prevent us from treating more than one "cancel" action, which would make no sense
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
	 * Check if certain coordinates are considered "on ground"
	 * 
	 * @param w	The world the coordinates belong to
	 * @param values The coordinates [lowerX, higherX, Y, lowerZ, higherZ] to be checked
	 * @param l The precise location that was used for calculation of "values"
	 * @return
	 */
	private int playerIsOnGround(final Location l, final double ymod) {

		final int types[] = MovingData.types;

		final World w = l.getWorld();

		final int lowerX = lowerBorder(l.getX());
		final int upperX = upperBorder(l.getX());
		final int Y = (int)Math.floor(l.getY() + ymod);
		final int lowerZ = lowerBorder(l.getZ());
		final int higherZ = upperBorder(l.getZ());


		int result;

		// check in what kind of block the player is standing "in"
		result = types[w.getBlockTypeIdAt(lowerX, Y, lowerZ)] | types[w.getBlockTypeIdAt(upperX, Y, lowerZ)] |
		types[w.getBlockTypeIdAt(lowerX, Y, higherZ)] | types[w.getBlockTypeIdAt(upperX, Y, higherZ)];

		if((result & MovingData.SOLID) != 0) {
			// return standing
			return MovingData.SOLID;
		}
		else if((result & MovingData.LIQUID) != 0) {
			// return swimming
			return MovingData.LIQUID;
		}

		// Check the four borders of the players hitbox for something he could be standing on
		result = types[w.getBlockTypeIdAt(lowerX, Y-1, lowerZ)] | types[w.getBlockTypeIdAt(upperX, Y-1, lowerZ)] |
		types[w.getBlockTypeIdAt(lowerX, Y-1, higherZ)] | types[w.getBlockTypeIdAt(upperX, Y-1, higherZ)];

		if((result & MovingData.SOLID) != 0) {
			// return standing
			return MovingData.SOLID;
		}


		// check if his head is "stuck" in an block
		result = types[w.getBlockTypeIdAt(lowerX, Y+1, lowerZ)] | types[w.getBlockTypeIdAt(upperX, Y+1, lowerZ)] |
		types[w.getBlockTypeIdAt(lowerX, Y+1, higherZ)] | types[w.getBlockTypeIdAt(upperX, Y+1, higherZ)];

		if((result & MovingData.SOLID) != 0) {
			// return standing
			return  MovingData.SOLID;
		}
		else if((result & MovingData.LIQUID) != 0) {
			// return swimming
			return MovingData.LIQUID;
		}

		// Running on fences causes problems if not treated specially
		result = types[w.getBlockTypeIdAt(lowerX, Y-2, lowerZ)] | types[w.getBlockTypeIdAt(upperX, Y-2, lowerZ)] |
		types[w.getBlockTypeIdAt(lowerX, Y-2, higherZ)] | types[w.getBlockTypeIdAt(upperX, Y-2, higherZ)];

		if((result & MovingData.FENCE) != 0) {
			// return standing
			return MovingData.SOLID;
		}

		// Water elevators - optional "feature"
		if(waterElevators) {
			result = types[w.getBlockTypeIdAt(lowerX+1, Y+1, lowerZ+1)] |
			types[w.getBlockTypeIdAt(lowerX+1, Y  , lowerZ+1)] |
			types[w.getBlockTypeIdAt(lowerX,   Y+1, lowerZ+1)] |
			types[w.getBlockTypeIdAt(lowerX  , Y  , lowerZ+1)] |
			types[w.getBlockTypeIdAt(lowerX+1, Y+1, lowerZ  )] |
			types[w.getBlockTypeIdAt(lowerX+1, Y  , lowerZ  )] ;

			if((result & MovingData.LIQUID) != 0) {
				return MovingData.SOLID; // Solid? Why that? Because that's closer to what the bug actually does than liquid
			}
		}
		// If nothing matches, he is somewhere in the air
		return MovingData.NONSOLID;
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

	@Override
	public void configure(NoCheatConfiguration config) {

		try {
			allowFlying = config.getBooleanValue("moving.allowflying");
			allowFakeSneak = config.getBooleanValue("moving.allowfakesneak");
			allowFastSwim = config.getBooleanValue("moving.allowfastswim");

			waterElevators = config.getBooleanValue("moving.waterelevators");

			logMessage = config.getStringValue("moving.logmessage").
			replace("[player]", "%1$s").
			replace("[world]", "%2$s").
			replace("[from]", "(%4$.1f, %5$.1f, %6$.1f)").
			replace("[to]", "(%7$.1f, %8$.1f, %9$.1f)").
			replace("[distance]", "(%10$.1f, %11$.1f, %12$.1f)");

			summaryMessage = config.getStringValue("moving.summarymessage").
			replace("[timeframe]", "%2$d").
			replace("[player]", "%1$s").
			replace("[violations]", "(%3$d,%4$d,%5$d)");

			actions = new Action[3][];

			actions[0] = config.getActionValue("moving.action.low");
			actions[1] = config.getActionValue("moving.action.med");
			actions[2] = config.getActionValue("moving.action.high");

			setActive(config.getBooleanValue("active.moving"));

			enforceTeleport = config.getBooleanValue("moving.enforceteleport");

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
