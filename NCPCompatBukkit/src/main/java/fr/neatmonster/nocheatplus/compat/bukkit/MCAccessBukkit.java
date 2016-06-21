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
package fr.neatmonster.nocheatplus.compat.bukkit;


import java.util.HashSet;
import java.util.Set;

import org.bukkit.Material;

import fr.neatmonster.nocheatplus.compat.blocks.BlockPropertiesSetup;
import fr.neatmonster.nocheatplus.config.WorldConfigProvider;
import fr.neatmonster.nocheatplus.utilities.map.BlockProperties;

public class MCAccessBukkit extends MCAccessBukkitBase implements BlockPropertiesSetup{

    public MCAccessBukkit() {
        super();
    }

    @Override
    public void setupBlockProperties(final WorldConfigProvider<?> worldConfigProvider) {
        // Note deprecation suppression: These ids should be unique for a server run, that should be ok for setting up generic properties.
        // TODO: (?) Set some generic properties matching what BlockCache.getShape returns.
        final Set<Material> fullBlocks = new HashSet<Material>();
        for (final Material mat : new Material[]{
                // TODO: Ice !? / Packed ice !?
                Material.GLASS, Material.GLOWSTONE, Material.ICE, Material.LEAVES,
                Material.COMMAND, Material.BEACON,
                Material.PISTON_BASE,
        }) {
            fullBlocks.add(mat);
        }
        for (final Material mat : Material.values()) {
            if (!mat.isBlock()) {
                continue;
            }
            if (fullBlocks.contains(mat)) {
                continue;
            }
            if (!mat.isOccluding() || !mat.isSolid() || mat.isTransparent()) {
                // Uncertain bounding-box, allow passing through.
                long flags = BlockProperties.F_IGN_PASSABLE;
                if ((BlockProperties.isSolid(mat) || BlockProperties.isGround(mat)) && !BlockProperties.isLiquid(mat)) {
                    // Block can be ground, so allow standing on any height.
                    flags |= BlockProperties.F_GROUND_HEIGHT;
                }
                BlockProperties.setBlockFlags(mat, BlockProperties.getBlockFlags(mat) | flags);
            }
        }
        // Blocks that are reported to be full and solid, but which are not.
        for (final Material mat : new Material[]{
                Material.ENDER_PORTAL_FRAME,
        }) {
            final long flags = BlockProperties.F_IGN_PASSABLE | BlockProperties.F_GROUND_HEIGHT;
            BlockProperties.setBlockFlags(mat, BlockProperties.getBlockFlags(mat) | flags);
        }
    }

}
