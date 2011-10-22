package cc.co.evenprime.bukkit.nocheat.checks.fight;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.config.cache.ConfigurationCache;
import cc.co.evenprime.bukkit.nocheat.data.BaseData;

public class SelfhitCheck {

    private final NoCheat plugin;

    public SelfhitCheck(NoCheat plugin) {
        this.plugin = plugin;
    }

    public boolean check(final Player player, final BaseData data, final Entity damagee, final ConfigurationCache cc) {

        boolean cancel = false;

        if(player.equals(damagee)) {

            // Player failed the check obviously

            data.fight.selfhitviolationLevel += 1;
            // Prepare some event-specific values for logging and custom
            // actions
            data.log.check = "fight.selfhit";

            cancel = plugin.execute(player, cc.fight.selfhitActions, (int) data.fight.selfhitviolationLevel, data.fight.history, cc);
        } else {
            data.fight.selfhitviolationLevel *= 0.99D;
        }

        return cancel;
    }

}
