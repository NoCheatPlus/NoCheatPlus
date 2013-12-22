package fr.neatmonster.nocheatplus.compat.blocks.init;

import org.bukkit.Material;

import fr.neatmonster.nocheatplus.utilities.BlockFlags;
import fr.neatmonster.nocheatplus.utilities.BlockProperties;

/**
 * Auxiliary methods for block initialization.
 * @author mc_dev
 *
 */
public class BlockInit {
	
	// TODO: Add "assertMaterialExist" for id and id + name (name: add assertMaterialMatches) also with name with RuntimeException
	
	/**
	 * Check for Material existence, throw RuntimeException if not.
	 * @param id
	 */
	public static void assertMaterialExists(int id) {
		if (Material.getMaterial(id) == null){
			throw new RuntimeException("Material " + id + " does not exist.");
		}
	}
	
	/**
	 * Check for material existence and naming (exact match). 
	 * @param id
	 * @param name
	 */
	public static void assertMaterialName(int id, String name) {
		Material mat = Material.getMaterial(id);
		if ( mat == null){
			throw new RuntimeException("Material " + id + " does not exist.");
		}
		if (mat.name().equals(name)){
			throw new RuntimeException("Name for Material " + id + " ('" + mat.name() + "') does not match '" + name + "'.");
		}
	}
	
	/**
	 * Check for material existence and naming (parts must all be contained with ignored case).
	 * @param id
	 * @param parts
	 */
	public static void assertMaterialNameMatch(int id, String... parts) {
		Material mat = Material.getMaterial(id);
		if ( mat == null){
			throw new RuntimeException("Material " + id + " does not exist.");
		}
		String name = mat.name().toLowerCase();
		for (String part : parts){
			if (name.indexOf(part.toLowerCase()) < 0){
				throw new RuntimeException("Name for Material " + id + " ('" + mat.name() + "') should contain '" + part + "'.");
			}
		}
	}
	
	/**
	 * Set block breaking properties same as the block of the given material.
	 * @param newId
	 * @param mat
	 */
	public static void setPropsAs(int newId, Material mat){
		setPropsAs(newId, mat.getId());
	}
	
	/**
	 * Set block breaking properties same as the block of the given id.
	 * @param newId
	 * @param mat
	 */
	public static void setPropsAs(int newId, int otherId){
		BlockProperties.setBlockProps(newId, BlockProperties.getBlockProps(otherId));
	}

	/**
	 * Set block breaking and shape properties same as the block of the given material.
	 * @param newId
	 * @param mat
	 */
	public static void setAs(int newId, Material mat){
		BlockFlags.setFlagsAs(newId, mat);
		setPropsAs(newId, mat);
	}
	
	/**
	 * Set block breaking and shape properties same as the block of the given id.
	 * @param newId
	 * @param mat
	 */
	public static void setAs(int newId, int otherId){
		BlockFlags.setFlagsAs(newId, otherId);
		setPropsAs(newId, otherId);
	}

}
