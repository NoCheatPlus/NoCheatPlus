package fr.neatmonster.nocheatplus.checks.blockplace;

import java.util.Locale;

import fr.neatmonster.nocheatplus.actions.ParameterName;
import fr.neatmonster.nocheatplus.players.NCPPlayer;
import fr.neatmonster.nocheatplus.players.informations.Statistics.Id;

/**
 * A check used to verify if the player isn't throwing projectiles too quickly
 * 
 */
public class ProjectileCheck extends BlockPlaceCheck {

    public ProjectileCheck() {
        super("projectile");
    }

    @Override
    public boolean check(final NCPPlayer player, final Object... args) {
        final BlockPlaceConfig cc = getConfig(player);
        final BlockPlaceData data = getData(player);

        boolean cancel = false;

        // Has the player thrown the projectiles too quickly
        if (data.lastProjectileTime != 0
                && System.currentTimeMillis() - data.lastProjectileTime < cc.projectileInterval) {
            if (data.previousProjectileRefused) {
                // He failed, increase vl and statistics
                data.projectileVL += cc.projectileInterval - System.currentTimeMillis() + data.lastProjectileTime;
                incrementStatistics(player, Id.BP_PROJECTILE, cc.projectileInterval - System.currentTimeMillis()
                        + data.lastProjectileTime);

                // Execute whatever actions are associated with this check and the
                // violation level and find out if we should cancel the event
                cancel = executeActions(player, cc.projectileActions, data.projectileVL);
            }
            data.previousProjectileRefused = true;
        } else {
            // Reward with lowering of the violation level
            data.projectileVL *= 0.90D;
            data.previousProjectileRefused = false;
        }

        data.lastProjectileTime = System.currentTimeMillis();

        return cancel;

    }

    @Override
    public String getParameter(final ParameterName wildcard, final NCPPlayer player) {

        if (wildcard == ParameterName.VIOLATIONS)
            return String.format(Locale.US, "%d", (int) getData(player).projectileVL);
        else
            return super.getParameter(wildcard, player);
    }
}
