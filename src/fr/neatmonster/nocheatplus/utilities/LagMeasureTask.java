package fr.neatmonster.nocheatplus.utilities;

import org.bukkit.Bukkit;

import fr.neatmonster.nocheatplus.NoCheatPlus;
import fr.neatmonster.nocheatplus.config.ConfPaths;
import fr.neatmonster.nocheatplus.config.ConfigManager;

/**
 * A task running in the background that measures tick time vs. real time
 * 
 */
public class LagMeasureTask implements Runnable {

    // private int ingameseconds = 1;
    private long    lastIngamesecondTime     = System.currentTimeMillis();
    private long    lastIngamesecondDuration = 2000L;
    private boolean skipCheck                = false;
    private int     lagMeasureTaskId         = -1;

    public void cancel() {
        if (lagMeasureTaskId != -1) {
            try {
                Bukkit.getServer().getScheduler().cancelTask(lagMeasureTaskId);
            } catch (final Exception e) {
                System.out.println("NoCheatPlus: Couldn't cancel LagMeasureTask: " + e.getMessage());
            }
            lagMeasureTaskId = -1;
        }
    }

    @Override
    public void run() {

        try {
            final boolean oldStatus = skipCheck;
            // If the previous second took to long, skip checks during
            // this second
            skipCheck = lastIngamesecondDuration > 2000;

            if (ConfigManager.getConfigFile().getBoolean(ConfPaths.LOGGING_DEBUGMESSAGES))
                if (oldStatus != skipCheck && skipCheck)
                    System.out.println("[NoCheatPlus] detected server lag, some checks will not work.");
                else if (oldStatus != skipCheck && !skipCheck)
                    System.out.println("[NoCheatPlus] server lag seems to have stopped, reenabling checks.");

            final long time = System.currentTimeMillis();
            lastIngamesecondDuration = time - lastIngamesecondTime;
            if (lastIngamesecondDuration < 1000)
                lastIngamesecondDuration = 1000;
            else if (lastIngamesecondDuration > 3600000)
                lastIngamesecondDuration = 3600000; // top limit of 1
                                                    // hour per "second"
            lastIngamesecondTime = time;
            // ingameseconds++;

            // Check if some data is outdated now and let it be removed
            // if (ingameseconds % 62 == 0)
            // NoCheatPlus.cleanDataMap();
        } catch (final Exception e) {
            // Just prevent this thread from dying for whatever reason
        }

    }

    public boolean skipCheck() {
        return skipCheck;
    }

    public void start() {
        // start measuring with a delay of 10 seconds
        lagMeasureTaskId = Bukkit.getServer().getScheduler()
                .scheduleSyncRepeatingTask(NoCheatPlus.instance, this, 20, 20);
    }
}
