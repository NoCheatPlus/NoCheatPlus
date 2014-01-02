package fr.neatmonster.nocheatplus.compat.blocks.init.vanilla;

import org.bukkit.Material;

import fr.neatmonster.nocheatplus.compat.blocks.BlockPropertiesSetup;
import fr.neatmonster.nocheatplus.compat.blocks.init.BlockInit;
import fr.neatmonster.nocheatplus.config.WorldConfigProvider;
import fr.neatmonster.nocheatplus.logging.LogUtil;


public class BlocksMC1_7_2 implements BlockPropertiesSetup{
	
	public BlocksMC1_7_2() {
		BlockInit.assertMaterialNameMatch(95, "stained", "glass");
		BlockInit.assertMaterialNameMatch(174, "packed", "ice");
	}

	@Override
	public void setupBlockProperties(WorldConfigProvider<?> worldConfigProvider) {
		// Stained glass
		BlockInit.setAs(95, Material.GLASS);
		// Stained glass pane
		BlockInit.setAs(160, 102);
		// Leaves 2
		BlockInit.setAs(161, Material.LEAVES);
		// Log 2
		BlockInit.setAs(162, Material.LOG);
		// Acacia wood stairs
		BlockInit.setAs(163, Material.WOOD_STAIRS);
		// Oak wood stairs
		BlockInit.setAs(164, Material.WOOD_STAIRS);
		// Packed ice
		BlockInit.setAs(174, Material.ICE);
		// Large flowers
		BlockInit.setAs(175, Material.YELLOW_FLOWER);
		
		LogUtil.logInfo("[NoCheatPlus] Added block-info for Minecraft 1.7.2 blocks.");
	}

}
