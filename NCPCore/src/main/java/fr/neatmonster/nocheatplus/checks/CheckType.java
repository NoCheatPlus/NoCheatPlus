/*
 * This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
import fr.neatmonster.nocheatplus.checks.net.NetConfigCache;
import fr.neatmonster.nocheatplus.checks.net.NetDataFactory;
import fr.neatmonster.nocheatplus.permissions.Permissions;

/**
 * Type of checks (containing configuration and dataFactory classes, name and
 * permission).
 */
public enum CheckType {
    ALL(Permissions.CHECKS),

    BLOCKBREAK(CheckType.ALL, BlockBreakConfig.factory, BlockBreakData.factory, Permissions.BLOCKBREAK),
    /** This will allow breaking all special blocks, currently only liquid. Later there might be more sub-types. */
    BLOCKBREAK_BREAK(BLOCKBREAK, Permissions.BLOCKBREAK_BREAK),
    BLOCKBREAK_DIRECTION(BLOCKBREAK, Permissions.BLOCKBREAK_DIRECTION),
    BLOCKBREAK_FASTBREAK(BLOCKBREAK, Permissions.BLOCKBREAK_FASTBREAK),
    BLOCKBREAK_FREQUENCY(BLOCKBREAK, Permissions.BLOCKBREAK_FREQUENCY),
    BLOCKBREAK_NOSWING(BLOCKBREAK, Permissions.BLOCKBREAK_NOSWING),
    BLOCKBREAK_REACH(BLOCKBREAK, Permissions.BLOCKBREAK_REACH),
    BLOCKBREAK_WRONGBLOCK(BLOCKBREAK, Permissions.BLOCKBREAK_WRONGBLOCK),

    BLOCKINTERACT(CheckType.ALL, BlockInteractConfig.factory, BlockInteractData.factory, Permissions.BLOCKINTERACT),
    BLOCKINTERACT_DIRECTION(BLOCKINTERACT, Permissions.BLOCKINTERACT_DIRECTION),
    BLOCKINTERACT_REACH(BLOCKINTERACT, Permissions.BLOCKINTERACT_REACH),
    BLOCKINTERACT_SPEED(BLOCKINTERACT, Permissions.BLOCKINTERACT_SPEED),
    BLOCKINTERACT_VISIBLE(BLOCKINTERACT, Permissions.BLOCKINTERACT_VISIBLE),

    BLOCKPLACE(CheckType.ALL, BlockPlaceConfig.factory, BlockPlaceData.factory, Permissions.BLOCKPLACE),
    BLOCKPLACE_AGAINST(BLOCKPLACE, Permissions.BLOCKPLACE_AGAINST),
    BLOCKPLACE_AUTOSIGN(BLOCKPLACE, Permissions.BLOCKPLACE_AUTOSIGN),
    BLOCKPLACE_DIRECTION(BLOCKPLACE, Permissions.BLOCKPLACE_DIRECTION),
    BLOCKPLACE_FASTPLACE(BLOCKPLACE, Permissions.BLOCKPLACE_FASTPLACE),
    BLOCKPLACE_NOSWING(BLOCKPLACE, Permissions.BLOCKPLACE_NOSWING),
    BLOCKPLACE_REACH(BLOCKPLACE, Permissions.BLOCKBREAK_REACH),
    BLOCKPLACE_SPEED(BLOCKPLACE, Permissions.BLOCKPLACE_SPEED),

    CHAT(CheckType.ALL, ChatConfig.factory, ChatData.factory, Permissions.CHAT),
    CHAT_CAPTCHA(CHAT, Permissions.CHAT_CAPTCHA),
    CHAT_COLOR(CHAT, Permissions.CHAT_COLOR),
    CHAT_COMMANDS(CHAT, Permissions.CHAT_COMMANDS),
    CHAT_TEXT(CHAT, Permissions.CHAT_TEXT),
    CHAT_LOGINS(CHAT, Permissions.CHAT_LOGINS),
    CHAT_RELOG(CHAT, Permissions.CHAT_RELOG),


    COMBINED(CheckType.ALL, CombinedConfig.factory, CombinedData.factory, Permissions.COMBINED),
    COMBINED_BEDLEAVE(COMBINED, Permissions.COMBINED_BEDLEAVE),
    COMBINED_IMPROBABLE(COMBINED, Permissions.COMBINED_IMPROBABLE),
    COMBINED_MUNCHHAUSEN(COMBINED, Permissions.COMBINED_MUNCHHAUSEN),
    /** Rather for data removal and exemption. */
    COMBINED_YAWRATE(COMBINED),

    FIGHT(CheckType.ALL, FightConfig.factory, FightData.factory, Permissions.FIGHT),
    FIGHT_ANGLE(FIGHT, Permissions.FIGHT_ANGLE),
    FIGHT_CRITICAL(FIGHT, Permissions.FIGHT_CRITICAL),
    FIGHT_DIRECTION(FIGHT, Permissions.FIGHT_DIRECTION),
    FIGHT_FASTHEAL(FIGHT, Permissions.FIGHT_FASTHEAL),
    FIGHT_GODMODE(FIGHT, Permissions.FIGHT_GODMODE),
    FIGHT_NOSWING(FIGHT, Permissions.FIGHT_NOSWING),
    FIGHT_REACH(FIGHT, Permissions.FIGHT_REACH),
    FIGHT_SELFHIT(CheckTypeType.CHECK, FIGHT, Permissions.FIGHT_SELFHIT, 
            FightConfig.factory, FightData.selfHitDataFactory),
    FIGHT_SPEED(FIGHT, Permissions.FIGHT_SPEED),

