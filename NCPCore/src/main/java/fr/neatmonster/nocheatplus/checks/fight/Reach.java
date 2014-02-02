package fr.neatmonster.nocheatplus.checks.fight;


import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Giant;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.combined.Improbable;
import fr.neatmonster.nocheatplus.permissions.Permissions;
import fr.neatmonster.nocheatplus.utilities.StringUtil;
import fr.neatmonster.nocheatplus.utilities.TickTask;

/*
 * MM"""""""`MM                            dP       
 * MM  mmmm,  M                            88       
 * M'        .M .d8888b. .d8888b. .d8888b. 88d888b. 
 * MM  MMMb. "M 88ooood8 88'  `88 88'  `"" 88'  `88 
 * MM  MMMMM  M 88.  ... 88.  .88 88.  ... 88    88 
 * MM  MMMMM  M `88888P' `88888P8 `88888P' dP    dP 
 * MMMMMMMMMMMM                                     
 */
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
     * Checks a player.
     * 
     * @param player
     *            the player
     * @param damaged
     *            the damaged
     * @return true, if successful
     */
    public boolean check(final Player player, final Entity damaged) {
        final FightConfig cc = FightConfig.getConfig(player);
        final FightData data = FightData.getData(player);

        boolean cancel = false;
        
        // The maximum distance allowed to interact with an entity in survival mode.
        final double SURVIVAL_DISTANCE = cc.reachSurvivalDistance; // 4.4D;
        // Amount which can be reduced by reach adaption.
        final double DYNAMIC_RANGE = cc.reachReduceDistance; // 0.9
        // Adaption amount for dynamic range.
        final double DYNAMIC_STEP = cc.reachReduceStep / SURVIVAL_DISTANCE; // 0.15

        final double distanceLimit = player.getGameMode() == GameMode.CREATIVE ? CREATIVE_DISTANCE : SURVIVAL_DISTANCE + getDistMod(damaged);
        final double distanceMin = (distanceLimit - DYNAMIC_RANGE) / distanceLimit;
        
        // Reference locations to check distance for.
        final Location dRef = damaged.getLocation();
        final double height = mcAccess.getHeight(damaged);
        final Location pRef = player.getEyeLocation();
        
        // Refine y position.
        // TODO: Make a little more accurate by counting in the actual bounding box.
        final double pY = pRef.getY();
        final double dY = dRef.getY();
        if (pY <= dY); // Keep the foot level y.
        else if (pY >= dY + height) dRef.setY(dY + height); // Highest ref y.
        else dRef.setY(pY); // Level with damaged.
        
        final Vector pRel = dRef.toVector().subtract(pRef.toVector());
        
        // Distance is calculated from eye location to center of targeted. If the player is further away from their target
        // than allowed, the difference will be assigned to "distance".
        final double lenpRel = pRel.length();
        
        double violation = lenpRel - distanceLimit;
        
        final double reachMod = data.reachMod; 

        if (violation > 0) {
            // They failed, increment violation level. This is influenced by lag, so don't do it if there was lag.
            if (TickTask.getLag(1000) < 1.5f){
            	// TODO: 1.5 is a fantasy value.
            	data.reachVL += violation;
            }

            // Execute whatever actions are associated with this check and the violation level and find out if we should
            // cancel the event.
            cancel = executeActions(player, data.reachVL, violation, cc.reachActions);
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
        
        if (cc.debug && player.hasPermission(Permissions.ADMINISTRATION_DEBUG)){
            player.sendMessage("NC+: Attack/reach " + damaged.getType()+ " height="+ StringUtil.fdec3.format(height) + " dist=" + StringUtil.fdec3.format(lenpRel) +" @" + StringUtil.fdec3.format(reachMod));
        }

        return cancel;
    }
}
