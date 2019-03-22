package fr.neatmonster.nocheatplus.checks.inventory;

import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.players.IPlayerData;

/**
 * A simple check to prevent players from interacting with their inventory if they shouldn't be allowed to.
 */
public class InventoryMove extends Check {
	
	public InventoryMove() {
        super(CheckType.INVENTORY_MOVE);
    }
	
	public boolean check(final Player player, final InventoryData data, final IPlayerData pData) {
		
		boolean cancel = false;
		
		if (player.isBlocking() || player.isSprinting() || player.isSneaking() || player.isSwimming()) {
			cancel = executeActions(player, data.invMoveVL, 1.0, pData.getGenericInstance(InventoryConfig.class).invMoveActionList).willCancel();
		}
		return cancel;
	}
	
}
