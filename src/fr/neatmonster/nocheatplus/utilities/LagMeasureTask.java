package fr.neatmonster.nocheatplus.utilities;

import org.bukkit.Bukkit;

import fr.neatmonster.nocheatplus.NoCheatPlus;
import fr.neatmonster.nocheatplus.config.ConfPaths;
import fr.neatmonster.nocheatplus.config.ConfigManager;
import fr.neatmonster.nocheatplus.metrics.MetricsData;

/*
 * M""MMMMMMMM                   M"""""`'"""`YM                                                       
 * M  MMMMMMMM                   M  mm.  mm.  M                                                       
 * M  MMMMMMMM .d8888b. .d8888b. M  MMM  MMM  M .d8888b. .d8888b. .d8888b. dP    dP 88d888b. .d8888b. 
 * M  MMMMMMMM 88'  `88 88'  `88 M  MMM  MMM  M 88ooood8 88'  `88 Y8ooooo. 88    88 88'  `88 88ooood8 
 * M  MMMMMMMM 88.  .88 88.  .88 M  MMM  MMM  M 88.  ... 88.  .88       88 88.  .88 88       88.  ... 
 * M         M `88888P8 `8888P88 M  MMM  MMM  M `88888P' `88888P8 `88888P' `88888P' dP       `88888P' 
 * MMMMMMMMMMM               .88 MMMMMMMMMMMMMM                                                       
 *                       d8888P                                                                       
 *                       
 * M""""""""M                   dP       
 * Mmmm  mmmM                   88       
 * MMMM  MMMM .d8888b. .d8888b. 88  .dP  
 * MMMM  MMMM 88'  `88 Y8ooooo. 88888"   
 * MMMM  MMMM 88.  .88       88 88  `8b. 
 * MMMM  MMMM `88888P8 `88888P' dP   `YP 
 * MMMMMMMMMM                            
 */
/**
 * A task running in the background that measures tick time vs. real time.
 */
public class LagMeasureTask implements Runnable {

    /** The instance of the class for a static access. */
    private static LagMeasureTask instance = new LagMeasureTask();

    /**
     * Cancel the task.
     */
    public static void cancel() {
        if (instance.lagMeasureTaskId != -1) {
            try {
                Bukkit.getServer().getScheduler().cancelTask(instance.lagMeasureTaskId);
            } catch (final Exception e) {
                System.out.println("[NoCheatPlus] Couldn't cancel LagMeasureTask: " + e.getMessage() + ".");
            }
            instance.lagMeasureTaskId = -1;
        }
    }

    /**
     * Returns if checking must be skipped (lag).
     * 
     * @return true, if successful
     */
    public static boolean skipCheck() {
        return instance.skipCheck;
    }

    /**
     * Start the task.
     * 
     * @param plugin
     *            the instance of NoCheatPlus
     */
    public static void start(final NoCheatPlus plugin) {
        instance.lagMeasureTaskId = Bukkit.getServer().getScheduler()
                .scheduleSyncRepeatingTask(plugin, instance, 20L, 20L);
    }

    /** The last in game second time. */
    private long    lastInGameSecondTime     = System.currentTimeMillis();

    /** The last in game second duration. */
    private long    lastInGameSecondDuration = 2000L;

    /** The lag measure task id. */
    private int     lagMeasureTaskId         = -1;

    /** The skip check. */
    private boolean skipCheck                = false;

    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
        try {
            final boolean oldStatus = skipCheck;
            // If the previous second took to long, skip checks during this second.
            skipCheck = lastInGameSecondDuration > 2000;

            // Metrics data.
            int ticks = (int) Math.round(20000D / lastInGameSecondDuration);
            if (ticks > 20)
                ticks = 20;
            MetricsData.addTicks(ticks);

            if (ConfigManager.getConfigFile().getBoolean(ConfPaths.LOGGING_DEBUG))
                if (oldStatus != skipCheck && skipCheck)
                    System.out.println("[NoCheatPlus] Detected server lag, some checks will not work.");
                else if (oldStatus != skipCheck && !skipCheck)
                    System.out.println("[NoCheatPlus] Server lag seems to have stopped, reenabling checks.");

            final long time = System.currentTimeMillis();
            lastInGameSecondDuration = time - lastInGameSecondTime;
            lastInGameSecondTime = time;
        } catch (final Exception e) {
            // Just prevent this thread from dying for whatever reason.
        }
    }
}
