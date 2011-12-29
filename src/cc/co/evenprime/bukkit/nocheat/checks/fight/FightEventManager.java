package cc.co.evenprime.bukkit.nocheat.checks.fight;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerAnimationEvent;

import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.NoCheatPlayer;
import cc.co.evenprime.bukkit.nocheat.config.ConfigurationCacheStore;
import cc.co.evenprime.bukkit.nocheat.config.Permissions;
import cc.co.evenprime.bukkit.nocheat.debug.PerformanceManager.EventType;
import cc.co.evenprime.bukkit.nocheat.events.EventManagerImpl;

public class FightEventManager extends EventManagerImpl {

    private final List<FightCheck> checks;

    public FightEventManager(NoCheat plugin) {
        super(plugin);

        this.checks = new ArrayList<FightCheck>(3);
        this.checks.add(new NoswingCheck(plugin));
        this.checks.add(new DirectionCheck(plugin));

        registerListener(Event.Type.ENTITY_DAMAGE, Priority.Lowest, true, plugin.getPerformance(EventType.FIGHT));
        registerListener(Event.Type.PLAYER_ANIMATION, Priority.Monitor, false, null);
    }

    @Override
    protected void handleEntityAttackDamageByPlayerEvent(final EntityDamageByEntityEvent event, final Priority priority) {

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

    @Override
    protected void handleProjectileDamageByPlayerEvent(final EntityDamageByEntityEvent event, final Priority priority) {

        final Player damager = (Player) ((Projectile) event.getDamager()).getShooter();
        final NoCheatPlayer player = plugin.getPlayer(damager);

        final FightData data = FightCheck.getData(player.getDataStore());

        // Skip the next damage event, because it is the same as this one
        // just mislabelled as a "direct" attack from one player onto another
        data.skipNext = true;

        return;
    }

    @Override
    protected void handleCustomDamageByPlayerEvent(final EntityDamageByEntityEvent event, final Priority priority) {

        final Player damager = (Player) event.getDamager();
        final NoCheatPlayer player = plugin.getPlayer(damager);

        final FightData data = FightCheck.getData(player.getDataStore());

        // Skip the next damage event, because it is with high probability
        // something from the Heroes plugin
        data.skipNext = true;

        return;
    }

    @Override
    protected void handlePlayerAnimationEvent(final PlayerAnimationEvent event, final Priority priority) {
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
