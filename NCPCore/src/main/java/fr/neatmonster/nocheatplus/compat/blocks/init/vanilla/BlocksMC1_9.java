package fr.neatmonster.nocheatplus.compat.blocks.init.vanilla;

import org.bukkit.Material;

import fr.neatmonster.nocheatplus.compat.blocks.BlockPropertiesSetup;
import fr.neatmonster.nocheatplus.compat.blocks.init.BlockInit;
import fr.neatmonster.nocheatplus.config.WorldConfigProvider;
import fr.neatmonster.nocheatplus.logging.StaticLog;
import fr.neatmonster.nocheatplus.utilities.BlockFlags;
import fr.neatmonster.nocheatplus.utilities.BlockProperties;
import fr.neatmonster.nocheatplus.utilities.BlockProperties.BlockProps;

@SuppressWarnings("deprecation")
public class BlocksMC1_9 implements BlockPropertiesSetup {

    public BlocksMC1_9() {
        BlockInit.assertMaterialNameMatch(198, "end_rod");
        BlockInit.assertMaterialNameMatch(208, "GRASS_PATH");
        BlockInit.assertMaterialNameMatch(209, "END_GATEWAY");
    }

    @Override
    public void setupBlockProperties(WorldConfigProvider<?> worldConfigProvider) {

        // TODO: Actual similarly fast/slow breaking blocks ?

        final long ground = BlockProperties.F_GROUND;
        final BlockProps instant = BlockProperties.instantType;

        // 198(END_ROD)
        BlockFlags.addFlags(198, ground);
        BlockProperties.setBlockProps(198, instant);

        // 199(CHORUS_PLANT) 
        BlockFlags.addFlags(199, ground);
        BlockProperties.setBlockProps(199, instant);

        // 200(CHORUS_FLOWER) 
        BlockFlags.addFlags(200, ground);
        BlockProperties.setBlockProps(200, instant);

        // 201(PURPUR_BLOCK / SOLID+GROUND) 
        BlockInit.setAs(201, Material.SMOOTH_BRICK);

        // 202(PURPUR_PILLAR / SOLID+GROUND) 
        BlockInit.setAs(202, Material.SMOOTH_BRICK); // Rough.

        // 203(PURPUR_STAIRS / SOLID+GROUND) 
        BlockInit.setAs(203, Material.SMOOTH_STAIRS); // Rough.

        // 204(PURPUR_DOUBLE_SLAB / SOLID+GROUND) 
        BlockInit.setAs(204, Material.DOUBLE_STEP);

        // 205(PURPUR_SLAB / SOLID+GROUND) 
        BlockInit.setAs(205, Material.STEP);

        // 206(END_BRICKS / SOLID+GROUND) 
        BlockInit.setAs(206, Material.SANDSTONE);

        // 207(BEETROOT_BLOCK) 
        BlockFlags.addFlags(207, ground);
        BlockProperties.setBlockProps(207, instant);

        // 208(GRASS_PATH / SOLID+GROUND) 
        BlockInit.setAs(208, Material.GRASS);

        // 209(END_GATEWAY) 
        // -> Leave flags as is (like air).
        BlockProperties.setBlockProps(209, BlockProperties.indestructibleType);

        // 210(COMMAND_REPEATING / SOLID+GROUND) 
        BlockInit.setAs(210, 137); // Like command block.

        // 211(COMMAND_CHAIN / SOLID+GROUND) 
        BlockInit.setAs(211, 137); // Like command block.

        // 212(FROSTED_ICE / SOLID+GROUND) 
        BlockInit.setAs(212, Material.ICE);

        // 255(STRUCTURE_BLOCK / SOLID+GROUND) 
        BlockInit.setAs(255, Material.BEDROCK);

        StaticLog.logInfo("Added block-info for Minecraft 1.8 blocks.");
    }

}
