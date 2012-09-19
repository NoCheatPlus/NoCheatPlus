package fr.neatmonster.nocheatplus.checks.fight;


import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.combined.Improbable;
import fr.neatmonster.nocheatplus.utilities.LagMeasureTask;

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
    public final double CREATIVE_DISTANCE = 6D;

    /** The maximum distance allowed to interact with an entity in survival mode. */
    public final double SURVIVAL_DISTANCE = 4.25D;

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

        final double distanceLimit = player.getGameMode() == GameMode.SURVIVAL ? SURVIVAL_DISTANCE : CREATIVE_DISTANCE;
        
        // Reference locations to check distance for.
        // TODO: improve reference location: depending on height difference choose min(foot/hitbox, attacker)
        final Location dRef;
        if (damaged instanceof LivingEntity){
        	dRef = ((LivingEntity) damaged).getEyeLocation();
        }
        else dRef = damaged.getLocation();
        final Location pRef = player.getEyeLocation();
        
        final Vector pRel = dRef.toVector().subtract(pRef.toVector());
        
        final double mod;
        if (cc.reachPrecision){
        	// Calculate how fast they are closing in or moving away from each other.
        	mod = 1D;
//            final Vector vRel = damaged.getVelocity().subtract(player.getVelocity());
//            System.out.println(vRel.angle(pRel)); // TODO: NaN
//            final double radial = vRel.length() * Math.cos((vRel.angle(pRel)));
//        	mod = (radial < 0 ? 0.8 : 1.0);
//        	System.out.println(player.getName() + " -> " + pRel.length() + ": " + radial + " => " + mod);
        }
        else mod = 1D;
        // Distance is calculated from eye location to center of targeted. If the player is further away from his target
        // than allowed, the difference will be assigned to "distance".
        double distance = pRel.length() - distanceLimit * mod;

        // Handle the EnderDragon differently.
        if (damaged instanceof EnderDragon)
            distance -= 6.5D;

        if (distance > 0) {
            // He failed, increment violation level. This is influenced by lag, so don't do it if there was lag.
            if (!LagMeasureTask.skipCheck())
                data.reachVL += distance;

            // Execute whatever actions are associated with this check and the violation level and find out if we should
            // cancel the event.
            cancel = executeActions(player, data.reachVL, distance, cc.reachActions);

            if (cancel)
                // If we should cancel, remember the current time too.
                data.reachLastViolationTime = System.currentTimeMillis();
        } else{
            // Player passed the check, reward him.
            data.reachVL *= 0.8D;
            
        }
        
        // Check if improbable.
        if (cancel || distance > -1.25){
        	if (Improbable.check(player, (float) (distance + 1.25) / 2f, System.currentTimeMillis()))
        		cancel = true;
        }


        // If the player is still in penalty time, cancel the event anyway.
        if (data.reachLastViolationTime + cc.reachPenalty > System.currentTimeMillis()) {
            // A safeguard to avoid people getting stuck in penalty time indefinitely in case the system time of the
            // server gets changed.
            if (data.reachLastViolationTime > System.currentTimeMillis())
                data.reachLastViolationTime = 0;

            // He is in penalty time, therefore request cancelling of the event.
            return true;
        }

        return cancel;
    }
}
