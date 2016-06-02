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
 * Having a looking direction, represented by pitch and yaw.
 * 
 * @author asofold
 *
 */
public interface IGetLook {

    /**
     * Angle for vertical looking direction component in grad.
     * 
     * @return
     */
    public float getPitch();

    /**
     * Angle on xz-plane for the looking direction in grad.
     * 
     * @return
     */
    public float getYaw();

}
