/*
 * This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package fr.neatmonster.nocheatplus.checks.fight;


import fr.neatmonster.nocheatplus.permissions.PermissionCache;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Giant;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.combined.Improbable;
import fr.neatmonster.nocheatplus.checks.moving.location.tracking.LocationTrace.ITraceEntry;
import fr.neatmonster.nocheatplus.permissions.Permissions;
import fr.neatmonster.nocheatplus.utilities.StringUtil;
import fr.neatmonster.nocheatplus.utilities.TickTask;
import fr.neatmonster.nocheatplus.utilities.location.TrigUtil;

/**
 * The Reach check will find out if a player interacts with something that's too far away.
 */
public class Reach extends Check {

    /** The maximum distance allowed to interact with an entity in creative mode. */
    public static final double CREATIVE_DISTANCE = 6D;

    /** Additum for distance, based on entity. */
    private static double getDistMod(final Entity damaged) {
        // Handle the EnderDragon differently.
        if (damaged instanceof EnderDragon)
            return 6.5D;
        else if (damaged instanceof Giant){
            return 1.5D;
        }
        else return 0;
    }

    /**
     * Instantiates a new reach check.
     */
    public Reach() {
        super(CheckType.FIGHT_REACH);
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
    public boolean check(final Player player, final Location pLoc, 
            final Entity damaged, final boolean damagedIsFake, final Location dRef, 
            final FightData data, final FightConfig cc) {
        boolean cancel = false;

        // The maximum distance allowed to interact with an entity in survival mode.
        final double SURVIVAL_DISTANCE = cc.reachSurvivalDistance; // 4.4D;
        // Amount which can be reduced by reach adaption.
        final double DYNAMIC_RANGE = cc.reachReduceDistance; // 0.9
        // Adaption amount for dynamic range.
        final double DYNAMIC_STEP = cc.reachReduceStep / SURVIVAL_DISTANCE; // 0.15

        final double distanceLimit = player.getGameMode() == GameMode.CREATIVE ? CREATIVE_DISTANCE : SURVIVAL_DISTANCE + getDistMod(damaged);
        final double distanceMin = (distanceLimit - DYNAMIC_RANGE) / distanceLimit;

        final double height = damagedIsFake ? (damaged instanceof LivingEntity ? ((LivingEntity) damaged).getEyeHeight() : 1.75) : mcAccess.getHandle().getHeight(damaged);

        // Refine y position.
        // TODO: Make a little more accurate by counting in the actual bounding box.
        final double pY = pLoc.getY() + player.getEyeHeight();
        final double dY = dRef.getY();
        if (pY <= dY); // Keep the foot level y.
        else if (pY >= dY + height) dRef.setY(dY + height); // Highest ref y.
        else dRef.setY(pY); // Level with damaged.

        final Vector pRel = dRef.toVector().subtract(pLoc.toVector().setY(pY)); // TODO: Run calculations on numbers only :p.

        // Distance is calculated from eye location to center of targeted. If the player is further away from their target
        // than allowed, the difference will be assigned to "distance".
        final double lenpRel = pRel.length();

        double violation = lenpRel - distanceLimit;

        final double reachMod = data.reachMod; 

        if (violation > 0) {
            // They failed, increment violation level. This is influenced by lag, so don't do it if there was lag.
            if (TickTask.getLag(1000, true) < 1.5f){
                // TODO: 1.5 is a fantasy value.
                data.reachVL += violation;
            }

            // Execute whatever actions are associated with this check and the violation level and find out if we should
            // cancel the event.
            cancel = executeActions(player, data.reachVL, violation, cc.reachActions).willCancel();
            if (Improbable.check(player, (float) violation / 2f, System.currentTimeMillis(), "fight.reach")){
                cancel = true;
            }
            if (cancel && cc.reachPenalty > 0){
                // Apply an attack penalty time.
                data.attackPenalty.applyPenalty(cc.reachPenalty);
            }
        }
        else if (lenpRel - distanceLimit * reachMod > 0){
            // Silent cancel.
            if (cc.reachPenalty > 0) {
                data.attackPenalty.applyPenalty(cc.reachPenalty / 2);
            }
            cancel = true;
            Improbable.feed(player, (float) (lenpRel - distanceLimit * reachMod) / 4f, System.currentTimeMillis());
        }
        else{
            // Player passed the check, reward them.
            data.reachVL *= 0.8D;

        }

        if (!cc.reachReduce){
            data.reachMod = 1d;
        }
        else if (lenpRel > distanceLimit - DYNAMIC_RANGE){
            data.reachMod = Math.max(distanceMin, data.reachMod - DYNAMIC_STEP);
        }
        else{
            data.reachMod = Math.min(1.0, data.reachMod + DYNAMIC_STEP);
        }

        if (data.debug && PermissionCache.hasPermission(player, Permissions.ADMINISTRATION_DEBUG)){
            player.sendMessage("NC+: Attack/reach " + damaged.getType()+ " height="+ StringUtil.fdec3.format(height) + " dist=" + StringUtil.fdec3.format(lenpRel) +" @" + StringUtil.fdec3.format(reachMod));
        }

        return cancel;
    }

    /**
     * Data context for iterating over ITraceEntry instances.
     * @param player
     * @param pLoc
     * @param damaged
     * @param damagedLoc
     * @param data
     * @param cc
     * @return
     */
    public ReachContext getContext(final Player player, final Location pLoc, final Entity damaged, final Location damagedLoc, final FightData data, final FightConfig cc) {
        final ReachContext context = new ReachContext();
        context.distanceLimit = player.getGameMode() == GameMode.CREATIVE ? CREATIVE_DISTANCE : cc.reachSurvivalDistance + getDistMod(damaged);
        context.distanceMin = (context.distanceLimit - cc.reachReduceDistance) / context.distanceLimit;
        //context.eyeHeight = player.getEyeHeight();
        context.pY = pLoc.getY() + player.getEyeHeight();
        return context;
    }

    /**
     * Check if the player fails the reach check, no change of FightData.
     * @param player
     * @param pLoc
     * @param damaged
     * @param dRef
     * @param context
     * @param data
     * @param cc
     * @return
     */
    public boolean loopCheck(final Player player, final Location pLoc, final Entity damaged, 
            final ITraceEntry dRef, final ReachContext context, 
            final FightData data, final FightConfig cc) {
        boolean cancel = false;

        // Refine y position.
        final double dY = dRef.getY();
        double y = dRef.getY();

        if (context.pY <= dY) {
            // Keep the foot level y.
        }
        else if (context.pY >= dY + dRef.getBoxMarginVertical()) {
            y = dY + dRef.getBoxMarginVertical(); // Highest ref y.
        }
        else {
            y = context.pY; // Level with damaged.
        }

        // Distance is calculated from eye location to center of targeted. If the player is further away from their target
        // than allowed, the difference will be assigned to "distance".
        // TODO: Run check on squared distances (quite easy to change to stored boundary-sq values).
        final double lenpRel = TrigUtil.distance(dRef.getX(), y, dRef.getZ(), pLoc.getX(), context.pY, pLoc.getZ());

        double violation = lenpRel - context.distanceLimit;

        if (violation > 0 || lenpRel - context.distanceLimit * data.reachMod > 0){
            // TODO: The silent cancel parts should be sen as "no violation" ?
            // Set minimum violation in context
            context.minViolation = Math.min(context.minViolation, lenpRel);
            cancel = true;
        }
        context.minResult = Math.min(context.minResult, lenpRel);

        return cancel;

    }

    /**
     * Apply changes to FightData according to check results (context), trigger violations.
     * @param player
     * @param pLoc
     * @param damaged
     * @param context
     * @param forceViolation
     * @param data
     * @param cc
     * @return
     */
    public boolean loopFinish(final Player player, final Location pLoc, final Entity damaged, 
            final ReachContext context, final ITraceEntry traceEntry, final boolean forceViolation, 
            final FightData data, final FightConfig cc) {
        final double lenpRel = forceViolation && context.minViolation != Double.MAX_VALUE ? context.minViolation : context.minResult;
        if (lenpRel == Double.MAX_VALUE) {
            return false;
        }
        double violation = lenpRel - context.distanceLimit;
        boolean cancel = false;
        if (violation > 0) {
            // They failed, increment violation level. This is influenced by lag, so don't do it if there was lag.
            if (TickTask.getLag(1000, true) < 1.5f){
                // TODO: 1.5 is a fantasy value.
                data.reachVL += violation;
            }

            // Execute whatever actions are associated with this check and the violation level and find out if we should
            // cancel the event.
            cancel = executeActions(player, data.reachVL, violation, cc.reachActions).willCancel();
            if (Improbable.check(player, (float) violation / 2f, System.currentTimeMillis(), "fight.reach")){
                cancel = true;
            }
            if (cancel && cc.reachPenalty > 0){
                // Apply an attack penalty time.
                data.attackPenalty.applyPenalty(cc.reachPenalty);
            }
        }
        else if (lenpRel - context.distanceLimit * data.reachMod > 0){
            // Silent cancel.
            if (cc.reachPenalty > 0) {
                data.attackPenalty.applyPenalty(cc.reachPenalty / 2);
            }
            cancel = true;
            Improbable.feed(player, (float) (lenpRel - context.distanceLimit * data.reachMod) / 4f, System.currentTimeMillis());
        }
        else{
            // Player passed the check, reward them.
            data.reachVL *= 0.8D;

        }
        // Adaption amount for dynamic range.
        final double DYNAMIC_STEP = cc.reachReduceStep / cc.reachSurvivalDistance;
        if (!cc.reachReduce){
            data.reachMod = 1d;
        }
        else if (lenpRel > context.distanceLimit - cc.reachReduceDistance){
            data.reachMod = Math.max(context.distanceMin, data.reachMod - DYNAMIC_STEP);
        }
        else{
            data.reachMod = Math.min(1.0, data.reachMod + DYNAMIC_STEP);
        }

        if (data.debug && PermissionCache.hasPermission(player, Permissions.ADMINISTRATION_DEBUG)){
            // TODO: Height: remember successful ITraceEntry
            player.sendMessage("NC+: Attack/reach " + damaged.getType()+ (traceEntry == null ? "" : (" height=" + traceEntry.getBoxMarginVertical())) + " dist=" + StringUtil.fdec3.format(lenpRel) +" @" + StringUtil.fdec3.format(data.reachMod));
        }

        return cancel;
    }
}
