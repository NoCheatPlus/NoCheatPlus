package fr.neatmonster.nocheatplus.checks.fight;

import net.minecraft.server.EntityPlayer;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.NoCheatPlus;
import fr.neatmonster.nocheatplus.actions.ParameterName;
import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckEvent;
import fr.neatmonster.nocheatplus.players.Permissions;

/*
 * MM'"""""`MM                dP M"""""`'"""`YM                dP          
 * M' .mmm. `M                88 M  mm.  mm.  M                88          
 * M  MMMMMMMM .d8888b. .d888b88 M  MMM  MMM  M .d8888b. .d888b88 .d8888b. 
 * M  MMM   `M 88'  `88 88'  `88 M  MMM  MMM  M 88'  `88 88'  `88 88ooood8 
 * M. `MMM' .M 88.  .88 88.  .88 M  MMM  MMM  M 88.  .88 88.  .88 88.  ... 
 * MM.     .MM `88888P' `88888P8 M  MMM  MMM  M `88888P' `88888P8 `88888P' 
 * MMMMMMMMMMM                   MMMMMMMMMMMMMM                            
 */
/**
 * The GodMode check will find out if a player tried to stay invulnerable after being hit or after dying.
 */
public class GodMode extends Check {

    /**
     * The event triggered by this check.
     */
    public class GodModeEvent extends CheckEvent {

        /**
         * Instantiates a new god mode event.
         * 
         * @param player
         *            the player
         */
        public GodModeEvent(final Player player) {
            super(player);
        }
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

        // Check at most once a second.
        if (data.godModeLastTime < System.currentTimeMillis() - 1000L) {
            data.godModeLastTime = System.currentTimeMillis();

            final int age = player.getTicksLived();

            // How much older did he get?
            final int ageDelta = Math.max(0, age - data.godModeLastAge);

            if (player.getNoDamageTicks() > 0 && ageDelta < 15) {
                // He is invulnerable and didn't age fast enough, that costs some points.
                data.godModeBuffer -= 15 - ageDelta;

                // Still points left?
                if (data.godModeBuffer <= 0) {
                    // No, that means we can increase his violation level.
                    data.godModeVL -= data.godModeBuffer;

                    // Dispatch a god mode event (API).
                    final GodModeEvent e = new GodModeEvent(player);
                    Bukkit.getPluginManager().callEvent(e);

                    // Execute whatever actions are associated with this check and the violation level and find out if
                    // we should cancel the event.
                    cancel = !e.isCancelled() && executeActions(player, cc.godModeActions, data.godModeVL);
                }
            } else {
                // Give some new points, once a second.
                data.godModeBuffer += 15;

                // Decrease the violation level.
                data.godModeVL *= 0.95;
            }

            if (data.godModeBuffer < 0)
                // Can't have less than 0!
                data.godModeBuffer = 0;
            else if (data.godModeBuffer > 30)
                // And 30 is enough for simple lag situations.
                data.godModeBuffer = 30;

            // Start age counting from a new time.
            data.godModeLastAge = age;
        }

        return cancel;
    }

    /**
     * If a player apparently died, make sure he really dies after some time if he didn't already, by setting up a
     * Bukkit task.
     * 
     * @param player
     *            the player
     */
    public void death(final Player player) {
        // First check if the player is really dead (e.g. another plugin could have just fired an artificial event).
        if (player.getHealth() <= 0 && player.isDead())
            try {
                final EntityPlayer entity = ((CraftPlayer) player).getHandle();

                // Schedule a task to be executed in roughly 1.5 seconds.
                final NoCheatPlus plugin = (NoCheatPlus) Bukkit.getPluginManager().getPlugin("NoCheatPlus");
                Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {

                    @Override
                    public void run() {
                        try {
                            // Check again if the player should be dead, and if the game didn't mark him as dead.
                            if (entity.getHealth() <= 0 && !entity.dead) {
                                // Artificially "kill" him.
                                entity.deathTicks = 19;
                                entity.g();
                            }
                        } catch (final Exception e) {}
                    }
                }, 30);
            } catch (final Exception e) {}
    }

    /* (non-Javadoc)
     * @see fr.neatmonster.nocheatplus.checks.Check#getParameter(fr.neatmonster.nocheatplus.actions.ParameterName, org.bukkit.entity.Player)
     */
    @Override
    public String getParameter(final ParameterName wildcard, final Player player) {
        if (wildcard == ParameterName.VIOLATIONS)
            return String.valueOf(Math.round(FightData.getData(player).godModeVL));
        else
            return super.getParameter(wildcard, player);
    }

    /* (non-Javadoc)
     * @see fr.neatmonster.nocheatplus.checks.Check#isEnabled(org.bukkit.entity.Player)
     */
    @Override
    protected boolean isEnabled(final Player player) {
        return !player.hasPermission(Permissions.FIGHT_GODMODE) && FightConfig.getConfig(player).godModeCheck;
    }
}
