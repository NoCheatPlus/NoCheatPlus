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
import fr.neatmonster.nocheatplus.checks.moving.MovingListener;
import fr.neatmonster.nocheatplus.permissions.Permissions;
import fr.neatmonster.nocheatplus.utilities.BlockProperties;
import fr.neatmonster.nocheatplus.utilities.StringUtil;

/**
 * A check used to verify that critical hits done by players are legit.
 */
public class Critical extends Check {

    /**
     * Instantiates a new critical check.
     */
    public Critical() {
        super(CheckType.FIGHT_CRITICAL);
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
        // TODO: All debugging to the trace (later allow hooking your own trace).
        if (mcFallDistance > 0.0 && cc.debug && player.hasPermission(Permissions.ADMINISTRATION_DEBUG)) {
            final MovingData mData = MovingData.getData(player);

            if (MovingListener.shouldCheckSurvivalFly(player, mData, mCc) && CheckType.MOVING_NOFALL.isEnabled(player)) {
                // TODO: Set max y in MovingListener, to be independent of sf/nofall!
                player.sendMessage("Critical: fd=" + mcFallDistance + "(" + mData.noFallFallDistance +") y=" + loc.getY() + ((mData.hasSetBack() && mData.getSetBackY() < mData.noFallMaxY) ? (" jumped=" + StringUtil.fdec3.format(mData.noFallMaxY - mData.getSetBackY())): ""));
            }
        }

        // Check if the hit was a critical hit (very small fall-distance, not on ladder, 
        //  not in liquid, not in vehicle, and without blindness effect).
        if (mcFallDistance > 0.0 && !player.isInsideVehicle() && !player.hasPotionEffect(PotionEffectType.BLINDNESS)) {
            // Might be a violation.
            final MovingData dataM = MovingData.getData(player);
            
            // TODO: Skip near the highest jump height (needs check if head collided with something solid, which also detects low jump).
            if (dataM.sfLowJump || mcFallDistance < cc.criticalFallDistance && !BlockProperties.isResetCond(player, loc, mCc.yOnGround)) {
                final MovingConfig ccM = MovingConfig.getConfig(player);
                if (MovingListener.shouldCheckSurvivalFly(player, dataM, ccM)) {
                    data.criticalVL += 1.0;
                    // Execute whatever actions are associated with this check and 
                    //  the violation level and find out if we should cancel the event.
                    final ViolationData vd = new ViolationData(this, player, data.criticalVL, 1.0, cc.criticalActions);
                    if (vd.needsParameters()) {
                        final List<String> tags = new ArrayList<String>();
                        if (dataM.sfLowJump) {
                            tags.add("sf_lowjump");
                        }
                        vd.setParameter(ParameterName.TAGS, StringUtil.join(tags, "+"));
                    }
                    cancel = executeActions(vd);	
                }
            }
        }

        return cancel;
    }
}
