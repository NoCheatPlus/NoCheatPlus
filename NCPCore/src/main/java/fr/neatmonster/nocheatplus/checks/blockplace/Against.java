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
import fr.neatmonster.nocheatplus.utilities.TickTask;
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

    public boolean check(final Player player, final Block block, final Material placedMat, final Block blockAgainst, final BlockPlaceData data, final BlockPlaceConfig cc) {
        boolean violation = false;
        // TODO: Make more precise (workarounds like WATER_LILY, general points).
        // Workaround for signs on cactus and similar.
        final Material againstType = blockAgainst.getType();
        if (BlockProperties.isAir(againstType)) {
            // Attempt to workaround blocks like cactus.
            final BlockInteractData bdata = BlockInteractData.getData(player);
            if (bdata.matchesLastBlock(TickTask.getTick(), blockAgainst)) {
                // Block was placed against something (e.g. cactus), allow it.
                // TODO: Later reset can conflict, though it makes sense to reset with placing blocks in general.
                // TODO: Reset on leaving the listener rather - why could it conflict?
                bdata.resetLastBlock(); 
                return false;
            }
        }
        if (BlockProperties.isLiquid(againstType)) {
            if ((placedMat != Material.WATER_LILY || !BlockProperties.isLiquid(block.getRelative(BlockFace.DOWN).getType()))  && !player.hasPermission(Permissions.BLOCKPLACE_AGAINST_LIQUIDS)) {
                violation = true;
            }
        }
        else if (BlockProperties.isAir(againstType) && !player.hasPermission(Permissions.BLOCKPLACE_AGAINST_AIR)) {
            violation = true;
        }
        // Handle violation and return.
        if (violation) {
            data.againstVL += 1.0;
            final ViolationData vd = new ViolationData(this, player, data.againstVL, 1, cc.againstActions);
            vd.setParameter(ParameterName.BLOCK_TYPE, placedMat.toString());
            vd.setParameter(ParameterName.BLOCK_ID, Integer.toString(BlockProperties.getId(placedMat)));
            return executeActions(vd).willCancel();
        } else {
            data.againstVL *=  0.99; // Assume one false positive every 100 blocks.
            return false;
        }
    }

}
