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
package fr.neatmonster.nocheatplus.checks.net;

import fr.neatmonster.nocheatplus.actions.ActionList;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.access.ACheckConfig;
import fr.neatmonster.nocheatplus.compat.versions.ServerVersion;
import fr.neatmonster.nocheatplus.config.ConfPaths;
import fr.neatmonster.nocheatplus.config.ConfigFile;
import fr.neatmonster.nocheatplus.config.ConfigManager;
import fr.neatmonster.nocheatplus.permissions.Permissions;
import fr.neatmonster.nocheatplus.permissions.RegisteredPermission;

/**
 * Configuration for the net checks (fast version, sparse).
 * @author asofold
 *
 */
public class NetConfig extends ACheckConfig {

    private static RegisteredPermission[] preferKeepUpdatedPermissions = new RegisteredPermission[] {
            Permissions.NET_ATTACKFREQUENCY,
            Permissions.NET_FLYINGFREQUENCY, 
            Permissions.NET_KEEPALIVEFREQUENCY,
            Permissions.NET_PACKETFREQUENCY,
            Permissions.NET_EQUALSROTATE
    };

    public static RegisteredPermission[] getPreferKeepUpdatedPermissions() {
        // TODO: Individual checks might want to register these, or just on permission checking.
        return preferKeepUpdatedPermissions;
    }

    /////////////
    // Instance
    /////////////

    public final boolean attackFrequencyActive;
    public final float attackFrequencyLimitSecondsHalf;
    public final float attackFrequencyLimitSecondsOne;
    public final float attackFrequencyLimitSecondsTwo;
    public final float attackFrequencyLimitSecondsFour;
    public final float attackFrequencyLimitSecondsEight;
    public final ActionList attackFrequencyActions;

    public final boolean flyingFrequencyActive;
    public final int flyingFrequencySeconds;
    public final double flyingFrequencyPPS;
    public final ActionList flyingFrequencyActions;
    public final boolean flyingFrequencyRedundantActive;
    public final int flyingFrequencyRedundantSeconds;
    public final ActionList flyingFrequencyRedundantActions;

    public final boolean keepAliveFrequencyActive;
    public final ActionList keepAliveFrequencyActions;

    public final boolean packetFrequencyActive;
    public final float packetFrequencyPacketsPerSecond;
    public final int packetFrequencySeconds;
    public final ActionList packetFrequencyActions;

    public final boolean soundDistanceActive;
    /** Maximum distance for lightning effects (squared). */
    public final double soundDistanceSq;

    public final boolean supersededFlyingCancelWaiting;

    public final boolean equalsRotateActive;
    public final ActionList equalsRotateActions;

    public NetConfig(final ConfigFile config) {
        // TODO: These permissions should have default policies.
        super(config, ConfPaths.NET);

        final ConfigFile globalConfig = ConfigManager.getConfigFile();

        attackFrequencyActive = config.getBoolean(ConfPaths.NET_ATTACKFREQUENCY_ACTIVE);
        attackFrequencyLimitSecondsHalf = config.getInt(ConfPaths.NET_ATTACKFREQUENCY_SECONDS_HALF);
        attackFrequencyLimitSecondsOne = config.getInt(ConfPaths.NET_ATTACKFREQUENCY_SECONDS_ONE);
        attackFrequencyLimitSecondsTwo = config.getInt(ConfPaths.NET_ATTACKFREQUENCY_SECONDS_TWO);
        attackFrequencyLimitSecondsFour= config.getInt(ConfPaths.NET_ATTACKFREQUENCY_SECONDS_FOUR);
        attackFrequencyLimitSecondsEight = config.getInt(ConfPaths.NET_ATTACKFREQUENCY_SECONDS_EIGHT);
        attackFrequencyActions = config.getOptimizedActionList(ConfPaths.NET_ATTACKFREQUENCY_ACTIONS, Permissions.NET_ATTACKFREQUENCY);

        flyingFrequencyActive = config.getBoolean(ConfPaths.NET_FLYINGFREQUENCY_ACTIVE);
        flyingFrequencySeconds = Math.max(1, globalConfig.getInt(ConfPaths.NET_FLYINGFREQUENCY_SECONDS));
        flyingFrequencyPPS = Math.max(1.0, globalConfig.getDouble(ConfPaths.NET_FLYINGFREQUENCY_PACKETSPERSECOND));
        flyingFrequencyActions = config.getOptimizedActionList(ConfPaths.NET_FLYINGFREQUENCY_ACTIONS, Permissions.NET_FLYINGFREQUENCY);
        flyingFrequencyRedundantActive = config.getBoolean(ConfPaths.NET_FLYINGFREQUENCY_CANCELREDUNDANT);
        flyingFrequencyRedundantSeconds = Math.max(1, config.getInt(ConfPaths.NET_FLYINGFREQUENCY_REDUNDANT_SECONDS));
        // Same permission for "silent".
        flyingFrequencyRedundantActions = config.getOptimizedActionList(ConfPaths.NET_FLYINGFREQUENCY_REDUNDANT_ACTIONS, Permissions.NET_FLYINGFREQUENCY);

        keepAliveFrequencyActive = config.getBoolean(ConfPaths.NET_KEEPALIVEFREQUENCY_ACTIVE);
        keepAliveFrequencyActions = config.getOptimizedActionList(ConfPaths.NET_KEEPALIVEFREQUENCY_ACTIONS, Permissions.NET_KEEPALIVEFREQUENCY);

        packetFrequencyActive = config.getAlmostBoolean(ConfPaths.NET_PACKETFREQUENCY_ACTIVE, ServerVersion.compareMinecraftVersion("1.9") < 0, false);
        packetFrequencyPacketsPerSecond = config.getInt(ConfPaths.NET_PACKETFREQUENCY_PPS);
        packetFrequencySeconds = config.getInt(ConfPaths.NET_PACKETFREQUENCY_SECONDS);
        packetFrequencyActions = config.getOptimizedActionList(ConfPaths.NET_PACKETFREQUENCY_ACTIONS, Permissions.NET_PACKETFREQUENCY);

        soundDistanceActive = config.getBoolean(ConfPaths.NET_SOUNDDISTANCE_ACTIVE);
        double dist = config.getDouble(ConfPaths.NET_SOUNDDISTANCE_MAXDISTANCE);
        soundDistanceSq = dist * dist;

        supersededFlyingCancelWaiting = config.getBoolean(ConfPaths.NET_SUPERSEDED_FLYING_CANCELWAITING);

        equalsRotateActive = config.getBoolean(ConfPaths.NET_EQALSROTATE_ACTIVE);
        equalsRotateActions = config.getOptimizedActionList(ConfPaths.NET_EQALSROTATE_ACTIONS, Permissions.NET_EQUALSROTATE);
    }

    @Override
    public boolean isEnabled(final CheckType checkType) {
        switch(checkType) {
            case NET_ATTACKFREQUENCY:
                return attackFrequencyActive;
            case NET_FLYINGFREQUENCY:
                return flyingFrequencyActive;
            case NET_PACKETFREQUENCY:
                return packetFrequencyActive;
            case NET_SOUNDDISTANCE:
                return soundDistanceActive;
            case NET_KEEPALIVEFREQUENCY:
                return keepAliveFrequencyActive;
            case NET_EQUALSROTATE:
                return equalsRotateActive;
            default:
                return true;
        }
    }

}
