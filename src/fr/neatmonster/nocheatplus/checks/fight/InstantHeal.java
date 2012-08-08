package fr.neatmonster.nocheatplus.checks.fight;

import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;

/*
 * M""M                     dP                       dP   M""MMMMM""MM                   dP 
 * M  M                     88                       88   M  MMMMM  MM                   88 
 * M  M 88d888b. .d8888b. d8888P .d8888b. 88d888b. d8888P M         `M .d8888b. .d8888b. 88 
 * M  M 88'  `88 Y8ooooo.   88   88'  `88 88'  `88   88   M  MMMMM  MM 88ooood8 88'  `88 88 
 * M  M 88    88       88   88   88.  .88 88    88   88   M  MMMMM  MM 88.  ... 88.  .88 88 
 * M  M dP    dP `88888P'   dP   `88888P8 dP    dP   dP   M  MMMMM  MM `88888P' `88888P8 dP 
 * MMMM                                                   MMMMMMMMMMMM                      
 */
/**
 * The InstantHeal check should find out if a player tried to artificially accelerate the health regeneration by food.
 */
public class InstantHeal extends Check {

    /**
     * Instantiates a new instant heal check.
     */
    public InstantHeal() {
        super(CheckType.FIGHT_INSTANTHEAL);
    }

    /**
     * Check a player.
     * 
     * @param player
     *            the player
     * @return true, if successful
     */
    public boolean check(final Player player) {
        final FightData data = FightData.getData(player);

        boolean cancel = false;

        // Security check if system time ran backwards.
        if (data.instantHealLastTime > System.currentTimeMillis()) {
            data.instantHealLastTime = 0L;
            return false;
        }

        final long delta = System.currentTimeMillis() - (data.instantHealLastTime + 3500L);
        data.instantHealBuffer += delta;

        if (data.instantHealBuffer < 0) {
            // Buffer has been fully consumed, increase the player's violation level;
            data.instantHealVL -= data.instantHealBuffer / 1000D;

            // Reset the buffer.
            data.instantHealBuffer = 0L;

            // Execute whatever actions are associated with this check and the violation level and find out if we should
            // cancel the event.
            cancel = executeActions(player);
        } else
            // Decrease the violation level.
            data.instantHealVL *= 0.9D;

        // Buffer can't be bigger than 2 seconds.
        if (data.instantHealBuffer > 2000L)
            data.instantHealBuffer = 2000L;

        if (!cancel)
            // New reference time.
            data.instantHealLastTime = System.currentTimeMillis();

        return cancel;
    }
}
