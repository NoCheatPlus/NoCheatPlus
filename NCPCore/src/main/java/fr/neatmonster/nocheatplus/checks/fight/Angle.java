package fr.neatmonster.nocheatplus.checks.fight;

import java.util.Iterator;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.utilities.TickTask;
import fr.neatmonster.nocheatplus.utilities.TrigUtil;

/**
 * A check used to verify if the player isn't using a forcefield in order to attack multiple entities at the same time.
 * 
 * Thanks @asofold for the original idea!
 */
public class Angle extends Check {

    public static class AttackLocation {
        public final double x, y, z;
        public final float yaw;
        public long time;
        public final UUID damagedId;
        /** Squared distance to the last location (0 if none given). */
        public final double distSqLast;
        /** Difference in yaw to the last location (0 if none given). */
        public final double yawDiffLast;
        /** Time difference to the last location (0 if none given). */
        public final long timeDiff;
        /** If the id differs from the last damaged entity (true if no lastLoc is given). */
        public final boolean idDiffLast;
        public AttackLocation(final Location loc, final UUID damagedId, final long time, final AttackLocation lastLoc) {
            x = loc.getX();
            y = loc.getY();
            z = loc.getZ();
            yaw = loc.getYaw();
            this.time = time;
            this.damagedId = damagedId;

            if (lastLoc != null) {
                distSqLast = TrigUtil.distanceSquared(x, y, z, lastLoc.x, lastLoc.y, lastLoc.z);
                yawDiffLast = TrigUtil.yawDiff(yaw, lastLoc.yaw);
                timeDiff = Math.max(0L, time - lastLoc.time);
                idDiffLast = !damagedId.equals(lastLoc.damagedId);
            } else {
                distSqLast = 0.0;
                yawDiffLast = 0f;
                timeDiff = 0L;
                idDiffLast = true;
            }
        }
    }

    public static long maxTimeDiff = 1000L;

    /**
     * Instantiates a new angle check.
     */
    public Angle() {
        super(CheckType.FIGHT_ANGLE);
    }

    /**
     * The Angle check.
     * @param player
     * @param loc Location of the player.
     * @param worldChanged
     * @param data
     * @param cc
     * @return
     */
    public boolean check(final Player player, final Location loc, final Entity damagedEntity, final boolean worldChanged, final FightData data, final FightConfig cc) {

        if (worldChanged){
            data.angleHits.clear();
        }

        boolean cancel = false;

        // Quick check for expiration of all entries.
        final long time = System.currentTimeMillis();
        AttackLocation lastLoc = data.angleHits.isEmpty() ? null : data.angleHits.getLast();
        if (lastLoc != null && time - lastLoc.time > maxTimeDiff) {
            data.angleHits.clear();
            lastLoc = null;
        }

        // Add the new location.
        data.angleHits.add(new AttackLocation(loc, damagedEntity.getUniqueId(), System.currentTimeMillis(), lastLoc));

        // Calculate the sums of differences.
        double deltaMove = 0D;
        long deltaTime = 0L;
        float deltaYaw = 0f;
        int deltaSwitchTarget = 0;
        final Iterator<AttackLocation> it = data.angleHits.iterator();
        while (it.hasNext()) {
            final AttackLocation refLoc = it.next();
            if (time - refLoc.time > maxTimeDiff) {
                it.remove();
                continue;
            }
            deltaMove += refLoc.distSqLast;
            deltaYaw += Math.abs(refLoc.yawDiffLast);
            deltaTime += refLoc.timeDiff;
            deltaSwitchTarget += refLoc.idDiffLast ? 1 : 0;
        }

        // Check if there is enough data present.
        if (data.angleHits.size() < 2) {
            return false;
        }
        
        final double n = (double) (data.angleHits.size() - 1);

        // Let's calculate the average move.
        final double averageMove = deltaMove / n;

        // And the average time elapsed.
        final double averageTime = (double) deltaTime / n;

        // And the average yaw delta.
        final double averageYaw = (double) deltaYaw / n;

        // Average target switching.
        final double averageSwitching = (double) deltaSwitchTarget / n;

        // Declare the variable.
        double violation = 0.0;

        // If the average move is between 0 and 0.2 block(s), add it to the violation.
        if (averageMove >= 0.0 && averageMove < 0.2D) {
            violation += 20.0 * (0.2 - averageMove) / 0.2;
        }

        // If the average time elapsed is between 0 and 150 millisecond(s), add it to the violation.
        if (averageTime >= 0.0 && averageTime < 150.0) {
            violation += 30.0 * (150.0 - averageTime) / 150.0;
        }

        // If the average difference of yaw is superior to 50 degrees, add it to the violation.
        if (averageYaw > 50.0) {
            violation += 30.0 * averageYaw / 180.0;
        }

        if (averageSwitching > 0.0) {
            violation += 20.0 * averageSwitching;
        }

        // Is the violation is superior to the threshold defined in the configuration?
        if (violation > cc.angleThreshold) {
            // Has the server lagged?
            if (TickTask.getLag(maxTimeDiff, true) < 1.5f){
                // TODO: 1.5 is a fantasy value.
                // If it hasn't, increment the violation level.
                data.angleVL += violation;
            }

            // Execute whatever actions are associated with this check and the violation level and find out if we should
            // cancel the event.
            cancel = executeActions(player, data.angleVL, violation, cc.angleActions);
        } else {
            // Reward the player by lowering their violation level.
            data.angleVL *= 0.98D;            
        }

        return cancel;
    }
}
