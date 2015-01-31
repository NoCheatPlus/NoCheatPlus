package fr.neatmonster.nocheatplus.checks.fight;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.moving.LocationTrace.TraceEntry;
import fr.neatmonster.nocheatplus.utilities.TrigUtil;

/**
 * The Direction check will find out if a player tried to interact with something that's not in their field of view.
 */
public class Direction extends Check {

    /**
     * Instantiates a new direction check.
     */
    public Direction() {
        super(CheckType.FIGHT_DIRECTION);
    }

    /**
     * "Classic" check.
     * 
     * @param player
     *            the player
     * @param damaged
     *            the damaged
     * @return true, if successful
     */
    public boolean check(final Player player, final Location loc, final Entity damaged, final Location dLoc, final FightData data, final FightConfig cc) {
        boolean cancel = false;

        // Safeguard, if entity is complex, this check will fail due to giant and hard to define hitboxes.
        //        if (damaged instanceof EntityComplex || damaged instanceof EntityComplexPart)
        if (mcAccess.isComplexPart(damaged)) {
            return false;
        }

        // Find out how wide the entity is.
        final double width = mcAccess.getWidth(damaged);

        // entity.height is broken and will always be 0, therefore. Calculate height instead based on boundingBox.
        final double height = mcAccess.getHeight(damaged);

        // TODO: allow any hit on the y axis (might just adapt interface to use foot position + height)!

        // How far "off" is the player with their aim. We calculate from the players eye location and view direction to
        // the center of the target entity. If the line of sight is more too far off, "off" will be bigger than 0.
        final Vector direction = loc.getDirection();

        final double off;
        if (cc.directionStrict){
            off = TrigUtil.combinedDirectionCheck(loc, player.getEyeHeight(), direction, dLoc.getX(), dLoc.getY() + height / 2D, dLoc.getZ(), width, height, TrigUtil.DIRECTION_PRECISION, 80.0);
        }
        else{
            // Also take into account the angle.
            off = TrigUtil.directionCheck(loc, player.getEyeHeight(), direction, dLoc.getX(), dLoc.getY() + height / 2D, dLoc.getZ(), width, height, TrigUtil.DIRECTION_PRECISION);
        }

        if (off > 0.1) {
            // Player failed the check. Let's try to guess how far they were from looking directly to the entity...
            final Vector blockEyes = new Vector(dLoc.getX() - loc.getX(),  dLoc.getY() + height / 2D - loc.getY() - player.getEyeHeight(), dLoc.getZ() - loc.getZ());
            final double distance = blockEyes.crossProduct(direction).length() / direction.length();

            // Add the overall violation level of the check.
            data.directionVL += distance;

            // Execute whatever actions are associated with this check and the violation level and find out if we should
            // cancel the event.
            cancel = executeActions(player, data.directionVL, distance, cc.directionActions);

            if (cancel) {
                // Deal an attack penalty time.
                data.attackPenalty.applyPenalty(cc.directionPenalty);
            }
        } else {
            // Reward the player by lowering their violation level.
            data.directionVL *= 0.8D;
        }

        return cancel;
    }

    /**
     * Data context for iterating over TraceEntry instances.
     * @param player
     * @param loc
     * @param damaged
     * @param damagedLoc
     * @param data
     * @param cc
     * @return
     */
    public DirectionContext getContext(final Player player, final Location loc, final Entity damaged, final Location damagedLoc, final FightData data, final FightConfig cc, final SharedContext sharedContext) {
        final DirectionContext context = new DirectionContext();
        context.damagedComplex = mcAccess.isComplexPart(damaged);
        // Find out how wide the entity is.
        context.damagedWidth = mcAccess.getWidth(damaged);
        // entity.height is broken and will always be 0, therefore. Calculate height instead based on boundingBox.
        context.damagedHeight = sharedContext.damagedHeight;
        context.direction = loc.getDirection();
        context.lengthDirection = context.direction.length();
        return context;
    }

    /**
     * Check if the player fails the direction check, no change of FightData.
     * @param player
     * @param loc
     * @param damaged
     * @param dLoc
     * @param context
     * @param data
     * @param cc
     * @return
     */
    public boolean loopCheck(final Player player, final Location loc, final Entity damaged, final TraceEntry dLoc, final DirectionContext context, final FightData data, final FightConfig cc) {

        // Ignore complex entities for the moment.
        if (context.damagedComplex) {
            // TODO: Revise :p
            return false;
        }
        boolean cancel = false;

        // TODO: allow any hit on the y axis (might just adapt interface to use foot position + height)!

        // How far "off" is the player with their aim. We calculate from the players eye location and view direction to
        // the center of the target entity. If the line of sight is more too far off, "off" will be bigger than 0.

        final double off;
        if (cc.directionStrict){
            off = TrigUtil.combinedDirectionCheck(loc, player.getEyeHeight(), context.direction, dLoc.x, dLoc.y + context.damagedHeight / 2D, dLoc.z, context.damagedWidth, context.damagedHeight, TrigUtil.DIRECTION_PRECISION, 80.0);
        }
        else{
            // Also take into account the angle.
            off = TrigUtil.directionCheck(loc, player.getEyeHeight(), context.direction, dLoc.x, dLoc.y + context.damagedHeight / 2D, dLoc.z, context.damagedWidth, context.damagedHeight, TrigUtil.DIRECTION_PRECISION);
        }

        if (off > 0.1) {
            // Player failed the check. Let's try to guess how far they were from looking directly to the entity...
            final Vector blockEyes = new Vector(dLoc.x - loc.getX(),  dLoc.y + context.damagedHeight / 2D - loc.getY() - player.getEyeHeight(), dLoc.z - loc.getZ());
            final double distance = blockEyes.crossProduct(context.direction).length() / context.lengthDirection;
            context.minViolation = Math.min(context.minViolation, distance);
            cancel = true;
        }
        context.minResult = Math.min(context.minResult, off);

        return cancel;
    }

    /**
     * Apply changes to FightData according to check results (context), trigger violations.
     * @param player
     * @param loc
     * @param damaged
     * @param context
     * @param forceViolation
     * @param data
     * @param cc
     * @return
     */
    public boolean loopFinish(final Player player, final Location loc, final Entity damaged, final DirectionContext context, final boolean forceViolation, final FightData data, final FightConfig cc) {
        boolean cancel = false;
        final double off = forceViolation && context.minViolation != Double.MAX_VALUE ? context.minViolation : context.minResult;
        if (off == Double.MAX_VALUE) {
            return false;
        }
        else if (off > 0.1) {
            // Add the overall violation level of the check.
            data.directionVL += context.minViolation;

            // Execute whatever actions are associated with this check and the violation level and find out if we should
            // cancel the event.
            cancel = executeActions(player, data.directionVL, context.minViolation, cc.directionActions);

            if (cancel) {
                // Deal an attack penalty time.
                data.attackPenalty.applyPenalty(cc.directionPenalty);
            }
        }
        else {
            // Reward the player by lowering their violation level.
            data.directionVL *= 0.8D;
        }

        return cancel;
    }

}
