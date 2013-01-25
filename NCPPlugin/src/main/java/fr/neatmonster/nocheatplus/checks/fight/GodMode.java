package fr.neatmonster.nocheatplus.checks.fight;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.NoCheatPlus;
import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;

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
     * Instantiates a new god mode check.
     */
    public GodMode() {
        super(CheckType.FIGHT_GODMODE);
    }

    /**
     * Checks a player.
     * 
     * @param player
     *            the player
     * @return true, if successful
     */
    public boolean check(final Player player) {
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

                    // Execute whatever actions are associated with this check and the violation level and find out if
                    // we should cancel the event.
                    cancel = executeActions(player, data.godModeVL, -data.godModeBuffer,
                            FightConfig.getConfig(player).godModeActions);
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
    	// TODO: Is this still relevant ?
        // First check if the player is really dead (e.g. another plugin could have just fired an artificial event).
        if (player.getHealth() <= 0 && player.isDead())
            try {
                // Schedule a task to be executed in roughly 1.5 seconds.
                final NoCheatPlus plugin = (NoCheatPlus) Bukkit.getPluginManager().getPlugin("NoCheatPlus");
                Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                    @Override
                    public void run() {
                        try {
                            // Check again if the player should be dead, and if the game didn't mark him as dead.
                            if (mcAccess.shouldBeZombie(player)){
                                // Artificially "kill" him.
                            	mcAccess.setDead(player, 19);
                            }
                        } catch (final Exception e) {}
                    }
                }, 30);
            } catch (final Exception e) {}
    }
}
