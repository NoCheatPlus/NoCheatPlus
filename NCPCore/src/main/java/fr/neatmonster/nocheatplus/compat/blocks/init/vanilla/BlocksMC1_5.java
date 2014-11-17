package fr.neatmonster.nocheatplus.compat.blocks.init.vanilla;

import org.bukkit.Material;

import fr.neatmonster.nocheatplus.compat.blocks.BlockPropertiesSetup;
import fr.neatmonster.nocheatplus.compat.blocks.init.BlockInit;
import fr.neatmonster.nocheatplus.config.WorldConfigProvider;
import fr.neatmonster.nocheatplus.logging.StaticLog;
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
		BlockInit.assertMaterialNameMatch(152, "redstone", "block");
	}

	@Override
	public void setupBlockProperties(WorldConfigProvider<?> worldConfigProvider) {
		
		/////////////////////
		// New blocks
		////////////////////
		
		// 146 Trapped Chest
		BlockInit.setAs(146, Material.CHEST);
		
		// 147 Weighted Pressure Plate (Light)
//		BlockFlags.addFlags(147, BlockProperties.F_IGN_PASSABLE | BlockProperties.F_GROUND | BlockProperties.F_GROUND_HEIGHT);
		BlockInit.setAs(147, Material.STONE_PLATE);

		// 148 Weighted Pressure Plate (Heavy)
//		BlockFlags.addFlags(148, BlockProperties.F_IGN_PASSABLE | BlockProperties.F_GROUND | BlockProperties.F_GROUND_HEIGHT);
		BlockInit.setAs(148, Material.STONE_PLATE);
		
		// 149 Redstone Comparator (inactive)
//		BlockFlags.addFlags(149, BlockProperties.F_IGN_PASSABLE | BlockProperties.F_GROUND | BlockProperties.F_GROUND_HEIGHT);
		BlockInit.setAs(149, Material.DIODE_BLOCK_OFF);
		
		// 150 Redstone Comparator (active)
//		BlockFlags.addFlags(150, BlockProperties.F_IGN_PASSABLE | BlockProperties.F_GROUND | BlockProperties.F_GROUND_HEIGHT);
		BlockInit.setAs(150, Material.DIODE_BLOCK_ON);
		
		// 151 Daylight Sensor
//		BlockFlags.addFlags(151, BlockProperties.F_IGN_PASSABLE | BlockProperties.F_GROUND | BlockProperties.F_GROUND_HEIGHT);
		BlockInit.setAs(151, Material.HUGE_MUSHROOM_1);
		
		// 152 Block of Redstone
		BlockInit.setAs(152, Material.ENCHANTMENT_TABLE);
		
		// 153 Nether Quartz Ore
		BlockInit.setAs(153, Material.COAL_ORE);
		
		// 154 Hopper
		BlockInit.setAs(154, Material.COAL_ORE);
		// TODO: Needs workaround. [workaround-flag + different purpose flag sets ?]
		BlockFlags.addFlags(154, BlockProperties.F_IGN_PASSABLE | BlockProperties.F_GROUND_HEIGHT);
		
		// 155 Block of Quartz
		BlockInit.setAs(155, Material.SANDSTONE);
		
		// 156 Quartz Stairs
		BlockInit.setAs(156, Material.SANDSTONE_STAIRS);
		
		// 157 Activator Rail
		BlockInit.setAs(157, Material.DETECTOR_RAIL);
		
		// 158 Dropper
//		BlockFlags.setFlagsAs(158, Material.DISPENSER);
		BlockInit.setAs(158, Material.DISPENSER);
		
		
		/////////////////////
		// Changed blocks
		////////////////////
		
		// 78 Snow
		BlockFlags.addFlags(78, BlockProperties.F_HEIGHT_8_INC);
		BlockFlags.removeFlags(78, BlockProperties.F_HEIGHT_8SIM_INC);
		
		// 95 Locked chest
		BlockProperties.setBlockProps(95, BlockProperties.instantType);
		
		StaticLog.logInfo("[NoCheatPlus] Added block-info for Minecraft 1.5 blocks.");
	}

}
