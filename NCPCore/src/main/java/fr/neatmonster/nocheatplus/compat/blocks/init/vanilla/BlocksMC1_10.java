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
