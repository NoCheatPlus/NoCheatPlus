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
package fr.neatmonster.nocheatplus.utilities.ds.map;

import java.util.Collection;
import java.util.concurrent.locks.Lock;

/**
 * Mapping Class<T> to <? extends T> for arbitrary T, aiming at simple generic
 * caches.
 * <hr/>
 * Until as specialized implementation is present, see {@link HashMapLOW} for
 * reference.
 * 
 * @author asofold
 *
 */
public class InstanceMapLOW {

    /*
     * TODO: Optimized implementation possible (performance/JIT)? Buckets and
     * entries could support the type relation.
     */
    private final HashMapLOW<Class<?>, Object> map;

    public InstanceMapLOW(final Lock lock, int initialSize) {
        map = new HashMapLOW<Class<?>, Object>(lock, 12);
    }

    @SuppressWarnings("unchecked")
    public <T, I extends T> T put(final Class<T> key, final I value) {
        return (T) map.put(key, value);
    }

    @SuppressWarnings("unchecked")
    public <T, I extends T> T putIfAbsent(final Class<T> key, final I value) {
        return (T) map.putIfAbsent(key, value);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(Class<T> key) {
        return (T) map.get(key);
    }

    @SuppressWarnings("unchecked")
    public <T> T getLocked(Class<T> key) {
        return (T) map.getLocked(key);
    }

    public boolean containsKey(final Class<?> key) {
        return map.containsKey(key);
    }

    public boolean containsKeyLocked(final Class<?> key) {
        return map.containsKeyLocked(key);
    }

    public Collection<Class<?>> getKeys() {
        return map.getKeys();
    }

    @SuppressWarnings("unchecked")
    public <T> T remove(final Class<T> key) {
        return (T) map.remove(key);
    }

    /**
     * (Note that all contained keys should be different classes.)
     * 
     * @param keys
     */
    public void remove(final Collection<Class<?>> keys) {
        map.remove(keys);
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }

    public int size() {
        return map.size();
    }

    public void clear() {
        map.clear();
    }

}
