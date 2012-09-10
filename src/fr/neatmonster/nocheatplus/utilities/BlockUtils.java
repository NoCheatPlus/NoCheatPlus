package fr.neatmonster.nocheatplus.utilities;

import org.bukkit.inventory.ItemStack;

/**
 * Poperties of blocks.
 * @author mc_dev
 *
 */
public class BlockUtils {
	/** Properties of a tool. */
	public static class ToolProps{
		
	}
	/** Properties of a block. */
	public static class BlockProps{
		public float hardness = 1;
		
	}
	
	/**
	 * Get the normal breaking duration, including enchantments, and tool properties.
	 * @param blockId
	 * @param itemInHand
	 * @return
	 */
	public static long getBreakingDuration(final int blockId, final ItemStack itemInHand){
		// TODO: GET EXACT BREAKING TIME !
		return 95;
	}
}
