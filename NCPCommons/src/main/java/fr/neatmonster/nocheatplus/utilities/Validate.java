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
package fr.neatmonster.nocheatplus.utilities;

/**
 * Simple parameter/thing validation.
 * 
 * @author asofold
 *
 */
public class Validate {

    /**
     * Throw a NullPointerException if any given object is null.
     * 
     * @param objects
     * @throws NullPointerException
     *             If any object is null.
     */
    public static void validateNotNull(final Object...objects) {
        for (int i = 0; i < objects.length; i++) {
            if (objects[i] == null) {
                throw new NullPointerException("Object at index " + i + " is null.");
            }
        }
    }

}
