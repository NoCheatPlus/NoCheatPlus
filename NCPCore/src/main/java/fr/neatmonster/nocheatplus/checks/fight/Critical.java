package fr.neatmonster.nocheatplus.checks.fight;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import fr.neatmonster.nocheatplus.actions.ParameterName;
import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.ViolationData;
import fr.neatmonster.nocheatplus.checks.moving.MovingConfig;
import fr.neatmonster.nocheatplus.checks.moving.MovingData;
import fr.neatmonster.nocheatplus.checks.moving.model.LiftOffEnvelope;
import fr.neatmonster.nocheatplus.checks.moving.model.MoveInfo;
import fr.neatmonster.nocheatplus.checks.moving.util.MovingUtil;
import fr.neatmonster.nocheatplus.compat.MCAccess;
import fr.neatmonster.nocheatplus.utilities.BlockProperties;
import fr.neatmonster.nocheatplus.utilities.StringUtil;

/**
 * A check used to verify that critical hits done by players are legit.
 */
public class Critical extends Check {

    private MoveInfo moveInfo;

    /**
     * Instantiates a new critical check.
     */
    public Critical() {
        super(CheckType.FIGHT_CRITICAL);
        moveInfo = new MoveInfo(mcAccess);
    }

    @Override
    public void setMCAccess(MCAccess mcAccess) {
        super.setMCAccess(mcAccess);
        moveInfo = new MoveInfo(mcAccess);
    }

    /**
     * Checks a player.
     * 
     * @param player
     *            the player
     * @return true, if successful
     */
    public boolean check(final Player player, final Location loc, final FightData data, final FightConfig cc) {
        boolean cancel = false;

        final double mcFallDistance = (double) player.getFallDistance();
        final MovingConfig mCc = MovingConfig.getConfig(player);

        if (data.debug) {
            debug(player, "y=" + loc.getY() + " mcfalldist=" + mcFallDistance);
        }

        // Check if the hit was a critical hit (very small fall-distance, not on ladder, 
        //  not in liquid, not in vehicle, and without blindness effect).
        if (mcFallDistance > 0.0 && !player.isInsideVehicle() && !player.hasPotionEffect(PotionEffectType.BLINDNESS)) {
            // Might be a violation.
            final MovingData dataM = MovingData.getData(player);

            // TODO: Skip near the highest jump height (needs check if head collided with something solid, which also detects low jump).
            if (!dataM.isVelocityJumpPhase() && 
                    (dataM.sfLowJump && !dataM.sfNoLowJump && dataM.liftOffEnvelope == LiftOffEnvelope.NORMAL
                    || mcFallDistance < cc.criticalFallDistance && !BlockProperties.isResetCond(player, loc, mCc.yOnGround))) {
                final MovingConfig ccM = MovingConfig.getConfig(player);
                // TODO: Use past move tracking to check for SurvivalFly and the like?
                moveInfo.set(player, loc, null, ccM.yOnGround);
                if (MovingUtil.shouldCheckSurvivalFly(player, moveInfo.from, dataM, ccM)) {
                    data.criticalVL += 1.0;
                    // Execute whatever actions are associated with this check and 
                    //  the violation level and find out if we should cancel the event.
                    final ViolationData vd = new ViolationData(this, player, data.criticalVL, 1.0, cc.criticalActions);
                    if (vd.needsParameters()) {
                        final List<String> tags = new ArrayList<String>();
                        if (dataM.sfLowJump) {
                            tags.add("lowjump");
                        }
                        vd.setParameter(ParameterName.TAGS, StringUtil.join(tags, "+"));
                    }
                    cancel = executeActions(vd).willCancel();
                }
                moveInfo.cleanup();
            }
        }

        return cancel;
    }



}
