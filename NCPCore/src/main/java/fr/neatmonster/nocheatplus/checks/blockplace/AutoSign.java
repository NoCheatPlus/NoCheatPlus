package fr.neatmonster.nocheatplus.checks.blockplace;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.actions.ParameterName;
import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.ViolationData;
import fr.neatmonster.nocheatplus.utilities.StringUtil;
import fr.neatmonster.nocheatplus.utilities.TickTask;

public class AutoSign extends Check {
	
	/** Reference time that is needed to edit the most complicated sign :). */
	private static long maxEditTime = 1500;
	/** Fastest time "possible" estimate for an empty sign. */
	private static long minEditTime = 150;
	/** Minimum time needed to add one extra line (not the first). */
	private static long minLineTime = 50;
	/** Minimum time needed to type a character. */
	private static long minCharTime = 50;
	
	private final List<String> tags = new ArrayList<String>();
	
	final Set<Character> chars = new HashSet<Character>(15 * 4);

	public AutoSign() {
		super(CheckType.BLOCKPLACE_AUTOSIGN);
	}

	public boolean check(final Player player, final Block block, final String[] lines) {
		// TODO: Might want to reset time + hash ?
		final long time = System.currentTimeMillis();
		tags.clear();
		final BlockPlaceData data = BlockPlaceData.getData(player);
		Material mat = block.getType();
		if (mat == Material.SIGN_POST || mat == Material.WALL_SIGN) {
			mat = Material.SIGN;
		}
		if (data.autoSignPlacedHash != BlockPlaceListener.getBlockPlaceHash(block, mat)){
			tags.add("block_mismatch");
			return handleViolation(player, maxEditTime, data);
		}
		if (time < data.autoSignPlacedTime){
			data.autoSignPlacedTime = 0;
			return false;
		}
		
		// check time, mind lag.
		
		final long editTime = time - data.autoSignPlacedTime;
		long expected = getExpectedEditTime(lines);
		expected = (long) (expected / TickTask.getLag(expected, true));
		
		if (expected > editTime){
			tags.add("edit_time");
			return handleViolation(player, expected - editTime, data);
		}
		return false;
	}

	private long getExpectedEditTime(final String[] lines) {
		long expected = minEditTime;
		int n = 0;
		for (String line : lines){
			if (line != null){
				line = line.trim().toLowerCase();
				if (!line.isEmpty()){
					chars.clear();
					n += 1;
					for (final char c : line.toCharArray()){
						chars.add(c);
					}
					expected += minCharTime * chars.size();
				}
			}
		}
		if (n > 1){
			expected += minLineTime * n;
		}
		return expected;
	}

	/**
	 * 
	 * @param player
	 * @param violationTime Amount of too fast editing.
	 * @param data
	 * @return
	 */
	private boolean handleViolation(final Player player, final long violationTime, final BlockPlaceData data) {
		final double addedVL = 10.0 * Math.min(maxEditTime, violationTime) / maxEditTime;
		data.autoSignVL += addedVL;
		final ViolationData vd = new ViolationData(this, player, data.autoSignVL, addedVL, BlockPlaceConfig.getConfig(player).autoSignActions);
		if (vd.needsParameters()){
			vd.setParameter(ParameterName.TAGS, StringUtil.join(tags, "+"));
		}
		return executeActions(vd);
	}
	

}
