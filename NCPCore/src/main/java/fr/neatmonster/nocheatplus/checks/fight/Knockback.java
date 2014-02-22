package fr.neatmonster.nocheatplus.checks.fight;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.utilities.TickTask;

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
        
        final long time = System.currentTimeMillis();

        // If the item has the knockback enchantment, do not check.
        if (player.getItemInHand().containsEnchantment(Enchantment.KNOCKBACK)
                || player.getItemInHand().containsEnchantment(Enchantment.ARROW_KNOCKBACK))
            return false;

        // How long ago has the player started sprinting?
        final long usedTime = (time - data.knockbackSprintTime);
        final long effectiveTime = (long) ((float)  usedTime * (cc.lag ? TickTask.getLag(usedTime): 1f));
        // Pretty rough: Completely skip on lag.
        if (data.knockbackSprintTime > 0L && effectiveTime < cc.knockbackInterval) {
            final double difference = cc.knockbackInterval - time + data.knockbackSprintTime;

            // Player failed the check, but this is influenced by lag, so don't do it if there was lag.
            // Increment the violation level
            data.knockbackVL += difference;

            // Execute whatever actions are associated with this check and the violation level and find out if we should
            // cancel the event.
            cancel = executeActions(player, data.knockbackVL, difference, cc.knockbackActions);
        }

        return cancel;
    }
}
