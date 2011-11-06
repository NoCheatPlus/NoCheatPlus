package cc.co.evenprime.bukkit.nocheat.checks;

import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.NoCheatPlayer;
import cc.co.evenprime.bukkit.nocheat.config.cache.CCBlockBreak;
import cc.co.evenprime.bukkit.nocheat.data.BlockBreakData;
import cc.co.evenprime.bukkit.nocheat.data.ExecutionHistory;

public abstract class BlockBreakCheck extends Check {

    public BlockBreakCheck(NoCheat plugin, String name, String permission) {
        super(plugin, name, permission);
    }

    public abstract boolean check(NoCheatPlayer player, BlockBreakData data, CCBlockBreak cc);

    public abstract boolean isEnabled(CCBlockBreak cc);

    @Override
    protected final ExecutionHistory getHistory(NoCheatPlayer player) {
        return player.getData().blockbreak.history;
    }
}
