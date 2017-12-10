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

    private static final String[] shulker_boxes = new String[]{
        "WHITE_SHULKER_BOX",
        "ORANGE_SHULKER_BOX",
        "MAGENTA_SHULKER_BOX",
        "LIGHT_BLUE_SHULKER_BOX",
        "YELLOW_SHULKER_BOX",
        "LIME_SHULKER_BOX",
        "PINK_SHULKER_BOX",
        "GRAY_SHULKER_BOX",
        "SILVER_SHULKER_BOX",
        "CYAN_SHULKER_BOX",
        "PURPLE_SHULKER_BOX",
        "BLUE_SHULKER_BOX",
        "BROWN_SHULKER_BOX",
        "GREEN_SHULKER_BOX",
        "RED_SHULKER_BOX",
        "BLACK_SHULKER_BOX"
    };

    public BlocksMC1_11() {
        BlockInit.assertMaterialExists("OBSERVER");
        for (String box : shulker_boxes) {
            BlockInit.assertMaterialExists(box);
        }
    }

    @Override
    public void setupBlockProperties(WorldConfigProvider<?> worldConfigProvider) {
        long solidFlags = BlockProperties.F_SOLID | BlockProperties.F_GROUND;
        // 218 OBSERVER
        // Wiki (16-11-25): 17.5, 2.65, 1.32, 0.9, 0.7, 0.45
        BlockProperties.setBlockProps("OBSERVER", new BlockProps(BlockProperties.woodPickaxe, 6,
                BlockProperties.secToMs(15.0, 2.2, 1.1, 0.7, 0.55, 0.45)));
        BlockProperties.setBlockFlags("OBSERVER", solidFlags);
        // ALL SORTS OF SHULKER BOXES
        for (String box : shulker_boxes) {
            // Wiki (16-11-25): 9, 4.5, 2.25, 1.5, 1.15, 0.75
            BlockProperties.setBlockProps(box, new BlockProps(BlockProperties.woodPickaxe, 6,
                    BlockProperties.secToMs(10.0, 1.45, 0.7, 0.5, 0.35, 0.2)));
            BlockProperties.setBlockFlags(box, solidFlags);
        }
        StaticLog.logInfo("Added block-info for Minecraft 1.11 blocks.");
    }

}
