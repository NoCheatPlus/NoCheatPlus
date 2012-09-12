package fr.neatmonster.nocheatplus.checks.blockbreak;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;

/**
 * This checks just limits the number of blocks broken per time frame.
 * @author mc_dev
 *
 */
public class Frequency extends Check {

	public Frequency() {
		super(CheckType.BLOCKBREAK_FREQUENCY);
	}
	
	public boolean check(final Player player){
        final BlockBreakConfig cc = BlockBreakConfig.getConfig(player);
        final BlockBreakData data = BlockBreakData.getData(player);
        
        final float interval = (float) ((player.getGameMode() == GameMode.CREATIVE)?(cc.frequencyIntervalCreative):(cc.frequencyIntervalSurvival));
        data.frequencyBuckets.add(System.currentTimeMillis(), interval);
        
        final float score = data.frequencyBuckets.getScore(cc.frequencyBucketFactor);
        final float allowed = cc.frequencyBuckets * cc.frequencyBucketDur;
        
        boolean cancel = false;
        if (score > allowed){
        	final double change = (score - allowed) / 1000;
        	data.frequencyVL += change;
        	cancel = executeActions(player, data.frequencyVL, change, cc.frequencyActions);
        }
        else if (data.frequencyVL > 0d && score < allowed * .75)
        	data.frequencyVL *= 0.95;
        
		return cancel;
	}

}
