package fr.neatmonster.nocheatplus.utilities;

import org.bukkit.Material;

/**
 * Utilities for block-flags.<br>
 * Later the flag constant definitions and parsing might be moved here.
 * @author mc_dev
 *
 */
public class BlockFlags {

	/**
	 * Set flags of id same as already set with flags for the given material. (Uses BlockProperties.)
	 * @param id
	 * @param mat
	 */
	public static void setFlagsAs(int id, Material mat){
		BlockProperties.setBlockFlags(id, BlockProperties.getBlockFlags(mat.getId()));
	}

	/**
	 * Add flags to existent flags. (Uses BlockProperties.)
	 * @param id
	 * @param flags
	 */
	public static void addFlags(int id, long flags){
		BlockProperties.setBlockFlags(id, BlockProperties.getBlockFlags(id) | flags);
	}

	/**
	 * Remove the given flags from existent flags.  (Uses BlockProperties.)
	 * @param id
	 * @param flags
	 */
	public static void removeFlags(int id, long flags){
		BlockProperties.setBlockFlags(id, BlockProperties.getBlockFlags(id) & ~flags);
	}

}
