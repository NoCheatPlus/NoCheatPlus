package fr.neatmonster.nocheatplus.checks.inventory;

import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.actions.ParameterName;
import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.ViolationData;

/*
 * M""M                     dP                       dP   MM""""""""`M            dP   
 * M  M                     88                       88   MM  mmmmmmmM            88   
 * M  M 88d888b. .d8888b. d8888P .d8888b. 88d888b. d8888P M`      MMMM .d8888b. d8888P 
 * M  M 88'  `88 Y8ooooo.   88   88'  `88 88'  `88   88   MM  MMMMMMMM 88'  `88   88   
 * M  M 88    88       88   88   88.  .88 88    88   88   MM  MMMMMMMM 88.  .88   88   
 * M  M dP    dP `88888P'   dP   `88888P8 dP    dP   dP   MM        .M `88888P8   dP   
 * MMMM                                                   MMMMMMMMMMMM                 
 */
/**
 * The InstantEat check will find out if a player eats his food too fast.
 */
public class InstantEat extends Check {

    /**
     * Instantiates a new instant eat check.
     */
    public InstantEat() {
        super(CheckType.INVENTORY_INSTANTEAT);
    }

    /**
     * Checks a player.
     * 
     * @param player
     *            the player
     * @param level
     *            the level
     * @return true, if successful
     */
    public boolean check(final Player player, final int level) {
        final InventoryData data = InventoryData.getData(player);

        boolean cancel = false;

        // Hunger level change seems to not be the result of eating.
        if (data.instantEatFood == null || level <= player.getFoodLevel())
            return false;

        // Rough estimation about how long it should take to eat
        final long expectedTimeWhenEatingFinished = data.instantEatLastTime + 700L;

        if (expectedTimeWhenEatingFinished < System.currentTimeMillis())
            // Acceptable, reduce VL to reward the player.
            data.instantEatVL *= 0.6D;
        else if (data.instantEatLastTime > System.currentTimeMillis())
            // Security test, if time ran backwards, reset.
            data.instantEatLastTime = 0;
        else {
            // Player was too fast, increase his violation level.
            data.instantEatVL += (expectedTimeWhenEatingFinished - System.currentTimeMillis()) / 100D;

            // Execute whatever actions are associated with this check and the violation level and find out if we should
            // cancel the event.
            cancel = executeActions(player, data.instantEatVL, InventoryConfig.getConfig(player).instantEatActions);
        }

        return cancel;
    }

    /* (non-Javadoc)
     * @see fr.neatmonster.nocheatplus.checks.Check#getParameter(fr.neatmonster.nocheatplus.actions.ParameterName, org.bukkit.entity.Player)
     */
    @Override
    public String getParameter(final ParameterName wildcard, final ViolationData violationData) {
        if (wildcard == ParameterName.FOOD)
            return InventoryData.getData(violationData.player).instantEatFood.toString();
        else
            return super.getParameter(wildcard, violationData);
    }
}
