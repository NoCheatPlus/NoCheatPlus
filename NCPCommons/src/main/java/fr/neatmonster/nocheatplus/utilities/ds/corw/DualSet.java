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
package fr.neatmonster.nocheatplus.utilities.ds.corw;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.locks.Lock;

/**
 * Use LinkedHashSet internally.
 * 
 * @author asofold
 *
 * @param <T>
 */
public class DualSet<T> extends DualCollection<T, Set<T>>{

    public DualSet() {
        super();
    }

    public DualSet(Lock lock) {
        super(lock);
    }

    @Override
    protected Set<T> newCollection() {
        return new LinkedHashSet<T>();
    }

}
