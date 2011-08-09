package cc.co.evenprime.bukkit.nocheat.listeners;

import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerVelocityEvent;

import cc.co.evenprime.bukkit.nocheat.DataManager;
import cc.co.evenprime.bukkit.nocheat.checks.MovingCheck;
import cc.co.evenprime.bukkit.nocheat.data.MovingData;

/**
 * 
 * @author Evenprime
 *
 */
public class MovingPlayerMonitor extends PlayerListener {

    private final MovingCheck check;
    private final DataManager dataManager;

    public MovingPlayerMonitor(DataManager dataManager, MovingCheck check) {
        this.dataManager = dataManager;
        this.check = check;
    }

    @Override
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        MovingData data = dataManager.getMovingData(event.getPlayer());
        data.wasTeleported = true;
        data.setBackPoint = null;
        data.jumpPhase = 0;
    }

    @Override
    public void onPlayerPortal(PlayerPortalEvent event) {
        check.teleported(event);		
    }

    @Override
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        check.teleported(event);
    }
    
    @Override
    public void onPlayerVelocity(PlayerVelocityEvent event) {
        check.updateVelocity(event.getVelocity(), dataManager.getMovingData(event.getPlayer()));
    }

    @Override
    public void onPlayerMove(PlayerMoveEvent event) {

        if(!event.isCancelled()) {
            if( event.getPlayer().isInsideVehicle()) {
                MovingData data = dataManager.getMovingData(event.getPlayer());
                data.setBackPoint = event.getTo();	
            }
        }
    }
}
