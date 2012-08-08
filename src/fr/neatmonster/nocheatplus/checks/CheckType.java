package fr.neatmonster.nocheatplus.checks;

import fr.neatmonster.nocheatplus.checks.blockbreak.BlockBreakConfig;
import fr.neatmonster.nocheatplus.checks.blockbreak.BlockBreakData;
import fr.neatmonster.nocheatplus.checks.blockinteract.BlockInteractConfig;
import fr.neatmonster.nocheatplus.checks.blockinteract.BlockInteractData;
import fr.neatmonster.nocheatplus.checks.blockplace.BlockPlaceConfig;
import fr.neatmonster.nocheatplus.checks.blockplace.BlockPlaceData;
import fr.neatmonster.nocheatplus.checks.chat.ChatConfig;
import fr.neatmonster.nocheatplus.checks.chat.ChatData;
import fr.neatmonster.nocheatplus.checks.fight.FightConfig;
import fr.neatmonster.nocheatplus.checks.fight.FightData;
import fr.neatmonster.nocheatplus.checks.inventory.InventoryConfig;
import fr.neatmonster.nocheatplus.checks.inventory.InventoryData;
import fr.neatmonster.nocheatplus.checks.moving.MovingConfig;
import fr.neatmonster.nocheatplus.checks.moving.MovingData;
import fr.neatmonster.nocheatplus.players.Permissions;

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
 * Type of checks (containing configuration and data classes, name and permission).
 */
public enum CheckType {
    ALL,

    BLOCKBREAK(BlockBreakConfig.class, BlockBreakData.class),
    BLOCKBREAK_DIRECTION(BLOCKBREAK, "direction", Permissions.BLOCKBREAK_DIRECTION),
    BLOCKBREAK_FASTBREAK(BLOCKBREAK, "fastBreak", Permissions.BLOCKBREAK_FASTBREAK),
    BLOCKBREAK_NOSWING(BLOCKBREAK, "noSwing", Permissions.BLOCKBREAK_NOSWING),
    BLOCKBREAK_REACH(BLOCKBREAK, "reach", Permissions.BLOCKBREAK_REACH),

    BLOCKINTERACT(BlockInteractConfig.class, BlockInteractData.class),
    BLOCKINTERACT_DIRECTION(BLOCKINTERACT, "direction", Permissions.BLOCKINTERACT_DIRECTION),
    BLOCKINTERACT_REACH(BLOCKINTERACT, "reach", Permissions.BLOCKINTERACT_REACH),

    BLOCKPLACE(BlockPlaceConfig.class, BlockPlaceData.class),
    BLOCKPLACE_DIRECTION(BLOCKPLACE, "direction", Permissions.BLOCKPLACE_DIRECTION),
    BLOCKPLACE_FASTPLACE(BLOCKPLACE, "fastPlace", Permissions.BLOCKPLACE_FASTPLACE),
    BLOCKPLACE_NOSWING(BLOCKPLACE, "noSwing", Permissions.BLOCKPLACE_NOSWING),
    BLOCKPLACE_REACH(BLOCKPLACE, "reach", Permissions.BLOCKBREAK_REACH),
    BLOCKPLACE_SPEED(BLOCKPLACE, "speed", Permissions.BLOCKPLACE_SPEED),

    CHAT(ChatConfig.class, ChatData.class),
    CHAT_COLOR(CHAT, "color", Permissions.CHAT_COLOR),
    CHAT_NOPWNAGE(CHAT, "noPwnage", Permissions.CHAT_NOPWNAGE),

    FIGHT(FightConfig.class, FightData.class),
    FIGHT_ANGLE(FIGHT, "angle", Permissions.FIGHT_ANGLE),
    FIGHT_CRITICAL(FIGHT, "critical", Permissions.FIGHT_CRITICAL),
    FIGHT_DIRECTION(FIGHT, "direction", Permissions.FIGHT_DIRECTION),
    FIGHT_GODMODE(FIGHT, "godMode", Permissions.FIGHT_GODMODE),
    FIGHT_INSTANTHEAL(FIGHT, "instantHeal", Permissions.FIGHT_INSTANTHEAL),
    FIGHT_KNOCKBACK(FIGHT, "knockback", Permissions.FIGHT_KNOCKBACK),
    FIGHT_NOSWING(FIGHT, "noSwing", Permissions.FIGHT_NOSWING),
    FIGHT_REACH(FIGHT, "reach", Permissions.FIGHT_REACH),
    FIGHT_SPEED(FIGHT, "speed", Permissions.FIGHT_SPEED),

    INVENTORY(InventoryConfig.class, InventoryData.class),
    INVENTORY_DROP(INVENTORY, "drop", Permissions.INVENTORY_DROP),
    INVENTORY_INSTANTBOW(INVENTORY, "instantBow", Permissions.INVENTORY_INSTANTBOW),
    INVENTORY_INSTANTEAT(INVENTORY, "instantEat", Permissions.INVENTORY_INSTANTEAT),

    MOVING(MovingConfig.class, MovingData.class),
    MOVING_CREATIVEFLY(MOVING, "creativeFly", Permissions.MOVING_CREATIVEFLY),
    MOVING_MOREPACKETS(MOVING, "morePackets", Permissions.MOVING_MOREPACKETS),
    MOVING_MOREPACKETSVEHICLE(MOVING, "morePacketsVehicle", Permissions.MOVING_MOREPACKETSVEHICLE),
    MOVING_NOFALL(MOVING, "noFall", Permissions.MOVING_NOFALL),
    MOVING_SURVIVALFLY(MOVING, "survivalFly", Permissions.MOVING_SURVIVALFLY),

    UNKNOWN;

    /** The group. */
    public final CheckType group;

    /** The config. */
    public final Class<?>  config;

    /** The data. */
    public final Class<?>  data;

    /** The name. */
    public final String    name;

    /** The permission. */
    public final String    permission;

    /**
     * Instantiates a new check type.
     */
    private CheckType() {
        this(null, null, null, null, null);
    }

    /**
     * Instantiates a new check type.
     * 
     * @param group
     *            the group
     * @param config
     *            the config class
     * @param data
     *            the data class
     * @param name
     *            the name
     * @param permission
     *            the permission
     */
    private CheckType(final CheckType group, final Class<?> config, final Class<?> data, final String name,
            final String permission) {
        this.group = group;
        this.config = config;
        this.data = data;
        this.name = name;
        this.permission = permission;
    }

    /**
     * Instantiates a new check type.
     * 
     * @param group
     *            the group
     * @param name
     *            the name
     * @param permission
     *            the permission
     */
    private CheckType(final CheckType group, final String name, final String permission) {
        this(group, group.getConfig(), group.getData(), name, permission);
    }

    /**
     * Instantiates a new check type.
     * 
     * @param config
     *            the config
     * @param data
     *            the data
     */
    private CheckType(final Class<?> config, final Class<?> data) {
        this(null, config, data, null, null);
    }

    /**
     * Gets the config class.
     * 
     * @return the config class
     */
    public Class<?> getConfig() {
        return config;
    }

    /**
     * Gets the data class.
     * 
     * @return the data class
     */
    public Class<?> getData() {
        return data;
    }

    /**
     * Gets the name.
     * 
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the permission.
     * 
     * @return the permission
     */
    public String getPermission() {
        return permission;
    }
}