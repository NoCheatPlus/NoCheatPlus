package cc.co.evenprime.bukkit.nocheat.checks.fight;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerAnimationEvent;
import cc.co.evenprime.bukkit.nocheat.EventManager;
import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.NoCheatPlayer;
import cc.co.evenprime.bukkit.nocheat.config.ConfigurationCacheStore;
import cc.co.evenprime.bukkit.nocheat.config.Permissions;

public class FightCheckListener implements Listener, EventManager {

    private final List<FightCheck> checks;
    private NoCheat                plugin;

    public FightCheckListener(NoCheat plugin) {

        this.checks = new ArrayList<FightCheck>(3);
        this.checks.add(new NoswingCheck(plugin));
        this.checks.add(new DirectionCheck(plugin));

        this.plugin = plugin;

        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void entityDamage(final EntityDamageByEntityEvent event) {
        if(event.isCancelled())
            return;

        if(event.getCause() == DamageCause.ENTITY_ATTACK) {
            normalDamage(event);
        } else {
            customDamage(event);
        }
    }

    private void normalDamage(final EntityDamageByEntityEvent event) {

        final Player damager = (Player) event.getDamager();

        final NoCheatPlayer player = plugin.getPlayer(damager);
        final CCFight cc = FightCheck.getConfig(player.getConfigurationStore());

        if(!cc.check || player.hasPermission(Permissions.FIGHT)) {
            return;
        }

        final FightData data = FightCheck.getData(player.getDataStore());

        // For some reason we decided to skip this event anyway
        if(data.skipNext) {
            data.skipNext = false;
            return;
        }

        boolean cancelled = false;

        // Get the attacked entity
        data.damagee = ((CraftEntity) event.getEntity()).getHandle();

        for(FightCheck check : checks) {
            // If it should be executed, do it
            if(!cancelled && check.isEnabled(cc) && !player.hasPermission(check.getPermission())) {
                cancelled = check.check(player, data, cc);
            }
        }

        data.damagee = null;

        if(cancelled)
            event.setCancelled(cancelled);
    }

    private void customDamage(final EntityDamageByEntityEvent event) {

        final Player damager = (Player) event.getDamager();
        final NoCheatPlayer player = plugin.getPlayer(damager);

        final FightData data = FightCheck.getData(player.getDataStore());

        // Skip the next damage event, because it is with high probability
        // something from the Heroes plugin
        data.skipNext = true;

        return;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    protected void armSwing(final PlayerAnimationEvent event) {
        FightCheck.getData(plugin.getPlayer(event.getPlayer()).getDataStore()).armswung = true;
    }

    public List<String> getActiveChecks(ConfigurationCacheStore cc) {
        LinkedList<String> s = new LinkedList<String>();

        CCFight f = FightCheck.getConfig(cc);

        if(f.check && f.directionCheck)
            s.add("fight.direction");
        if(f.check && f.noswingCheck)
            s.add("fight.noswing");
        return s;
    }
}
