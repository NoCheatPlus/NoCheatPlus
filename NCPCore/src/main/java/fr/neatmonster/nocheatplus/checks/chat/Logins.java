package fr.neatmonster.nocheatplus.checks.chat;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.components.IData;
import fr.neatmonster.nocheatplus.components.IRemoveData;
import fr.neatmonster.nocheatplus.utilities.TickTask;
import fr.neatmonster.nocheatplus.utilities.ds.count.ActionFrequency;

public class Logins extends Check implements IRemoveData{
    
    /** Per world count (if set in the config, only "" is used). */
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
        final long now = System.currentTimeMillis();
        // Skip if is too close to the startup time.
        if (now - TickTask.getTimeStart() < cc.loginsStartupDelay) return false;
        // Split into 6 buckets always.
        final long durBucket = 1000L * cc.loginsSeconds / 6;
        final ActionFrequency freq = getActionFrequency(player.getWorld().getName(), 6, durBucket, cc.loginsPerWorldCount);
        freq.update(now);
        final boolean cancel = freq.score(1f) > cc.loginsLimit; // TODO: >= ...  This will be 1 after the first login (!).
        if (!cancel) freq.add(1f);
        return cancel;
    }

	/**
	 * Called by ChatListener
	 */
	public void onReload() {
		counts.clear();
	}

	@Override
	public IData removeData(final String playerName) {
		// Ignore
		return null;
	}

	@Override
	public void removeAllData() {
		counts.clear();
	}

}
