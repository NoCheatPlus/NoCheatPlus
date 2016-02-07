package fr.neatmonster.nocheatplus.checks.net;

import java.util.Iterator;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.access.CheckDataFactory;
import fr.neatmonster.nocheatplus.components.ICanHandleTimeRunningBackwards;
import fr.neatmonster.nocheatplus.utilities.ds.map.HashMapLOW;

/**
 * Factory for NetData.
 * @author asofold
 *
 */
public class NetDataFactory implements CheckDataFactory, ICanHandleTimeRunningBackwards {

    private final HashMapLOW<String, NetData> dataMap = new HashMapLOW<String, NetData>(Math.min(Bukkit.getServer().getMaxPlayers(), 500));

    @Override
    public void removeAllData() {
        dataMap.clear();
    }

    @Override
    public NetData getData(Player player) {
        NetData data = dataMap.get(player.getName());
        if (data != null) {
            return data;
        } else {
            data = new NetData((NetConfig) CheckType.NET.getConfigFactory().getConfig(player));
            dataMap.put(player.getName(), data);
            return data;
        }
    }

    @Override
    public NetData removeData(String playerName) {
        return dataMap.remove(playerName);
    }

    @Override
    public void handleTimeRanBackwards() {
        final Iterator<Entry<String, NetData>> it = dataMap.iterator();
        while (it.hasNext()) {
            final Entry<String, NetData> entry = it.next();
            entry.getValue().handleSystemTimeRanBackwards();
        }
    }

}
