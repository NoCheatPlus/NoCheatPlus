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
package fr.neatmonster.nocheatplus.components.pool;

import java.util.ArrayList;

public abstract class AbstractPool <O> implements GenericPool <O> {

    private final int maxPoolSize;
    private final ArrayList<O> pool;

    /**
     * 
     * @param maxPoolSize
     *            A value <= 0 means that all elements are pooled always.
     */
    protected AbstractPool(int maxPoolSize) {
        this.maxPoolSize = maxPoolSize;
        pool = new ArrayList<O>(Math.max(10, maxPoolSize));
    }

    @Override
    public O getInstance() {
        if (!pool.isEmpty()) {
            return pool.remove(pool.size() - 1); // Prevent re-size.
        }
        else {
            return newInstance();
        }
    }

    @Override
    public void returnInstance(O instance) {
        if (instance == null) {
            throw new NullPointerException("The passed instance must not be null.");
        }
        if (maxPoolSize <= 0 || pool.size() < maxPoolSize) {
            pool.add(instance);
        }
        else {
            // GC.
        }
    }

    /**
     * Get a new instance to return directly.
     * @return
     */
    protected abstract O newInstance();

}
