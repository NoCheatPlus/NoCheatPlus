package fr.neatmonster.nocheatplus.checks.blockplace;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.metrics.MetricsData;
import fr.neatmonster.nocheatplus.utilities.LagMeasureTask;

/*
 * MM""""""""`M                     dP   MM"""""""`YM dP                            
 * MM  mmmmmmmM                     88   MM  mmmmm  M 88                            
 * M'      MMMM .d8888b. .d8888b. d8888P M'        .M 88 .d8888b. .d8888b. .d8888b. 
 * MM  MMMMMMMM 88'  `88 Y8ooooo.   88   MM  MMMMMMMM 88 88'  `88 88'  `"" 88ooood8 
 * MM  MMMMMMMM 88.  .88       88   88   MM  MMMMMMMM 88 88.  .88 88.  ... 88.  ... 
 * MM  MMMMMMMM `88888P8 `88888P'   dP   MM  MMMMMMMM dP `88888P8 `88888P' `88888P' 
 * MMMMMMMMMMMM                          MMMMMMMMMMMM                               
 */
/**
 * A check used to verify if the player isn't placing blocks too quickly.
 */
public class FastPlace extends Check {

    /**
     * Instantiates a new fast place check.
     */
    public FastPlace() {
        super(CheckType.BLOCKPLACE_FASTPLACE);
    }

    /**
     * Checks a player.
     * 
     * @param player
     *            the player
     * @param block
     *            the block
     * @return true, if successful
     */
    public boolean check(final Player player, final Block block) {
        // Metrics data.
        MetricsData.addChecked(type);

        final BlockPlaceConfig cc = BlockPlaceConfig.getConfig(player);
        final BlockPlaceData data = BlockPlaceData.getData(player);

        boolean cancel = false;

        // Has the player placed blocks too quickly?
        if (data.fastPlaceLastTime != 0 && System.currentTimeMillis() - data.fastPlaceLastTime < cc.fastPlaceInterval) {
            if (!LagMeasureTask.skipCheck()) {
                if (data.fastPlaceLastRefused) {
                    final double difference = cc.fastPlaceInterval - System.currentTimeMillis()
                            + data.fastPlaceLastTime;

                    // He failed, increase his violation level.
                    data.fastPlaceVL += difference;

                    // Execute whatever actions are associated with this check and the violation level and find out if
                    // we should cancel the event.
                    cancel = executeActions(player, data.fastPlaceVL, difference, cc.fastPlaceActions);
                }

                data.fastPlaceLastRefused = true;
            }
        } else {
            // Reward him by lowering his violation level.
            data.fastPlaceVL *= 0.9D;
            data.fastPlaceLastRefused = false;
        }

        data.fastPlaceLastTime = System.currentTimeMillis();

        return cancel;
    }
}
