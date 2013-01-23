package fr.neatmonster.nocheatplus.checks.fight;

import java.util.Map;

import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.actions.ParameterName;
import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.ViolationData;
import fr.neatmonster.nocheatplus.utilities.TickTask;

/*
 * MP""""""`MM                                  dP 
 * M  mmmmm..M                                  88 
 * M.      `YM 88d888b. .d8888b. .d8888b. .d888b88 
 * MMMMMMM.  M 88'  `88 88ooood8 88ooood8 88'  `88 
 * M. .MMM'  M 88.  .88 88.  ... 88.  ... 88.  .88 
 * Mb.     .dM 88Y888P' `88888P' `88888P' `88888P8 
 * MMMMMMMMMMM 88                                  
 *             dP                                  
 */
/**
 * The Speed check is used to detect players who are attacking entities too quickly.
 */
public class Speed extends Check {

    /**
     * Instantiates a new speed check.
     */
    public Speed() {
        super(CheckType.FIGHT_SPEED);
    }

    /**
     * Checks a player.
     * 
     * @param player
     *            the player
     * @param now 
     * @return true, if successful
     */
    public boolean check(final Player player, final long now) {
        final FightConfig cc = FightConfig.getConfig(player);
        final FightData data = FightData.getData(player);

        boolean cancel = false;
        
        // Add to frequency.
        data.speedBuckets.add(now, 1f);
        
        // Medium term (normalized to one second), account for server side lag.
        final long fullTime = cc.speedBucketDur * cc.speedBuckets;
        final float fullLag = cc.lag ? TickTask.getLag(fullTime, true) : 1f;
        final float total = data.speedBuckets.score(cc.speedBucketFactor) * 1000f / (fullLag * fullTime);
        
        // Short term.
        final int tick = TickTask.getTick();
        if (tick < data.speedShortTermTick){
        	// Tick task got reset.
        	data.speedShortTermTick = tick;
        	data.speedShortTermCount = 1;
        }
        else if (tick - data.speedShortTermTick < cc.speedShortTermTicks){
        	// Account for server side lag.
        	if (!cc.lag || TickTask.getLag(50L * (tick - data.speedShortTermTick), true) < 1.5f){
            	// Within range, add.
            	data.speedShortTermCount ++;
        	}
        	else{
        		// Too much lag, reset.
            	data.speedShortTermTick = tick;
            	data.speedShortTermCount = 1;
        	}
        }
        else{
        	data.speedShortTermTick = tick;
        	data.speedShortTermCount = 1;
        }
        
        final float shortTerm = (float ) data.speedShortTermCount * 1000f / (50f * cc.speedShortTermTicks);
        
        final float max = Math.max(shortTerm, total);

        // Too many attacks?
        if (max > cc.speedLimit) {
            // If there was lag, don't count it towards violation level.
            data.speedVL += max - cc.speedLimit;

            // Execute whatever actions are associated with this check and the violation level and find out if we should
            // cancel the event.
            cancel = executeActions(player, data.speedVL, max - cc.speedLimit, cc.speedActions);
        }
        else data.speedVL *= 0.96;

        return cancel;
    }
    
	@Override
	protected Map<ParameterName, String> getParameterMap(final ViolationData violationData) {
		final Map<ParameterName, String> parameters = super.getParameterMap(violationData);
		parameters.put(ParameterName.LIMIT, String.valueOf(Math.round(FightConfig.getConfig(violationData.player).speedLimit)));
		return parameters;
	}
}
