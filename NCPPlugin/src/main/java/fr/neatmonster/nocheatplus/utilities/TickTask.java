package fr.neatmonster.nocheatplus.utilities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.NoCheatPlus;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.ViolationData;
import fr.neatmonster.nocheatplus.checks.access.ICheckData;
import fr.neatmonster.nocheatplus.components.TickListener;
import fr.neatmonster.nocheatplus.logging.LogUtil;
import fr.neatmonster.nocheatplus.metrics.MetricsData;
import fr.neatmonster.nocheatplus.players.DataManager;

/**
 * Task to run every tick, to update permissions and execute actions, and for lag measurement.
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
	
	public static final int lagMaxTicks = 80;
	
	/** Permissions to update: player name -> check type. */
	private static final Set<PermissionUpdateEntry> permissionUpdates = new LinkedHashSet<PermissionUpdateEntry>(50);
	
	/** Actions to execute. */
	private static final List<ViolationData> delayedActions = new LinkedList<ViolationData>();
	
	/** Tick listeners to call every tick. */
	private static final List<TickListener> tickListeners = new ArrayList<TickListener>();
	
	/** Last n tick durations, measured from run to run.*/
	private static final long[] tickDurations = new long[lagMaxTicks];
	
	/** Tick durations summed up in packs of n (nxn time covered) */
	private static final long[] tickDurationsSq = new long[lagMaxTicks];
	
	/** Lag spike durations (min) to keep track of. */
	private static long[] spikeDurations = new long[]{150, 450, 1000, 5000};
	
	/** Lag spikes > 150 ms counting (3 x 20 minutes). For lag spike length see spikeDurations. */
	private static ActionFrequency[] spikes = new ActionFrequency[spikeDurations.length];
	
	/** Task id of the running TickTask */
	protected static int taskId = -1;
	
	protected static int tick = 0;
	
	protected static long timeStart = 0;
	
	protected static long timeLast = 0;
	
	/** Lock flag set on disable. */
	protected static boolean locked = true;
	
	static{
		for (int i = 0; i < spikeDurations.length; i++){
			spikes[i] = new ActionFrequency(3, 1000L * 60L * 20L);
		}
	}
	
	
	//////////////////////////////////////////////////////////////
	// Special static methods, usually not called from outside.
	//////////////////////////////////////////////////////////////
	
	/**
	 * Force executing actions.<br>
	 * Note: Only call from the main thread!
	 */
	public static void executeActions() {
		final List<ViolationData> copyActions = new LinkedList<ViolationData>();
		synchronized (delayedActions) {
			if (delayedActions.isEmpty()) return;
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
			if (permissionUpdates.isEmpty()) return;
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
		synchronized(permissionUpdates){
			if (locked) return;
			permissionUpdates.add(new PermissionUpdateEntry(playerName, checkType));
		}
	}
	
	/**
	 * Request actions execution.<br>
	 * NOTE: Thread safe.
	 * @param actions
	 */
	public static void requestActionsExecution(final ViolationData actions) {
		synchronized (delayedActions) {
			if (locked) return;
			delayedActions.add(actions);
		}
	}
	
	/**
	 * Add a tick listener. Should be thread safe, though... why?
	 * @param listener
	 */
	public static void addTickListener(TickListener listener){
		synchronized (tickListeners) {
			if (locked) return;
			tickListeners.add(listener);
		}
	}
	
	/**
	 * Remove a tick listener. Should be thread safe, though... why?
	 * @param listener
	 * @return If previously contained.
	 */
	public static boolean removeTickListener(TickListener listener){
		synchronized (tickListeners) {
			return tickListeners.remove(listener);
		}
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
	
	/**
	 * Time when last time processing was finished.
	 * @return
	 */
	public static final long getTimeLast(){
	    return timeLast;
	}
	
	/**
	 * Get lag percentage for the last ms milliseconds.<br>
	 * NOTE: Will not be synchronized, still can be called from other threads.
	 * @param ms Past milliseconds to cover. A longer period of time may be used, up to two times if ms > lagMaxTicks * 50.
	 * @return Lag factor (1.0 = 20 tps, 2.0 = 10 tps), excluding the current tick.
	 */
	public static final float getLag(final long ms){
		return getLag(ms, false);
	}
	
	/**
	 * Get lag percentage for the last ms milliseconds.<br>
	 * NOTE: Using "exact = true" is meant for checks in the main thread. If called from another thread, exact should be set to false.
	 * @param ms Past milliseconds to cover. A longer period of time may be used, up to two times if ms > lagMaxTicks * 50.
	 * @param exact If to include the currently running tick, if possible. Should only be set to true, if called from the main thread (or while the main thread is blocked).
	 * @return Lag factor (1.0 = 20 tps, 2.0 = 10 tps).
	 */
	public static final float getLag(final long ms, final boolean exact){
		if (ms < 0){
			// Account for freezing (i.e. check timeLast, might be an extra method)!
			return getLag(0, exact);
		}
		final int tick = TickTask.tick;
		if (tick == 0) return 1f;
		final int add = (ms % 50) == 0 ? 0 : 1;
		final int totalTicks = Math.min(tick, add + (int) (ms / 50));
		final int maxTick = Math.min(lagMaxTicks, totalTicks);
		long sum = tickDurations[maxTick - 1];
		long covered = maxTick * 50;
		
		// Only count fully covered:
		if (totalTicks > lagMaxTicks){
			int maxTickSq = Math.min(lagMaxTicks, totalTicks / lagMaxTicks);
			if (lagMaxTicks * maxTickSq == totalTicks) maxTickSq -= 1;
			sum += tickDurationsSq[maxTickSq - 1];
			covered += lagMaxTicks * 50 * maxTickSq; 
		}
		
		if (exact){
			// Attempt to count in the current tick.
			final long passed = System.currentTimeMillis() - timeLast;
			if (passed > 50){
				// Only count in in the case of "overtime".
				covered += 50;
				sum += passed;
			}
		}
		
		return (float) sum / (float) covered;
	}
	
	/**
	 * Get moderate lag spikes of the last hour (>150 ms, lowest tracked spike duration).
	 * @deprecated What is moderate :) ?
	 * @return
	 */
	public static final int getModerateLagSpikes(){
		spikes[0].update(System.currentTimeMillis());
		return (int) spikes[0].score(1f);
	}
	
	/**
	 * Get heavy lag spikes of the last hour (> 450 ms supposedly, first duration bigger than 150 ms).
	 * @deprecated What is heavy :) ?
	 * @return
	 */
	public static final int getHeavyLagSpikes(){
		spikes[1].update(System.currentTimeMillis());
		return (int) spikes[1].score(1f);
	}
	
	/**
	 * Get total number of lag spikes counted at all. This is the number of lag spikes with a duration above spikeDuations[0] which should be 150 ms. This is the score of spikes[0].
	 * @return
	 */
	public static final int getNumberOfLagSpikes(){
		spikes[0].update(System.currentTimeMillis());
		return (int) spikes[0].score(1f);
	}
	
	/**
	 * Get the stepping for lag spike duration tracking.
	 * @return
	 */
	public static final long[] getLagSpikeDurations(){
		return Arrays.copyOf(spikeDurations, spikeDurations.length);
	}
	
	/**
	 * Get lag spike count according to getLagSpikeDurations() values. Entries of lower indexes contain the entries of higher indexes (so subtraction would be necessary to get spikes from...to).
	 * @return
	 */
	public static final int[] getLagSpikes(){
		final int[] out = new int[spikeDurations.length];
		final long now = System.currentTimeMillis();
		for (int i = 0; i < spikeDurations.length; i++){
			spikes[i].update(now);
			out[i] = (int) spikes[i].score(1f);
		}
		return out;
	}
	
	/**
	 * Check if new permission update requests and actions can be added.
	 * @return True if locked.
	 */
	public boolean isLocked(){
		return locked;
	}
	
	////////////////////////////////////////
	// Public methods for internal use.
	////////////////////////////////////////
	
	public static int start(final NoCheatPlus plugin){
		cancel();
		taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new TickTask(), 1, 1);
		if (taskId != -1) timeStart = System.currentTimeMillis();
		else timeStart = 0;
		return taskId;
	}
	
	public static void cancel(){
		if (taskId == -1) return;
		Bukkit.getScheduler().cancelTask(taskId);
		taskId = -1;
	}
	
	/**
	 * Control if new elements can be added to request queues.<br>
	 * NOTE: This is just a flag, no sync is done here.
	 * @param locked
	 */
	public static void setLocked(boolean locked){
		TickTask.locked = locked;
	}
	
	/**
	 * Empty queues (better call after setLocked(true)) and tickListeners.
	 */
	public static void purge(){
		synchronized (permissionUpdates) {
			permissionUpdates.clear();
		}
		synchronized (delayedActions) {
			delayedActions.clear();
		}
		synchronized (tickListeners) {
			tickListeners.clear();
		}
	}
	
	/** 
	 * Reset tick and tick stats to 0 (!).
	 */
	public static void reset(){
		tick = 0;
		timeLast = 0;
		for (int i = 0; i < lagMaxTicks; i++){
			tickDurations[i] = 0;
			tickDurationsSq[i] = 0;
		}
		for (int i = 0; i < spikeDurations.length; i++){
			spikes[i].clear(0);
		}
	}
	
	//////////////////////////
	// Instance methods (meant private).
	//////////////////////////
	
	/**
	 * 
	 * Notify all listeners.
	 * 
	 */
	private final void notifyListeners() {
		final List<TickListener> copyListeners = new ArrayList<TickListener>();
		synchronized (tickListeners) {
			// Synchronized to allow concurrent adding (!? why ?!).
			// (Ignores the locked state while still running.)
			copyListeners.addAll(tickListeners);
		}
		for (final TickListener listener : copyListeners){
			try{
				listener.onTick(tick, timeLast);
			}
			catch(Throwable t){
				LogUtil.logSevere("[NoCheatPlus] (TickTask) TickListener generated an exception:");
				LogUtil.logSevere(t);
			}
		}
	}
	
	@Override
	public void run() {		
		// Actions.
		executeActions();
		// Permissions.
		updatePermissions();
		// Listeners.
		notifyListeners();
		// Metrics
		if (tick > 0 && (tick % 20) == 0){
			// Count every second.
			final int ticks = Math.min(20, (int) (1000f / (50f * getLag(1000, true)))); 
			MetricsData.addTicks(ticks);
		}
		
		// Measure time after heavy stuff.
		final long time = System.currentTimeMillis();
		final long lastDur;
		
		// Time running backwards check (not only players can!).
		if (timeLast > time) {
			LogUtil.logSevere("[NoCheatPlus] System time ran backwards (" + timeLast + "->" + time + "), clear all data and history...");
			DataManager.clearData(CheckType.ALL);
			lastDur = 50;
			for (int i = 0; i < spikeDurations.length; i++){
				spikes[i].clear(0);
			}
		}
		else if (tick > 0){
			lastDur = time - timeLast;
		}
		else{
			lastDur = 50;
		}
		
		// Update sums of sums of tick durations.
		if (tick > 0 && (tick % lagMaxTicks) == 0){
			final long sum = tickDurations[lagMaxTicks - 1];
			for (int i = 1; i < lagMaxTicks; i++){
				tickDurationsSq[i] = tickDurationsSq[i - 1] + sum;
			}
			tickDurationsSq[0] = sum;
		}
		
		// Update tick duration sums.
		for (int i = 1; i < lagMaxTicks; i++){
			tickDurations[i] = tickDurations[i - 1] + lastDur;
		}
		tickDurations[0] = lastDur;
		
		// Lag spikes counting. [Subject to adjustments!]
		if (lastDur > spikeDurations[0] && tick > 0){
			spikes[0].add(time, 1f);
			for (int i = 1; i < spikeDurations.length; i++){
				if (lastDur > spikeDurations[i]){
					spikes[i].add(time, 1f);
				}
				else break;
			}
		}
			
		// Finish.
		tick ++;
		timeLast = time;
	}

}
