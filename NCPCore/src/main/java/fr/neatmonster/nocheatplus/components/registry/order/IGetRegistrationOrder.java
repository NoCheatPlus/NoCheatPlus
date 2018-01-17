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
package fr.neatmonster.nocheatplus.components.registry.order;

/**
 * Just provide a neutral getter, independent of what context it is used in.
 * There will be confusion potential, so this remains subject to an overhaul
 * later on.
 * <hr>
 * Typical uses:
 * <ul>
 * <li>IRegisterWithOrder might just extend this one, renaming and adding more
 * methods pending there.</li>
 * <li>IGetRegistrationOrder can be implemented to enable use of sorting
 * methods.</li>
 * </ul>
 * 
 * @author asofold
 *
 */
public interface IGetRegistrationOrder {

    public RegistrationOrder getRegistrationOrder();

}
