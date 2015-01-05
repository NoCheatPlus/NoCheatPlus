package fr.neatmonster.nocheatplus.test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import fr.neatmonster.nocheatplus.NCPAPIProvider;
import fr.neatmonster.nocheatplus.PluginTests;
import fr.neatmonster.nocheatplus.compat.bukkit.MCAccessBukkit;
import fr.neatmonster.nocheatplus.config.ConfigFile;
import fr.neatmonster.nocheatplus.config.DefaultConfig;
import fr.neatmonster.nocheatplus.config.RawConfigFile;
import fr.neatmonster.nocheatplus.config.WorldConfigProvider;
import fr.neatmonster.nocheatplus.utilities.BlockProperties;

/**
 * Auxiliary classes packed in here.
 * @author dev1mc
 *
 */
public class BlockTests {

    public static class SimpleWorldConfigProvider <C extends RawConfigFile> implements WorldConfigProvider <C>{

        private final C config;

        public SimpleWorldConfigProvider(C config) {
            this.config = config;
        }

        @Override
        public C getDefaultConfig() {
            return config;
        }

        @Override
        public C getConfig(String worldName) {
            return config;
        }

        @Override
        public Collection<C> getAllConfigs() {
            final List<C> list = new ArrayList<C>();
            list.add(config);
            return list;
        }

    }

    public static class DefaultConfigWorldConfigProvider extends SimpleWorldConfigProvider<ConfigFile> {
        public DefaultConfigWorldConfigProvider() {
            super(new DefaultConfig());
        }
    }

    /**
     * Initialize BlockProperties with default config and Bukkit-API compliance :p.
     */
    public static void initBlockProperties() {
        PluginTests.setDummNoCheatPlusAPI(false);
        BlockProperties.init(NCPAPIProvider.getNoCheatPlusAPI().getMCAccess(), new DefaultConfigWorldConfigProvider());
    }

}
