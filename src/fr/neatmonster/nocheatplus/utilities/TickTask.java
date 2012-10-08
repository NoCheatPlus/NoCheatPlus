package fr.neatmonster.nocheatplus.utilities;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.NoCheatPlus;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.ViolationData;
import fr.neatmonster.nocheatplus.checks.access.ICheckData;
import fr.neatmonster.nocheatplus.players.DataManager;

/**
 * Task to run every tick, to update permissions and execute actions, and maybe later for extended lag measurement.
 * 
 * <hr>
 * The permissions updates and actions execution is meant for use by the asynchronously run checks, not for normal use.
 * @author mc_dev
 *
 */
public class TickTask implements Runnable {
	
	protected static final class PermissionUpdateEntry{
		public CheckType checkType;
		public String playerName;
		private final int hashCode;
		public PermissionUpdateEntry(final String playerName, final CheckType checkType){
			this.playerName = playerName;
			this.checkType = checkType;
			hashCode = playerName.hashCode() ^ checkType.hashCode();
		}
		@Override
		public boolean equals(final Object obj) {
			if (!(obj instanceof PermissionUpdateEntry)) return false;
			final PermissionUpdateEntry other = (PermissionUpdateEntry) obj;
			return playerName.equals(other.playerName) && checkType.equals(other.checkType);
		}
		@Override
		public int hashCode() {
			return hashCode;
		}
	}
	
	/** Permissions to update: player name -> check type. */
	private static final Set<PermissionUpdateEntry> permissionUpdates = Collections.synchronizedSet(new HashSet<PermissionUpdateEntry>(50));
	
	/** Actions to execute. */
	public static final List<ViolationData> delayedActions = Collections.synchronizedList(new LinkedList<ViolationData>());
	
	/** Task id of the running TickTask */
	protected static int taskId = -1;
	
	protected static int tick = 0;
	
	protected static long timeStart = 0;
	
	protected static long timeLast = 0;
	
	
	//////////////////////////////////////////////////////////////
	// Special static methods, usually not called from outside.
	//////////////////////////////////////////////////////////////
	
	/**
	 * Force executing actions.<br>
	 * Note: Only call from the main thread!
	 */
	public void executeActions() {
		final List<ViolationData> copyActions = new LinkedList<ViolationData>();
		synchronized (delayedActions) {
			copyActions.addAll(delayedActions);
			delayedActions.clear();
		}
		for (final ViolationData violationData : copyActions){
			violationData.executeActions();
		}
	}
	
	/**
	 * Force a permissions update.<br>
	 * Note: Only call from the main thread!
	 */
	public static void updatePermissions() {
		final List<PermissionUpdateEntry> copyPermissions = new LinkedList<PermissionUpdateEntry>();
		synchronized (permissionUpdates) {
			copyPermissions.addAll(permissionUpdates);
			permissionUpdates.clear();
		}
		for (final PermissionUpdateEntry entry : copyPermissions){
			final Player player = Bukkit.getPlayerExact(entry.playerName);
			if (player == null || !player.isOnline()) continue;
			final String[] perms = entry.checkType.getConfigFactory().getConfig(player).getCachePermissions();
			if (perms == null) continue;
			final ICheckData data = entry.checkType.getDataFactory().getData(player);
			for (final String permission : perms){
				data.setCachedPermission(permission, player.hasPermission(permission));
			}	
			
		}
	}
	
	///////////////////////////////////
	// Public static access methods
	///////////////////////////////////
	
	/**
	 * Access method to request permission updates.<br>
	 * NOTE: Thread safe.
	 * @param playerName
	 * @param checkType
	 */
	public static void requestPermissionUpdate(final String playerName, final CheckType checkType){
		permissionUpdates.add(new PermissionUpdateEntry(playerName, checkType));
	}
	
	/**
	 * Request actions execution.<br>
	 * NOTE: Thread safe.
	 * @param actions
	 */
	public static void requestActionsExecution(final ViolationData actions) {
		delayedActions.add(actions);
	}
	
	/**
	 * Get the tasks tick count. It is increased with every server tick.<br>
	 * NOTE: Can be called from other threads.
	 * @return The current tick count.
	 */
	public static final int getTick(){
		return tick;
	}
	
	/**
	 * Get the time at which the task was started.
	 * @return
	 */
	public static final long getTimeStart(){
	    return timeStart;
	}
	
	public static final long getTimeLast(){
	    return timeLast;
	}
	
	////////////////////////////////////////
	// Public methods for internal use.
	////////////////////////////////////////
	
	public static int start(final NoCheatPlus plugin){
		cancel();
		taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new TickTask(), 1, 1);
		if (taskId != -1) timeStart = System.currentTimeMillis();
		return taskId;
	}
	
	public static void cancel(){
		if (taskId == -1) return;
		Bukkit.getScheduler().cancelTask(taskId);
		taskId = -1;
	}
	
	//////////////////////////
	// Instance methods
	//////////////////////////
	
	@Override
	public void run() {
		tick ++;
		final long time = System.currentTimeMillis();
		// The isEmpty checks are faster than synchronizing fully always, the actions get delayed one tick at most.
		if (!delayedActions.isEmpty()) executeActions();
		if (!permissionUpdates.isEmpty()) updatePermissions();
		if (timeLast > time){
		    CheckUtils.logSevere("[NoCheatPlus] System time ran backwards (" + timeLast + "->" + time + "), clear all data and history...");
		    DataManager.clear(CheckType.ALL);
		}
		timeLast = time;
	}

}
