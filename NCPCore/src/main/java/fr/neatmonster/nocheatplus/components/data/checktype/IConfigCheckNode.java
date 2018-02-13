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
package fr.neatmonster.nocheatplus.components.data.checktype;

/**
 * Generic per check type node within an IConfigData instance. These are meant
 * to be stored within context-specific data/configurations for most efficient
 * access.
 * 
 * @author asofold
 *
 */
public interface IConfigCheckNode extends IBaseCheckNode {

    public boolean isCheckActive();

}
