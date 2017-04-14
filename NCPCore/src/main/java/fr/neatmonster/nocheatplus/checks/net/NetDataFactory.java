/*
 * This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package fr.neatmonster.nocheatplus.checks.net;

import java.util.Iterator;
import java.util.UUID;
import java.util.Map.Entry;

import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.access.CheckDataFactory;
import fr.neatmonster.nocheatplus.checks.access.ICheckData;
import fr.neatmonster.nocheatplus.components.data.ICanHandleTimeRunningBackwards;
import fr.neatmonster.nocheatplus.utilities.ds.map.HashMapLOW;

/**
 * Factory for NetData.
 * @author asofold
 *
 */
public class NetDataFactory implements CheckDataFactory, ICanHandleTimeRunningBackwards {

    private final HashMapLOW<String, NetData> dataMap = new HashMapLOW<String, NetData>(35);

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
    public ICheckData getDataIfPresent(UUID playerId, String playerName) {
        return dataMap.get(playerName);
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
