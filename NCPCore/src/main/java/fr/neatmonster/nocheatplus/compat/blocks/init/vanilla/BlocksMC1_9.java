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
        BlockInit.assertMaterialNameMatch(198, "end_rod");
        BlockInit.assertMaterialNameMatch(208, "GRASS_PATH");
        BlockInit.assertMaterialNameMatch(209, "END_GATEWAY");
    }

    @Override
    public void setupBlockProperties(WorldConfigProvider<?> worldConfigProvider) {

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
        BlockInit.setInstantAir(255);

        // Special case activation.
        // TODO: Is this the right place?
        BlockProperties.setSpecialCaseTrapDoorAboveLadder(true);

        StaticLog.logInfo("Added block-info for Minecraft 1.9 blocks.");
    }

}
