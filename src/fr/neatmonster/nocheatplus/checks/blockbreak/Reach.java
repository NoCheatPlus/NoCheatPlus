package fr.neatmonster.nocheatplus.checks.blockbreak;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.actions.ParameterName;
import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.ViolationData;
import fr.neatmonster.nocheatplus.utilities.CheckUtils;

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

    /** The maximum distance allowed to interact with a block in creative mode. */
    public final double CREATIVE_DISTANCE = 5.6D;

    /** The maximum distance allowed to interact with a block in survival mode. */
    public final double SURVIVAL_DISTANCE = 5.2D;

    /**
     * Instantiates a new reach check.
     */
    public Reach() {
        super(CheckType.BLOCKBREAK_REACH);
    }

    /**
     * Checks a player.
     * 
     * @param player
     *            the player
     * @param location
     *            the location
     * @return true, if successful
     */
    public boolean check(final Player player, final Location location, final BlockBreakData data) {

        boolean cancel = false;

        final double distanceLimit = player.getGameMode() == GameMode.SURVIVAL ? SURVIVAL_DISTANCE : CREATIVE_DISTANCE;

        // Distance is calculated from eye location to center of targeted block. If the player is further away from his
        // target than allowed, the difference will be assigned to "distance".
        final double distance = CheckUtils.distance(player.getEyeLocation(), location.add(0.5D, 0.5D, 0.5D))
                - distanceLimit;

        if (distance > 0) {
            // He failed, increment violation level.
            data.reachVL += distance;

            // Remember how much further than allowed he tried to reach for logging, if necessary.
            data.reachDistance = distance;

            // Execute whatever actions are associated with this check and the violation level and find out if we should
            // cancel the event.
            cancel = executeActions(player, data.reachVL, distance, BlockBreakConfig.getConfig(player).reachActions);
        } else{
            // Player passed the check, reward him.
            data.reachVL *= 0.9D;
            
//        	// Check if improbable
//            if (distance > -0.1){
//            	if (Improbable.check(player, 1.0f, System.currentTimeMillis()))
//            		cancel = true;
//            }
        }

        return cancel;
    }

    /* (non-Javadoc)
     * @see fr.neatmonster.nocheatplus.checks.Check#getParameter(fr.neatmonster.nocheatplus.actions.ParameterName, org.bukkit.entity.Player)
     */
    @Override
    public String getParameter(final ParameterName wildcard, final ViolationData violationData) {
        if (wildcard == ParameterName.REACH_DISTANCE)
            return String.valueOf(Math.round(BlockBreakData.getData(violationData.player).reachDistance));
        else
            return super.getParameter(wildcard, violationData);
    }
}
