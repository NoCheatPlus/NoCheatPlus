package cc.co.evenprime.bukkit.nocheat.listeners;

import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerPickupItemEvent;

import cc.co.evenprime.bukkit.nocheat.checks.BogusitemsCheck;

public class BogusitemsPlayerListener extends PlayerListener {

    private final BogusitemsCheck check;

    public BogusitemsPlayerListener(BogusitemsCheck bogusitemsCheck) {
        check = bogusitemsCheck;
    }

    @Override
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {

        if(!event.isCancelled())
            check.check(event);
    }

    @Override
    public void onPlayerDropItem(PlayerDropItemEvent event) {

        check.check(event);
    }

    @Override
    public void onPlayerInteract(PlayerInteractEvent event) {

        if(!event.isCancelled())
            check.check(event);
    }

}
