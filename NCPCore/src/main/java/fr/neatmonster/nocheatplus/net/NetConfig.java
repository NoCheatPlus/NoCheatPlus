package fr.neatmonster.nocheatplus.net;

import fr.neatmonster.nocheatplus.config.ConfPaths;
import fr.neatmonster.nocheatplus.config.ConfigFile;
import fr.neatmonster.nocheatplus.config.ConfigManager;

/**
 * Configuration for the net checks (fast version, sparse).
 * @author web4web1
 *
 */
public class NetConfig {

    public final boolean flyingFrequencyActive;
    public final int flyingFrequencySeconds;
    public final int flyingFrequencyMaxPackets;
    public final boolean flyingFrequencyCancelRedundant;

    public final boolean soundDistanceActive;
    /** Maximum distance for lightning effects (squared). */
    public final double soundDistanceSq;

    public NetConfig(final ConfigFile config) {

        final ConfigFile globalConfig = ConfigManager.getConfigFile();
        flyingFrequencyActive = config.getBoolean(ConfPaths.NET_FLYINGFREQUENCY_ACTIVE);
        flyingFrequencySeconds = Math.max(1, globalConfig.getInt(ConfPaths.NET_FLYINGFREQUENCY_SECONDS));
        flyingFrequencyMaxPackets = Math.max(1, globalConfig.getInt(ConfPaths.NET_FLYINGFREQUENCY_MAXPACKETS));
        flyingFrequencyCancelRedundant = config.getBoolean(ConfPaths.NET_FLYINGFREQUENCY_CANCELREDUNDANT);

        soundDistanceActive = config.getBoolean(ConfPaths.NET_SOUNDDISTANCE_ACTIVE);
        double dist = config.getDouble(ConfPaths.NET_SOUNDDISTANCE_MAXDISTANCE);
        soundDistanceSq = dist * dist;

    }

}
