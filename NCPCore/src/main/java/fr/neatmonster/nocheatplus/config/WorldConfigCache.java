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
package fr.neatmonster.nocheatplus.config;

import org.bukkit.World;

/**
 * World-specific cache for configurations. World names are used as keys, convenience methods are added.
 * @author asofold
 *
 * @param <C>
 */
public abstract class WorldConfigCache<C> extends ConfigCache<String, C> {

    /**
     * 
     * @param cow If true, copy-on-write is used (thread-safe), otherwise an ordinary HashMap.
     */
    public WorldConfigCache(boolean cow) {
        this(cow, 10);
    }

    /**
     * 
     * @param cow If true, copy-on-write is used (thread-safe), otherwise an ordinary HashMap.
     * @param initialCapacity
     */
    public WorldConfigCache(boolean cow, int initialCapacity) {
        super(cow, initialCapacity);
    }

    public C getConfig(final World world) {
        return getConfig(world.getName());
    }

    @Override
    protected C newConfig(final String key) {
        return newConfig(key, ConfigManager.getConfigFile(key));
    }

    protected abstract C newConfig(String key, ConfigFile configFile);

}
