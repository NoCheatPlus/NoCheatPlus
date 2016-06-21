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

import org.bukkit.Location;
import org.bukkit.World;

import fr.neatmonster.nocheatplus.NCPAPIProvider;
import fr.neatmonster.nocheatplus.logging.Streams;
import fr.neatmonster.nocheatplus.utilities.StringUtil;

/**
 * Map-related static utility.
 * 
 * @author asofold
 *
 */
public class MapUtil {

    /**
     * Convenience method to check if the bounds as returned by getBounds cover
     * a whole block.
     *
     * @param bounds
     *            Can be null, must have 6 fields.
     * @return true, if is full bounds
     */
    public static final boolean isFullBounds(final double[] bounds) {
        if (bounds == null) return false;
        for (int i = 0; i < 3; i ++) {
            if (bounds[i] > 0.0) return false;
            if (bounds[i + 3] < 1.0) return false;
        }
        return true;
    }

    /**
     * Check if chunks are loaded and load all not yet loaded chunks, using
     * normal world coordinates.<br>
     * NOTE: Not sure where to put this. Method does not use any caching.
     *
     * @param world
     *            the world
     * @param x
     *            the x
     * @param z
     *            the z
     * @param xzMargin
     *            the xz margin
     * @return Number of loaded chunks.
     */
    public static int ensureChunksLoaded(final World world, final double x, final double z, final double xzMargin) {
        int loaded = 0;
        final int minX = Location.locToBlock(x - xzMargin) / 16;
        final int maxX = Location.locToBlock(x + xzMargin) / 16;
        final int minZ = Location.locToBlock(z - xzMargin) / 16;
        final int maxZ = Location.locToBlock(z + xzMargin) / 16;
        for (int cx = minX; cx <= maxX; cx ++) {
            for (int cz = minZ; cz <= maxZ; cz ++) {
                if (!world.isChunkLoaded(cx, cz)) {
                    try {
                        world.loadChunk(cx, cz);
                        loaded ++;
                    } catch (Exception ex) {
                        // (Can't seem to catch more precisely: TileEntity with CB 1.7.10)
                        NCPAPIProvider.getNoCheatPlusAPI().getLogManager().severe(Streams.STATUS, "Failed to load chunk at " + (cx * 16) + "," + (cz * 16) + " (real coordinates):\n" + StringUtil.throwableToString(ex));
                        // (Don't count as loaded.)
                    }
                }
            }
        }
        return loaded;
    }

}
