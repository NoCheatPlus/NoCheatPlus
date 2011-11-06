package cc.co.evenprime.bukkit.nocheat.checks;

import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.NoCheatPlayer;
import cc.co.evenprime.bukkit.nocheat.config.cache.CCFight;
import cc.co.evenprime.bukkit.nocheat.data.ExecutionHistory;
import cc.co.evenprime.bukkit.nocheat.data.FightData;

/**
 * Check various things related to fighting players/entities
 * 
 */
public abstract class FightCheck extends Check {

    public FightCheck(NoCheat plugin, String name, String permission) {
        super(plugin, name, permission);
    }

    public abstract boolean check(NoCheatPlayer player, FightData data, CCFight cc);

    public abstract boolean isEnabled(CCFight cc);

    @Override
    protected final ExecutionHistory getHistory(NoCheatPlayer player) {
        return player.getData().fight.history;
    }
}
