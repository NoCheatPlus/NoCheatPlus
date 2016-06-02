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

import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.checks.access.CheckConfigFactory;
import fr.neatmonster.nocheatplus.config.ConfigFile;
import fr.neatmonster.nocheatplus.config.WorldConfigCache;

/**
 * Copy-on-write per-world configuration cache.
 * @author asofold
 *
 */
public class NetConfigCache extends WorldConfigCache<NetConfig> implements CheckConfigFactory {

    public NetConfigCache() {
        super(true);
    }

    @Override
    protected NetConfig newConfig(String key, ConfigFile configFile) {
        return new NetConfig(configFile);
    }

    @Override
    public NetConfig getConfig(final Player player) {
        return getConfig(player.getWorld());
    }

    @Override
    public void removeAllConfigs() {
        clearAllConfigs();
    }

}
