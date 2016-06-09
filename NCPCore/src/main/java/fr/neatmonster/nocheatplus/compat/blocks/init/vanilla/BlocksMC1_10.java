package fr.neatmonster.nocheatplus.compat.blocks.init.vanilla;

import org.bukkit.Material;

import fr.neatmonster.nocheatplus.compat.blocks.BlockPropertiesSetup;
import fr.neatmonster.nocheatplus.compat.blocks.init.BlockInit;
import fr.neatmonster.nocheatplus.config.WorldConfigProvider;
import fr.neatmonster.nocheatplus.logging.StaticLog;

/**
 * Blocks for Minecraft 1.10.
 * 
 * @author asofold
 *
 */
public class BlocksMC1_10 implements BlockPropertiesSetup {

    public BlocksMC1_10() {
        BlockInit.assertMaterialNameMatch(213, "MAGMA");
        BlockInit.assertMaterialNameMatch(216, "BONE_BLOCK");
        BlockInit.assertMaterialNameMatch(217, "STRUCTURE_VOID");
    }

    @Override
    public void setupBlockProperties(WorldConfigProvider<?> worldConfigProvider) {
        // 213 MAGMA
        BlockInit.setAs(213, Material.STONE_PLATE);
        // 214 NETHER_WART_BLOCK
        BlockInit.setAs(214, Material.SKULL);
        // 215 RED_NETHER_BRICK
        BlockInit.setAs(215, Material.NETHER_BRICK);
        // 216 BONE_BLOCK
        BlockInit.setAs(216, Material.COBBLESTONE);
        // 217 STRUCTURE_VOID
        BlockInit.setAs(217, 255); // Like STRUCTURE_BLOCK.

        StaticLog.logInfo("Added block-info for Minecraft 1.10 blocks.");
    }

}
