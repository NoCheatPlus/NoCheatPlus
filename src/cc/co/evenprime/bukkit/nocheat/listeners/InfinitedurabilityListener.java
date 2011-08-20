package cc.co.evenprime.bukkit.nocheat.listeners;

import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerListener;

import cc.co.evenprime.bukkit.nocheat.checks.InfinitedurabilityCheck;


public class InfinitedurabilityListener extends PlayerListener {

    private final InfinitedurabilityCheck check;

    public InfinitedurabilityListener(InfinitedurabilityCheck check) {
        this.check = check;
    }

    @Override
    public void onItemHeldChange(PlayerItemHeldEvent event) {
        check.check(event);
    }
    
    @Override
    public void onPlayerInteract(PlayerInteractEvent event) {
        check.check(event);
    }
}
