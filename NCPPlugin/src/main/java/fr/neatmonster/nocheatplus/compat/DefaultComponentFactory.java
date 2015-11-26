package fr.neatmonster.nocheatplus.compat;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import fr.neatmonster.nocheatplus.NCPAPIProvider;
import fr.neatmonster.nocheatplus.NoCheatPlus;
import fr.neatmonster.nocheatplus.checks.inventory.FastConsume;
import fr.neatmonster.nocheatplus.checks.inventory.Gutenberg;
import fr.neatmonster.nocheatplus.checks.net.protocollib.ProtocolLibComponent;
import fr.neatmonster.nocheatplus.compat.versions.GenericVersion;
import fr.neatmonster.nocheatplus.compat.versions.ServerVersion;
import fr.neatmonster.nocheatplus.config.ConfPaths;
import fr.neatmonster.nocheatplus.config.ConfigManager;
import fr.neatmonster.nocheatplus.logging.StaticLog;

/**
 * Default factory for add-in components which might only be available under certain circumstances.
 * 
 * @author mc_dev
 */
public class DefaultComponentFactory {

    /**
     * This will be called from within the plugin in onEnable, after registration of all core listeners and components. After each components addition processQueuedSubComponentHolders() will be called to allow registries for further optional components.
     * @param plugin 
     * @return
     */
    public Collection<Object> getAvailableComponentsOnEnable(NoCheatPlus plugin){
        final List<Object> available = new LinkedList<Object>();

        // Add components (try-catch).
        // TODO: catch ClassNotFound, incompatibleXY rather !?

        // Check: inventory.fastconsume.
        try{
            // TODO: Static test methods !?
            FastConsume.testAvailability();
            available.add(new FastConsume());
            if (ConfigManager.isTrueForAnyConfig(ConfPaths.INVENTORY_FASTCONSUME_CHECK)) {
                NCPAPIProvider.getNoCheatPlusAPI().addFeatureTags("checks", Arrays.asList(FastConsume.class.getSimpleName()));
            }
        }
        catch (Throwable t){
            StaticLog.logInfo("Inventory checks: FastConsume is not available.");
        }

        // Check: inventory.gutenberg.
        try {
            Gutenberg.testAvailability();
            available.add(new Gutenberg());
            if (ConfigManager.isTrueForAnyConfig(ConfPaths.INVENTORY_GUTENBERG_CHECK)) {
                NCPAPIProvider.getNoCheatPlusAPI().addFeatureTags("checks", Arrays.asList(Gutenberg.class.getSimpleName()));
            }
        } catch (Throwable t) {
            StaticLog.logInfo("Inventory checks: Gutenberg is not available.");
        }

        // ProtocolLib dependencies.
        Plugin pluginProtocolLib = Bukkit.getPluginManager().getPlugin("ProtocolLib");
        try {
            if (ServerVersion.isMinecraftVersionBetween("1.8", true, "1.9", false) 
                    || ServerVersion.isMinecraftVersionBetween("1.2.5", true, "1.9", false)
                    && pluginProtocolLib != null && GenericVersion.compareVersions("3.6.4", pluginProtocolLib.getDescription().getVersion()) == 0) {
                available.add(new ProtocolLibComponent(plugin));
            } else {
                if (pluginProtocolLib != null) {
                    StaticLog.logWarning("Can't tell if the packet level hooks are compatible to the version of ProtocolLib in use.");
                }
                StaticLog.logInfo("Packet level access: ProtocolLib is not available.");
            }
        } catch (Throwable t){
            StaticLog.logInfo("Packet level access: ProtocolLib is not available.");
        }

        return available;
    }
}
