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

/**
 * This is an attempt to add Minecraft 1.5 blocks information without actual 1.5 dependency.
 * @author mc_dev
 *
 */
public class BlocksMC1_5 implements BlockPropertiesSetup {

    public BlocksMC1_5(){
        // Test if materials exist.
        BlockInit.assertMaterialExists("REDSTONE_BLOCK");
    }

    @Override
    public void setupBlockProperties(WorldConfigProvider<?> worldConfigProvider) {

        /////////////////////
        // New blocks
        ////////////////////

        // 146 Trapped Chest
        BlockInit.setAs("TRAPPED_CHEST", Material.CHEST);

        // 147 Weighted Pressure Plate (Light)
        //		BlockFlags.addFlags(147, BlockProperties.F_IGN_PASSABLE | BlockProperties.F_GROUND | BlockProperties.F_GROUND_HEIGHT);
        BlockInit.setAs("GOLD_PLATE", Material.STONE_PLATE);

        // 148 Weighted Pressure Plate (Heavy)
        //		BlockFlags.addFlags(148, BlockProperties.F_IGN_PASSABLE | BlockProperties.F_GROUND | BlockProperties.F_GROUND_HEIGHT);
        BlockInit.setAs("IRON_PLATE", Material.STONE_PLATE);

        // 149 Redstone Comparator (inactive)
        //		BlockFlags.addFlags(149, BlockProperties.F_IGN_PASSABLE | BlockProperties.F_GROUND | BlockProperties.F_GROUND_HEIGHT);
        BlockInit.setAs("REDSTONE_COMPARATOR_OFF", Material.DIODE_BLOCK_OFF);

        // 150 Redstone Comparator (active)
        //		BlockFlags.addFlags(150, BlockProperties.F_IGN_PASSABLE | BlockProperties.F_GROUND | BlockProperties.F_GROUND_HEIGHT);
        BlockInit.setAs("REDSTONE_COMPARATOR_ON", Material.DIODE_BLOCK_ON);

        // 151 Daylight Sensor
        //		BlockFlags.addFlags(151, BlockProperties.F_IGN_PASSABLE | BlockProperties.F_GROUND | BlockProperties.F_GROUND_HEIGHT);
        BlockInit.setAs("DAYLIGHT_DETECTOR", Material.HUGE_MUSHROOM_1);

        // 152 Block of Redstone
        BlockInit.setAs("REDSTONE_BLOCK", Material.ENCHANTMENT_TABLE);

        // 153 Nether Quartz Ore
        BlockInit.setAs("QUARTZ_ORE", Material.COAL_ORE);

        // 154 Hopper
        BlockInit.setAs("HOPPER", Material.COAL_ORE);
        // TODO: Needs workaround. [workaround-flag + different purpose flag sets ?]
        BlockFlags.addFlags("HOPPER", BlockProperties.F_IGN_PASSABLE | BlockProperties.F_GROUND_HEIGHT);

        // 155 Block of Quartz
        BlockInit.setAs("QUARTZ_BLOCK", Material.SANDSTONE);

        // 156 Quartz Stairs
        BlockInit.setAs("QUARTZ_STAIRS", Material.SANDSTONE_STAIRS);

        // 157 Activator Rail
        BlockInit.setAs("ACTIVATOR_RAIL", Material.DETECTOR_RAIL);

        // 158 Dropper
        //		BlockFlags.setFlagsAs(158, Material.DISPENSER);
        BlockInit.setAs("DROPPER", Material.DISPENSER);


        /////////////////////
        // Changed blocks
        ////////////////////

        // 78 Snow
        BlockFlags.addFlags("SNOW", BlockProperties.F_HEIGHT_8_INC);
        BlockFlags.removeFlags("SNOW", BlockProperties.F_HEIGHT_8SIM_INC);

        // 95 Locked chest
        // BlockProperties.setBlockProps("LOCKED_CHEST", BlockProperties.instantType);

        StaticLog.logInfo("Added block-info for Minecraft 1.5 blocks.");
    }

}
