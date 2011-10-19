package cc.co.evenprime.bukkit.nocheat.debug;

import java.util.List;

import org.bukkit.World;

import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.config.cache.ConfigurationCache;
import cc.co.evenprime.bukkit.nocheat.events.EventManager;
import cc.co.evenprime.bukkit.nocheat.log.LogLevel;


public class ActiveCheckPrinter {

    public static void printActiveChecks(NoCheat plugin, List<EventManager> eventManagers) {
        
        boolean introPrinted = false;
        String intro = "[NoCheat] Active Checks: ";

        // Print active checks for NoCheat, if needed.
        for(World world : plugin.getServer().getWorlds()) {

            StringBuilder line = new StringBuilder("  ").append(world.getName()).append(": ");

            int length = line.length();

            ConfigurationCache cc = plugin.getConfig(world);

            if(!cc.debug.showchecks)
                continue;

            for(EventManager em : eventManagers) {
                if(em.getActiveChecks(cc).size() == 0)
                    continue;

                for(String active : em.getActiveChecks(cc)) {
                    line.append(active).append(' ');
                }

                if(!introPrinted) {
                    plugin.logToConsole(LogLevel.LOW, intro);
                    introPrinted = true;
                }

                plugin.logToConsole(LogLevel.LOW, line.toString());

                line = new StringBuilder(length);

                for(int i = 0; i < length; i++) {
                    line.append(' ');
                }
            }

        }
    }
}
