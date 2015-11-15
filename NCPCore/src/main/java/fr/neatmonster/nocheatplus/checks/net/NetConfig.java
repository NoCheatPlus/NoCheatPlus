package fr.neatmonster.nocheatplus.checks.net;

import fr.neatmonster.nocheatplus.actions.ActionList;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.access.ACheckConfig;
import fr.neatmonster.nocheatplus.config.ConfPaths;
import fr.neatmonster.nocheatplus.config.ConfigFile;
import fr.neatmonster.nocheatplus.config.ConfigManager;
import fr.neatmonster.nocheatplus.permissions.Permissions;

/**
 * Configuration for the net checks (fast version, sparse).
 * @author asofold
 *
 */
public class NetConfig extends ACheckConfig {

    public final boolean attackFrequencyActive;

    public final boolean flyingFrequencyActive;
    public final int flyingFrequencySeconds;
    public final double flyingFrequencyPPS;
    public final ActionList flyingFrequencyActions;
    public final boolean flyingFrequencyStrayPacketsCancel; 
    public final boolean flyingFrequencyRedundantActive;
    public final int flyingFrequencyRedundantSeconds;
    public final ActionList flyingFrequencyRedundantActions;

    public final boolean keepAliveFrequencyActive;
    public final ActionList keepAliveFrequencyActions;

    public final boolean soundDistanceActive;
    /** Maximum distance for lightning effects (squared). */
    public final double soundDistanceSq;

    public NetConfig(final ConfigFile config) {
        super(config, ConfPaths.NET, new String[] {
                Permissions.NET_ATTACKFREQUENCY,
                Permissions.NET_FLYINGFREQUENCY, 
                Permissions.NET_KEEPALIVEFREQUENCY,
        });

        final ConfigFile globalConfig = ConfigManager.getConfigFile();

        attackFrequencyActive = config.getBoolean(ConfPaths.NET_ATTACKFREQUENCY_ACTIVE);
        // TODO: Others.

        flyingFrequencyActive = config.getBoolean(ConfPaths.NET_FLYINGFREQUENCY_ACTIVE);
        flyingFrequencySeconds = Math.max(1, globalConfig.getInt(ConfPaths.NET_FLYINGFREQUENCY_SECONDS));
        flyingFrequencyPPS = Math.max(1.0, globalConfig.getDouble(ConfPaths.NET_FLYINGFREQUENCY_PACKETSPERSECOND));
        flyingFrequencyActions = config.getOptimizedActionList(ConfPaths.NET_FLYINGFREQUENCY_ACTIONS, Permissions.NET_FLYINGFREQUENCY);
        flyingFrequencyStrayPacketsCancel = config.getBoolean(ConfPaths.NET_FLYINGFREQUENCY_STRAYPACKETS_CANCEL);
        flyingFrequencyRedundantActive = config.getBoolean(ConfPaths.NET_FLYINGFREQUENCY_CANCELREDUNDANT);
        flyingFrequencyRedundantSeconds = Math.max(1, config.getInt(ConfPaths.NET_FLYINGFREQUENCY_REDUNDANT_SECONDS));
        // Same permission for "silent".
        flyingFrequencyRedundantActions = config.getOptimizedActionList(ConfPaths.NET_FLYINGFREQUENCY_REDUNDANT_ACTIONS, Permissions.NET_FLYINGFREQUENCY);

        keepAliveFrequencyActive = config.getBoolean(ConfPaths.NET_KEEPALIVEFREQUENCY_ACTIVE);
        keepAliveFrequencyActions = config.getOptimizedActionList(ConfPaths.NET_KEEPALIVEFREQUENCY_ACTIONS, Permissions.NET_KEEPALIVEFREQUENCY);

        soundDistanceActive = config.getBoolean(ConfPaths.NET_SOUNDDISTANCE_ACTIVE);
        double dist = config.getDouble(ConfPaths.NET_SOUNDDISTANCE_MAXDISTANCE);
        soundDistanceSq = dist * dist;

    }

    @Override
    public boolean isEnabled(final CheckType checkType) {
        switch(checkType) {
            case NET_ATTACKFREQUENCY:
                return attackFrequencyActive;
            case NET_FLYINGFREQUENCY:
                return flyingFrequencyActive;
            case NET_SOUNDDISTANCE:
                return soundDistanceActive;
            case NET_KEEPALIVEFREQUENCY:
                return keepAliveFrequencyActive;
            default:
                return true;
        }
    }

}
