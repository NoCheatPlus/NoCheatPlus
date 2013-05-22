package fr.neatmonster.nocheatplus.compat.blocks.blockbreak;

/**
 * NOT YET IN USE.
 * <hr>
 * This class will contain block breaking properties for blocks and tools combinations. It will allow for generic definitions as well as overriding specific entries.
 * @author mc_dev
 *
 */
public class BlockBreakDurations {
	
	// TODO: 1. most specific one 2. general one (depth first).
	
	/**
	 * One entry for a specific block+tool combination.
	 * <hr>
	 * TODO: Name.
	 * 
	 * @author mc_dev
	 *
	 */
	public static interface BlockBreakEntry{
		
		/**
		 * TODO: Arguments.
		 * @return
		 */
		public long getBreakingDuration(); // Player player);
		
	}
	
	// TODO: abstract class DefaultBLockBreakEntry (sub method for using only relevant stuff). + notBreak(tool-id / tool-type) method
	// Could use a default provider (change once change all!?)
	
	public static final BlockBreakEntry INSTANT_BREAK = new BlockBreakEntry() {
		@Override
		public long getBreakingDuration() {
			return 0;
		}
	};
	
}
