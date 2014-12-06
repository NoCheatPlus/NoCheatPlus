package fr.neatmonster.nocheatplus.checks.moving;

import java.util.Locale;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.NCPAPIProvider;
import fr.neatmonster.nocheatplus.actions.ParameterName;
import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.ViolationData;
import fr.neatmonster.nocheatplus.logging.Streams;
import fr.neatmonster.nocheatplus.utilities.PlayerLocation;
import fr.neatmonster.nocheatplus.utilities.TrigUtil;

/**
 * A check designed for people that are allowed to fly. The complement to the "SurvivalFly", which is for people that
 * aren't allowed to fly, and therefore have tighter rules to obey.
 */
public class CreativeFly extends Check {

    /** The horizontal speed in creative mode. */
    private static final double HORIZONTAL_SPEED = 0.6D;

    /** The vertical speed in creative mode. */
    private static final double VERTICAL_SPEED   = 1D;
    
    /** Modifier for sprinting (1.8 feature). */
    public static final double modSprintFly = 1.92;

    /**
     * Instantiates a new creative fly check.
     */
    public CreativeFly() {
        super(CheckType.MOVING_CREATIVEFLY);
    }

    /**
     * 
     * @param player
     * @param from
     * @param to
     * @param data
     * @param cc
     * @param time Millis.
     * @return
     */
    public Location check(final Player player, final PlayerLocation from, final PlayerLocation to, final MovingData data, final MovingConfig cc, final long time) {

        // Ensure we have a set-back location.
        if (!data.hasSetBack()) {
            data.setSetBack(from);
        }

        // Before doing anything, do a basic height check to determine if players are flying too high.
        final int maximumHeight = cc.creativeFlyMaxHeight + player.getWorld().getMaxHeight();
        if (to.getY() > maximumHeight) {
            // TODO: USE velocity if possible.
            return new Location(player.getWorld(), data.getSetBackX(), Math.max(maximumHeight - 10D, to.getWorld().getMaxHeight()), data.getSetBackZ(), to.getYaw(), to.getPitch());
        }

        // Calculate some distances.
        final double xDistance = to.getX() - from.getX();
        final double yDistance = to.getY() - from.getY();
        final double zDistance = to.getZ() - from.getZ();

        // How far did the player move horizontally?
        final double hDistance = Math.sqrt(xDistance * xDistance + zDistance * zDistance);

        // Sprinting.
        final boolean sprinting = time <= data.timeSprinting + cc.sprintingGrace;

        // If the player is affected by potion of swiftness.

        final double speedModifier = mcAccess.getFasterMovementAmplifier(player);
        double fSpeed;

        // TODO: Make this configurable ! [Speed effect should not affect flying if not on ground.]
        if (speedModifier == Double.NEGATIVE_INFINITY) {
            fSpeed = 1D;
        }
        else {
            fSpeed = 1D + 0.2D * (speedModifier + 1D);
        }

        final boolean flying = player.isFlying();
        if (flying) {
            // TODO: Consider mechanichs for flying backwards.
            fSpeed *= data.flySpeed / 0.1;
            if (sprinting) {
                // TODO: Prevent for pre-1.8?
                fSpeed *= modSprintFly;
            }
        }
        else {
            // (Ignore sprinting here).
            fSpeed *= data.walkSpeed / 0.2;
        }

        final double limitH = cc.creativeFlyHorizontalSpeed / 100D * HORIZONTAL_SPEED * fSpeed;

        // Finally, determine how far the player went beyond the set limits.
        //        double resultH = Math.max(0.0D, hDistance - data.horizontalFreedom - limitH);
        double resultH = Math.max(0.0D, hDistance - limitH);

        // Check velocity.
        if (resultH > 0) {
            double hFreedom = data.getHorizontalFreedom();
            if (hFreedom < resultH) {
                // Use queued velocity if possible.
                hFreedom += data.useHorizontalVelocity(resultH - hFreedom);
            }
            if (hFreedom > 0.0) {
                resultH = Math.max(0.0, resultH - hFreedom);
            }
        }
        else {
            data.clearActiveHorVel(); // TODO: test/check !
        }

        data.bunnyhopDelay--;

        if (!flying && resultH > 0 && sprinting) {
            // TODO: Flying and bunnyhop ? <- 8 blocks per second - could be a case.
            // Try to treat it as a the "bunnyhop" problem. The bunnyhop problem is that landing and immediately jumping
            // again leads to a player moving almost twice as far in that step.
            // TODO: Real modeling for that kind of moving pattern (same with sf?).
            if (data.bunnyhopDelay <= 0 && resultH < 0.4D) {
                data.bunnyhopDelay = 9;
                resultH = 0D;
            }
        }

        resultH *= 100D;

        final double limitV = cc.creativeFlyVerticalSpeed / 100D * VERTICAL_SPEED; // * data.jumpAmplifier;

        // Super simple, just check distance compared to max distance vertical.
        // TODO: max descending speed ! [max fall speed, use maximum with speed or added ?]

        // TODO:_ signum considerations (aligned ...).
        //        double vDistanceAboveLimit = yDistance - data.getVerticalFreedom() - limitV;
        //        if (vDistanceAboveLimit > 0.0) {
        //            // TODO: consume / use vvel
        //        }
        //        final double resultV = (vDistanceAboveLimit - limitV) * 100D;
        //        final double result = Math.max(0.0, resultH) + Math.max(0D, resultV);
        // Old handling.
        final double resultV = (yDistance - data.verticalFreedom - limitV) * 100D;
        final double result = Math.max(0.0, resultH) + Math.max(0D, resultV);

        if (cc.debug) {
            outpuDebugMove(player, hDistance, limitH, yDistance, data.verticalFreedom, limitV);
        }

        // The player went to far, either horizontal or vertical.
        if (result > 0D) {
            // TODO: Get rid of creativeFlyPreviousRefused.
            if (data.creativeFlyPreviousRefused) {
                // Increment violation level.
                data.creativeFlyVL += result;

                // Execute whatever actions are associated with this check and the violation level and find out if we
                // should cancel the event.
                final ViolationData vd = new ViolationData(this, player, data.creativeFlyVL, result, cc.creativeFlyActions);
                if (vd.needsParameters()) {
                    vd.setParameter(ParameterName.LOCATION_FROM, String.format(Locale.US, "%.2f, %.2f, %.2f", from.getX(), from.getY(), from.getZ()));
                    vd.setParameter(ParameterName.LOCATION_TO, String.format(Locale.US, "%.2f, %.2f, %.2f", to.getX(), to.getY(), to.getZ()));
                    vd.setParameter(ParameterName.DISTANCE, String.format(Locale.US, "%.2f", TrigUtil.distance(from,  to)));
                }
                if (executeActions(vd)) {
                    // Compose a new location based on coordinates of "newTo" and viewing direction of "event.getTo()"
                    // to allow the player to look somewhere else despite getting pulled back by NoCheatPlus.
                    return data.getSetBack(to);
                }
            } else {
                data.creativeFlyPreviousRefused = true;
            }
        } else {
            data.creativeFlyPreviousRefused = false;
        }

        // Slowly reduce the violation level with each event.
        data.creativeFlyVL *= 0.97D;

        // If the event did not get cancelled, define a new setback point.
        data.setSetBack(to);
        return null;
    }

    private void outpuDebugMove(final Player player, final double hDistance, final double limitH, final double yDistance, final double verticalFreedom, final double limitV) {
        StringBuilder builder = new StringBuilder(350);
        builder.append(player.getName());
        builder.append(" CreativeFly hdist=" + hDistance + " hlimit=" + limitH + " ydist=" + yDistance + " vfreedom=" + verticalFreedom + " vlimit=" + limitV);
        NCPAPIProvider.getNoCheatPlusAPI().getLogManager().debug(Streams.TRACE_FILE, builder.toString());
    }
}
