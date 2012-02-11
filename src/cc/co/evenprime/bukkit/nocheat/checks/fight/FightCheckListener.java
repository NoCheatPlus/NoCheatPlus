package cc.co.evenprime.bukkit.nocheat.checks.fight;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerAnimationEvent;
import cc.co.evenprime.bukkit.nocheat.EventManager;
import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.NoCheatPlayer;
import cc.co.evenprime.bukkit.nocheat.config.ConfigurationCacheStore;
import cc.co.evenprime.bukkit.nocheat.config.Permissions;

public class FightCheckListener implements Listener, EventManager {

    private final List<FightCheck> checks;

    private final GodmodeCheck     godmodeCheck;
    private final NoCheat          plugin;

    public FightCheckListener(NoCheat plugin) {

        this.checks = new ArrayList<FightCheck>(3);
        this.checks.add(new SpeedCheck(plugin));
        this.checks.add(new NoswingCheck(plugin));
        this.checks.add(new DirectionCheck(plugin));
        this.checks.add(new ReachCheck(plugin));

        this.godmodeCheck = new GodmodeCheck(plugin);

        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void entityDamage(final EntityDamageEvent event) {

        if(event.isCancelled() || !(event instanceof EntityDamageByEntityEvent))
            return;

        final EntityDamageByEntityEvent e = (EntityDamageByEntityEvent) event;
        if(!(e.getDamager() instanceof Player)) {
            return;
        }

        if(e.getCause() == DamageCause.ENTITY_ATTACK) {
            normalDamage(e);
        } else if(e.getCause() == DamageCause.CUSTOM) {
            customDamage(e);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void entityDamageForGodmodeCheck(final EntityDamageEvent event) {

        if(event.isCancelled())
            return;

        final Entity entity = event.getEntity();
        if(!(entity instanceof Player) || entity.isDead()) {
            return;
        }

        NoCheatPlayer player = plugin.getPlayer((Player) entity);
        FightConfig cc = FightCheck.getConfig(player.getConfigurationStore());

        if(!godmodeCheck.isEnabled(cc) || player.hasPermission(godmodeCheck.getPermission())) {
            return;
        }

        FightData data = FightCheck.getData(player.getDataStore());
        boolean cancelled = godmodeCheck.check(plugin.getPlayer((Player) entity), data, cc);
        if(cancelled) {
            // Remove the invulnerability from the player
            player.getPlayer().setNoDamageTicks(0);
        }
    }

    private void normalDamage(final EntityDamageByEntityEvent event) {

        final Player damager = (Player) event.getDamager();

        final NoCheatPlayer player = plugin.getPlayer(damager);
        final FightConfig cc = FightCheck.getConfig(player.getConfigurationStore());

        if(!cc.damageChecks || player.hasPermission(Permissions.FIGHT)) {
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

        FightConfig f = FightCheck.getConfig(cc);

        if(f.directionCheck)
            s.add("fight.direction");
        if(f.noswingCheck)
            s.add("fight.noswing");
        if(f.reachCheck)
            s.add("fight.reach");
        if(f.speedCheck)
            s.add("fight.speed");
        if(f.godmodeCheck)
            s.add("fight.godmode");
        return s;
    }
}
