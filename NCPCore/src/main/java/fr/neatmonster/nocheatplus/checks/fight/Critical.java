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
import fr.neatmonster.nocheatplus.utilities.TickTask;

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
    public boolean check(final Player player) {
        final FightConfig cc = FightConfig.getConfig(player);
        final FightData data = FightData.getData(player);

        boolean cancel = false;

        // We'll need the PlayerLocation to know some important stuff.
        final Location loc = player.getLocation();
		
		final float mcFallDistance = player.getFallDistance();
		final MovingConfig mCc = MovingConfig.getConfig(player);
		if (mcFallDistance > 0.0 && cc.debug && player.hasPermission(Permissions.ADMINISTRATION_DEBUG)){
			final MovingData mData = MovingData.getData(player);
			
			if (MovingListener.shouldCheckSurvivalFly(player, mData, mCc) && CheckType.MOVING_NOFALL.isEnabled(player)){
				// TODO: Set max y in MovingListener, to be independent of sf/nofall!
				player.sendMessage("Critical: fd=" + mcFallDistance + "(" + mData.noFallFallDistance +") y=" + loc.getY() + ((mData.hasSetBack() && mData.getSetBackY() < mData.noFallMaxY) ? (" jumped=" + StringUtil.fdec3.format(mData.noFallMaxY - mData.getSetBackY())): ""));
			}
		}

        // Check if the hit was a critical hit (positive fall distance, entity in the air, not on ladder, not in liquid
        // and without blindness effect).
		
		// TODO: Skip the on-ground check somehow?
		// TODO: Implement low jump penalty.
        if (mcFallDistance > 0f && !player.hasPotionEffect(PotionEffectType.BLINDNESS)){
        	// Might be a violation.
        	final MovingData dataM = MovingData.getData(player);
        	if (dataM.sfLowJump || player.getFallDistance() < cc.criticalFallDistance && !BlockProperties.isOnGroundOrResetCond(player, loc, mCc.yOnGround)){
        		final MovingConfig ccM = MovingConfig.getConfig(player);
            	if (MovingListener.shouldCheckSurvivalFly(player, dataM, ccM)){
                    final double deltaFallDistance = (cc.criticalFallDistance - player.getFallDistance()) / cc.criticalFallDistance;
                    final double deltaVelocity = (cc.criticalVelocity - Math.abs(player.getVelocity().getY())) / cc.criticalVelocity;
                    double delta = deltaFallDistance > 0D ? deltaFallDistance : 0D + deltaVelocity > 0D ? deltaVelocity : 0D;
                    
                    final List<String> tags = new ArrayList<String>();
                    
                    // Player failed the check, but this is influenced by lag so don't do it if there was lag.
                    if (TickTask.getLag(1000) < 1.5){
                    	// TODO: 1.5 is a fantasy value.
                        // Increment the violation level.
                        data.criticalVL += delta;
                    }
                    else{
                    	tags.add("lag");
                    	delta = 0;
                    }
                    
                    // Execute whatever actions are associated with this check and the violation level and find out if we
                    // should cancel the event.
                    final ViolationData vd = new ViolationData(this, player, data.criticalVL, delta, cc.criticalActions);
                    if (vd.needsParameters()){
                    	if (dataM.sfLowJump){
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
