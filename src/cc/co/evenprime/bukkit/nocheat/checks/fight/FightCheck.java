package cc.co.evenprime.bukkit.nocheat.checks.fight;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.config.Permissions;
import cc.co.evenprime.bukkit.nocheat.config.cache.ConfigurationCache;
import cc.co.evenprime.bukkit.nocheat.data.BaseData;

/**
 * Check various things related to fighting players/entities
 * 
 */
public class FightCheck {

    private final NoCheat        plugin;

    private final DirectionCheck directionCheck;
    private final SelfhitCheck   selfhitCheck;

    public FightCheck(NoCheat plugin) {

        this.plugin = plugin;

        this.directionCheck = new DirectionCheck(plugin);
        this.selfhitCheck = new SelfhitCheck(plugin);
    }

    public boolean check(final Player player, final Entity damagee, final ConfigurationCache cc) {

        boolean cancel = false;

        final boolean selfhitcheck = cc.fight.selfhitCheck && !player.hasPermission(Permissions.FIGHT_SELFHIT);
        final boolean directioncheck = cc.fight.directionCheck && !player.hasPermission(Permissions.FIGHT_DIRECTION);

        BaseData data = plugin.getData(player.getName());

        if(directioncheck) {
            cancel = directionCheck.check(player, data, damagee, cc);
        }

        if(!cancel && selfhitcheck) {
            cancel = selfhitCheck.check(player, data, damagee, cc);
        }

        return cancel;
    }
}
