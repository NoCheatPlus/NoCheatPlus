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
package fr.neatmonster.nocheatplus.components.location;

/**
 * A standard (3D) block position with coordinate access (int). Some classes
 * might implement both IGetPosition and IGetBlockPosition, thus utility methods
 * that use IGetBlockPosition as arguments might better have a suffix 'Block',
 * for the case that the same kind of method just using IGetPosition exists (see
 * TrigUtil/LocUtil).
 * 
 * @author asofold
 *
 */
public interface IGetBlockPosition {

    public int getBlockX();

    public int getBlockY();

    public int getBlockZ();

}
