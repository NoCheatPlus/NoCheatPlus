package fr.neatmonster.nocheatplus.checks.combined;

import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.compat.AlmostBoolean;

public class BedLeave extends Check {

	public BedLeave() {
		super(CheckType.COMBINED_BEDLEAVE);
	}

	/**
	 * Checks a player.
	 * 
	 * @param player
	 *            the player
	 * @return If to prevent action (would be set back location of survivalfly).
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
			// He has, everything is allright.
			data.wasInBed = false;
			// TODO: think about decreasing the vl ?
		}
		return cancel;
	}
	
}
