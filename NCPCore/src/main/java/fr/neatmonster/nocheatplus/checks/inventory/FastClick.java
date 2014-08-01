package fr.neatmonster.nocheatplus.checks.inventory;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.ViolationData;
import fr.neatmonster.nocheatplus.permissions.Permissions;
import fr.neatmonster.nocheatplus.utilities.InventoryUtil;
import fr.neatmonster.nocheatplus.utilities.TickTask;

/**
 * The FastClick check will prevents players from taking automatically all items from any inventory (chests, etc.).
 */
public class FastClick extends Check {

    /**
     * Instantiates a new fast click check.
     */
    public FastClick() {
        super(CheckType.INVENTORY_FASTCLICK);
    }

//    /**
//     * Checks a player.
//     * 
//     * @param player
//     *            the player
//     * @param cc 
//     * @return true, if successful
//     */
//    public boolean check(final Player player) {
//    	// Take time once.
//    	final long time = System.currentTimeMillis();
//    	
//        final InventoryData data = InventoryData.getData(player);
//
//        boolean cancel = false;
//
//        // If the last inventory click has been made within 45 milliseconds.
//        if (time - data.fastClickLastTime < 45L) {
//            if (data.fastClickLastCancelled) {
//
//                // Calculate the difference between the limit and the time elapsed.
//                final double difference = 45L - time + data.fastClickLastTime;
//                final InventoryConfig cc = InventoryConfig.getConfig(player);
//                final ViolationData vd = new ViolationData(this, player, data.fastClickVL + difference, difference, cc.fastClickActions);
//                if (TickTask.getLag(150, true) > 1.7f){
//                	// Don't increase vl here.
//                	cancel = vd.hasCancel();
//                }
//                else{
//                    // Increment the violation level.
//                    data.fastClickVL += difference;
//
//                    // Find out if we need to cancel the event.
//                    cancel = executeActions(vd);	
//                }
//            } else
//                data.fastClickLastCancelled = true;
//        } else {
//            data.fastClickLastCancelled = false;
//
//            // Reduce the violation level.
//            data.fastClickVL *= 0.98D;
//        }
//
//        // Remember the current time.s
//        data.fastClickLastTime = time;
//
//        return cancel;
//    }
    
    public boolean check(final Player player, final long now, final InventoryView view, final int slot, final ItemStack cursor, final ItemStack clicked, final boolean isShiftClick, final InventoryData data, final InventoryConfig cc) {
    	// Take time once.
    	
    	final float amount;
    	
    	if (cursor != null && cc.fastClickTweaks1_5){
    		final Material cursorMat = cursor.getType();
    		final int cursorAmount = Math.max(1, cursor.getAmount());
    		final Material clickedMat = clicked == null ? Material.AIR : clicked.getType();
    		if (cursorMat != data.fastClickLastCursor && (!isShiftClick || clicked == null || clicked.getType() != data.fastClickLastClicked) || cursorMat == Material.AIR || cursorAmount != data.fastClickLastCursorAmount){
    			amount = 1f;
    		}
    		else{
    			if (clickedMat == Material.AIR || clickedMat == cursorMat || isShiftClick && clickedMat == data.fastClickLastClicked ){
    				amount = Math.min(cc.fastClickNormalLimit , cc.fastClickShortTermLimit) / (float) (isShiftClick && clickedMat != Material.AIR ? (1.0 + Math.max(cursorAmount, InventoryUtil.getStackCount(view, clicked))) : cursorAmount)  * 0.75f;
    			}
    			else{
    				amount = 1f;
    			}
    		}
        	data.fastClickLastCursor = cursorMat;
        	data.fastClickLastClicked = clickedMat;
        	data.fastClickLastCursorAmount = cursorAmount;
    	}
    	else{
        	data.fastClickLastCursor = null;
        	data.fastClickLastClicked = null;
        	data.fastClickLastCursorAmount = 0;
    		amount = 1f;
    	}
        
        data.fastClickFreq.add(now, amount);
        
        float shortTerm = data.fastClickFreq.bucketScore(0);
        if (shortTerm > cc.fastClickShortTermLimit){
        	// Check for lag.
        	shortTerm /= (float) TickTask.getLag(data.fastClickFreq.bucketDuration(), true);
        }
        shortTerm -= cc.fastClickShortTermLimit;
        
        float normal = data.fastClickFreq.score(1f);
        if (normal > cc.fastClickNormalLimit){
        	// Check for lag.
        	normal /= (float) TickTask.getLag(data.fastClickFreq.bucketDuration() * data.fastClickFreq.numberOfBuckets(), true);
        }
        normal -= cc.fastClickNormalLimit;

        final double violation = Math.max(shortTerm, normal);
        
        boolean cancel = false;
        
        if (violation > 0){
        	data.fastClickVL += violation;
        	final ViolationData vd = new ViolationData(this, player, data.fastClickVL + violation, violation, cc.fastClickActions);
        	cancel = executeActions(vd);
        }
        
        if (cc.debug && player.hasPermission(Permissions.ADMINISTRATION_DEBUG)){
        	player.sendMessage("FastClick: " + data.fastClickFreq.bucketScore(0) + " | " + data.fastClickFreq.score(1f) + " | cursor=" + cursor + " | clicked=" + clicked);
        }
        
        return cancel;
    }
}
