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
package fr.neatmonster.nocheatplus.compat.registry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import fr.neatmonster.nocheatplus.NCPAPIProvider;
import fr.neatmonster.nocheatplus.NoCheatPlus;
import fr.neatmonster.nocheatplus.checks.inventory.FastConsume;
import fr.neatmonster.nocheatplus.checks.inventory.Gutenberg;
import fr.neatmonster.nocheatplus.checks.inventory.HotFixFallingBlockPortalEnter;
import fr.neatmonster.nocheatplus.checks.net.protocollib.ProtocolLibComponent;
import fr.neatmonster.nocheatplus.compat.versions.Activation;
import fr.neatmonster.nocheatplus.components.registry.activation.IActivation;
import fr.neatmonster.nocheatplus.components.registry.activation.IDescriptiveActivation;
import fr.neatmonster.nocheatplus.config.ConfPaths;
import fr.neatmonster.nocheatplus.config.ConfigManager;
import fr.neatmonster.nocheatplus.logging.StaticLog;
import fr.neatmonster.nocheatplus.logging.Streams;
import fr.neatmonster.nocheatplus.utilities.StringUtil;

/**
 * Default factory for add-in components which might only be available under certain circumstances.
 * 
 * @author mc_dev
 */
public class DefaultComponentFactory {

    private final IActivation protocolLibPresent = new Activation().pluginExist("ProtocolLib");
    private final List<IDescriptiveActivation> protocolLibActivation = new ArrayList<IDescriptiveActivation>();

    public DefaultComponentFactory() {
        for (Activation condition : Arrays.asList(
                new Activation()
                .neutralDescription("ProtocolLib 4.1 or later for Minecraft 1.8.x to 1.10.x")
                .pluginVersionGT("ProtocolLib", "4.1", true)
                .minecraftVersionBetween("1.8", true, "1.11", false)
                .advertise(true)
                ,
                new Activation()
                .neutralDescription("ProtocolLib 4.0.2 for Minecraft 1.10.x")
                .pluginVersionBetween("ProtocolLib", "4.0.2", true, "4.1", false)
                .minecraftVersionBetween("1.10", true, "1.11", false)
                ,
                new Activation()
                .neutralDescription("ProtocolLib 4.0.1 or 4.0.0 for Minecraft 1.9.x")
                .pluginVersionBetween("ProtocolLib", "4.0.0", true, "4.0.1", true)
                .minecraftVersionBetween("1.9", true, "1.10", false)
                ,
                new Activation()
                .neutralDescription("ProtocolLib 3.7.0 for Minecraft 1.7.x and earlier")
                .pluginVersionBetween("ProtocolLib", "3.7", true, "3.7.0", true)
                .minecraftVersionLT("1.10", false) // Allowed, but not necessarily recommended.
                .advertise(true)
                ,
                new Activation()
                .neutralDescription("ProtocolLib 3.6.5 or 3.6.4 for Minecraft 1.8.x")
                .pluginVersionBetween("ProtocolLib", "3.6.4", true, "3.6.5", true)
                .minecraftVersionBetween("1.8", true, "1.9", false)
                ,
                new Activation()
                .neutralDescription("ProtocolLib 3.6.6 for PaperSpigot 1.8.x")
                .pluginVersionEQ("ProtocolLib", "3.6.6")
                .serverVersionContainsIgnoreCase("paperspigot")
                .minecraftVersionBetween("1.8", true, "1.9", false)
                .advertise(true)
                ,
                new Activation()
                .neutralDescription("ProtocolLib 3.6.4 before Minecraft 1.9")
                .pluginVersionEQ("ProtocolLib", "3.6.4")
                .minecraftVersionBetween("1.2.5", true, "1.9", false)
                )) {
            protocolLibActivation.add(condition);
        };
    }

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
            NCPAPIProvider.getNoCheatPlusAPI().addFeatureTags("checks", Arrays.asList(FastConsume.class.getSimpleName()));
        }
        catch (Throwable t){
            StaticLog.logInfo("Inventory checks: FastConsume is not available.");
        }

        // Check: inventory.gutenberg.
        try {
            Gutenberg.testAvailability();
            available.add(new Gutenberg());
            NCPAPIProvider.getNoCheatPlusAPI().addFeatureTags("checks", Arrays.asList(Gutenberg.class.getSimpleName()));
        } catch (Throwable t) {
            StaticLog.logInfo("Inventory checks: Gutenberg is not available.");
        }

        // Hot fix: falling block end portal.
        try {
            HotFixFallingBlockPortalEnter.testAvailability();
            available.add(new HotFixFallingBlockPortalEnter());
            NCPAPIProvider.getNoCheatPlusAPI().addFeatureTags("checks", Arrays.asList(HotFixFallingBlockPortalEnter.class.getSimpleName()));
        }
        catch (RuntimeException e) {}

        // ProtocolLib dependencies.
        if (protocolLibPresent.isAvailable()) {
            // Check conditions.
            boolean protocolLibAvailable = false;
            for (final IActivation condition : protocolLibActivation) {
                if (condition.isAvailable()) {
                    protocolLibAvailable = true;
                    break;
                }
            }
            // Attempt to react.
            if (protocolLibAvailable) {
                try {
                    available.add(new ProtocolLibComponent(plugin));
                } catch (Throwable t){
                    StaticLog.logWarning("Failed to set up packet level access with ProtocolLib.");
                    if (ConfigManager.getConfigFile().getBoolean(ConfPaths.LOGGING_EXTENDED_STATUS)) {
                        NCPAPIProvider.getNoCheatPlusAPI().getLogManager().debug(Streams.INIT, t);
                    }
                }
            }
            else {
                List<String> parts = new LinkedList<String>();
                parts.add("Packet level access via ProtocolLib not available, supported configurations: ");
                for (IDescriptiveActivation cond : protocolLibActivation) {
                    if (cond.advertise()) {
                        parts.add(cond.getNeutralDescription());
                    }
                }
                StaticLog.logWarning(StringUtil.join(parts, " | "));
            }
        }
        else {
            StaticLog.logInfo("Packet level access: ProtocolLib is not available.");
        }

        return available;
    }
}
