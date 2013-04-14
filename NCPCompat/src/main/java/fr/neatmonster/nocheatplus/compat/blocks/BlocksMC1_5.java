package fr.neatmonster.nocheatplus.compat.blocks;

import org.bukkit.Material;

import fr.neatmonster.nocheatplus.compat.BlockPropertiesSetup;
import fr.neatmonster.nocheatplus.config.WorldConfigProvider;
import fr.neatmonster.nocheatplus.utilities.BlockFlags;
import fr.neatmonster.nocheatplus.utilities.BlockProperties;

/**
 * This is an attempt to add Minecraft 1.5 blocks information without actual 1.5 dependency.
 * @author mc_dev
 *
 */
public class BlocksMC1_5 implements BlockPropertiesSetup {
	
	/**
	 * TODO: Move somewhere.
	 * @param newId
	 * @param mat
	 */
	public static void setPropsAs(int newId, Material mat){
		BlockProperties.setBlockProps(newId, BlockProperties.getBlockProps(mat.getId()));
	}
	
	public static void setAs(int newId, Material mat){
		BlockFlags.setFlagsAs(newId, mat);
		setPropsAs(newId, mat);
	}
	
	public BlocksMC1_5(){
		// Test if materials exist.
		if (Material.getMaterial(152) == null){
			throw new RuntimeException("Material for 1.5 does not exist.");
		}
	}

	@Override
	public void setupBlockProperties(WorldConfigProvider<?> worldConfigProvider) {
		
		/////////////////////
		// New blocks
		////////////////////
		
		// 146 Trapped Chest
		setAs(146, Material.CHEST);
		
		// 147 Weighted Pressure Plate (Light)
//		BlockFlags.addFlags(147, BlockProperties.F_IGN_PASSABLE | BlockProperties.F_GROUND | BlockProperties.F_GROUND_HEIGHT);
		setAs(147, Material.STONE_PLATE);

		// 148 Weighted Pressure Plate (Heavy)
//		BlockFlags.addFlags(148, BlockProperties.F_IGN_PASSABLE | BlockProperties.F_GROUND | BlockProperties.F_GROUND_HEIGHT);
		setAs(148, Material.STONE_PLATE);
		
		// 149 Redstone Comparator (inactive)
//		BlockFlags.addFlags(149, BlockProperties.F_IGN_PASSABLE | BlockProperties.F_GROUND | BlockProperties.F_GROUND_HEIGHT);
		setAs(149, Material.DIODE_BLOCK_OFF);
		
		// 150 Redstone Comparator (active)
//		BlockFlags.addFlags(150, BlockProperties.F_IGN_PASSABLE | BlockProperties.F_GROUND | BlockProperties.F_GROUND_HEIGHT);
		setAs(150, Material.DIODE_BLOCK_ON);
		
		// 151 Daylight Sensor
//		BlockFlags.addFlags(151, BlockProperties.F_IGN_PASSABLE | BlockProperties.F_GROUND | BlockProperties.F_GROUND_HEIGHT);
		setAs(151, Material.HUGE_MUSHROOM_1);
		
		// 152 Block of Redstone
		setAs(152, Material.ENCHANTMENT_TABLE);
		
		// 153 Nether Quartz Ore
		setAs(153, Material.COAL_ORE);
		
		// 154 Hopper
		setAs(154, Material.COAL_ORE);
		// TODO: Needs workaround. [workaround-flag + different purpose flag sets ?]
		BlockFlags.addFlags(154, BlockProperties.F_IGN_PASSABLE | BlockProperties.F_GROUND_HEIGHT);
		
		// 155 Block of Quartz
		setAs(155, Material.SANDSTONE);
		
		// 156 Quartz Stairs
		setAs(156, Material.SANDSTONE_STAIRS);
		
		// 157 Activator Rail
		setAs(157, Material.DETECTOR_RAIL);
		
		// 158 Dropper
//		BlockFlags.setFlagsAs(158, Material.DISPENSER);
		setAs(158, Material.DISPENSER);
		
		
		/////////////////////
		// Changed blocks
		////////////////////
		
		// 78 Snow
		BlockFlags.addFlags(78, BlockProperties.F_HEIGHT_8_INC);
		BlockFlags.removeFlags(78, BlockProperties.F_HEIGHT_8SIM_INC);
		
		// 95 Locked chest
		BlockProperties.setBlockProps(95, BlockProperties.instantType);
	}

}
