package me.neatmonster.nocheatplus.checks.blockplace;

import java.util.LinkedList;
import java.util.List;

import me.neatmonster.nocheatplus.EventManager;
import me.neatmonster.nocheatplus.NoCheatPlus;
import me.neatmonster.nocheatplus.NoCheatPlusPlayer;
import me.neatmonster.nocheatplus.config.ConfigurationCacheStore;
import me.neatmonster.nocheatplus.config.Permissions;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 * Central location to listen to Block-related events and dispatching them to
 * checks
 * 
 */
public class BlockPlaceCheckListener implements Listener, EventManager {

    private final FastPlaceCheck  fastPlaceCheck;
    private final ReachCheck      reachCheck;
    private final DirectionCheck  directionCheck;
    private final ProjectileCheck projectileCheck;
    private final NoCheatPlus     plugin;

    public BlockPlaceCheckListener(final NoCheatPlus plugin) {

        this.plugin = plugin;

        fastPlaceCheck = new FastPlaceCheck(plugin);
        reachCheck = new ReachCheck(plugin);
        directionCheck = new DirectionCheck(plugin);
        projectileCheck = new ProjectileCheck(plugin);
    }

    @Override
    public List<String> getActiveChecks(final ConfigurationCacheStore cc) {
        final LinkedList<String> s = new LinkedList<String>();

        final BlockPlaceConfig bp = BlockPlaceCheck.getConfig(cc);

        if (bp.fastPlaceCheck)
            s.add("blockplace.fastplace");
        if (bp.reachCheck)
            s.add("blockplace.reach");
        if (bp.directionCheck)
            s.add("blockplace.direction");
        if (bp.projectileCheck)
            s.add("blockplace.projectileCheck");

        return s;
    }

    /**
     * We listen to BlockPlace events for obvious reasons
     * 
     * @param event
     *            the BlockPlace event
     */
    @EventHandler(
            ignoreCancelled = true, priority = EventPriority.LOWEST)
    protected void handleBlockPlaceEvent(final BlockPlaceEvent event) {

        if (event.getBlock() == null || event.getBlockAgainst() == null)
            return;

        boolean cancelled = false;

        final NoCheatPlusPlayer player = plugin.getPlayer(event.getPlayer());
        final BlockPlaceConfig cc = BlockPlaceCheck.getConfig(player);
        final BlockPlaceData data = BlockPlaceCheck.getData(player);

        // Remember these locations and put them in a simpler "format"
        data.blockPlaced.set(event.getBlock());
        data.blockPlacedAgainst.set(event.getBlockAgainst());

        // Now do the actual checks

        // First the fastplace check
        if (cc.fastPlaceCheck && !player.hasPermission(Permissions.BLOCKPLACE_FASTPLACE))
            cancelled = fastPlaceCheck.check(player, data, cc);

        // Second the reach check
        if (!cancelled && cc.reachCheck && !player.hasPermission(Permissions.BLOCKPLACE_REACH))
            cancelled = reachCheck.check(player, data, cc);

        // Third the direction check
        if (!cancelled && cc.directionCheck && !player.hasPermission(Permissions.BLOCKPLACE_DIRECTION))
            cancelled = directionCheck.check(player, data, cc);

        // If one of the checks requested to cancel the event, do so
        if (cancelled)
            event.setCancelled(cancelled);
    }

    @EventHandler(
            ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void monsterEgg(final PlayerInteractEvent event) {

        // We are only interested by monster eggs
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getPlayer().getItemInHand() == null
                || event.getPlayer().getItemInHand().getType() != Material.MONSTER_EGG)
            return;

        final NoCheatPlusPlayer player = plugin.getPlayer(event.getPlayer());
        final BlockPlaceConfig cc = BlockPlaceCheck.getConfig(player);
        final BlockPlaceData data = BlockPlaceCheck.getData(player);

        // Do the actual check
        if (cc.projectileCheck && !player.hasPermission(Permissions.BLOCKPLACE_PROJECTILE)
                && projectileCheck.check(player, data, cc))
            // If the check is positive, cancel the event
            event.setCancelled(true);
    }

    @EventHandler(
            ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void otherProjectiles(final ProjectileLaunchEvent event) {

        // We are only interested by enderpears, endersignals, eggs, snowballs and expbottles
        // of course thrown by a player
        if (!(event.getEntity().getShooter() instanceof Player))
            return;
        switch (event.getEntityType()) {
        case ENDER_PEARL:
            break;
        case ENDER_SIGNAL:
            break;
        case EGG:
            break;
        case SNOWBALL:
            break;
        case THROWN_EXP_BOTTLE:
            break;
        default:
            return;
        }

        final NoCheatPlusPlayer player = plugin.getPlayer((Player) event.getEntity().getShooter());
        final BlockPlaceConfig cc = BlockPlaceCheck.getConfig(player);
        final BlockPlaceData data = BlockPlaceCheck.getData(player);

        // Do the actual check
        if (cc.projectileCheck && !player.hasPermission(Permissions.BLOCKPLACE_PROJECTILE)
                && projectileCheck.check(player, data, cc))
            // If the check is positive, cancel the event
            event.setCancelled(true);
    }

    /**
     * If the player places three times the same sign,
     * the sign will be destroyed and looted
     * 
     * @param event
     *            the SignChange event
     */
    @EventHandler(
            ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void sign(final SignChangeEvent event) {

        final NoCheatPlusPlayer player = plugin.getPlayer(event.getPlayer());
        final BlockPlaceData data = BlockPlaceCheck.getData(player);

        // Check if the sign's content is empty
        if (event.getLine(0).length() + event.getLine(1).length() + event.getLine(2).length()
                + event.getLine(3).length() == 0)
            return;

        // Check if the text is the same
        if (!event.getPlayer().hasPermission(Permissions.BLOCKPLACE_AUTOSIGN)
                && event.getLine(0).equals(data.lastSignText[0]) && event.getLine(1).equals(data.lastSignText[1])
                && event.getLine(2).equals(data.lastSignText[2]) && event.getLine(3).equals(data.lastSignText[3])
                && data.lastSignText[0].equals(data.lastLastSignText[0])
                && data.lastSignText[1].equals(data.lastLastSignText[1])
                && data.lastSignText[2].equals(data.lastLastSignText[2])
                && data.lastSignText[3].equals(data.lastLastSignText[3]))
            event.getBlock().breakNaturally();

        // Save the text
        data.lastLastSignText[3] = data.lastSignText[3];
        data.lastLastSignText[2] = data.lastSignText[2];
        data.lastLastSignText[1] = data.lastSignText[1];
        data.lastLastSignText[0] = data.lastSignText[0];
        data.lastSignText[3] = event.getLine(3);
        data.lastSignText[2] = event.getLine(2);
        data.lastSignText[1] = event.getLine(1);
        data.lastSignText[0] = event.getLine(0);
    }
}
