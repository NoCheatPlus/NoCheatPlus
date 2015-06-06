package fr.neatmonster.nocheatplus.compat;

import fr.neatmonster.nocheatplus.config.ConfPaths;
import fr.neatmonster.nocheatplus.config.ConfigFile;
import fr.neatmonster.nocheatplus.config.ConfigManager;

public class MCAccessConfig {

    public final boolean enableCBDedicated;
    public final boolean enableCBReflect;

    public MCAccessConfig() {
        final ConfigFile config = ConfigManager.getConfigFile();
        this.enableCBDedicated = config.getBoolean(ConfPaths.COMPATIBILITY_SERVER_CBDEDICATED_ENABLE);
        this.enableCBReflect = config.getBoolean(ConfPaths.COMPATIBILITY_SERVER_CBREFLECT_ENABLE);
    }

}
