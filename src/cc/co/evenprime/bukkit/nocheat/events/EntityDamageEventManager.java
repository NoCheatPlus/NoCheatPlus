package cc.co.evenprime.bukkit.nocheat.events;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityListener;
import org.bukkit.plugin.PluginManager;

import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.checks.fight.FightCheck;
import cc.co.evenprime.bukkit.nocheat.config.Permissions;
import cc.co.evenprime.bukkit.nocheat.config.cache.ConfigurationCache;
import cc.co.evenprime.bukkit.nocheat.debug.Performance;
import cc.co.evenprime.bukkit.nocheat.debug.PerformanceManager.Type;

public class EntityDamageEventManager extends EntityListener implements EventManager {

    private final NoCheat     plugin;
    private final FightCheck  fightCheck;
    private final Performance fightPerformance;

    public EntityDamageEventManager(NoCheat plugin) {

        this.plugin = plugin;
        this.fightCheck = new FightCheck(plugin);

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

        final Entity damagee = event.getEntity();
        // We can cast like crazy here because we ruled out all other
        // possibilities above
        final Player player = (Player) ((EntityDamageByEntityEvent) event).getDamager();

        final ConfigurationCache cc = plugin.getConfig(player);

        // Find out if checks need to be done for that player
        if(cc.fight.check && !player.hasPermission(Permissions.FIGHT)) {

            boolean cancel = false;

            cancel = fightCheck.check(player, damagee, cc);

            if(cancel) {
                event.setCancelled(true);
            }
        }

        // store performance time
        if(performanceCheck)
            fightPerformance.addTime(System.nanoTime() - nanoTimeStart);
    }

    public List<String> getActiveChecks(ConfigurationCache cc) {
        LinkedList<String> s = new LinkedList<String>();

        if(cc.fight.check && cc.fight.directionCheck)
            s.add("fight.direction");
        return s;
    }
}
