package fr.neatmonster.nocheatplus.checks.fight;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.utilities.LagMeasureTask;

/*
 * M""MMMMM""M                            dP       dP                         dP       
 * M  MMMM' .M                            88       88                         88       
 * M       .MM 88d888b. .d8888b. .d8888b. 88  .dP  88d888b. .d8888b. .d8888b. 88  .dP  
 * M  MMMb. YM 88'  `88 88'  `88 88'  `"" 88888"   88'  `88 88'  `88 88'  `"" 88888"   
 * M  MMMMb  M 88    88 88.  .88 88.  ... 88  `8b. 88.  .88 88.  .88 88.  ... 88  `8b. 
 * M  MMMMM  M dP    dP `88888P' `88888P' dP   `YP 88Y8888' `88888P8 `88888P' dP   `YP 
 * MMMMMMMMMMM                                                                         
 */
/**
 * A check used to verify if players aren't "knockbacking" other players when it's not technically possible.
 */
public class Knockback extends Check {

    /**
     * Instantiates a new knockback check.
     */
    public Knockback() {
        super(CheckType.FIGHT_KNOCKBACK);
    }

    /**
     * Checks a player.
     * 
     * @param player
     *            the player
     * @return true, if successful
     */
    public boolean check(final Player player) {
        final FightConfig cc = FightConfig.getConfig(player);
        final FightData data = FightData.getData(player);

        boolean cancel = false;

        // If the item has the knockback enchantment, do not check.
        if (player.getItemInHand().containsEnchantment(Enchantment.KNOCKBACK)
                || player.getItemInHand().containsEnchantment(Enchantment.ARROW_KNOCKBACK))
            return false;

        // How long ago has the player started sprinting?
        if (data.knockbackSprintTime > 0L
                && System.currentTimeMillis() - data.knockbackSprintTime < cc.knockbackInterval) {
            final double difference = cc.knockbackInterval - System.currentTimeMillis() + data.knockbackSprintTime;

            // Player failed the check, but this is influenced by lag, so don't do it if there was lag.
            if (!LagMeasureTask.skipCheck())
                // Increment the violation level
                data.knockbackVL += difference;

            // Execute whatever actions are associated with this check and the violation level and find out if we should
            // cancel the event.
            cancel = executeActions(player, data.knockbackVL, difference, cc.knockbackActions);
        }

        return cancel;
    }
}
