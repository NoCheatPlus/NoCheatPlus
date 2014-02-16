package fr.neatmonster.nocheatplus.checks.blockplace;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.actions.ParameterName;
import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.ViolationData;
import fr.neatmonster.nocheatplus.utilities.BlockProperties;
/**
 * Check if the placing is legitimate in terms of surrounding materials.
 * @author mc_dev
 *
 */
public class Against extends Check {
	
	public Against() {
		super(CheckType.BLOCKPLACE_AGAINST);
	}
	
	public boolean check(final Player player, final Block block, final Material mat, final Block blockAgainst, final BlockPlaceData data, final BlockPlaceConfig cc) {
		boolean violation = false;
		// TODO: Make more precise (workarounds like WATER_LILY, general points).
        final int againstId = blockAgainst.getTypeId();
        if (BlockProperties.isLiquid(againstId)) {
            if ((mat != Material.WATER_LILY || !BlockProperties.isLiquid(block.getRelative(BlockFace.DOWN).getTypeId()))) {
            	violation = true;
            }
        }
        else if (againstId == Material.AIR.getId()) {
        	violation = true;
        }
        // Handle violation and return.
		if (violation) {
			data.againstVL += 1.0;
			final ViolationData vd = new ViolationData(this, player, data.againstVL, 1, cc.againstActions);
			vd.setParameter(ParameterName.BLOCK_ID, Integer.toString(mat.getId()));
			return executeActions(vd);
		} else {
			data.againstVL *=  100; // Assume one false positive every 100 blocks.
			return false;
		}
	}
	
}
