package cc.co.evenprime.bukkit.nocheat.checks.blockplace;

import org.bukkit.entity.Player;

import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.config.cache.ConfigurationCache;
import cc.co.evenprime.bukkit.nocheat.data.BaseData;

public class NoswingCheck {

    private final NoCheat plugin;

    public NoswingCheck(NoCheat plugin) {
        this.plugin = plugin;
    }

    public boolean check(final Player player, final BaseData data, final ConfigurationCache cc) {

        boolean cancel = false;

        // did he swing his arm before?
        if(data.armswung) {
            data.armswung = false;
            data.blockplace.noswingVL *= 0.90D;
        } else {
            data.blockplace.noswingVL += 1;
            // Prepare some event-specific values for logging and custom
            // actions
            data.log.check = "blockplace.noswing";

            cancel = plugin.execute(player, cc.blockplace.noswingActions, (int) data.blockplace.noswingVL, data.blockplace.history, cc);
        }

        return cancel;
    }
}
