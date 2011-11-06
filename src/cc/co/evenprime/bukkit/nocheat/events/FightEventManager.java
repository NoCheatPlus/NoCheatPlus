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
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityListener;
import org.bukkit.plugin.PluginManager;

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
import cc.co.evenprime.bukkit.nocheat.debug.Performance;
import cc.co.evenprime.bukkit.nocheat.debug.PerformanceManager.Type;

public class FightEventManager extends EntityListener implements EventManager {

    private final NoCheat          plugin;
    private final List<FightCheck> checks;
    private final Performance      fightPerformance;

    public FightEventManager(NoCheat plugin) {

        this.plugin = plugin;

        this.checks = new ArrayList<FightCheck>(3);
        this.checks.add(new NoswingCheck(plugin));
        this.checks.add(new DirectionCheck(plugin));
        this.checks.add(new SelfhitCheck(plugin));

        this.fightPerformance = plugin.getPerformance(Type.FIGHT);

        PluginManager pm = plugin.getServer().getPluginManager();

        pm.registerEvent(Event.Type.ENTITY_DAMAGE, this, Priority.Lowest, plugin);
    }

    @Override
    public void onEntityDamage(EntityDamageEvent event) {

        // Event cancelled?
        if(event.isCancelled()) {
            return;
        }

        // Event relevant at all?
        if(event.getCause() != DamageCause.ENTITY_ATTACK || !(((EntityDamageByEntityEvent) event).getDamager() instanceof Player)) {
            return;
        }

        // Performance counter setup
        long nanoTimeStart = 0;
        final boolean performanceCheck = fightPerformance.isEnabled();

        if(performanceCheck)
            nanoTimeStart = System.nanoTime();

        handleEvent(event);

        // store performance time
        if(performanceCheck)
            fightPerformance.addTime(System.nanoTime() - nanoTimeStart);
    }

    private void handleEvent(EntityDamageEvent event) {

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
