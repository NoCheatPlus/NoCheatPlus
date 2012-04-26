package fr.neatmonster.nocheatplus.checks.fight;

import java.util.Locale;

import net.minecraft.server.Entity;
import net.minecraft.server.EntityComplex;
import net.minecraft.server.EntityComplexPart;
import fr.neatmonster.nocheatplus.NoCheatPlus;
import fr.neatmonster.nocheatplus.actions.ParameterName;
import fr.neatmonster.nocheatplus.checks.CheckUtils;
import fr.neatmonster.nocheatplus.players.NCPPlayer;
import fr.neatmonster.nocheatplus.players.informations.Permissions;
import fr.neatmonster.nocheatplus.players.informations.Statistics.Id;

/**
 * The DirectionCheck will find out if a player tried to interact with something
 * that's not in his field of view.
 * 
 */
public class DirectionCheck extends FightCheck {

    public DirectionCheck() {
        super("direction", Permissions.FIGHT_DIRECTION);
    }

    @Override
    public boolean check(final NCPPlayer player, final Object... args) {
        final FightConfig cc = getConfig(player);
        final FightData data = getData(player);

        boolean cancel = false;

        final long time = System.currentTimeMillis();

        // Get the damagee (entity that got hit)
        final Entity entity = data.damagee;

        // Safeguard, if entity is complex, this check will fail
        // due to giant and hard to define hitboxes
        if (entity instanceof EntityComplex || entity instanceof EntityComplexPart)
            return false;

        // Find out how wide the entity is
        final float width = entity.length > entity.width ? entity.length : entity.width;
        // entity.height is broken and will always be 0, therefore
        // calculate height instead based on boundingBox
        final double height = entity.boundingBox.e - entity.boundingBox.b;

        // How far "off" is the player with his aim. We calculate from the
        // players eye location and view direction to the center of the target
        // entity. If the line of sight is more too far off, "off" will be
        // bigger than 0
        final double off = CheckUtils.directionCheck(player, entity.locX, entity.locY + height / 2D, entity.locZ,
                width, height, cc.directionPrecision);

        if (off < 0.1D)
            // Player did probably nothing wrong
            // reduce violation counter to reward him
            data.directionVL *= 0.80D;
        else {
            // Player failed the check
            // Increment violation counter and statistics, but only if there
            // wasn't serious lag
            if (!NoCheatPlus.skipCheck()) {
                final double sqrt = Math.sqrt(off);
                data.directionVL += sqrt;
                incrementStatistics(player, Id.FI_DIRECTION, sqrt);
            }

            // Execute whatever actions are associated with this check and the
            // violation level and find out if we should cancel the event
            cancel = executeActions(player, cc.directionActions, data.directionVL);

            if (cancel)
                // if we should cancel, remember the current time too
                data.directionLastViolationTime = time;
        }

        // If the player is still in penalty time, cancel the event anyway
        if (data.directionLastViolationTime + cc.directionPenaltyTime > time) {
            // A safeguard to avoid people getting stuck in penalty time
            // indefinitely in case the system time of the server gets changed
            if (data.directionLastViolationTime > time)
                data.directionLastViolationTime = 0;

            // He is in penalty time, therefore request cancelling of the event
            return true;
        }

        return cancel;
    }

    @Override
    public String getParameter(final ParameterName wildcard, final NCPPlayer player) {

        if (wildcard == ParameterName.VIOLATIONS)
            return String.format(Locale.US, "%d", (int) getData(player).directionVL);
        else
            return super.getParameter(wildcard, player);
    }

    @Override
    public boolean isEnabled(final FightConfig cc) {
        return cc.directionCheck;
    }
}
