package cc.co.evenprime.bukkit.nocheat.checks.blockbreak;

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
            data.blockbreak.noswingVL *= 0.90D;
        } else {
            data.blockbreak.noswingVL += 1;
            // Prepare some event-specific values for logging and custom
            // actions
            data.log.check = "blockbreak.noswing";

            cancel = plugin.execute(player, cc.blockbreak.noswingActions, (int) data.blockbreak.noswingVL, data.blockbreak.history, cc);
        }

        return cancel;
    }
}
