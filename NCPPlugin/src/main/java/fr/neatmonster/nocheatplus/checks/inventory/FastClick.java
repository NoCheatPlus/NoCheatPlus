package fr.neatmonster.nocheatplus.checks.inventory;

import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.ViolationData;
import fr.neatmonster.nocheatplus.permissions.Permissions;
import fr.neatmonster.nocheatplus.utilities.TickTask;

/*
 * MM""""""""`M                     dP   MM'""""'YMM dP oo          dP       
 * MM  mmmmmmmM                     88   M' .mmm. `M 88             88       
 * M'      MMMM .d8888b. .d8888b. d8888P M  MMMMMooM 88 dP .d8888b. 88  .dP  
 * MM  MMMMMMMM 88'  `88 Y8ooooo.   88   M  MMMMMMMM 88 88 88'  `"" 88888"   
 * MM  MMMMMMMM 88.  .88       88   88   M. `MMM' .M 88 88 88.  ... 88  `8b. 
 * MM  MMMMMMMM `88888P8 `88888P'   dP   MM.     .dM dP dP `88888P' dP   `YP 
 * MMMMMMMMMMMM                          MMMMMMMMMMM                         
 */
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
//                if (TickTask.getLag(150) > 1.7f){
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
    
    public boolean check(final Player player, final long now, final InventoryData data, final InventoryConfig cc) {
    	// Take time once.
        
        data.fastClickFreq.add(now, 1f);
        
        float shortTerm = data.fastClickFreq.bucketScore(0);
        if (shortTerm > cc.fastClickShortTermLimit){
        	// Check for lag.
        	shortTerm /= (float) TickTask.getLag(data.fastClickFreq.bucketDuration());
        }
        shortTerm -= cc.fastClickShortTermLimit;
        
        float normal = data.fastClickFreq.score(1f);
        if (normal > cc.fastClickNormalLimit){
        	// Check for lag.
        	normal /= (float) TickTask.getLag(data.fastClickFreq.bucketDuration() * data.fastClickFreq.numberOfBuckets());
        }
        normal -= cc.fastClickNormalLimit;

        final double violation = Math.max(shortTerm, normal);
        
        boolean cancel = false;
        
        if (violation > 0){
        	final ViolationData vd = new ViolationData(this, player, data.fastClickVL + violation, violation, cc.fastClickActions);
        	cancel = executeActions(vd);
        }
        
        if (cc.debug && player.hasPermission(Permissions.ADMINISTRATION_DEBUG)){
        	player.sendMessage("FastClick: " + ((int) data.fastClickFreq.bucketScore(0)) + " / " + ((int) data.fastClickFreq.score(1f)));
        }
        
        return cancel;
    }
}
