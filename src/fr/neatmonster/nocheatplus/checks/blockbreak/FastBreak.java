package fr.neatmonster.nocheatplus.checks.blockbreak;

import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.combined.Improbable;
import fr.neatmonster.nocheatplus.players.Permissions;
import fr.neatmonster.nocheatplus.utilities.BlockUtils;

/*
 * MM""""""""`M                     dP   M#"""""""'M                             dP       
 * MM  mmmmmmmM                     88   ##  mmmm. `M                            88       
 * M'      MMMM .d8888b. .d8888b. d8888P #'        .M 88d888b. .d8888b. .d8888b. 88  .dP  
 * MM  MMMMMMMM 88'  `88 Y8ooooo.   88   M#  MMMb.'YM 88'  `88 88ooood8 88'  `88 88888"   
 * MM  MMMMMMMM 88.  .88       88   88   M#  MMMM'  M 88       88.  ... 88.  .88 88  `8b. 
 * MM  MMMMMMMM `88888P8 `88888P'   dP   M#       .;M dP       `88888P' `88888P8 dP   `YP 
 * MMMMMMMMMMMM                          M#########M                                      
 */
/**
 * A check used to verify if the player isn't breaking blocks faster than possible.
 */
public class FastBreak extends Check {

    /**
     * Instantiates a new fast break check.
     */
    public FastBreak() {
        super(CheckType.BLOCKBREAK_FASTBREAK);
    }

    /**
     * Checks a player.
     * 
     * @param player
     *            the player
     * @param block
     *            the block
     * @param data 
     * @param cc 
     * @param elaspedTime
     * @return true, if successful
     */
    public boolean check(final Player player, final Block block, final BlockBreakConfig cc, final BlockBreakData data) {
    	final long now = System.currentTimeMillis();

        boolean cancel = false;
        
        // First, check the game mode of the player and choose the right limit.
        long breakingTime = Math.round((double) cc.fastBreakModSurvival / 100D * (double) BlockUtils.getBreakingDuration(block.getTypeId(), player));
        if (player.getGameMode() == GameMode.CREATIVE)
            breakingTime = Math.round((double) cc.fastBreakModCreative / 100D * (double) 95);
        
    	// fastBreakDamageTime is now first interact on block (!).
        final long elapsedTime = now - data.fastBreakDamageTime;
        
        // Check if the time used time is lower than expected.
    	if (elapsedTime + cc.fastBreakDelay < breakingTime){
    		// lag or cheat or Minecraft.
    		        		
    		final long missingTime = breakingTime - elapsedTime;
    		
    		
    		
    		// Add as penalty
    		data.fastBreakPenalties.add(now, (float) missingTime);
    		
    		// Only raise a violation, if the total penalty score exceeds the contention duration (for lag, delay).
    		if (data.fastBreakPenalties.getScore(cc.fastBreakBucketFactor) > cc.fastBreakBucketContention){
    			// TODO: maybe add one absolute penalty time for big amounts to stop breaking until then
    			data.fastBreakVL += missingTime;
    			cancel = executeActions(player, data.fastBreakVL, missingTime, cc.fastBreakActions);
    		}
    		// else: still within contention limits.
    		
//        		System.out.println("violation : " + missingTime);
    	}
    	else{
    		data.fastBreakVL *= 0.9D;
    	}
    	
    	 if (cc.fastBreakDebug && player.hasPermission(Permissions.ADMINISTRATION_DEBUG)){
             data.stats.addStats(data.stats.getId(Integer.toString(block.getTypeId())+"u", true), elapsedTime);
             data.stats.addStats(data.stats.getId(Integer.toString(block.getTypeId())+ "r", true), breakingTime);
             player.sendMessage(data.stats.getStatsStr(true));
         }
    	
        
      
        // Remember the block breaking time.
        data.fastBreakBreakTime = now;
        
        // Combined speed:
        // TODO: use some value corresponding to allowed block breaking speed !
        if (cc.improbableFastBreakCheck && Improbable.check(player, 1f, now))
        	cancel = true;

        return cancel;
    }
}
