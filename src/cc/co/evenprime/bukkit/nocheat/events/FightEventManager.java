package cc.co.evenprime.bukkit.nocheat.events;

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
import cc.co.evenprime.bukkit.nocheat.checks.FightCheck;
import cc.co.evenprime.bukkit.nocheat.checks.fight.DirectionCheck;
import cc.co.evenprime.bukkit.nocheat.checks.fight.NoswingCheck;
import cc.co.evenprime.bukkit.nocheat.checks.fight.SelfhitCheck;
import cc.co.evenprime.bukkit.nocheat.config.Permissions;
import cc.co.evenprime.bukkit.nocheat.config.cache.CCFight;
import cc.co.evenprime.bukkit.nocheat.config.cache.ConfigurationCache;
import cc.co.evenprime.bukkit.nocheat.data.FightData;
import cc.co.evenprime.bukkit.nocheat.debug.PerformanceManager.EventType;

public class FightEventManager extends EventManagerImpl {

    private final List<FightCheck> checks;

    public FightEventManager(NoCheat plugin) {
        super(plugin);

        this.checks = new ArrayList<FightCheck>(3);
        this.checks.add(new NoswingCheck(plugin));
        this.checks.add(new SelfhitCheck(plugin));
        this.checks.add(new DirectionCheck(plugin));

        registerListener(Event.Type.ENTITY_DAMAGE, Priority.Lowest, true, plugin.getPerformance(EventType.FIGHT));
        registerListener(Event.Type.PLAYER_ANIMATION, Priority.Monitor, false, null);
    }

    @Override
    protected void handleEntityAttackDamageByPlayerEvent(final EntityDamageByEntityEvent event, final Priority priority) {

        final Player damager = (Player) event.getDamager();

        final NoCheatPlayer player = plugin.getPlayer(damager);
        final CCFight cc = player.getConfiguration().fight;

        if(!cc.check || player.hasPermission(Permissions.FIGHT)) {
            return;
        }

        final FightData data = player.getData().fight;

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

        final FightData data = player.getData().fight;

        // Skip the next damage event, because it is the same as this one
        // just mislabelled as a "direct" attack from one player onto another
        data.skipNext = true;

        return;
    }

    @Override
    protected void handleCustomDamageByPlayerEvent(final EntityDamageByEntityEvent event, final Priority priority) {

        final Player damager = (Player) event.getDamager();
        final NoCheatPlayer player = plugin.getPlayer(damager);

        final FightData data = player.getData().fight;

        // Skip the next damage event, because it is with high probability
        // something from the Heroes plugin
        data.skipNext = true;

        return;
    }

    @Override
    protected void handlePlayerAnimationEvent(final PlayerAnimationEvent event, final Priority priority) {
        plugin.getPlayer(event.getPlayer()).getData().fight.armswung = true;
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
