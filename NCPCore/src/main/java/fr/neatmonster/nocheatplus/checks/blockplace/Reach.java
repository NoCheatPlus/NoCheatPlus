package fr.neatmonster.nocheatplus.checks.blockplace;

import java.util.Map;

import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.actions.ParameterName;
import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.ViolationData;
import fr.neatmonster.nocheatplus.utilities.TrigUtil;

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
    public static final double CREATIVE_DISTANCE = 5.6D;

    /** The maximum distance allowed to interact with a block in survival mode. */
    public static final double SURVIVAL_DISTANCE = 5.2D;

    /**
     * Instantiates a new reach check.
     */
    public Reach() {
        super(CheckType.BLOCKPLACE_REACH);
    }

    /**
     * Checks a player.
     * 
     * @param player
     *            the player
     * @param cc 
     * @param data2 
     * @param location
     *            the location
     * @return true, if successful
     */
    public boolean check(final Player player, final Block block, final BlockPlaceData data, final BlockPlaceConfig cc) {

        boolean cancel = false;

        final double distanceLimit = player.getGameMode() == GameMode.CREATIVE ? CREATIVE_DISTANCE : SURVIVAL_DISTANCE;

        // Distance is calculated from eye location to center of targeted block. If the player is further away from their
        // target than allowed, the difference will be assigned to "distance".
        final double distance = TrigUtil.distance(player.getEyeLocation(), block) - distanceLimit;

        if (distance > 0) {
            // They failed, increment violation level.
            data.reachVL += distance;

            // Remember how much further than allowed they tried to reach for logging, if necessary.
            data.reachDistance = distance;

            // Execute whatever actions are associated with this check and the violation level and find out if we should
            // cancel the event.
            cancel = executeActions(player, data.reachVL, distance, cc.reachActions);
        } else{
            // Player passed the check, reward them.
            data.reachVL *= 0.9D;
        }


        return cancel;
    }
    
	@Override
	protected Map<ParameterName, String> getParameterMap(final ViolationData violationData) {
		final Map<ParameterName, String> parameters = super.getParameterMap(violationData);
		parameters.put(ParameterName.REACH_DISTANCE, "" +Math.round(BlockPlaceData.getData(violationData.player).reachDistance));
		return parameters;
	}

}
