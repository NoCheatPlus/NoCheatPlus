package fr.neatmonster.nocheatplus.compat.blocks.init.vanilla;

import fr.neatmonster.nocheatplus.compat.blocks.BlockPropertiesSetup;
import fr.neatmonster.nocheatplus.compat.blocks.init.BlockInit;
import fr.neatmonster.nocheatplus.config.WorldConfigProvider;
import fr.neatmonster.nocheatplus.logging.StaticLog;
import fr.neatmonster.nocheatplus.utilities.map.BlockProperties;
import fr.neatmonster.nocheatplus.utilities.map.BlockProperties.BlockProps;

@SuppressWarnings("deprecation")
public class BlocksMC1_11 implements BlockPropertiesSetup {

    private static final int first_shulker_box = 219;
    private static final int last_shulker_box = 234;

    public BlocksMC1_11() {
        BlockInit.assertMaterialNameMatch(218, "OBSERVER");
        for (int i = first_shulker_box; i <= last_shulker_box; i++) {
            BlockInit.assertMaterialNameMatch(i, "SHULKER_BOX");
        }
    }

    @Override
    public void setupBlockProperties(WorldConfigProvider<?> worldConfigProvider) {
        long solidFlags = BlockProperties.F_SOLID | BlockProperties.F_GROUND;
        // 218 OBSERVER
        // Wiki (16-11-25): 17.5, 2.65, 1.32, 0.9, 0.7, 0.45
        BlockProperties.setBlockProps(218, new BlockProps(BlockProperties.woodPickaxe, 6,
                BlockProperties.secToMs(15.0, 2.2, 1.1, 0.7, 0.55, 0.45)));
        BlockProperties.setBlockFlags(218, solidFlags);
        // ALL SORTS OF SHULKER BOXES
        for (int i = first_shulker_box; i <= last_shulker_box; i++) {
            // Wiki (16-11-25): 9, 4.5, 2.25, 1.5, 1.15, 0.75
            BlockProperties.setBlockProps(i, new BlockProps(BlockProperties.woodPickaxe, 6,
                    BlockProperties.secToMs(10.0, 1.45, 0.7, 0.5, 0.35, 0.2)));
            BlockProperties.setBlockFlags(i, solidFlags);
        }
        StaticLog.logInfo("Added block-info for Minecraft 1.11 blocks.");
    }

}
