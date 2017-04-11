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

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.Lock;

/**
 * Use LinkedList internally.
 * 
 * @author asofold
 *
 * @param <T>
 */
public class DualList<T> extends DualCollection<T, List<T>> {

    public DualList() {
        super();
    }

    public DualList(Lock lock) {
        super(lock);
    }

    @Override
    protected List<T> newCollection() {
        return new LinkedList<T>();
    }

}
