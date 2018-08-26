/*
 * This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package fr.neatmonster.nocheatplus.compat.blocks.init.vanilla;

import org.bukkit.Material;

import fr.neatmonster.nocheatplus.compat.BridgeMaterial;
import fr.neatmonster.nocheatplus.compat.blocks.BlockPropertiesSetup;
import fr.neatmonster.nocheatplus.compat.blocks.init.BlockInit;
import fr.neatmonster.nocheatplus.config.WorldConfigProvider;
import fr.neatmonster.nocheatplus.logging.StaticLog;
import fr.neatmonster.nocheatplus.utilities.map.BlockFlags;
import fr.neatmonster.nocheatplus.utilities.map.BlockProperties;
import fr.neatmonster.nocheatplus.utilities.map.BlockProperties.BlockProps;

@SuppressWarnings("deprecation")
public class BlocksMC1_9 implements BlockPropertiesSetup {

    public BlocksMC1_9() {
        BlockInit.assertMaterialExists("END_ROD");
        BlockInit.assertMaterialExists("GRASS_PATH");
        BlockInit.assertMaterialExists("END_GATEWAY");
    }

    @Override
    public void setupBlockProperties(WorldConfigProvider<?> worldConfigProvider) {

        final long ground = BlockFlags.SOLID_GROUND; // BlockProperties.F_GROUND;
        final BlockProps instant = BlockProperties.instantType;

        // 198(END_ROD)
        BlockFlags.addFlags("END_ROD", ground);
        BlockProperties.setBlockProps("END_ROD", instant);

        // 199(CHORUS_PLANT) 
        BlockFlags.addFlags("CHORUS_PLANT", ground);
        BlockProperties.setBlockProps("CHORUS_PLANT", instant);

        // 200(CHORUS_FLOWER) 
        BlockFlags.addFlags("CHORUS_FLOWER", ground);
        BlockProperties.setBlockProps("CHORUS_FLOWER", instant);

        // 201(PURPUR_BLOCK / SOLID+GROUND) 
        BlockInit.setAs("PURPUR_BLOCK", BridgeMaterial.STONE_BRICKS);

        // 202(PURPUR_PILLAR / SOLID+GROUND) 
        BlockInit.setAs("PURPUR_PILLAR", BridgeMaterial.STONE_BRICKS); // Rough.

        // 203(PURPUR_STAIRS / SOLID+GROUND) 
        BlockInit.setAs("PURPUR_STAIRS", BridgeMaterial.STONE_BRICK_STAIRS); // Rough.

        // 204(PURPUR_DOUBLE_SLAB / SOLID+GROUND) 
        if (BridgeMaterial.has("PURPUR_DOUBLE_SLAB")) {
            if (BridgeMaterial.has("PURPUR_DOUBLE_SLAB")) {
                BlockInit.setAs("PURPUR_DOUBLE_SLAB", "DOUBLE_STEP");
            }
        }

        // 205(PURPUR_SLAB / SOLID+GROUND) 
        BlockInit.setAs("PURPUR_SLAB", BridgeMaterial.STONE_SLAB);

        // 206(END_BRICKS / SOLID+GROUND) 
        BlockInit.setAs(BridgeMaterial.END_STONE_BRICKS, Material.SANDSTONE);

        // 207(BEETROOT_BLOCK) 
        BlockInit.setInstantPassable(BridgeMaterial.BEETROOTS);

        // 208(GRASS_PATH / SOLID+GROUND) 
        BlockInit.setAs("GRASS_PATH", BridgeMaterial.GRASS_BLOCK); // Later it'll be lower!

        // 209(END_GATEWAY) 
        // -> Leave flags as is (like air).
        BlockProperties.setBlockProps("END_GATEWAY", BlockProperties.indestructibleType);

        // 210(COMMAND_REPEATING / SOLID+GROUND) 
        BlockInit.setAs(BridgeMaterial.REPEATING_COMMAND_BLOCK, BridgeMaterial.COMMAND_BLOCK);

        // 211(COMMAND_CHAIN / SOLID+GROUND) 
        BlockInit.setAs(BridgeMaterial.CHAIN_COMMAND_BLOCK, BridgeMaterial.COMMAND_BLOCK);

        // 212(FROSTED_ICE / SOLID+GROUND) 
        BlockInit.setAs("FROSTED_ICE", Material.ICE);

        // 255(STRUCTURE_BLOCK / SOLID+GROUND) 
        BlockInit.setInstantPassable("STRUCTURE_BLOCK");

        // Special case activation.
        // TODO: Is this the right place?
        BlockProperties.setSpecialCaseTrapDoorAboveLadder(true);

        StaticLog.logInfo("Added block-info for Minecraft 1.9 blocks.");
    }

}
