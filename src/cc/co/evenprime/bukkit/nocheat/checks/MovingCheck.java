
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
		
		helper = new MovingEventHelper();
		flyingCheck = new FlyingCheck();
		runningCheck = new RunningCheck();
	}

	private int ticksBeforeSummary;

	public long statisticElapsedTimeNano = 0;

	public boolean allowFlying;
	public boolean allowFakeSneak;
	public boolean allowFastSwim;

	public double stepWidth;
	public double sneakWidth;
	public double swimWidth;
	
	private boolean waterElevators;

	private String logMessage;
	private String summaryMessage;

	// How should moving violations be treated?
	private Action actions[][];

	public long statisticTotalEvents = 1; // Prevent accidental division by 0 at some point

	private boolean enforceTeleport;

	private final MovingEventHelper helper;
	private final FlyingCheck flyingCheck;
	private final RunningCheck runningCheck;


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

	public Location check(Player player, Location from, Location to,
			MovingData data) {
				
		updateVelocity(player.getVelocity(), data);
		
		Location newToLocation = null;
		
		final long startTime = System.nanoTime();
		
		/************* DECIDE WHICH CHECKS NEED TO BE RUN *************/
		final boolean flyCheck = !allowFlying && !plugin.hasPermission(player, PermissionData.PERMISSION_FLYING, checkOPs);		
		final boolean runCheck = true;

		/***************** REFINE EVENT DATA FOR CHECKS ***************/

		if(flyCheck || runCheck) {
						
			// In both cases it will be interesting to know the type of underground the player 
			// is in or goes to
			final int fromType = helper.isLocationOnGround(from.getWorld(), from.getX(), from.getY(), from.getZ(), false);
			final int toType = helper.isLocationOnGround(to.getWorld(), to.getX(), to.getY(),to.getZ(), false);

			final boolean fromOnGround = fromType != MovingEventHelper.NONSOLID;
			final boolean toOnGround = toType != MovingEventHelper.NONSOLID;

			// Distribute data to checks in the form needed by the checks

			/********************* EXECUTE THE CHECKS ********************/
			double result = 0.0D;

			if(flyCheck) {
				result += Math.max(0D, flyingCheck.check(player, from, fromOnGround, to, toOnGround, data));
			}
			else
			{
			    // If players are allowed to fly, there's no need to remember the last location on ground
			    data.setBackPoint = from;
			}

			if(runCheck) {
				result += Math.max(0D, runningCheck.check(from, to, 
						!allowFakeSneak && player.isSneaking(), !allowFastSwim && (fromType & toType & MovingEventHelper.LIQUID) > 0, data, this));
			}
			
			/********* HANDLE/COMBINE THE RESULTS OF THE CHECKS ***********/

			data.jumpPhase++;
			
			if(fromOnGround) {
                data.setBackPoint = from;
                data.jumpPhase = 0;
            }
            else if(result <= 0 && toOnGround) {
                data.jumpPhase = 0;
            }
			
			if(result > 0) {
				// Increment violation counter
				data.violationLevel += result;
				if(data.setBackPoint == null) data.setBackPoint = from;
			}

			if(result > 0 && data.violationLevel > 1) {
	
				setupSummaryTask(player, data);

				int level = limitCheck(data.violationLevel-1);
				
				data.violationsInARow[level]++;

				newToLocation = action(player, from, to, actions[level], data.violationsInARow[level], data);
			}
		}
		
		// Slowly reduce the level with each event
		data.violationLevel *= 0.97;
		data.horizFreedom *= 0.97;
		
		statisticElapsedTimeNano += System.nanoTime() - startTime;
		statisticTotalEvents++;
		
		
		return newToLocation;
	}

	/**
	 * Various corner cases that would cause this check to fail or require special treatment
	 * @param player
	 * @param data
	 * @param from
	 * @param to
	 * @return
	 */
	public boolean shouldBeApplied(final Player player, final MovingData data, final Location from, final Location to) {

		if(player.isDead() || player.isInsideVehicle()) return false;

		if(data.wasTeleported) {
			// Remember this location
			data.teleportedTo = from.clone();
			data.wasTeleported = false;
			data.jumpPhase = 0;
		}

		if(data.teleportedTo != null && data.teleportedTo.getWorld().equals(from.getWorld())) {
			// As long as the from-Location doesn't change, the player didn't accept the teleport
			if(data.teleportedTo.distanceSquared(from) < 0.01D) {
				// Event after Teleport ignored
				return false;
			}
			else {
				// The player finally accepted the teleport with the previous event
				data.teleportedTo = null;
			}
		}

		// If the target is a bed, don't check (going to bed is a kind of mini teleport...)
		if(to.getWorld().getBlockTypeIdAt(to) == Material.BED_BLOCK.getId()) {
			return false;
		}

		return true;
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
				    
				    try {
					if(data.highestLogLevel != null) {
						String logString =  String.format(summaryMessage, p.getName(), ticksBeforeSummary/20, data.violationsInARow[0], data.violationsInARow[1],data.violationsInARow[2]);
						plugin.log(data.highestLogLevel, logString);
						
						data.highestLogLevel = Level.ALL;
					}
					// deleting its own reference
					data.summaryTask = -1;

					data.violationsInARow[0] = 0;
					data.violationsInARow[1] = 0;
					data.violationsInARow[2] = 0;
				    }
				    catch(Exception e) { }
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

		MovingData data = plugin.getDataManager().getMovingData(event.getPlayer());

		// We can enforce a teleport, if that flag is explicitly set (but I'd rather have other plugins
		// not arbitrarily cancel teleport events in the first place...
		if(data.teleportInitializedByMe != null && event.isCancelled() &&  enforceTeleport && event.getTo().equals(data.teleportInitializedByMe)) {
			event.setCancelled(false);
			data.teleportInitializedByMe = null;
		}

		if(!event.isCancelled()) {
			data.wasTeleported = true;
			data.setBackPoint = event.getTo().clone();
			//data.lastLocation = event.getTo().clone();
		}

		// reset anyway - if another plugin cancelled our teleport it's no use to try and be precise
		data.jumpPhase = 0;
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
	 * @return 
	 */
	private Location action( Player player, Location from, Location to, Action[] actions, int violations, MovingData data) {


		Location newToLocation = null;
		
		if(actions == null) return newToLocation;
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
						int i = 0;
						for(; i < 20; i++) {
							if(playerIsOnGround(data.setBackPoint, -0.5*i) != MovingData.NONSOLID) {
								break;
							}
						}	
						y -= 0.5*i;

						data.setBackPoint.setY(y);

						// Remember the location we send the player to, to identify teleports that were started by us
						data.teleportInitializedByMe = new Location(data.setBackPoint.getWorld(), data.setBackPoint.getX(), y, data.setBackPoint.getZ(), to.getYaw(), to.getPitch());

						newToLocation = data.teleportInitializedByMe;

						cancelled = true; // just prevent us from treating more than one "cancel" action, which would make no sense
					}
					else if(a instanceof CustomAction)
						plugin.handleCustomAction((CustomAction)a, player);
				}
			}
		}
		
		return newToLocation;
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

			checkOPs = config.getBooleanValue("moving.checkops");
			
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
			
			ticksBeforeSummary = config.getIntegerValue("moving.summaryafter")*20;

			actions = new Action[3][];

			actions[0] = config.getActionValue("moving.action.low");
			actions[1] = config.getActionValue("moving.action.med");
			actions[2] = config.getActionValue("moving.action.high");

			setActive(config.getBooleanValue("active.moving"));

			enforceTeleport = config.getBooleanValue("moving.enforceteleport");
			
			stepWidth =  ((double)config.getIntegerValue("moving.limits.walking")) /100D;
			sneakWidth = ((double)config.getIntegerValue("moving.limits.sneaking"))/100D;
			swimWidth =  ((double)config.getIntegerValue("moving.limits.swimming"))/100D;

		} catch (ConfigurationException e) {
			setActive(false);
			e.printStackTrace();
		}

	}

	@Override
	protected void registerListeners() {
		PluginManager pm = Bukkit.getServer().getPluginManager();

		Listener movingPlayerMonitor = new MovingPlayerMonitor(plugin.getDataManager(), this);

		// Register listeners for moving check
		pm.registerEvent(Event.Type.PLAYER_MOVE, new MovingPlayerListener(plugin.getDataManager(), this), Priority.Lowest, plugin);
		pm.registerEvent(Event.Type.PLAYER_INTERACT, movingPlayerMonitor, Priority.Monitor, plugin);
		pm.registerEvent(Event.Type.PLAYER_MOVE, movingPlayerMonitor, Priority.Monitor, plugin);
		pm.registerEvent(Event.Type.ENTITY_DAMAGE, new MovingEntityListener(plugin.getDataManager(), this), Priority.Monitor, plugin);
		pm.registerEvent(Event.Type.PLAYER_TELEPORT, movingPlayerMonitor, Priority.Monitor, plugin);
		pm.registerEvent(Event.Type.PLAYER_PORTAL, movingPlayerMonitor, Priority.Monitor, plugin);
		pm.registerEvent(Event.Type.PLAYER_RESPAWN, movingPlayerMonitor, Priority.Monitor, plugin);
	}
}
