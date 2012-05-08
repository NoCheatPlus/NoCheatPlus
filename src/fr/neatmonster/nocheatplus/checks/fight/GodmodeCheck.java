package fr.neatmonster.nocheatplus.checks.fight;

import net.minecraft.server.EntityPlayer;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.entity.CraftPlayer;

import fr.neatmonster.nocheatplus.NoCheatPlus;
import fr.neatmonster.nocheatplus.actions.ParameterName;
import fr.neatmonster.nocheatplus.actions.types.ActionList;
import fr.neatmonster.nocheatplus.players.NCPPlayer;
import fr.neatmonster.nocheatplus.players.informations.Permissions;
import fr.neatmonster.nocheatplus.players.informations.Statistics;

/**
 * The Godmode Check will find out if a player tried to stay invulnerable after
 * being hit or after dying
 * 
 */
public class GodmodeCheck extends FightCheck {

    public class GodmodeCheckEvent extends FightEvent {

        public GodmodeCheckEvent(final GodmodeCheck check, final NCPPlayer player, final ActionList actions,
                final double vL) {
            super(check, player, actions, vL);
        }
    }

    public GodmodeCheck() {
        super("godmode", Permissions.FIGHT_GODMODE);
    }

    @Override
    public boolean check(final NCPPlayer player, final Object... args) {
        final FightConfig cc = getConfig(player);
        final FightData data = getData(player);

        boolean cancelled = false;

        final long time = System.currentTimeMillis();

        // Check at most once a second
        if (data.godmodeLastDamageTime + 1000L < time) {
            data.godmodeLastDamageTime = time;

            // How old is the player now?
            final int age = player.getBukkitPlayer().getTicksLived();
            // How much older did he get?
            final int ageDiff = Math.max(0, age - data.godmodeLastAge);
            // Is he invulnerable?
            final int nodamageTicks = player.getBukkitPlayer().getNoDamageTicks();

            if (nodamageTicks > 0 && ageDiff < 15) {
                // He is invulnerable and didn't age fast enough, that costs
                // some points
                data.godmodeBuffer -= 15 - ageDiff;

                // Still points left?
                if (data.godmodeBuffer <= 0) {
                    // No, that means VL and statistics increased
                    data.godmodeVL -= data.godmodeBuffer;
                    incrementStatistics(player, Statistics.Id.FI_GODMODE, -data.godmodeBuffer);

                    // Execute whatever actions are associated with this check and the
                    // violation level and find out if we should cancel the event
                    cancelled = executeActions(player, cc.godmodeActions, data.godmodeVL);
                }
            } else {
                // Give some new points, once a second
                data.godmodeBuffer += 15;
                data.godmodeVL *= 0.95;
            }

            if (data.godmodeBuffer < 0)
                // Can't have less than 0
                data.godmodeBuffer = 0;
            else if (data.godmodeBuffer > 30)
                // And 30 is enough for simple lag situations
                data.godmodeBuffer = 30;

            // Start age counting from a new time
            data.godmodeLastAge = age;
        }

        return cancelled;
    }

    /**
     * If a player apparently died, make sure he really dies after some time
     * if he didn't already, by setting up a Bukkit task
     * 
     * @param player
     *            The player
     */
    public void death(final CraftPlayer player) {
        // First check if the player is really dead (e.g. another plugin could
        // have just fired an artificial event)
        if (player.getHealth() <= 0 && player.isDead())
            try {
                final EntityPlayer entity = player.getHandle();

                // Schedule a task to be executed in roughly 1.5 seconds
                Bukkit.getScheduler().scheduleSyncDelayedTask(NoCheatPlus.instance, new Runnable() {

                    @Override
                    public void run() {
                        try {
                            // Check again if the player should be dead, and
                            // if the game didn't mark him as dead
                            if (entity.getHealth() <= 0 && !entity.dead) {
                                // Artificially "kill" him
                                entity.deathTicks = 19;
                                entity.a(true);
                            }
                        } catch (final Exception e) {}
                    }
                }, 30);
            } catch (final Exception e) {}
    }

    @Override
    protected boolean executeActions(final NCPPlayer player, final ActionList actionList, final double violationLevel) {
        final GodmodeCheckEvent event = new GodmodeCheckEvent(this, player, actionList, violationLevel);
        Bukkit.getPluginManager().callEvent(event);
        if (!event.isCancelled())
            return super.executeActions(player, event.getActions(), event.getVL());
        return false;
    }

    @Override
    public String getParameter(final ParameterName wildcard, final NCPPlayer player) {

        if (wildcard == ParameterName.VIOLATIONS)
            return String.valueOf(Math.round((int) getData(player).godmodeVL));
        else
            return super.getParameter(wildcard, player);
    }

    @Override
    public boolean isEnabled(final FightConfig cc) {
        return cc.godmodeCheck;
    }
}
