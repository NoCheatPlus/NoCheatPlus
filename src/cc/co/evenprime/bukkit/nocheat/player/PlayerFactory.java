package cc.co.evenprime.bukkit.nocheat.player;

import org.bukkit.entity.Player;

import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.data.BaseData;

public class PlayerFactory {

    public static NoCheatPlayerImpl createPlayer(Player player, NoCheat plugin) {

        switch (plugin.getMCVersion()) {
        case MC100:
            return new NoCheatPlayer_100(player, plugin, new BaseData());
        case MC181:
            return new NoCheatPlayer_181(player, plugin, new BaseData());
        default:
            return new NoCheatPlayerImpl(player, plugin, new BaseData());
        }
    }
}
