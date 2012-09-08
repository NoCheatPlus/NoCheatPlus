package fr.neatmonster.nocheatplus.utilities;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.NoCheatPlus;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.ICheckData;

/**
 * Task to run every tick, to update permissions, and maybe later for extended lag measurement.
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
	
	public static void requestPermissionUpdate(final String playerName, final CheckType checkType){
		permissionUpdates.add(new PermissionUpdateEntry(playerName, checkType));
	}
	
	protected static int taskId = -1;
	
	public static void cancel(){
		if (taskId == -1) return;
		Bukkit.getScheduler().cancelTask(taskId);
		taskId = -1;
	}
	
	public static int start(final NoCheatPlus plugin){
		cancel();
		taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new TickTask(), 1, 1);
		return taskId;
	}
	
	@Override
	public void run() {
		if (!permissionUpdates.isEmpty()) updatePermissions();
	}

	/**
	 * Only call from the main thread!
	 */
	public static void updatePermissions() {
		synchronized (permissionUpdates) {
			for (final PermissionUpdateEntry entry : permissionUpdates){
				final Player player = Bukkit.getPlayerExact(entry.playerName);
				if (player == null || !player.isOnline()) continue;
				final String[] perms = entry.checkType.getConfigFactory().getConfig(player).getCachePermissions();
				if (perms == null) continue;
				final ICheckData data = entry.checkType.getDataFactory().getData(player);
				for (final String permission : perms){
					data.setCachedPermission(permission, player.hasPermission(permission));
				}	
				
			}
			permissionUpdates.clear();
		}
	}
	


}
