package fr.neatmonster.nocheatplus.checks;

import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.checks.access.CheckConfigFactory;
import fr.neatmonster.nocheatplus.checks.access.CheckDataFactory;
import fr.neatmonster.nocheatplus.checks.blockbreak.BlockBreakConfig;
import fr.neatmonster.nocheatplus.checks.blockbreak.BlockBreakData;
import fr.neatmonster.nocheatplus.checks.blockinteract.BlockInteractConfig;
import fr.neatmonster.nocheatplus.checks.blockinteract.BlockInteractData;
import fr.neatmonster.nocheatplus.checks.blockplace.BlockPlaceConfig;
import fr.neatmonster.nocheatplus.checks.blockplace.BlockPlaceData;
import fr.neatmonster.nocheatplus.checks.chat.ChatConfig;
import fr.neatmonster.nocheatplus.checks.chat.ChatData;
import fr.neatmonster.nocheatplus.checks.combined.CombinedConfig;
import fr.neatmonster.nocheatplus.checks.combined.CombinedData;
import fr.neatmonster.nocheatplus.checks.fight.FightConfig;
import fr.neatmonster.nocheatplus.checks.fight.FightData;
import fr.neatmonster.nocheatplus.checks.inventory.InventoryConfig;
import fr.neatmonster.nocheatplus.checks.inventory.InventoryData;
import fr.neatmonster.nocheatplus.checks.moving.MovingConfig;
import fr.neatmonster.nocheatplus.checks.moving.MovingData;
import fr.neatmonster.nocheatplus.permissions.Permissions;
import fr.neatmonster.nocheatplus.players.DataManager;

/*
 * MM'""""'YMM dP                         dP       M""""""""M                            
 * M' .mmm. `M 88                         88       Mmmm  mmmM                            
 * M  MMMMMooM 88d888b. .d8888b. .d8888b. 88  .dP  MMMM  MMMM dP    dP 88d888b. .d8888b. 
 * M  MMMMMMMM 88'  `88 88ooood8 88'  `"" 88888"   MMMM  MMMM 88    88 88'  `88 88ooood8 
 * M. `MMM' .M 88    88 88.  ... 88.  ... 88  `8b. MMMM  MMMM 88.  .88 88.  .88 88.  ... 
 * MM.     .dM dP    dP `88888P' `88888P' dP   `YP MMMM  MMMM `8888P88 88Y888P' `88888P' 
 * MMMMMMMMMMM                                     MMMMMMMMMM      .88 88                
 *                                                             d8888P  dP                
 */
/**
 * Type of checks (containing configuration and dataFactory classes, name and permission).
 */
public enum CheckType {
    ALL(Permissions.CHECKS),

    BLOCKBREAK(BlockBreakConfig.factory, BlockBreakData.factory, Permissions.BLOCKBREAK),
    /** This will allow breaking all special blocks, currently only liquid. Later there might be more sub-types. */
    BLOCKBREAK_BREAK(BLOCKBREAK, Permissions.BLOCKBREAK_BREAK),
    BLOCKBREAK_DIRECTION(BLOCKBREAK, Permissions.BLOCKBREAK_DIRECTION),
    BLOCKBREAK_FASTBREAK(BLOCKBREAK, Permissions.BLOCKBREAK_FASTBREAK),
    BLOCKBREAK_FREQUENCY(BLOCKBREAK, Permissions.BLOCKBREAK_FREQUENCY),
    BLOCKBREAK_NOSWING(BLOCKBREAK, Permissions.BLOCKBREAK_NOSWING),
    BLOCKBREAK_REACH(BLOCKBREAK, Permissions.BLOCKBREAK_REACH),
    BLOCKBREAK_WRONGBLOCK(BLOCKBREAK, Permissions.BLOCKBREAK_WRONGBLOCK),

    BLOCKINTERACT(BlockInteractConfig.factory, BlockInteractData.factory, Permissions.BLOCKINTERACT),
    BLOCKINTERACT_DIRECTION(BLOCKINTERACT, Permissions.BLOCKINTERACT_DIRECTION),
    BLOCKINTERACT_REACH(BLOCKINTERACT, Permissions.BLOCKINTERACT_REACH),
    BLOCKINTERACT_SPEED(BLOCKINTERACT, Permissions.BLOCKINTERACT_SPEED),
    BLOCKINTERACT_VISIBLE(BLOCKINTERACT, Permissions.BLOCKINTERACT_VISIBLE),

