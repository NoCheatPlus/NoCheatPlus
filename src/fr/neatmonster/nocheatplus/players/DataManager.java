package fr.neatmonster.nocheatplus.players;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.ViolationHistory;
import fr.neatmonster.nocheatplus.checks.access.CheckDataFactory;
import fr.neatmonster.nocheatplus.checks.blockbreak.BlockBreakConfig;
import fr.neatmonster.nocheatplus.checks.blockinteract.BlockInteractConfig;
import fr.neatmonster.nocheatplus.checks.blockplace.BlockPlaceConfig;
import fr.neatmonster.nocheatplus.checks.chat.ChatConfig;
import fr.neatmonster.nocheatplus.checks.combined.CombinedConfig;
import fr.neatmonster.nocheatplus.checks.combined.CombinedData;
import fr.neatmonster.nocheatplus.checks.fight.FightConfig;
import fr.neatmonster.nocheatplus.checks.inventory.InventoryConfig;
import fr.neatmonster.nocheatplus.checks.moving.MovingConfig;
import fr.neatmonster.nocheatplus.command.INotifyReload;
import fr.neatmonster.nocheatplus.config.ConfPaths;
import fr.neatmonster.nocheatplus.config.ConfigFile;
import fr.neatmonster.nocheatplus.config.ConfigManager;
import fr.neatmonster.nocheatplus.config.INeedConfig;
import fr.neatmonster.nocheatplus.hooks.APIUtils;

public class DataManager implements Listener, INotifyReload, INeedConfig{
	
	protected static DataManager instance = null;
	
	/*
	 * 
	 */
	/**
	 * Access order for playerName (exact) -> ms time of logout.
	 * <hr>
	 * Later this might hold central player data objects instead of the long only.
	 */
	private final Map<String, Long> lastLogout = new LinkedHashMap<String, Long>(50, 0.75f, true);
	
	/**
	 * Execution histories of the checks.
	 */
	protected final Map<CheckType, Map<String, ExecutionHistory>> executionHistories = new HashMap<CheckType, Map<String,ExecutionHistory>>();
	
	protected long durExpireData = 0;
	
	protected boolean deleteData = true;
	protected boolean deleteHistory = false;
	
	public DataManager(){
		instance = this;
	}
	
	public void checkExpiration(){
		if (durExpireData <= 0) return;
		final long now = System.currentTimeMillis();
		final Set<CheckDataFactory> factories = new LinkedHashSet<CheckDataFactory>();
		final Set<Entry<String, Long>> entries = lastLogout.entrySet();
		final Iterator<Entry<String, Long>> iterator = entries.iterator();
		while (iterator.hasNext()){
			final Entry<String, Long> entry = iterator.next();
			final long ts = entry.getValue();
			if (now - ts <= durExpireData) break;
			final String playerName = entry.getKey();
			if (deleteData){
				factories.clear();
				for (final CheckType type : CheckType.values()){
					final CheckDataFactory factory = type.getDataFactory();
					if (factory != null) factories.add(factory);
				}
				for (final CheckDataFactory factory : factories){
					factory.removeData(playerName);
				}
			}
			if (deleteData || deleteHistory) removeExecutionHistory(CheckType.ALL, playerName);
			if (deleteHistory) ViolationHistory.removeHistory(playerName);
			iterator.remove();
		}
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerJoin(final PlayerJoinEvent event){
	    final Player player = event.getPlayer();
		lastLogout.remove(player.getName());
		CombinedData.getData(player).lastJoinTime = System.currentTimeMillis();
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerQuit(final PlayerQuitEvent event){
	    onLeave(event.getPlayer());
	}
	
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerKick(final PlayerKickEvent event){
        onLeave(event.getPlayer());
    }
	
	private final void onLeave(final Player player) {
	    final long now = System.currentTimeMillis();
        lastLogout.put(player.getName(), now);
        CombinedData.getData(player).lastLogoutTime = now;
    }

	@Override
	public void onReload() {
		// future
		adjustSettings();
	}

	private void adjustSettings() {
		final ConfigFile config = ConfigManager.getConfigFile();
		durExpireData = config.getLong(ConfPaths.DATA_EXPIRATION_DURATION) * 60000L; // in minutes
		deleteData = config.getBoolean(ConfPaths.DATA_EXPIRATION_DATA, true); // hidden.
		deleteHistory = config.getBoolean(ConfPaths.DATA_EXPIRATION_HISTORY);
	}

	public static void registerExecutionHistory(CheckType type, Map<String, ExecutionHistory> histories) {
		instance.executionHistories.put(type, histories);
	}
	
	/**
	 * Access method to the the execution history for check type for a player.
	 * @param type
	 * @param playerName Exact case for player name.
	 * @return null if not present.
	 */
	public static ExecutionHistory getExecutionHistory(final CheckType type, final String playerName){
	    final Map<String, ExecutionHistory> map = instance.executionHistories.get(type);
        if (map != null) return map.get(playerName);
        return null;
	}
	
	public static boolean removeExecutionHistory(final CheckType type, final String playerName){
		boolean removed = false;
		// TODO: design ...
		for (final CheckType refType : APIUtils.getWithChildren(type)){
			final Map<String, ExecutionHistory> map = instance.executionHistories.get(refType);
			if (map != null && map.remove(playerName) != null) removed = true;
		}
		return removed;
	}

	/**
	 * Remove data and history of all players for the given check type and sub checks.
	 * @param checkType
	 */
	public static void clear(final CheckType checkType) {
		final Set<CheckDataFactory> factories = new HashSet<CheckDataFactory>();
		for (final CheckType type : APIUtils.getWithChildren(checkType)){
			final Map<String, ExecutionHistory> map = instance.executionHistories.get(type);
			if (map != null) map.clear();
			final CheckDataFactory factory = type.getDataFactory();
			if (factory != null) factories.add(factory);
		}
		for (final CheckDataFactory factory : factories){
			factory.removeAllData();
		}
		ViolationHistory.clear(checkType);
	}
	
	/**
	 * Clear all stored (check) config instances.<br>
	 * This does not cleanup ConfigManager, i.e. stored yml-versions.
	 */
	public static void clearConfigs() {
		// The dirty bit !
		BlockBreakConfig.clear();
		BlockInteractConfig.clear();
		BlockPlaceConfig.clear();
		ChatConfig.clear();
		CombinedConfig.clear();
		FightConfig.clear();
		InventoryConfig.clear();
		MovingConfig.clear();
	}
}
