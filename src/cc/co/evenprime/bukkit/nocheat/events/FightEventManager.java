package cc.co.evenprime.bukkit.nocheat.events;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import net.minecraft.server.Entity;

import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.NoCheatPlayer;
import cc.co.evenprime.bukkit.nocheat.checks.FightCheck;
import cc.co.evenprime.bukkit.nocheat.checks.fight.DirectionCheck;
import cc.co.evenprime.bukkit.nocheat.checks.fight.NoswingCheck;
import cc.co.evenprime.bukkit.nocheat.checks.fight.SelfhitCheck;
import cc.co.evenprime.bukkit.nocheat.config.Permissions;
import cc.co.evenprime.bukkit.nocheat.config.cache.CCFight;
import cc.co.evenprime.bukkit.nocheat.config.cache.ConfigurationCache;
import cc.co.evenprime.bukkit.nocheat.data.FightData;
import cc.co.evenprime.bukkit.nocheat.debug.PerformanceManager.Type;

public class FightEventManager extends EventManager {

    private final List<FightCheck> checks;

    public FightEventManager(NoCheat plugin) {
        super(plugin);

        this.checks = new ArrayList<FightCheck>(3);
        this.checks.add(new NoswingCheck(plugin));
        this.checks.add(new DirectionCheck(plugin));
        this.checks.add(new SelfhitCheck(plugin));

        registerListener(Event.Type.ENTITY_DAMAGE, Priority.Lowest, true, plugin.getPerformance(Type.FIGHT));
    }

    @Override
    protected void handleEntityDamageEvent(EntityDamageEvent event, Priority priority) {

        final Entity damagee = ((CraftEntity) event.getEntity()).getHandle();

        // We can cast like crazy here because we ruled out all other
        // possibilities above
        final NoCheatPlayer player = plugin.getPlayer(((Player) ((EntityDamageByEntityEvent) event).getDamager()).getName());
        final FightData data = player.getData().fight;
        final CCFight cc = player.getConfiguration().fight;

        if(!cc.check || player.hasPermission(Permissions.FIGHT)) {
            return;
        }

        boolean cancelled = false;

        data.damagee = damagee;

        for(FightCheck check : checks) {
            // If it should be executed, do it
            if(!cancelled && check.isEnabled(cc) && !player.hasPermission(check.getPermission())) {
                check.check(player, data, cc);
            }
        }

        data.damagee = null;

        if(cancelled) {
            event.setCancelled(cancelled);
        }
    }

    public List<String> getActiveChecks(ConfigurationCache cc) {
        LinkedList<String> s = new LinkedList<String>();

        if(cc.fight.check && cc.fight.directionCheck)
            s.add("fight.direction");
        if(cc.fight.check && cc.fight.selfhitCheck)
            s.add("fight.selfhit");
        if(cc.fight.check && cc.fight.noswingCheck)
            s.add("fight.noswing");
        return s;
    }
}
