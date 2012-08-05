package fr.neatmonster.nocheatplus.checks.fight;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import fr.neatmonster.nocheatplus.actions.ParameterName;
import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckEvent;
import fr.neatmonster.nocheatplus.players.Permissions;
import fr.neatmonster.nocheatplus.utilities.LagMeasureTask;
import fr.neatmonster.nocheatplus.utilities.PlayerLocation;

/*
 * MM'""""'YMM          oo   dP   oo                   dP 
 * M' .mmm. `M               88                        88 
 * M  MMMMMooM 88d888b. dP d8888P dP .d8888b. .d8888b. 88 
 * M  MMMMMMMM 88'  `88 88   88   88 88'  `"" 88'  `88 88 
 * M. `MMM' .M 88       88   88   88 88.  ... 88.  .88 88 
 * MM.     .dM dP       dP   dP   dP `88888P' `88888P8 dP 
 * MMMMMMMMMMM                                            
 */
/**
 * A check used to verify that critical hits done by players are legit.
 */
public class Critical extends Check {

    public class CriticalEvent extends CheckEvent {

        public CriticalEvent(final Player player) {
            super(player);
        }
    }

    public boolean check(final Player player) {
        final FightConfig cc = FightConfig.getConfig(player);
        final FightData data = FightData.getData(player);

        boolean cancel = false;

        // We'll need the PlayerLocation to know some important stuff.
        final PlayerLocation location = new PlayerLocation(player.getLocation(), player);

        // Check if the hit was a critical hit (positive fall distance, entity in the air, not on ladder, not in liquid
        // and without blindness effect).
        if (player.getFallDistance() > 0f && !location.isOnGround() && !location.isOnLadder() && !location.isInLiquid()
                && !player.hasPotionEffect(PotionEffectType.BLINDNESS))

            // It was a critical hit, now check if the player has jumped or has sent a packet to mislead the server.
            if (player.getFallDistance() < cc.criticalFallDistance
                    || Math.abs(player.getVelocity().getY()) < cc.criticalVelocity) {
                final double deltaFallDistance = (cc.criticalFallDistance - player.getFallDistance())
                        / cc.criticalFallDistance;
                final double deltaVelocity = (cc.criticalVelocity - Math.abs(player.getVelocity().getY()))
                        / cc.criticalVelocity;
                final double delta = deltaFallDistance > 0D ? deltaFallDistance
                        : 0D + deltaVelocity > 0D ? deltaVelocity : 0D;

                // Player failed the check, but this is influenced by lag so don't do it if there was lag.
                if (!LagMeasureTask.skipCheck())
                    // Increment the violation level.
                    data.criticalVL += delta;

                // Dispatch a critical event (API).
                final CriticalEvent e = new CriticalEvent(player);
                Bukkit.getPluginManager().callEvent(e);

                // Execute whatever actions are associated with this check and the violation level and find out if we
                // should cancel the event.
                cancel = !e.isCancelled() && executeActions(player, cc.criticalActions, data.criticalVL);
            }

        return cancel;
    }

    @Override
    public String getParameter(final ParameterName wildcard, final Player player) {
        if (wildcard == ParameterName.VIOLATIONS)
            return String.valueOf(Math.round(FightData.getData(player).criticalVL));
        else
            return super.getParameter(wildcard, player);
    }

    @Override
    protected boolean isEnabled(final Player player) {
        return !player.hasPermission(Permissions.FIGHT_CRITICAL) && FightConfig.getConfig(player).criticalCheck;
    }
}
