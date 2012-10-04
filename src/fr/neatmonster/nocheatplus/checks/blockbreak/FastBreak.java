package fr.neatmonster.nocheatplus.checks.blockbreak;

import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.actions.ParameterName;
import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.ViolationData;
import fr.neatmonster.nocheatplus.players.Permissions;
import fr.neatmonster.nocheatplus.utilities.BlockProperties;

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
     * @param isInstaBreak 
     * @param data 
     * @param cc 
     * @param elaspedTime
     * @return true, if successful
     */
    public boolean check(final Player player, final Block block, final boolean isInstaBreak, final BlockBreakConfig cc, final BlockBreakData data) {
    	final long now = System.currentTimeMillis();
        boolean cancel = false;
        
        // First, check the game mode of the player and choose the right limit.
        final long breakingTime;
        final int id = block.getTypeId();
        if (player.getGameMode() == GameMode.CREATIVE)
        	// Modifier defaults to 0, the Frequency check is responsible for those.
            breakingTime = Math.round((double) cc.fastBreakModCreative / 100D * (double) 100);
        else
        	breakingTime = Math.round((double) cc.fastBreakModSurvival / 100D * (double) BlockProperties.getBreakingDuration(id, player));
    	// fastBreakfirstDamage is the first interact on block (!).
        final long elapsedTime = (data.fastBreakBreakTime > data.fastBreakfirstDamage) ? 0 : now - data.fastBreakfirstDamage;
          
        // Check if the time used time is lower than expected.
//        if (isInstaBreak){
//        	// Ignore those for now.
//        }
//        else 
        if (elapsedTime + cc.fastBreakDelay < breakingTime){
    		// lag or cheat or Minecraft.
    		        		
    		final long missingTime = breakingTime - elapsedTime;
    		
    		// Add as penalty
    		data.fastBreakPenalties.add(now, (float) missingTime);
    		
    		// Only raise a violation, if the total penalty score exceeds the contention duration (for lag, delay).
    		if (data.fastBreakPenalties.getScore(cc.fastBreakBucketFactor) > cc.fastBreakBucketContention){
    			// TODO: maybe add one absolute penalty time for big amounts to stop breaking until then
    			data.fastBreakVL += missingTime;
    			final ViolationData vd = new ViolationData(this, player, data.fastBreakVL, missingTime, cc.fastBreakActions);
    			vd.setParameter(ParameterName.BLOCK_ID, "" + id);
    			cancel = executeActions(vd);
    		}
    		// else: still within contention limits.
    	}
    	else if (breakingTime > cc.fastBreakDelay){
    		// Fast breaking does not decrease violation level.
    		data.fastBreakVL *= 0.9D;
    	}
    	
        if ((cc.fastBreakDebug || cc.debug) && player.hasPermission(Permissions.ADMINISTRATION_DEBUG)){
        	// General stats:
        	if (data.stats != null){
                data.stats.addStats(data.stats.getId(Integer.toString(block.getTypeId())+"u", true), elapsedTime);
                data.stats.addStats(data.stats.getId(Integer.toString(block.getTypeId())+ "r", true), breakingTime);
                player.sendMessage(data.stats.getStatsStr(true));
            }
        	// Send info about current break:
        	final int blockId = block.getTypeId();
        	final boolean isValidTool = BlockProperties.isValidTool(blockId, player.getItemInHand());
        	String msg = (isInstaBreak ? "[Insta]" : "[Normal]") + "[" + blockId + "] "+ elapsedTime + "u / " + breakingTime +"r (" + (isValidTool?"tool":"no-tool") + ")";
        	player.sendMessage(msg);
        }
    	 
    	 // (The break time is set in the listener).

        return cancel;
    }
}
