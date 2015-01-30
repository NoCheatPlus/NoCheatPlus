package fr.neatmonster.nocheatplus.checks.inventory;

import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.actions.ParameterName;
import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.ViolationData;

/**
 * The InstantEat check will find out if a player eats their food too fast.
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
        // Take time once.
        final long time = System.currentTimeMillis();

        final InventoryData data = InventoryData.getData(player);

        boolean cancel = false;

        // Hunger level change seems to not be the result of eating.
        if (data.instantEatFood == null || level <= player.getFoodLevel())
            return false;

        // Rough estimation about how long it should take to eat
        final long expectedTimeWhenEatingFinished = Math.max(data.instantEatInteract, data.lastClickTime) + 700L;

        if (data.instantEatInteract > 0 && expectedTimeWhenEatingFinished < time){
            // Acceptable, reduce VL to reward the player.
            data.instantEatVL *= 0.6D;
        }
        else if (data.instantEatInteract > time){
            // Security test, if time ran backwards.
        }
        else {
            final double difference = (expectedTimeWhenEatingFinished - time) / 100D;

            // Player was too fast, increase their violation level.
            data.instantEatVL += difference;

            // Execute whatever actions are associated with this check and the violation level and find out if we should
            // cancel the event.
            final ViolationData vd = new ViolationData(this, player, data.instantEatVL, difference, InventoryConfig.getConfig(player).instantEatActions);
            if (data.instantEatFood != null) {
                vd.setParameter(ParameterName.FOOD, data.instantEatFood.toString());
            }
            cancel = executeActions(vd);
        }

        data.instantEatInteract = 0;
        data.instantEatFood = null;
        return cancel;
    }

}
