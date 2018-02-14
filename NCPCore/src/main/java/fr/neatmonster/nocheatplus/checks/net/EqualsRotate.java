package fr.neatmonster.nocheatplus.checks.net;

import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.players.DataManager;
import fr.neatmonster.nocheatplus.utilities.CheckUtils;
import org.bukkit.entity.Player;

public class EqualsRotate extends Check {

    public EqualsRotate() {
        super(CheckType.NET_EQUALSROTATE);
    }

    public boolean check(final Player player, final NetData data, final NetConfig cc, float yaw, float pitch) {
        if (yaw == data.lastYaw && pitch == data.lastPitch && !CheckUtils.hasBypass(CheckType.NET_KEEPALIVEFREQUENCY, player, DataManager.getPlayerData(player))) {
            data.equalsRotateVio++;
            if (executeActions(player, data.equalsRotateVio, 1, cc.equalsRotateActions).willCancel())
                return true;
        }
        data.equalsRotateVio = Math.max(0, data.equalsRotateVio-0.02);
        return false;
    }
}
