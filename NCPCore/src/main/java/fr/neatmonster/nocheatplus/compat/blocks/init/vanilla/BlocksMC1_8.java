package fr.neatmonster.nocheatplus.compat.blocks.init.vanilla;

import org.bukkit.Material;

import fr.neatmonster.nocheatplus.compat.blocks.BlockPropertiesSetup;
import fr.neatmonster.nocheatplus.compat.blocks.init.BlockInit;
import fr.neatmonster.nocheatplus.config.WorldConfigProvider;
import fr.neatmonster.nocheatplus.logging.StaticLog;
import fr.neatmonster.nocheatplus.utilities.BlockFlags;

public class BlocksMC1_8 implements BlockPropertiesSetup {
    
    public BlocksMC1_8() {
        BlockInit.assertMaterialNameMatch(166, "barrier");
        BlockInit.assertMaterialNameMatch(165, "slime");
        BlockInit.assertMaterialNameMatch(187, "fence", "gate");
        BlockInit.assertMaterialNameMatch(176, "banner");
        BlockInit.assertMaterialNameMatch(169, "sea", "lantern");
    }
    
    @Override
    public void setupBlockProperties(WorldConfigProvider<?> worldConfigProvider) {
        // 165(SLIME_BLOCK
        BlockInit.setAs(165, Material.TNT); // Full block, instant break.
        
        // 166(BARRIER
        BlockInit.setAs(166, Material.BEDROCK); // Full block, unbreakable.
        
        // 167(IRON_TRAP_DOOR
        BlockFlags.setFlagsAs(167, Material.TRAP_DOOR);
        BlockInit.setPropsAs(167, Material.IRON_DOOR_BLOCK);
        
        // 168(PRISMARINE
        BlockInit.setAs(168, Material.STONE);
        
        // 169(SEA_LANTERN
        BlockInit.setAs(169, Material.REDSTONE_LAMP_OFF);
        
        // 176(STANDING_BANNER
        BlockInit.setInstantAir(176);
        
        // 177(WALL_BANNER
        BlockInit.setInstantAir(177);
        
        // 178(DAYLIGHT_DETECTOR_INVERTED
        BlockInit.setAs(178, Material.DAYLIGHT_DETECTOR);
        
        // 179(RED_SANDSTONE
        BlockInit.setAs(179, Material.SANDSTONE);
        
        // 180(RED_SANDSTONE_STAIRS
        BlockInit.setAs(180, Material.SANDSTONE_STAIRS);
        
        // 181(DOUBLE_STEP_2
        BlockInit.setAs(181, Material.DOUBLE_STEP); // TODO: red sandstone / prismarine ?
        
        // 182(STEP_2
        BlockInit.setAs(182, Material.STEP); // TODO: red sandstone / prismarine ?
        
        // 183(SPRUCE_FENCE_GATE
        BlockInit.setAs(183, Material.FENCE_GATE);
        
        // 184(BIRCH_FENCE_GATE
        BlockInit.setAs(184, Material.FENCE_GATE);
        
        // 185(JUNGLE_FENCE_GATE
        BlockInit.setAs(185, Material.FENCE_GATE);
        
        // 186(DARK_OAK_FENCE_GATE
        BlockInit.setAs(186, Material.FENCE_GATE);
        
        // 187(ACACIA_FENCE_GATE
        BlockInit.setAs(187, Material.FENCE_GATE);
        
        // 188(SPRUCE_FENCE
        BlockInit.setAs(188, Material.FENCE);
        
        // 189(BIRCH_FENCE
        BlockInit.setAs(189, Material.FENCE);
        
        // 190(JUNGLE_FENCE
        BlockInit.setAs(190, Material.FENCE);
        
        // 191(DARK_OAK_FENCE
        BlockInit.setAs(191, Material.FENCE);
        
        // 192(ACACIA_FENCE
        BlockInit.setAs(192, Material.FENCE);
        
        // 193(SPRUCE_DOOR
        BlockInit.setAs(193, Material.WOODEN_DOOR);
        
        // 194(BIRCH_DOOR
        BlockInit.setAs(194, Material.WOODEN_DOOR);
        
        // 195(JUNGLE_DOOR
        BlockInit.setAs(195, Material.WOODEN_DOOR);
        
        // 196(ACACIA_DOOR
        BlockInit.setAs(196, Material.WOODEN_DOOR);
        
        // 197(DARK_OAK_DOOR
        BlockInit.setAs(197, Material.WOODEN_DOOR);
        
        StaticLog.logInfo("[NoCheatPlus] Added block-info for Minecraft 1.8 blocks.");
    }

}
