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


public class BlocksMC1_6_1 implements BlockPropertiesSetup{

    public BlocksMC1_6_1(){
        BlockInit.assertMaterialExists("COAL_BLOCK");
    }

    @Override
    public void setupBlockProperties(WorldConfigProvider<?> worldConfigProvider) {

        // Block of Coal: like block of redstone.
        BlockInit.setAs("COAL_BLOCK", "REDSTONE_BLOCK");

        // (hard_clay and stained clay via generic setup.)

        // Hay Bale
        BlockInit.setPropsAs("HAY_BLOCK", Material.STONE_BUTTON);
        BlockFlags.setFlagsAs("HAY_BLOCK", Material.STONE); // TODO: Assumption (!).

        // (Carpet via generic setup.)

        StaticLog.logInfo("Added block-info for Minecraft 1.6.1 blocks.");
    }

}
