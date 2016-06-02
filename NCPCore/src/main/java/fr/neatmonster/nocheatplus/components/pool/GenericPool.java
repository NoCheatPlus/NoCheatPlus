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

/**
 * A generic pool allowing to get instances and return to be pooled for
 * efficiency. These are meant fail-safe, unless stated otherwise. So extra
 * conditions like maximum number of instances in use must be specified by the
 * implementation.
 * 
 * @author asofold
 *
 * @param <O>
 */
public interface GenericPool <O> {

    /**
     * Get an instance.
     * @return
     */
    public O getInstance();

    /**
     * Return an instance to be returned on getInstance later on.
     * 
     * @param instance
     * 
     * @throws NullPointerException
     *             if instance is null.
     */
    public void returnInstance(O instance);

}
