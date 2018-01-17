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

public class CoordHash {

    private static final int p1 = 73856093;
    private static final int p2 = 19349663;
    private static final int p3 = 83492791;

    /**
     * Standard int-based hash code for a 3D-space, using multiplication with
     * primes and XOR results.
     * 
     * @param x
     * @param y
     * @param z
     * @return
     */
    // TODO: Link paper, or find a better one :p.
    public static final int hashCode3DPrimes(final int x, final int y, final int z) {
        return p1 * x ^ p2 * y ^ p3 * z;
    }

}
