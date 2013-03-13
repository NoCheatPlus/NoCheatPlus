package fr.neatmonster.nocheatplus.compat.blocks;

import org.bukkit.Material;

import fr.neatmonster.nocheatplus.compat.BlockPropertiesSetup;
import fr.neatmonster.nocheatplus.config.WorldConfigProvider;
import fr.neatmonster.nocheatplus.utilities.BlockProperties;

/**
 * This is an attempt to add Minecraft 1.5 blocks information without actual 1.5 dependency.
 * @author mc_dev
 *
 */
public class BlocksMC1_5 implements BlockPropertiesSetup {
	
	public BlocksMC1_5(){
		// Test if materials exist.
		if (Material.getMaterial(152) == null){
			throw new RuntimeException("Material for 1.5 does not exist.");
		}
	}

	@Override
	public void setupBlockProperties(WorldConfigProvider<?> worldConfigProvider) {
		
		// TODO: Block flag info ...
		// TODO: Tool and breaking time infos...
		
		// TODO: This is guessing !
		
		/////////////////////
		// New blocks
		////////////////////
		
		// 146 Trapped Chest
		setFlagsAs(146, Material.CHEST);
		BlockProperties.setBlockProps(146, BlockProperties.getBlockProps(Material.CHEST.getId()));
		
		// 147 Weighted Pressure Plate (Light)
		// 148 Weighted Pressure Plate (Heavy)
		addFlags(147, BlockProperties.F_IGN_PASSABLE | BlockProperties.F_GROUND | BlockProperties.F_GROUND_HEIGHT);
		addFlags(148, BlockProperties.F_IGN_PASSABLE | BlockProperties.F_GROUND | BlockProperties.F_GROUND_HEIGHT);
		
		// 149 Redstone Comparator (inactive)
		// 150 Redstone Comparator (active)
		addFlags(149, BlockProperties.F_IGN_PASSABLE | BlockProperties.F_GROUND | BlockProperties.F_GROUND_HEIGHT);
		addFlags(150, BlockProperties.F_IGN_PASSABLE | BlockProperties.F_GROUND | BlockProperties.F_GROUND_HEIGHT);
		
		// 151 Daylight Sensor
		addFlags(151, BlockProperties.F_IGN_PASSABLE | BlockProperties.F_GROUND | BlockProperties.F_GROUND_HEIGHT);
		
		// 152 Block of Redstone
		// 153 Nether Quartz Ore
		// 155 Block of Quartz
		
		// 154 Hopper
		addFlags(144, BlockProperties.F_IGN_PASSABLE | BlockProperties.F_GROUND_HEIGHT);
		
		// 158 Dropper
		setFlagsAs(158, Material.DISPENSER);

		// 156 Quartz Stairs
		setFlagsAs(156, Material.COBBLESTONE_STAIRS);
		
		// 157 Activator Rail
		setFlagsAs(157, Material.DETECTOR_RAIL);
		
		/////////////////////
		// Changed blocks
		////////////////////
		
		// 78 Snow
		addFlags(78, BlockProperties.F_HEIGHT_8_INC);
		removeFlags(78, BlockProperties.F_HEIGHT_8SIM_INC);
		
	}
	
	/**
	 * Set flags of id same as with material.
	 * @param id
	 * @param mat
	 */
	public static void setFlagsAs(int id, Material mat){
		BlockProperties.setBlockFlags(id, BlockProperties.getBlockFlags(mat.getId()));
	}
	
	public static void addFlags(int id, long flags){
		BlockProperties.setBlockFlags(id, BlockProperties.getBlockFlags(id) | flags);
	}
	
	public static void removeFlags(int id, long flags){
		BlockProperties.setBlockFlags(id, BlockProperties.getBlockFlags(id) & ~flags);
	}

}
