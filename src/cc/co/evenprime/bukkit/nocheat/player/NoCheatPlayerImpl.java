package cc.co.evenprime.bukkit.nocheat.player;

import net.minecraft.server.EntityPlayer;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;

import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.NoCheatPlayer;
import cc.co.evenprime.bukkit.nocheat.config.cache.ConfigurationCache;
import cc.co.evenprime.bukkit.nocheat.data.BaseData;

public class NoCheatPlayerImpl implements NoCheatPlayer {

    private final Player   player;
    private final NoCheat  plugin;
    private final BaseData data;

    public NoCheatPlayerImpl(String playerName, NoCheat plugin, BaseData data) {
        this.player = Bukkit.getServer().getPlayer(playerName);
        this.plugin = plugin;
        this.data = data;
    }

    public boolean hasPermission(String permission) {
        return player.hasPermission(permission);
    }

    public BaseData getData() {
        return data;
    }

    public Player getPlayer() {
        return player;
    }

    public ConfigurationCache getConfiguration() {
        return plugin.getConfig(player);
    }

    public String getName() {
        return player.getName();
    }

    public int getTicksLived() {
        return player.getTicksLived();
    }

    public void increaseAge(int ticks) {
        EntityPlayer p = ((CraftPlayer) player).getHandle();
        for(int i = 0; i < ticks; i++) {
            // TODO: This is highly fragile and breaks every update!!

            p.b(true); // Catch up with the server, one tick at a time
        }
    }

}
