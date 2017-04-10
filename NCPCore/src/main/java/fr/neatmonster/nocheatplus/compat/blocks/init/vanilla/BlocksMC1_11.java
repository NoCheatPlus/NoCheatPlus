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
