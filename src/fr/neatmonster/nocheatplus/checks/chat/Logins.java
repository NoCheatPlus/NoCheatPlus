package fr.neatmonster.nocheatplus.checks.chat;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.utilities.ActionFrequency;

public class Logins extends Check {
    
    /** Per world count (only used if set in config). */
    private final Map<String, ActionFrequency> counts = new HashMap<String, ActionFrequency>();

	public Logins() {
		super(CheckType.CHAT_LOGINS);
	}
	
	private ActionFrequency getActionFrequency(String worldName, int buckets, long durBucket, boolean perWorldCount){
	    if (!perWorldCount) worldName = "";
	    ActionFrequency freq = counts.get(worldName);
	    if (freq == null) freq = new ActionFrequency(buckets, durBucket);
	    counts.put(worldName, freq);
	    return freq;
	}

    public boolean check(final Player player, final ChatConfig cc, final ChatData data) {
        // Split into 6 buckets always.
        final long durBucket = 1000L * cc.loginsSeconds / 6;
        final ActionFrequency freq = getActionFrequency(player.getWorld().getName(), 6, durBucket, cc.loginsPerWorldCount);
        final long now = System.currentTimeMillis();
        freq.update(now);
        final boolean cancel = freq.getScore(1f) > cc.loginsLimit;
        if (!cancel) freq.add(now, 1f);
        return cancel;
    }

}
