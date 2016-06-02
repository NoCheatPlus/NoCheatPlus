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
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

/**
 * Map that provides time stamp based expiration of entries. Time stamps get set
 * with each access.
 * 
 * @author mc_dev
 *
 * @param <K>
 * @param <V>
 */
public class ManagedMap<K, V>{

    protected class ValueWrap{
        public long ts;
        public  V value;
        public ValueWrap(final V value){
            ts = System.currentTimeMillis();
            this.value = value;
        }
    }

    protected final LinkedHashMap<K, ValueWrap> map;

    public ManagedMap(int defaultCapacity, float loadFactor){
        map = new LinkedHashMap<K, ValueWrap>(defaultCapacity, loadFactor, true);
    }

    /**
     * 
     * @param key
     * @param value Previously contained value or null.
     * @return
     */
    public V put(final K key, final V value){
        final ValueWrap wrap = map.get(key);
        if (wrap == null){
            map.put(key, new ValueWrap(value));
            return null;
        }
        else{
            final V res = wrap.value;
            wrap.value = value;
            wrap.ts = System.currentTimeMillis();
            return res;
        }
    }

    public V get(final K key){
        final ValueWrap wrap = map.get(key);
        if (wrap == null) return null;
        else{
            wrap.ts = System.currentTimeMillis();
            return wrap.value;
        }
    }

    public V remove(final K key){
        final ValueWrap wrap = map.remove(key);
        if (wrap == null) return null;
        else return wrap.value;
    }

    public void clear(){
        map.clear();
    }

    /**
     * Remove entries that are older than ts.
     * @param ts
     * @return
     */
    public Collection<K> expire(final long ts){
        final List<K> rem = new LinkedList<K>(); 
        for (final Entry<K, ValueWrap> entry : map.entrySet()){
            final ValueWrap wrap = entry.getValue();
            if (wrap.ts < ts) rem.add(entry.getKey());
            else break;
        }
        for (final K key : rem){
            map.remove(key);
        }
        return rem;
    }

}
