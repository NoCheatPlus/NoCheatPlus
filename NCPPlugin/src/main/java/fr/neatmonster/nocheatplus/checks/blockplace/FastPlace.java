package fr.neatmonster.nocheatplus.checks.blockplace;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.utilities.TickTask;

/*
 * MM""""""""`M                     dP   MM"""""""`YM dP                            
 * MM  mmmmmmmM                     88   MM  mmmmm  M 88                            
 * M'      MMMM .d8888b. .d8888b. d8888P M'        .M 88 .d8888b. .d8888b. .d8888b. 
 * MM  MMMMMMMM 88'  `88 Y8ooooo.   88   MM  MMMMMMMM 88 88'  `88 88'  `"" 88ooood8 
 * MM  MMMMMMMM 88.  .88       88   88   MM  MMMMMMMM 88 88.  .88 88.  ... 88.  ... 
 * MM  MMMMMMMM `88888P8 `88888P'   dP   MM  MMMMMMMM dP `88888P8 `88888P' `88888P' 
 * MMMMMMMMMMMM                          MMMMMMMMMMMM                               
 */
/**
 * A check used to verify if the player isn't placing blocks too quickly.
 */
public class FastPlace extends Check {

    /**
     * Instantiates a new fast place check.
     */
    public FastPlace() {
        super(CheckType.BLOCKPLACE_FASTPLACE);
    }

    /**
     * Checks a player.
     * 
     * @param player
     *            the player
     * @param block
     *            the block
     * @return true, if successful
     */
    public boolean check(final Player player, final Block block) {
        final BlockPlaceConfig cc = BlockPlaceConfig.getConfig(player);
        final BlockPlaceData data = BlockPlaceData.getData(player);

        data.fastPlaceBuckets.add(System.currentTimeMillis(), 1f);
        
        // Full period frequency.
        final float fullScore = data.fastPlaceBuckets.score(1f);
        
        // Short term arrivals.
        final int tick = TickTask.getTick();
        if (tick - data.fastPlaceShortTermTick < cc.fastPlaceShortTermTicks){
        	// Account for server side lag.
        	if (!cc.lag || TickTask.getLag(50L * (tick - data.fastPlaceShortTermTick), true) < 1.2){
        		// Within range, add.
        		data.fastPlaceShortTermCount ++;
        	}
        	else{
        		// Too much lag, reset.
            	data.fastPlaceShortTermTick = tick;
            	data.fastPlaceShortTermCount = 1;
        	}
        }
        else{
        	data.fastPlaceShortTermTick = tick;
        	data.fastPlaceShortTermCount = 1;
        }
        
        // Find if one of both or both are violations:
        final float fullViolation;
        if (fullScore > cc.fastPlaceLimit){
        	// Account for server side lag.
        	if (cc.lag){
        		fullViolation = fullScore / TickTask.getLag(data.fastPlaceBuckets.bucketDuration() * data.fastPlaceBuckets.numberOfBuckets(), true) - cc.fastPlaceLimit;
        	}
        	else{
        		fullViolation = fullScore - cc.fastPlaceLimit;
        	}	
        }
        else{
        	fullViolation = 0;
        }
        final float shortTermViolation = data.fastPlaceShortTermCount - cc.fastPlaceShortTermLimit; 
        final float violation = Math.max(fullViolation, shortTermViolation);
        
        boolean cancel = false;
        if (violation > 0){
        	final double change = violation / 1000;
        	data.fastPlaceVL += change;
        	cancel = executeActions(player, data.fastPlaceVL, change, cc.fastPlaceActions);
        }
        else if (data.fastPlaceVL > 0d && fullScore < cc.fastPlaceLimit * .75)
        	data.fastPlaceVL *= 0.95;
        
		return cancel;
    }
}