    BLOCKPLACE(BlockPlaceConfig.factory, BlockPlaceData.factory, Permissions.BLOCKPLACE),
    BLOCKPLACE_AGAINST(BLOCKPLACE, Permissions.BLOCKPLACE_AGAINST),
    BLOCKPLACE_AUTOSIGN(BLOCKPLACE, Permissions.BLOCKPLACE_AUTOSIGN),
    BLOCKPLACE_DIRECTION(BLOCKPLACE, Permissions.BLOCKPLACE_DIRECTION),
    BLOCKPLACE_FASTPLACE(BLOCKPLACE, Permissions.BLOCKPLACE_FASTPLACE),
    BLOCKPLACE_NOSWING(BLOCKPLACE, Permissions.BLOCKPLACE_NOSWING),
    BLOCKPLACE_REACH(BLOCKPLACE, Permissions.BLOCKBREAK_REACH),
    BLOCKPLACE_SPEED(BLOCKPLACE, Permissions.BLOCKPLACE_SPEED),

    CHAT(ChatConfig.factory, ChatData.factory, Permissions.CHAT),
    CHAT_CAPTCHA(CHAT, Permissions.CHAT_CAPTCHA),
    CHAT_COLOR(CHAT, Permissions.CHAT_COLOR),
    CHAT_COMMANDS(CHAT, Permissions.CHAT_COMMANDS),
    CHAT_TEXT(CHAT, Permissions.CHAT_TEXT),
    CHAT_LOGINS(CHAT, Permissions.CHAT_LOGINS),
    CHAT_RELOG(CHAT, Permissions.CHAT_RELOG),
    
    
    COMBINED(CombinedConfig.factory, CombinedData.factory, Permissions.COMBINED),
    COMBINED_BEDLEAVE(COMBINED, Permissions.COMBINED_BEDLEAVE),
    COMBINED_IMPROBABLE(COMBINED, Permissions.COMBINED_IMPROBABLE),
    COMBINED_MUNCHHAUSEN(COMBINED, Permissions.COMBINED_MUNCHHAUSEN),

    FIGHT(FightConfig.factory, FightData.factory, Permissions.FIGHT),
    FIGHT_ANGLE(FIGHT, Permissions.FIGHT_ANGLE),
    FIGHT_CRITICAL(FIGHT, Permissions.FIGHT_CRITICAL),
    FIGHT_DIRECTION(FIGHT, Permissions.FIGHT_DIRECTION),
    FIGHT_FASTHEAL(FIGHT, Permissions.FIGHT_FASTHEAL),
    FIGHT_GODMODE(FIGHT, Permissions.FIGHT_GODMODE),
    FIGHT_KNOCKBACK(FIGHT, Permissions.FIGHT_KNOCKBACK),
    FIGHT_NOSWING(FIGHT, Permissions.FIGHT_NOSWING),
    FIGHT_REACH(FIGHT, Permissions.FIGHT_REACH),
    FIGHT_SELFHIT(FIGHT, Permissions.FIGHT_SELFHIT),
    FIGHT_SPEED(FIGHT, Permissions.FIGHT_SPEED),

    INVENTORY(InventoryConfig.factory, InventoryData.factory, Permissions.INVENTORY),
    INVENTORY_DROP(INVENTORY, Permissions.INVENTORY_DROP),
    INVENTORY_FASTCLICK(INVENTORY, Permissions.INVENTORY_FASTCLICK),
    INVENTORY_FASTCONSUME(INVENTORY, Permissions.INVENTORY_FASTCONSUME),
    INVENTORY_INSTANTBOW(INVENTORY, Permissions.INVENTORY_INSTANTBOW),
    INVENTORY_INSTANTEAT(INVENTORY, Permissions.INVENTORY_INSTANTEAT),
    INVENTORY_ITEMS(INVENTORY, Permissions.INVENTORY_ITEMS),
    INVENTORY_OPEN(INVENTORY, Permissions.INVENTORY_OPEN),

