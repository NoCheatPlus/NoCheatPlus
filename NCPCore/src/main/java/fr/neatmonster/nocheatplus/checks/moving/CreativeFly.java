package fr.neatmonster.nocheatplus.checks.moving;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.actions.ParameterName;
import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.ViolationData;
import fr.neatmonster.nocheatplus.checks.moving.magic.Magic;
import fr.neatmonster.nocheatplus.checks.moving.model.ModelFlying;
import fr.neatmonster.nocheatplus.checks.moving.model.MoveData;
import fr.neatmonster.nocheatplus.compat.BridgeMisc;
import fr.neatmonster.nocheatplus.utilities.PlayerLocation;
import fr.neatmonster.nocheatplus.utilities.StringUtil;
import fr.neatmonster.nocheatplus.utilities.TrigUtil;

/**
 * A check designed for people that are allowed to fly. The complement to the "SurvivalFly", which is for people that
 * aren't allowed to fly, and therefore have tighter rules to obey.
 */
public class CreativeFly extends Check {

    private final List<String> tags = new LinkedList<String>();

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

        // Reset tags, just in case.
        tags.clear();

        // Some edge data for this move.
        final GameMode gameMode = player.getGameMode();
        final ModelFlying model = cc.getModelFlying(player, from);
        final MoveData thisMove = data.thisMove;
        thisMove.modelFlying = model;
        final MoveData lastMove = data.moveData.getFirst();

        // Before doing anything, do a basic height check to determine if players are flying too high.
        final double maximumHeight = model.maxHeight + player.getWorld().getMaxHeight();
        if (to.getY() > maximumHeight) {
            // TODO: USE velocity if possible.
            return new Location(player.getWorld(), data.getSetBackX(), Math.max(maximumHeight - 10D, to.getWorld().getMaxHeight()), data.getSetBackZ(), to.getYaw(), to.getPitch());
        }

        // Calculate some distances.
        final double yDistance = thisMove.yDistance;
        final double hDistance = thisMove.hDistance;

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

        final boolean flying = gameMode == BridgeMisc.GAME_MODE_SPECTATOR || player.isFlying();
        if (flying) {
            // TODO: Consider mechanichs for flying backwards.
            fSpeed *= data.flySpeed / 0.1;
            if (sprinting) {
                // TODO: Prevent for pre-1.8?
                fSpeed *= model.hModSprint;
            }
            tags.add("flying");
        }
        else {
            // (Ignore sprinting here).
            fSpeed *= data.walkSpeed / 0.2;
        }

        double limitH = model.hMod / 100D * ModelFlying.HORIZONTAL_SPEED * fSpeed;

        if (lastMove.toIsValid) {
            // TODO: Use last friction (as well)?
            // TODO: Test/adjust more.
            double frictionDist = lastMove.hDistance * Magic.FRICTION_MEDIUM_AIR;
            limitH = Math.max(frictionDist, limitH);
        }

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
        if (resultH > 0.0) {
            tags.add("hdist");
        }

        // TODO: max descending speed ! [max fall speed, use maximum with speed or added ?]
        double limitV = model.vMod / 100D * ModelFlying.VERTICAL_SPEED; // * data.jumpAmplifier;

        if (flying && yDistance > 0.0) {
            // Let fly speed apply with moving upwards.
            limitV *= data.flySpeed / 0.1;
        }

        if (lastMove.toIsValid) {
            // (Disregard gravity.)
            // TODO: Use last friction (as well)?
            double frictionDist = lastMove.yDistance * Magic.FRICTION_MEDIUM_AIR;
            if (!flying) {
                frictionDist -= Magic.GRAVITY_MIN;
            }
            limitV = Math.max(frictionDist, limitV);
        }

        final double resultV;
        if (yDistance > limitV && data.getOrUseVerticalVelocity(yDistance) != null) {
            resultV = 0.0;
        } else {
            resultV = (yDistance - limitV) * 100D;
        }

        if (resultV > 0.0) {
            tags.add("vdist");
        }

        final double result = Math.max(0.0, resultH) + Math.max(0D, resultV);

        if (data.debug) {
            outpuDebugMove(player, hDistance, limitH, yDistance, limitV, model, tags, data);
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
                    if (!tags.isEmpty()) {
                        vd.setParameter(ParameterName.TAGS, StringUtil.join(tags, "+"));
                    }
                }
                if (executeActions(vd).willCancel()) {
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

        // Adjust the set-back and other last distances.
        data.setSetBack(to);
        return null;
    }

    private void outpuDebugMove(final Player player, final double hDistance, final double limitH, final double yDistance, final double limitV, final ModelFlying model, final List<String> tags, final MovingData data) {
        StringBuilder builder = new StringBuilder(350);
        builder.append("hdist=" + hDistance + " hlimit=" + limitH + " ydist=" + yDistance + " vlimit=" + limitV);
        if (data.verVelUsed != null) {
            builder.append(" vvel_use=" + data.verVelUsed);
        }
        builder.append(" model=" + model.id);
        if (!tags.isEmpty()) {
            builder.append(" tags=");
            builder.append(StringUtil.join(tags, "+"));
        }
        debug(player, builder.toString());
    }

}
