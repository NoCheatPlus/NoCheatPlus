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
		BlockFlags.setFlagsAs(146, Material.CHEST);
		BlockProperties.setBlockProps(146, BlockProperties.getBlockProps(Material.CHEST.getId()));
		
		// 147 Weighted Pressure Plate (Light)
		// 148 Weighted Pressure Plate (Heavy)
		BlockFlags.addFlags(147, BlockProperties.F_IGN_PASSABLE | BlockProperties.F_GROUND | BlockProperties.F_GROUND_HEIGHT);
		BlockFlags.addFlags(148, BlockProperties.F_IGN_PASSABLE | BlockProperties.F_GROUND | BlockProperties.F_GROUND_HEIGHT);
		
		// 149 Redstone Comparator (inactive)
		// 150 Redstone Comparator (active)
		BlockFlags.addFlags(149, BlockProperties.F_IGN_PASSABLE | BlockProperties.F_GROUND | BlockProperties.F_GROUND_HEIGHT);
		BlockFlags.addFlags(150, BlockProperties.F_IGN_PASSABLE | BlockProperties.F_GROUND | BlockProperties.F_GROUND_HEIGHT);
		
		// 151 Daylight Sensor
		BlockFlags.addFlags(151, BlockProperties.F_IGN_PASSABLE | BlockProperties.F_GROUND | BlockProperties.F_GROUND_HEIGHT);
		
		// 152 Block of Redstone
		// 153 Nether Quartz Ore
		// 155 Block of Quartz
		
		// 154 Hopper
		BlockFlags.addFlags(144, BlockProperties.F_IGN_PASSABLE | BlockProperties.F_GROUND_HEIGHT);
		
		// 158 Dropper
		BlockFlags.setFlagsAs(158, Material.DISPENSER);

		// 156 Quartz Stairs
		BlockFlags.setFlagsAs(156, Material.COBBLESTONE_STAIRS);
		
		// 157 Activator Rail
		BlockFlags.setFlagsAs(157, Material.DETECTOR_RAIL);
		
		/////////////////////
		// Changed blocks
		////////////////////
		
		// 78 Snow
		BlockFlags.addFlags(78, BlockProperties.F_HEIGHT_8_INC);
		BlockFlags.removeFlags(78, BlockProperties.F_HEIGHT_8SIM_INC);
		
	}

}
