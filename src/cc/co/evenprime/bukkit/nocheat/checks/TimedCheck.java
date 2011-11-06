package cc.co.evenprime.bukkit.nocheat.checks;

import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.NoCheatPlayer;
import cc.co.evenprime.bukkit.nocheat.config.cache.CCTimed;
import cc.co.evenprime.bukkit.nocheat.data.ExecutionHistory;
import cc.co.evenprime.bukkit.nocheat.data.TimedData;

public abstract class TimedCheck extends Check {

    public TimedCheck(NoCheat plugin, String name, String permission) {
        super(plugin, name, permission);
    }

    public abstract boolean check(final NoCheatPlayer player, TimedData data, CCTimed cc);

    public abstract boolean isEnabled(CCTimed cc);

    @Override
    protected ExecutionHistory getHistory(NoCheatPlayer player) {
        // TODO Auto-generated method stub
        return player.getData().timed.history;
    }
}