    INVENTORY(CheckType.ALL, InventoryConfig.factory, InventoryData.factory, Permissions.INVENTORY),
    INVENTORY_DROP(INVENTORY, Permissions.INVENTORY_DROP),
    INVENTORY_FASTCLICK(INVENTORY, Permissions.INVENTORY_FASTCLICK),
    INVENTORY_FASTCONSUME(INVENTORY, Permissions.INVENTORY_FASTCONSUME),
    INVENTORY_GUTENBERG(INVENTORY, Permissions.INVENTORY_GUTENBERG),
    INVENTORY_INSTANTBOW(INVENTORY, Permissions.INVENTORY_INSTANTBOW),
    INVENTORY_INSTANTEAT(INVENTORY, Permissions.INVENTORY_INSTANTEAT),
    INVENTORY_ITEMS(INVENTORY, Permissions.INVENTORY_ITEMS),
    INVENTORY_OPEN(INVENTORY, Permissions.INVENTORY_OPEN),

    MOVING(CheckType.ALL, MovingConfig.factory, MovingData.factory, Permissions.MOVING),
    MOVING_CREATIVEFLY(MOVING, Permissions.MOVING_CREATIVEFLY),
    MOVING_MOREPACKETS(MOVING, Permissions.MOVING_MOREPACKETS),
    MOVING_NOFALL(MOVING, Permissions.MOVING_NOFALL),
    MOVING_PASSABLE(MOVING, Permissions.MOVING_PASSABLE),
    MOVING_SURVIVALFLY(MOVING, Permissions.MOVING_SURVIVALFLY),
    MOVING_VEHICLE(MOVING, Permissions.MOVING_VEHICLE),
    MOVING_VEHICLE_MOREPACKETS(MOVING_VEHICLE, Permissions.MOVING_VEHICLE_MOREPACKETS),
    MOVING_VEHICLE_ENVELOPE(MOVING_VEHICLE, Permissions.MOVING_VEHICLE_ENVELOPE),

    NET(CheckType.ALL, new NetConfigCache(), new NetDataFactory(), Permissions.NET),
    NET_ATTACKFREQUENCY(NET, Permissions.NET_ATTACKFREQUENCY),
    NET_FLYINGFREQUENCY(NET, Permissions.NET_FLYINGFREQUENCY),
    NET_KEEPALIVEFREQUENCY(NET, Permissions.NET_KEEPALIVEFREQUENCY),
    NET_PACKETFREQUENCY(NET, Permissions.NET_PACKETFREQUENCY),
    NET_SOUNDDISTANCE(NET), // Can not exempt players from this one.

    ;

    public static enum CheckTypeType {
        /** Special types, like ALL */
        SPECIAL,
        /** Potentially obsolete: A check group that is not a check itself. */
        GROUP,
        /** An actual check. Could in future still have sub checks. */
        CHECK
    }

    /** The type of the check type. */
    private final CheckTypeType type;

    /** If not null, this is the check group usually. */
    private final CheckType parent;

    /** The check config factory (access CheckConfig instances by CheckType). */
    private final CheckConfigFactory configFactory;

    /** The check data factory (access CheckData instances by CheckType). */
    private final CheckDataFactory dataFactory;

    /** The bypass permission. */
    private final String permission;

    /**
     * Special purpose for grouping (ALL).
     * 
     * @param permission
     */
    private CheckType(final String permission){
        this(CheckTypeType.SPECIAL, null, permission, null, null);
    }

    /**
     * Constructor for root checks or check groups, that are not grouped under
     * another check type.
     * 
     * @param configFactory
     * @param dataFactory
     * @param permission
     */
    private CheckType(final CheckType parent, 
            final CheckConfigFactory configFactory, final CheckDataFactory dataFactory, 
            final String permission) {
        this(CheckTypeType.GROUP, parent, permission, configFactory, dataFactory);
    }

    /**
     * Constructor for sub-checks grouped under another check type, without
     * having a permission set.
     * 
     * @param parent
     */
    private CheckType(final CheckType parent) {
        this(parent, null);
    }

    /**
     * Constructor for sub-checks grouped under another check type.
     * 
     * @param parent
     * @param permission
     */
    private CheckType(final CheckType parent, final String permission) {
        this(CheckTypeType.CHECK, parent, permission, parent.getConfigFactory(), parent.getDataFactory());
    }

    /**
     * General constructor
     * 
     * @param type
     *            The type of the check type.
     * @param parent
     *            Super check type (usually the group).
     * @param permission
     *            Bypass permission.
     * @param configFactory
     *            Check config factory.
     * @param dataFactory
     *            Check data factory.
     */
    private CheckType(final CheckTypeType type, final CheckType parent, final String permission, 
            final CheckConfigFactory configFactory, final CheckDataFactory dataFactory) {
        this.type = type;
        this.parent = parent;
        this.permission = permission;
        this.configFactory = configFactory;
        this.dataFactory = dataFactory;
    }

    public CheckTypeType getType() {
        return type;
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
     * Quick permission check for cached entriy only (for async checks). If not
     * present, after failure will need to deal with this.
     * 
     * @param player
     * @return
     */
    public boolean hasCachedPermission(final Player player){
        return hasCachedPermission(player, getPermission());
    }

    /**
     * Quick permission check for cached entries only (for async checks). If not
     * present, after failure will need to deal with this.
     * 
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

}