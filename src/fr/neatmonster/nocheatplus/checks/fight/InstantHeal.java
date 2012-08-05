package fr.neatmonster.nocheatplus.checks.fight;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.actions.ParameterName;
import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckEvent;
import fr.neatmonster.nocheatplus.players.Permissions;

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
     * The event triggered by this check.
     */
    public class InstantHealEvent extends CheckEvent {

        /**
         * Instantiates a new instant heal event.
         * 
         * @param player
         *            the player
         */
        public InstantHealEvent(final Player player) {
            super(player);
        }
    }

    /**
     * Check a player.
     * 
     * @param player
     *            the player
     * @return true, if successful
     */
    public boolean check(final Player player) {
        final FightConfig cc = FightConfig.getConfig(player);
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
            data.instantHealBuffer = 0;

            // Dispatch an instant heal event (API).
            final InstantHealEvent e = new InstantHealEvent(player);
            Bukkit.getPluginManager().callEvent(e);

            // Execute whatever actions are associated with this check and the violation level and find out if we should
            // cancel the event.
            cancel = !e.isCancelled() && executeActions(player, cc.instantHealActions, data.instantHealVL);
        } else
            // Decrease the violation level.
            data.instantHealVL *= 0.9D;

        // Buffer can't be bigger than 2 seconds.
        if (data.instantHealBuffer > 2000)
            data.instantHealBuffer = 2000;

        if (!cancel)
            // New reference time.
            data.instantHealLastTime = System.currentTimeMillis();

        return cancel;
    }

    /* (non-Javadoc)
     * @see fr.neatmonster.nocheatplus.checks.Check#getParameter(fr.neatmonster.nocheatplus.actions.ParameterName, org.bukkit.entity.Player)
     */
    @Override
    public String getParameter(final ParameterName wildcard, final Player player) {
        if (wildcard == ParameterName.VIOLATIONS)
            return String.valueOf(Math.round(FightData.getData(player).instantHealVL));
        else
            return super.getParameter(wildcard, player);
    }

    /* (non-Javadoc)
     * @see fr.neatmonster.nocheatplus.checks.Check#isEnabled(org.bukkit.entity.Player)
     */
    @Override
    protected boolean isEnabled(final Player player) {
        return !player.hasPermission(Permissions.FIGHT_INSTANTHEAL) && FightConfig.getConfig(player).instantHealCheck;
    }
}