    MOVING(MovingConfig.factory, MovingData.factory, Permissions.MOVING),
    MOVING_CREATIVEFLY(MOVING, Permissions.MOVING_CREATIVEFLY),
    MOVING_MOREPACKETS(MOVING, Permissions.MOVING_MOREPACKETS),
    MOVING_MOREPACKETSVEHICLE(MOVING, Permissions.MOVING_MOREPACKETSVEHICLE),
    MOVING_NOFALL(MOVING, Permissions.MOVING_NOFALL),
    MOVING_PASSABLE(MOVING, Permissions.MOVING_PASSABLE),
    MOVING_SURVIVALFLY(MOVING, Permissions.MOVING_SURVIVALFLY),
    
    UNKNOWN;

	/** If not null, this is the check group usually. */
	private final CheckType parent;

	/** The check config factory (access CheckConfig instances by CheckType). */
	private final CheckConfigFactory configFactory;

	/** The check data factory (access CheckData instances by CheckType). */
	private final CheckDataFactory dataFactory;

	/** The bypass permission. */
	private final String permission;

	/**
	 * Special purpose check types (likely not actual checks).
	 */
	private CheckType() {
		this(null, null, null);
	}
	
	/**
	 * Special purpose for grouping (ALL).
	 * @param permission
	 */
	private CheckType(final String permission){
		this(null, null, permission);
	}
	
	/**
	 * Constructor for root checks or check groups, that are not grouped under another check type.
	 * @param configFactory
	 * @param dataFactory
	 * @param permission
	 */
	private CheckType(final CheckConfigFactory configFactory, final CheckDataFactory dataFactory, final String permission) {
		this(null, permission, configFactory, dataFactory);
	}

	/**
	 * Constructor for sub-checks grouped under another check type.
	 * @param parent
	 * @param permission
	 */
	private CheckType(final CheckType parent, final String permission) {
		this(parent, permission, parent.getConfigFactory(), parent.getDataFactory());
	}

	/**
	 * Root constructor.
	 * @param parent Super check type (usually the group).
	 * @param permission Bypass permission.
	 * @param configFactory Check config factory.
	 * @param dataFactory Check data factory.
	 */
	private CheckType(final CheckType parent, final String permission, final CheckConfigFactory configFactory, final CheckDataFactory dataFactory) {
		this.parent = parent;
		this.permission = permission;
		this.configFactory = configFactory;
		this.dataFactory = dataFactory;
	}

    /**
     * Gets the configFactory.
     * 
     * @return the configFactory
     */
    public CheckConfigFactory getConfigFactory() {
        return configFactory;
    }

    /**
     * Gets the dataFactory.
     * 
     * @return the dataFactory
     */
    public CheckDataFactory getDataFactory() {
        return dataFactory;
    }

    /**
     * Gets the name.
     * 
     * @return the name
     */
    public String getName() {
        return toString().toLowerCase().replace("_", ".");
    }

    /**
     * Gets the parent.
     * 
     * @return the parent
     */
    public CheckType getParent() {
        return parent;
    }

    /**
     * Gets the bypass permission for this check type.
     * 
     * @return the permission
     */
    public String getPermission() {
        return permission;
    }
    
    /**
     * Quick permission check for cached entriy only (for async checks). If not present, after failure will need to deal with this.
     * @param player
     * @return
     */
    public boolean hasCachedPermission(final Player player){
    	return hasCachedPermission(player, getPermission());
    }
    		
    /**
     * Quick permission check for cached entries only (for async checks). If not present, after failure will need to deal with this.
     * @param player
     * @param permission
     * @return
     */
    public boolean hasCachedPermission(final Player player, final String permission){
    	return dataFactory.getData(player).hasCachedPermission(permission);
    }

    /**
     * Check if the check is enabled by configuration (no permission check).
     * 
     * @param player
     *            the player
     * @return true, if the check is enabled
     */
    public final boolean isEnabled(final Player player) {
        return configFactory.getConfig(player).isEnabled(this);
    }

    /**
     * Remove the player data for a given player and a given check type. CheckType.ALL and null will be interpreted as removing all data.<br>
     * @deprecated Will be removed, use DataManager.removeData instead.
     * @param playerName
     * @param checkType 
     * @return If any data was present.
     */
	public static boolean removeData(final String playerName, final CheckType checkType) {
		return DataManager.removeData(playerName, checkType);
	}
}