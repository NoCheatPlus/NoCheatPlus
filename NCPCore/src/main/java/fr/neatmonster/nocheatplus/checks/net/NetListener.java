package fr.neatmonster.nocheatplus.checks.net;

import fr.neatmonster.nocheatplus.checks.CheckListener;
import fr.neatmonster.nocheatplus.checks.CheckType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerTeleportEvent;

public class NetListener extends CheckListener {

    public NetListener() {
        super(CheckType.NET);
    }

    public final EqualsRotate equalsRotate = new EqualsRotate();

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onTeleport(final PlayerTeleportEvent event){
        final long time = System.currentTimeMillis();
        final Player player = event.getPlayer();
        final NetData data = (NetData) CheckType.NET.getDataFactory().getData(player);
        //final NetConfig config = (NetConfig) CheckType.NET.getConfigFactory().getConfig(player);
        equalsRotate.handleTeleport(time, data);
    }
}
