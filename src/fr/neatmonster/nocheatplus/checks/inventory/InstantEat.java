package fr.neatmonster.nocheatplus.checks.inventory;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.actions.ParameterName;
import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckEvent;
import fr.neatmonster.nocheatplus.players.Permissions;

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
     * The event triggered by this check.
     */
    public class InstantEatEvent extends CheckEvent {

        /**
         * Instantiates a new instant eat event.
         * 
         * @param player
         *            the player
         */
        public InstantEatEvent(final Player player) {
            super(player);
        }
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
        final InventoryConfig cc = InventoryConfig.getConfig(player);
        final InventoryData data = InventoryData.getData(player);

        boolean cancel = false;

        // Hunger level change seems to not be the result of eating.
        if (data.instantEatFood == null || level <= player.getFoodLevel())
            return false;

        // rough estimation about how long it should take to eat
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

            // Dispatch an instant eat event (API).
            final InstantEatEvent e = new InstantEatEvent(player);
            Bukkit.getPluginManager().callEvent(e);

            // Execute whatever actions are associated with this check and the violation level and find out if we should
            // cancel the event.
            cancel = !e.isCancelled() && executeActions(player, cc.instantEatActions, data.instantEatVL);
        }

        return cancel;
    }

    /* (non-Javadoc)
     * @see fr.neatmonster.nocheatplus.checks.Check#getParameter(fr.neatmonster.nocheatplus.actions.ParameterName, org.bukkit.entity.Player)
     */
    @Override
    public String getParameter(final ParameterName wildcard, final Player player) {
        if (wildcard == ParameterName.VIOLATIONS)
            return String.valueOf(Math.round(InventoryData.getData(player).instantEatVL));
        else if (wildcard == ParameterName.FOOD)
            return InventoryData.getData(player).instantEatFood.toString();
        else
            return super.getParameter(wildcard, player);
    }

    /* (non-Javadoc)
     * @see fr.neatmonster.nocheatplus.checks.Check#isEnabled(org.bukkit.entity.Player)
     */
    @Override
    protected boolean isEnabled(final Player player) {
        return !player.hasPermission(Permissions.INVENTORY_INSTANTEAT)
                && InventoryConfig.getConfig(player).instantEatCheck;
    }
}
