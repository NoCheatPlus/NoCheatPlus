package cc.co.evenprime.bukkit.nocheat.checks.blockplace;

import org.bukkit.entity.Player;

import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.config.cache.ConfigurationCache;
import cc.co.evenprime.bukkit.nocheat.data.BaseData;

/**
 * The noswingcheck will only identify if an action happened without a preceding "swing"
 * 
 */
public class NoSwingCheck {

    private final NoCheat plugin;

    public NoSwingCheck(NoCheat plugin) {
        this.plugin = plugin;
    }

    public boolean check(Player player, ConfigurationCache cc) {

        boolean cancel = false;

        BaseData data = plugin.getData(player.getName());

       

        return cancel;
    }

}
