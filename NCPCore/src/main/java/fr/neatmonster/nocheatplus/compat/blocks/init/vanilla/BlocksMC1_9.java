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

        final long ground = BlockProperties.F_GROUND;
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
        BlockInit.setAs("PURPUR_BLOCK", Material.SMOOTH_BRICK);

        // 202(PURPUR_PILLAR / SOLID+GROUND) 
        BlockInit.setAs("PURPUR_PILLAR", Material.SMOOTH_BRICK); // Rough.

        // 203(PURPUR_STAIRS / SOLID+GROUND) 
        BlockInit.setAs("PURPUR_STAIRS", Material.SMOOTH_STAIRS); // Rough.

        // 204(PURPUR_DOUBLE_SLAB / SOLID+GROUND) 
        BlockInit.setAs("PURPUR_DOUBLE_SLAB", Material.DOUBLE_STEP);

        // 205(PURPUR_SLAB / SOLID+GROUND) 
        BlockInit.setAs("PURPUR_SLAB", Material.STEP);

        // 206(END_BRICKS / SOLID+GROUND) 
        BlockInit.setAs("END_BRICKS", Material.SANDSTONE);

        // 207(BEETROOT_BLOCK) 
        BlockFlags.addFlags("BEETROOT_BLOCK", ground);
        BlockProperties.setBlockProps("BEETROOT_BLOCK", instant);

        // 208(GRASS_PATH / SOLID+GROUND) 
        BlockInit.setAs("GRASS_PATH", Material.GRASS);

        // 209(END_GATEWAY) 
        // -> Leave flags as is (like air).
        BlockProperties.setBlockProps("END_GATEWAY", BlockProperties.indestructibleType);

        // 210(COMMAND_REPEATING / SOLID+GROUND) 
        BlockInit.setAs("COMMAND_REPEATING", "COMMAND"); // Like command block.

        // 211(COMMAND_CHAIN / SOLID+GROUND) 
        BlockInit.setAs("COMMAND_CHAIN", "COMMAND"); // Like command block.

        // 212(FROSTED_ICE / SOLID+GROUND) 
        BlockInit.setAs("FROSTED_ICE", Material.ICE);

        // 255(STRUCTURE_BLOCK / SOLID+GROUND) 
        BlockInit.setInstantAir("STRUCTURE_BLOCK");

        // Special case activation.
        // TODO: Is this the right place?
        BlockProperties.setSpecialCaseTrapDoorAboveLadder(true);

        StaticLog.logInfo("Added block-info for Minecraft 1.9 blocks.");
    }

}
