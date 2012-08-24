package fr.neatmonster.nocheatplus.checks.inventory;

import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;

/*
 * MM""""""""`M                     dP   MM'""""'YMM dP oo          dP       
 * MM  mmmmmmmM                     88   M' .mmm. `M 88             88       
 * M'      MMMM .d8888b. .d8888b. d8888P M  MMMMMooM 88 dP .d8888b. 88  .dP  
 * MM  MMMMMMMM 88'  `88 Y8ooooo.   88   M  MMMMMMMM 88 88 88'  `"" 88888"   
 * MM  MMMMMMMM 88.  .88       88   88   M. `MMM' .M 88 88 88.  ... 88  `8b. 
 * MM  MMMMMMMM `88888P8 `88888P'   dP   MM.     .dM dP dP `88888P' dP   `YP 
 * MMMMMMMMMMMM                          MMMMMMMMMMM                         
 */
/**
 * The FastClick check will prevents players from taking automatically all items from any inventory (chests, etc.).
 */
public class FastClick extends Check {

    /**
     * Instantiates a new fast click check.
     */
    public FastClick() {
        super(CheckType.INVENTORY_FASTCLICK);
    }

    /**
     * Checks a player.
     * 
     * @param player
     *            the player
     * @return true, if successful
     */
    public boolean check(final Player player) {
        final InventoryData data = InventoryData.getData(player);

        boolean cancel = false;

        // If the last inventory click has been made within 45 milliseconds.
        if (System.currentTimeMillis() - data.fastClickLastTime < 45L) {
            if (data.fastClickLastCancelled) {

                // Calculate the difference between the limit and the time elapsed.
                final double difference = 45L - System.currentTimeMillis() + data.fastClickLastTime;

                // Increment the violation level.
                data.fastClickVL += difference;

                // Find out if we need to cancel the event.
                cancel = executeActions(player, data.fastClickVL, difference,
                        InventoryConfig.getConfig(player).fastClickActions);
            } else
                data.fastClickLastCancelled = true;
        } else {
            data.fastClickLastCancelled = false;

            // Reduce the violation level.
            data.fastClickVL *= 0.98D;
        }

        // Remember the current time.s
        data.fastClickLastTime = System.currentTimeMillis();

        return cancel;
    }
}
