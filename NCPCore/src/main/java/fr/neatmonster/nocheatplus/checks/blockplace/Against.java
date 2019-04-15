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
package fr.neatmonster.nocheatplus.checks.blockplace;

import fr.neatmonster.nocheatplus.permissions.PermissionCache;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.actions.ParameterName;
import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.ViolationData;
import fr.neatmonster.nocheatplus.checks.blockinteract.BlockInteractData;
import fr.neatmonster.nocheatplus.permissions.Permissions;
import fr.neatmonster.nocheatplus.utilities.map.BlockProperties;
/**
 * Check if the placing is legitimate in terms of surrounding materials.
 * @author mc_dev
 *
 */
public class Against extends Check {

    public Against() {
        super(CheckType.BLOCKPLACE_AGAINST);
    }

    public boolean check(final Player player, final Block block, final Material placedMat, final Block blockAgainst, 
            final boolean isInteractBlock, final BlockPlaceData data, final BlockPlaceConfig cc) {
        boolean violation = false;
        // TODO: Make more precise (workarounds like WATER_LILY, general points, such as action?).
        // Workaround for signs on cactus and similar.
        final BlockInteractData bdata = BlockInteractData.getData(player); // TODO: pass as argument.
        final Material againstType = blockAgainst.getType();
        if (bdata.isConsumedCheck(this.type) && !bdata.isPassedCheck(this.type)) {
            // TODO: Awareness of repeated violation probably is to be implemented below somewhere.
            violation = true;
            if (data.debug) {
                debug(player, "Cancel due to block having been consumed by this check.");
            }
        }
        else if (BlockProperties.isAir(againstType)) {
            // Attempt to workaround blocks like cactus.
            final Material matAgainst = bdata.getLastType();
            if (isInteractBlock && !BlockProperties.isAir(matAgainst) && ! BlockProperties.isLiquid(matAgainst)) {
                // Block was placed against something (e.g. cactus), allow it.
            }
            else if (!PermissionCache.hasPermission(player, Permissions.BLOCKPLACE_AGAINST_AIR)) {
                violation = true;
            }
        }
        else if (BlockProperties.isLiquid(againstType)) {
            // TODO: F_PLACE_AGAINST_WATER|LIQUID...
            if ((placedMat != Material.WATER_LILY 
                    || !BlockProperties.isLiquid(block.getRelative(BlockFace.DOWN).getType()))  
                    && !PermissionCache.hasPermission(player, Permissions.BLOCKPLACE_AGAINST_LIQUIDS)) {
                violation = true;
            }
        }
        // Handle violation and return.
        bdata.addConsumedCheck(this.type);
        if (violation) {
            data.againstVL += 1.0;
            final ViolationData vd = new ViolationData(this, player, data.againstVL, 1, cc.againstActions);
            vd.setParameter(ParameterName.BLOCK_TYPE, placedMat.toString());
            return executeActions(vd).willCancel();
        }
        else {
            data.againstVL *=  0.99; // Assume one false positive every 100 blocks.
            bdata.addPassedCheck(this.type);
            return false;
        }
    }

}
