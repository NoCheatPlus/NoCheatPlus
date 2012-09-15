package fr.neatmonster.nocheatplus.checks.inventory;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.actions.ActionList;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.access.ACheckConfig;
import fr.neatmonster.nocheatplus.checks.access.CheckConfigFactory;
import fr.neatmonster.nocheatplus.checks.access.ICheckConfig;
import fr.neatmonster.nocheatplus.config.ConfPaths;
import fr.neatmonster.nocheatplus.config.ConfigFile;
import fr.neatmonster.nocheatplus.config.ConfigManager;
import fr.neatmonster.nocheatplus.players.Permissions;

/*
 * M""M                                       dP                              
 * M  M                                       88                              
 * M  M 88d888b. dP   .dP .d8888b. 88d888b. d8888P .d8888b. 88d888b. dP    dP 
 * M  M 88'  `88 88   d8' 88ooood8 88'  `88   88   88'  `88 88'  `88 88    88 
 * M  M 88    88 88 .88'  88.  ... 88    88   88   88.  .88 88       88.  .88 
 * M  M dP    dP 8888P'   `88888P' dP    dP   dP   `88888P' dP       `8888P88 
 * MMMM                                                                   .88 
 *                                                                    d8888P  
 *                                                                    
 * MM'""""'YMM                   .8888b oo          
 * M' .mmm. `M                   88   "             
 * M  MMMMMooM .d8888b. 88d888b. 88aaa  dP .d8888b. 
 * M  MMMMMMMM 88'  `88 88'  `88 88     88 88'  `88 
 * M. `MMM' .M 88.  .88 88    88 88     88 88.  .88 
 * MM.     .dM `88888P' dP    dP dP     dP `8888P88 
 * MMMMMMMMMMM                                  .88 
 *                                          d8888P  
 */
/**
 * Configurations specific for the "inventory" checks. Every world gets one of
 * these assigned to it, or if a world doesn't get it's own, it will use the
 * "global" version.
 */
public class InventoryConfig extends ACheckConfig {

	/** The factory creating configurations. */
	public static final CheckConfigFactory factory = new CheckConfigFactory() {
		@Override
		public final ICheckConfig getConfig(final Player player) {
			return InventoryConfig.getConfig(player);
		}
	};

	/** The map containing the configurations per world. */
	private static final Map<String, InventoryConfig> worldsMap = new HashMap<String, InventoryConfig>();

	/**
	 * Clear all the configurations.
	 */
	public static void clear() {
		worldsMap.clear();
	}

	/**
	 * Gets the configuration for a specified player.
	 * 
	 * @param player
	 *            the player
	 * @return the configuration
	 */
	public static InventoryConfig getConfig(final Player player) {
		if (!worldsMap.containsKey(player.getWorld().getName()))
			worldsMap.put(player.getWorld().getName(), new InventoryConfig(
					ConfigManager.getConfigFile(player.getWorld().getName())));
		return worldsMap.get(player.getWorld().getName());
	}

	public final boolean dropCheck;
	public final int dropLimit;
	public final long dropTimeFrame;
	public final ActionList dropActions;

	public final boolean fastClickCheck;
	public final ActionList fastClickActions;

	public final boolean instantBowCheck;
	public final ActionList instantBowActions;

	public final boolean instantEatCheck;
	public final ActionList instantEatActions;

	/**
	 * Instantiates a new inventory configuration.
	 * 
	 * @param data
	 *            the data
	 */
	public InventoryConfig(final ConfigFile data) {
		dropCheck = data.getBoolean(ConfPaths.INVENTORY_DROP_CHECK);
		dropLimit = data.getInt(ConfPaths.INVENTORY_DROP_LIMIT);
		dropTimeFrame = data.getLong(ConfPaths.INVENTORY_DROP_TIMEFRAME);
		dropActions = data.getActionList(ConfPaths.INVENTORY_DROP_ACTIONS,
				Permissions.INVENTORY_DROP);

		fastClickCheck = data.getBoolean(ConfPaths.INVENTORY_FASTCLICK_CHECK);
		fastClickActions = data.getActionList(
				ConfPaths.INVENTORY_FASTCLICK_ACTIONS,
				Permissions.INVENTORY_FASTCLICK);

		instantBowCheck = data.getBoolean(ConfPaths.INVENTORY_INSTANTBOW_CHECK);
		instantBowActions = data.getActionList(
				ConfPaths.INVENTORY_INSTANTBOW_ACTIONS,
				Permissions.INVENTORY_INSTANTBOW);

		instantEatCheck = data.getBoolean(ConfPaths.INVENTORY_INSTANTEAT_CHECK);
		instantEatActions = data.getActionList(
				ConfPaths.INVENTORY_INSTANTEAT_ACTIONS,
				Permissions.INVENTORY_INSTANTEAT);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.neatmonster.nocheatplus.checks.ICheckConfig#isEnabled(fr.neatmonster
	 * .nocheatplus.checks.CheckType)
	 */
	@Override
	public final boolean isEnabled(final CheckType checkType) {
		switch (checkType) {
		case INVENTORY_DROP:
			return dropCheck;
		case INVENTORY_FASTCLICK:
			return fastClickCheck;
		case INVENTORY_INSTANTBOW:
			return instantBowCheck;
		case INVENTORY_INSTANTEAT:
			return instantEatCheck;
		default:
			return true;
		}
	}

}
