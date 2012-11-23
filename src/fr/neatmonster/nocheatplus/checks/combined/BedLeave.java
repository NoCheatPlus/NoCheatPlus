package fr.neatmonster.nocheatplus.checks.combined;

import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;

public class BedLeave extends Check {

	public BedLeave() {
		super(CheckType.COMBINED_BEDLEAVE);
	}

	/**
	 * Checks a player.
	 * 
	 * @param player
	 *            the player
	 * @return Location to teleport to if it is a violation.
	 */
	public boolean checkBed(final Player player) {
		final CombinedData data = CombinedData.getData(player);
		
		boolean cancel = false;
		// Check if the player had been in bed at all.
		if (!data.wasInBed) {
			// Violation ...
			data.bedLeaveVL += 1D;
			
			// TODO: add tag

			// And return if we need to do something or not.
			if (executeActions(player, data.bedLeaveVL, 1D, CombinedConfig.getConfig(player).bedLeaveActions)){
				cancel = true;
			}
		} else{
			// He has, everything is alright.
			data.wasInBed = false;
		}
		return cancel;
	}
	
}
