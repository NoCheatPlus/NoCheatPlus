/*
 * This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package fr.neatmonster.nocheatplus.checks.inventory;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.ViolationData;
import fr.neatmonster.nocheatplus.checks.combined.Improbable;
import fr.neatmonster.nocheatplus.permissions.Permissions;
import fr.neatmonster.nocheatplus.players.IPlayerData;
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
    //                if (TickTask.getLag(150, true) > 1.7f) {
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

    public boolean check(final Player player, final long now, 
            final InventoryView view, final int slot, final ItemStack cursor, 
            final ItemStack clicked, final boolean isShiftClick, 
            final String inventoryAction, final InventoryData data, final InventoryConfig cc, 
            final IPlayerData pData) {
        // Take time once.

        final float amount;
        final Material clickedMat = clicked == null ? Material.AIR : clicked.getType();
        final Material cursorMat;
        final int cursorAmount;
        if (cursor != null) {
            cursorMat = cursor.getType();
            cursorAmount = Math.max(1, cursor.getAmount());
        }
        else {
            cursorMat = null;
            cursorAmount = 0;
        }

        if (inventoryAction != null) {
            amount = getAmountWithAction(view, slot, clicked, clickedMat, 
                    cursorMat, cursorAmount, isShiftClick, inventoryAction, 
                    data, cc);
        }
        else if (cursor != null && cc.fastClickTweaks1_5) {
            // Detect shift-click features indirectly.
            amount = detectTweaks1_5(view, slot, clicked, clickedMat,
                    cursorMat, cursorAmount, isShiftClick, data, cc);
        }
        else {
            amount = 1f;
        }

        data.fastClickFreq.add(now, amount);

        float shortTerm = data.fastClickFreq.bucketScore(0);
        if (shortTerm > cc.fastClickShortTermLimit) {
            // Check for lag.
            shortTerm /= (float) TickTask.getLag(data.fastClickFreq.bucketDuration(), true);
        }
        shortTerm -= cc.fastClickShortTermLimit;

        float normal = data.fastClickFreq.score(1f);
        if (normal > cc.fastClickNormalLimit) {
            // Check for lag.
            normal /= (float) TickTask.getLag(data.fastClickFreq.bucketDuration() * data.fastClickFreq.numberOfBuckets(), true);
        }
        normal -= cc.fastClickNormalLimit;

        final double violation = Math.max(shortTerm, normal);

        boolean cancel = false;

        if (violation > 0) {
            data.fastClickVL += violation;
            final ViolationData vd = new ViolationData(this, player, data.fastClickVL, violation, cc.fastClickActions);
            cancel = executeActions(vd).willCancel();
        }

        if (pData.isDebugActive(type) && pData.hasPermission(Permissions.ADMINISTRATION_DEBUG, player)) {
            player.sendMessage("FastClick: " + data.fastClickFreq.bucketScore(0) + " | " + data.fastClickFreq.score(1f) + " | cursor=" + cursor + " | clicked=" + clicked + " | action=" + inventoryAction);
        }

        data.fastClickLastClicked = clickedMat;
        data.fastClickLastSlot = slot;
        data.fastClickLastCursor = cursorMat;
        data.fastClickLastCursorAmount = cursorAmount;

        // Feed the improbable.
        Improbable.feed(player, 0.7f * amount, System.currentTimeMillis());

        return cancel;
    }

    private float detectTweaks1_5(final InventoryView view, 
            final int slot, final ItemStack clicked, 
            final Material clickedMat, final Material cursorMat, 
            final int cursorAmount, final boolean isShiftClick, 
            final InventoryData data, final InventoryConfig cc) {
        if (cursorMat != data.fastClickLastCursor 
                && (!isShiftClick || clickedMat == Material.AIR || clickedMat != data.fastClickLastClicked) 
                || cursorMat == Material.AIR || cursorAmount != data.fastClickLastCursorAmount) {
            return 1f;
        }
        else if (clickedMat == Material.AIR || clickedMat == cursorMat 
                || isShiftClick && clickedMat == data.fastClickLastClicked ) {
            return Math.min(cc.fastClickNormalLimit , cc.fastClickShortTermLimit) 
                    / (float) (isShiftClick && clickedMat != Material.AIR ? (1.0 + Math.max(cursorAmount, InventoryUtil.getStackCount(view, clicked))) : cursorAmount)  * 0.75f;
        }
        else{
            return 1f;
        }
    }

    private float getAmountWithAction(final InventoryView view, 
            final int slot, final ItemStack clicked, 
            final Material clickedMat, final Material cursorMat, 
            final int cursorAmount, final boolean isShiftClick, 
            final String inventoryAction,
            final InventoryData data, final InventoryConfig cc) {
        // Continuous drop feature with open inventories.
        if (inventoryAction.equals("DROP_ONE_SLOT")
                && slot == data.fastClickLastSlot 
                && clickedMat == data.fastClickLastClicked
                && view.getType() == InventoryType.CRAFTING
                // TODO: Distinguish if the inventory is really open.
                ) {
            return 0.6f;
        }

        // Collect to cursor.
        if (inventoryAction.equals("COLLECT_TO_CURSOR")) {
            final int stackCount = InventoryUtil.getStackCount(view, clicked);
            return stackCount <= 0 ? 1f : 
                Math.min(cc.fastClickNormalLimit , cc.fastClickShortTermLimit) 
                / stackCount * 0.75f;
        }

        // Shift click features.
        if ((inventoryAction.equals("MOVE_TO_OTHER_INVENTORY"))
                && cursorMat != Material.AIR && cc.fastClickTweaks1_5) {
            // Let the legacy method do the side condition checks and counting for now.
            return detectTweaks1_5(view, slot, clicked, clickedMat, 
                    cursorMat, cursorAmount, isShiftClick, data, cc);
        }

        // 
        return 1f;
    }
}
