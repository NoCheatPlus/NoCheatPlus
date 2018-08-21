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
package fr.neatmonster.nocheatplus.utilities.map;

import org.bukkit.Material;

/**
 * Utilities for block-flags.<br>
 * Later the flag constant definitions and parsing might be moved here.
 * @author mc_dev
 *
 */
public class BlockFlags {

    //////////////////
    // Summary flags
    //////////////////

    /** Explicitly set full bounds. */
    public static final long FULL_BOUNDS = BlockProperties.F_XZ100 | BlockProperties.F_HEIGHT100;

    /** SOLID and GROUND set. Treatment of SOLID/GROUND may be changed later. */
    public static final long SOLID_GROUND = BlockProperties.F_SOLID | BlockProperties.F_GROUND;

    /**
     * Set flags of id same as already set with flags for the given material.
     * (Uses BlockProperties.)
     *
     * @param id
     *            the id
     * @param mat
     *            the mat
     */
    public static void setFlagsAs(String id, Material mat) {
        BlockProperties.setBlockFlags(id, BlockProperties.getBlockFlags(mat));
    }

    /**
     * Set flags of id same as already set with flags for the given material.
     * (Uses BlockProperties.)
     *
     * @param id
     *            the id
     * @param otherId
     *            the other id
     */
    public static void setFlagsAs(String id, String otherId) {
        BlockProperties.setBlockFlags(id, BlockProperties.getBlockFlags(otherId));
    }

    /**
     * Set the same flags for newMat as are present for mat.
     * 
     * @param newMat
     * @param mat
     */
    public static void setFlagsAs(Material newMat, Material mat) {
        BlockProperties.setBlockFlags(newMat, BlockProperties.getBlockFlags(mat));
    }

    /**
     * Add flags to existent flags. (Uses BlockProperties.)
     *
     * @param id
     *            Id of the block.
     * @param flags
     *            Block flags.
     */
    public static void addFlags(String id, long flags) {
        BlockProperties.setBlockFlags(id, BlockProperties.getBlockFlags(id) | flags);
    }

    /**
     * Add flags to existent flags. (Uses BlockProperties.)
     *
     * @param blockType
     *            Bukkit Material type. Id of the block.
     * @param flags
     *            Block flags.
     */
    public static void addFlags(Material blockType, long flags) {
        BlockProperties.setBlockFlags(blockType, BlockProperties.getBlockFlags(blockType) | flags);
    }

    /**
     * Remove the given flags from existent flags. (Uses BlockProperties.)
     *
     * @param id
     *            the id
     * @param flags
     *            the flags
     */
    public static void removeFlags(String id, long flags) {
        BlockProperties.setBlockFlags(id, BlockProperties.getBlockFlags(id) & ~flags);
    }

    /**
     * Test if any flags within testFlags are contained.
     * 
     * @param flags
     * @param testFlags
     * @return
     */
    public static boolean hasAnyFlag(long flags, long testFlags) {
        return (flags & testFlags) != 0L;
    }

    /**
     * Test if all flags within testFlags are contained.
     * 
     * @param flags
     * @param testFlags
     * @return
     */
    public static boolean hasAllFlags(long flags, long testFlags) {
        return (flags & testFlags) == testFlags;
    }

    /**
     * Test if no flags within testFlags are contained.
     * 
     * @param flags
     * @param testFlags
     * @return
     */
    public static boolean hasNoFlags(long flags, long testFlags) {
        return (flags & testFlags) == 0L;
    }

    /** Override flags to a fully solid block (set explicitly). */
    public static void setFullySolidFlags(String blockId) {
        BlockProperties.setBlockFlags(blockId, FULL_BOUNDS | SOLID_GROUND);
    }

}
