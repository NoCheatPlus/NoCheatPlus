package fr.neatmonster.nocheatplus.checks.blockbreak;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.utilities.TickTask;

/**
 * This checks just limits the number of blocks broken per time frame.
 * @author mc_dev
 *
 */
public class Frequency extends Check {

	public Frequency() {
		super(CheckType.BLOCKBREAK_FREQUENCY);
	}
	
	public boolean check(final Player player, final BlockBreakConfig cc, final BlockBreakData data){
        
        final float interval = (float) ((player.getGameMode() == GameMode.CREATIVE)?(cc.frequencyIntervalCreative):(cc.frequencyIntervalSurvival));
        data.frequencyBuckets.add(System.currentTimeMillis(), interval);
        
        // Full period frequency.
        final float fullScore = data.frequencyBuckets.score(cc.frequencyBucketFactor);
        final float fullTime = cc.frequencyBuckets * cc.frequencyBucketDur;
        
        // Short term arrivals.
        final int tick = TickTask.getTick();
        if (tick - data.frequencyShortTermTick < cc.frequencyShortTermTicks){
        	// Within range, add.
        	data.frequencyShortTermCount ++;
        }
        else{
        	data.frequencyShortTermTick = tick;
        	data.frequencyShortTermCount = 1;
        }
        
        // Find if one of both or both are violations:
        final float fullViolation = (fullScore > fullTime) ? (fullScore - fullTime) : 0;
        final float shortTermWeight = 50f * cc.frequencyShortTermTicks / (float) cc.frequencyShortTermLimit; 
        final float shortTermViolation = (data.frequencyShortTermCount > cc.frequencyShortTermLimit) 
        		? (data.frequencyShortTermCount - cc.frequencyShortTermLimit) * shortTermWeight : 0; 
        final float violation = Math.max(fullViolation, shortTermViolation);
        
        boolean cancel = false;
        if (violation > 0){
        	final double change = violation / 1000;
        	data.frequencyVL += change;
        	cancel = executeActions(player, data.frequencyVL, change, cc.frequencyActions);
        }
        else if (data.frequencyVL > 0d && fullScore < fullTime * .75)
        	data.frequencyVL *= 0.95;
        
		return cancel;
	}

}
