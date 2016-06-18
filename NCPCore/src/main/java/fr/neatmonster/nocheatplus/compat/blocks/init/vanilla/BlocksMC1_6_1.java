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
import fr.neatmonster.nocheatplus.utilities.BlockFlags;
import fr.neatmonster.nocheatplus.utilities.BlockProperties;
import fr.neatmonster.nocheatplus.utilities.BlockProperties.BlockProps;

@SuppressWarnings("deprecation")
public class BlocksMC1_6_1 implements BlockPropertiesSetup{

    public BlocksMC1_6_1(){
        BlockInit.assertMaterialNameMatch(173, "coal", "block");
    }

    @Override
    public void setupBlockProperties(WorldConfigProvider<?> worldConfigProvider) {

        // Block of Coal: like block of redstone.
        BlockInit.setAs(173, 152);

        // Hardened Clay
        BlockProperties.setBlockProps(172, new BlockProps(BlockProperties.woodPickaxe, 1.25f, BlockProperties.secToMs(6.25, 0.95, 0.5, 0.35, 0.25, 0.2)));
        BlockFlags.setFlagsAs(172, Material.STONE); // TODO: Assumption (!).

        // Stained Clay: Set as hardened clay.
        BlockInit.setAs(159, 172);

        // Hay Bale
        BlockInit.setPropsAs(170, Material.STONE_BUTTON);
        BlockFlags.setFlagsAs(170, Material.STONE); // TODO: Assumption (!).

        // Carpet
        BlockProperties.setBlockProps(171, new BlockProps(BlockProperties.noTool, 0.1f, BlockProperties.secToMs(0.15)));
        BlockProperties.setBlockFlags(171, BlockProperties.F_GROUND|BlockProperties.F_IGN_PASSABLE|BlockProperties.F_GROUND_HEIGHT);

        StaticLog.logInfo("Added block-info for Minecraft 1.6.1 blocks.");
    }

}
