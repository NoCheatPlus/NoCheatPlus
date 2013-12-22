package fr.neatmonster.nocheatplus.checks.inventory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.actions.ActionList;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.access.ACheckConfig;
import fr.neatmonster.nocheatplus.checks.access.CheckConfigFactory;
import fr.neatmonster.nocheatplus.checks.access.ICheckConfig;
import fr.neatmonster.nocheatplus.config.ConfPaths;
import fr.neatmonster.nocheatplus.config.ConfigFile;
import fr.neatmonster.nocheatplus.config.ConfigManager;
import fr.neatmonster.nocheatplus.permissions.Permissions;

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

	public final boolean    dropCheck;
	public final int        dropLimit;
	public final long       dropTimeFrame;
	public final ActionList dropActions;

	public final boolean    fastClickCheck;
    public final boolean    fastClickSpareCreative;
	public final boolean 	fastClickTweaks1_5;
	public final float		fastClickShortTermLimit;
	public final float		fastClickNormalLimit;
	public final ActionList fastClickActions;
	
	public final boolean    fastConsumeCheck;
	public final long		fastConsumeDuration;
	public final boolean    fastConsumeWhitelist;
	public final Set<Integer> fastConsumeItems = new HashSet<Integer>();
	public final ActionList fastConsumeActions;

	public final boolean    instantBowCheck;
	public final boolean 	instantBowStrict;
    public final long       instantBowDelay;
	public final ActionList instantBowActions;

	public final boolean    instantEatCheck;
	public final ActionList instantEatActions;

    public final boolean    itemsCheck;
    
    public final boolean    openCheck;
    public final boolean	openClose;
    public final boolean	openCancelOther;
    
	/**
	 * Instantiates a new inventory configuration.
	 * 
	 * @param data
	 *            the data
	 */
	public InventoryConfig(final ConfigFile data) {
	    super(data, ConfPaths.INVENTORY);
		dropCheck = data.getBoolean(ConfPaths.INVENTORY_DROP_CHECK);
		dropLimit = data.getInt(ConfPaths.INVENTORY_DROP_LIMIT);
		dropTimeFrame = data.getLong(ConfPaths.INVENTORY_DROP_TIMEFRAME);
		dropActions = data.getOptimizedActionList(ConfPaths.INVENTORY_DROP_ACTIONS, Permissions.INVENTORY_DROP);

		fastClickCheck = data.getBoolean(ConfPaths.INVENTORY_FASTCLICK_CHECK);
		fastClickSpareCreative = data.getBoolean(ConfPaths.INVENTORY_FASTCLICK_SPARECREATIVE);
		fastClickTweaks1_5 = data.getBoolean(ConfPaths.INVENTORY_FASTCLICK_TWEAKS1_5);
		fastClickShortTermLimit = (float) data.getDouble(ConfPaths.INVENTORY_FASTCLICK_LIMIT_SHORTTERM);
		fastClickNormalLimit = (float) data.getDouble(ConfPaths.INVENTORY_FASTCLICK_LIMIT_NORMAL);
		fastClickActions = data.getOptimizedActionList(ConfPaths.INVENTORY_FASTCLICK_ACTIONS, Permissions.INVENTORY_FASTCLICK);
		
		fastConsumeCheck = data.getBoolean(ConfPaths.INVENTORY_FASTCONSUME_CHECK);
		fastConsumeDuration = (long) (1000.0 * data.getDouble(ConfPaths.INVENTORY_FASTCONSUME_DURATION));
		fastConsumeWhitelist = data.getBoolean(ConfPaths.INVENTORY_FASTCONSUME_WHITELIST);
		data.readMaterialFromList(ConfPaths.INVENTORY_FASTCONSUME_ITEMS, fastConsumeItems);
		fastConsumeActions = data.getOptimizedActionList(ConfPaths.INVENTORY_FASTCONSUME_ACTIONS, Permissions.INVENTORY_FASTCONSUME);

		instantBowCheck = data.getBoolean(ConfPaths.INVENTORY_INSTANTBOW_CHECK);
		instantBowStrict = data.getBoolean(ConfPaths.INVENTORY_INSTANTBOW_STRICT);
		instantBowDelay = data.getInt(ConfPaths.INVENTORY_INSTANTBOW_DELAY);
		instantBowActions = data.getOptimizedActionList(ConfPaths.INVENTORY_INSTANTBOW_ACTIONS, Permissions.INVENTORY_INSTANTBOW);

		instantEatCheck = data.getBoolean(ConfPaths.INVENTORY_INSTANTEAT_CHECK);
		instantEatActions = data.getOptimizedActionList(ConfPaths.INVENTORY_INSTANTEAT_ACTIONS, Permissions.INVENTORY_INSTANTEAT);
		
		itemsCheck = data.getBoolean(ConfPaths.INVENTORY_ITEMS_CHECK);
		
		openCheck = data.getBoolean(ConfPaths.INVENTORY_OPEN_CHECK);
		openClose = data.getBoolean(ConfPaths.INVENTORY_OPEN_CLOSE);
		openCancelOther = data.getBoolean(ConfPaths.INVENTORY_OPEN_CANCELOTHER);
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
		case INVENTORY_FASTCLICK:
			return fastClickCheck;
		case INVENTORY_ITEMS:
		    return itemsCheck;
		case INVENTORY_OPEN:
			return openCheck;
		case INVENTORY_DROP:
			return dropCheck;
		case INVENTORY_INSTANTBOW:
			return instantBowCheck;
		case INVENTORY_INSTANTEAT:
			return instantEatCheck;
		case INVENTORY_FASTCONSUME:
			return fastConsumeCheck;
		default:
			return true;
		}
	}

}
