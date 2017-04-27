package fr.neatmonster.nocheatplus.checks.net;

import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.net.model.DataPacketFlying;
import fr.neatmonster.nocheatplus.components.registry.event.IHandle;

/**
 * Convenience for providing several checks with a lazy-init handle for fetching
 * queued flying packets. Future concept should be somehow linking follow-up
 * packets (flying->dig) to each other...
 * 
 * @author asofold
 *
 */
public class FlyingQueueHandle implements IHandle<DataPacketFlying[]> {

    private final Player player;
    private DataPacketFlying[] queue;

    public FlyingQueueHandle(Player player) {
        this.player = player;
    }

    @Override
    public DataPacketFlying[] getHandle() {
        if (queue == null) {
            queue = ((NetData) CheckType.NET.getDataFactory().getData(player)).copyFlyingQueue();
        }
        return queue;
    }

}
