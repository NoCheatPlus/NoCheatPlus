package cc.co.evenprime.bukkit.nocheat.listeners;

import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerMoveEvent;

import cc.co.evenprime.bukkit.nocheat.checks.SpeedhackCheck;


public class SpeedhackPlayerMonitor extends PlayerListener {
    
    private final SpeedhackCheck check;

    public SpeedhackPlayerMonitor(SpeedhackCheck check) {
        this.check = check;
    }
    @Override
    public void onPlayerMove(PlayerMoveEvent event) {
        if(event.isCancelled()) {
            check.teleported(event.getPlayer());
        }
    }
}
