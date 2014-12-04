package fr.neatmonster.nocheatplus.checks.blockinteract;

import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.permissions.Permissions;
import fr.neatmonster.nocheatplus.utilities.TickTask;

public class Speed extends Check {

	public Speed() {
		super(CheckType.BLOCKINTERACT_SPEED);
	}
	
	public boolean check(final Player player, final BlockInteractData data, final BlockInteractConfig cc){
		
		final long time = System.currentTimeMillis();
		
		if (time < data.speedTime || time > data.speedTime + cc.speedInterval){
			data.speedTime = time;
			data.speedCount = 0;
		}
		
		// Increase count.
		data.speedCount ++;
		
		boolean cancel = false;
		
		if (data.speedCount > cc.speedLimit){
			// Lag correction
			final int correctedCount = (int) ((double) data.speedCount / TickTask.getLag(time - data.speedTime, true));
			if (correctedCount > cc.speedLimit){
				data.speedVL ++;
				if (executeActions(player, data.speedVL, 1, cc.speedActions)){
					cancel = true;
				}
			}
			// else: keep vl.
		}
		else{
			data.speedVL *= 0.99;
		}
		
		if (data.debug && player.hasPermission(Permissions.ADMINISTRATION_DEBUG)){
			player.sendMessage("Interact speed: " + data.speedCount);
		}
		
		return cancel;
	}

}
