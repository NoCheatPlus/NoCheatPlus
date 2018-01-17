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
public class BlocksMC1_12 implements BlockPropertiesSetup {

    private static final String[] terracotta = new String[]{
        "WHITE_GLAZED_TERRACOTTA",
        "ORANGE_GLAZED_TERRACOTTA",
        "MAGENTA_GLAZED_TERRACOTTA",
        "LIGHT_BLUE_GLAZED_TERRACOTTA",
        "YELLOW_GLAZED_TERRACOTTA",
        "LIME_GLAZED_TERRACOTTA",
        "PINK_GLAZED_TERRACOTTA",
        "GRAY_GLAZED_TERRACOTTA",
        "SILVER_GLAZED_TERRACOTTA",
        "CYAN_GLAZED_TERRACOTTA",
        "PURPLE_GLAZED_TERRACOTTA",
        "BLUE_GLAZED_TERRACOTTA",
        "BROWN_GLAZED_TERRACOTTA",
        "GREEN_GLAZED_TERRACOTTA",
        "RED_GLAZED_TERRACOTTA",
        "BLACK_GLAZED_TERRACOTTA"
    };

    public BlocksMC1_12() {
        BlockInit.assertMaterialExists("CONCRETE");
        for (String glazed : terracotta) {
            BlockInit.assertMaterialExists(glazed);
        }
    }

    @Override
    public void setupBlockProperties(WorldConfigProvider<?> worldConfigProvider) {

        //        * MISSING 235(WHITE_GLAZED_TERRACOTTA / SOLID+GROUND) 
        //        * MISSING 236(ORANGE_GLAZED_TERRACOTTA / SOLID+GROUND) 
        //        * MISSING 237(MAGENTA_GLAZED_TERRACOTTA / SOLID+GROUND) 
        //        * MISSING 238(LIGHT_BLUE_GLAZED_TERRACOTTA / SOLID+GROUND) 
        //        * MISSING 239(YELLOW_GLAZED_TERRACOTTA / SOLID+GROUND) 
        //        * MISSING 240(LIME_GLAZED_TERRACOTTA / SOLID+GROUND) 
        //        * MISSING 241(PINK_GLAZED_TERRACOTTA / SOLID+GROUND) 
        //        * MISSING 242(GRAY_GLAZED_TERRACOTTA / SOLID+GROUND) 
        //        * MISSING 243(SILVER_GLAZED_TERRACOTTA / SOLID+GROUND) 
        //        * MISSING 244(CYAN_GLAZED_TERRACOTTA / SOLID+GROUND) 
        //        * MISSING 245(PURPLE_GLAZED_TERRACOTTA / SOLID+GROUND) 
        //        * MISSING 246(BLUE_GLAZED_TERRACOTTA / SOLID+GROUND) 
        //        * MISSING 247(BROWN_GLAZED_TERRACOTTA / SOLID+GROUND) 
        //        * MISSING 248(GREEN_GLAZED_TERRACOTTA / SOLID+GROUND) 
        //        * MISSING 249(RED_GLAZED_TERRACOTTA / SOLID+GROUND) 
        //        * MISSING 250(BLACK_GLAZED_TERRACOTTA / SOLID+GROUND) 
        //        * MISSING 251(CONCRETE / SOLID+GROUND) 
        //        * MISSING 252(CONCRETE_POWDER / SOLID+GROUND)

        BlockProps props = new BlockProps(BlockProperties.woodPickaxe, 1.4f, 
                BlockProperties.secToMs(7.0, 1.05, 0.55, 0.35, 0.3, 0.2));
           for (String glazed : terracotta) {
            // Set flags as with "hardened clay".
            BlockFlags.setFlagsAs(glazed, "HARD_CLAY");
            // Breaking times.
            BlockProperties.setBlockProps(glazed, props);
        }

        // Concrete
        BlockFlags.setFlagsAs("CONCRETE", Material.COBBLESTONE);
        BlockProperties.setBlockProps("CONCRETE",
                new BlockProps(BlockProperties.woodPickaxe, 1.8f,
                        // TODO: 2.7 with bare hands seems unlikely.
                        BlockProperties.secToMs(2.7, 1.35, 0.7, 0.45, 0.35, 0.25)
                        )
                );

        // Concrete powder
        BlockInit.setAs("CONCRETE_POWDER", Material.DIRT);

        StaticLog.logInfo("Added block-info for Minecraft 1.12 blocks.");
    }

}
