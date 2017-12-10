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
        BlockInit.assertMaterialExists("MAGMA");
        BlockInit.assertMaterialExists("BONE_BLOCK");
        BlockInit.assertMaterialExists("STRUCTURE_VOID");
    }

    @Override
    public void setupBlockProperties(WorldConfigProvider<?> worldConfigProvider) {
        // 213 MAGMA
        BlockInit.setAs("MAGMA", Material.STONE_PLATE);
        // 214 NETHER_WART_BLOCK
        BlockInit.setAs("NETHER_WART_BLOCK", Material.SKULL);
        // 215 RED_NETHER_BRICK
        BlockInit.setAs("RED_NETHER_BRICK", Material.NETHER_BRICK);
        // 216 BONE_BLOCK
        BlockInit.setAs("BONE_BLOCK", Material.COBBLESTONE);
        // 217 STRUCTURE_VOID
        BlockInit.setAs("STRUCTURE_VOID", "STRUCTURE_BLOCK"); // Like STRUCTURE_BLOCK.

        StaticLog.logInfo("Added block-info for Minecraft 1.10 blocks.");
    }

}
