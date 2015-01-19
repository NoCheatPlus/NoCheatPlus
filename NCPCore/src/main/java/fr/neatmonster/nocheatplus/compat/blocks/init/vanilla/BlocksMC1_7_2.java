package fr.neatmonster.nocheatplus.compat.blocks.init.vanilla;

import org.bukkit.Material;

import fr.neatmonster.nocheatplus.compat.blocks.BlockPropertiesSetup;
import fr.neatmonster.nocheatplus.compat.blocks.init.BlockInit;
import fr.neatmonster.nocheatplus.config.WorldConfigProvider;
import fr.neatmonster.nocheatplus.logging.StaticLog;
import fr.neatmonster.nocheatplus.utilities.BlockProperties;
import fr.neatmonster.nocheatplus.utilities.BlockProperties.BlockProps;


@SuppressWarnings("deprecation")
public class BlocksMC1_7_2 implements BlockPropertiesSetup{

    public BlocksMC1_7_2() {
        BlockInit.assertMaterialNameMatch(95, "stained", "glass");
        BlockInit.assertMaterialNameMatch(174, "packed", "ice");
    }

    @Override
    public void setupBlockProperties(WorldConfigProvider<?> worldConfigProvider) {
        // Block shapes.

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

        // Block breaking.
        final long[] ironTimes = BlockProperties.secToMs(15, 7.5, 1.15, 0.75, 0.56, 1.25);
        final BlockProps ironType = new BlockProps(BlockProperties.woodPickaxe, 3, ironTimes);
        for (Material mat : new Material[]{
                Material.LAPIS_ORE, Material.LAPIS_BLOCK, Material.IRON_ORE,
        }) {
            BlockProperties.setBlockProps(BlockProperties.getId(mat), ironType);
        }
        final long[] diamondTimes = BlockProperties.secToMs(15, 7.5, 3.75, 0.75, 0.56, 1.25);
        final BlockProps diamondType = new BlockProps(BlockProperties.woodPickaxe, 3, diamondTimes);
        for (Material mat : new Material[]{
                Material.REDSTONE_ORE, Material.GLOWING_REDSTONE_ORE,
                Material.EMERALD_ORE, Material.GOLD_ORE, Material.DIAMOND_ORE,
        }) {
            BlockProperties.setBlockProps(BlockProperties.getId(mat), diamondType);
        }

        StaticLog.logInfo("[NoCheatPlus] Added block-info for Minecraft 1.7.2 blocks.");
    }

}
