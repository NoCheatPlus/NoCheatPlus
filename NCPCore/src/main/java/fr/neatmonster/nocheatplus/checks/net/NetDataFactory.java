package fr.neatmonster.nocheatplus.checks.net;

import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.access.CheckDataFactory;
import fr.neatmonster.nocheatplus.utilities.ds.corw.LinkedHashMapCOW;

/**
 * Currently primary thread only!
 * @author asofold
 *
 */
public class NetDataFactory implements CheckDataFactory {

    private final LinkedHashMapCOW<String, NetData> dataMap = new LinkedHashMapCOW<String, NetData>();

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

}
