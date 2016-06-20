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
package fr.neatmonster.nocheatplus.components.entity;

import org.bukkit.entity.Entity;

import fr.neatmonster.nocheatplus.components.location.IGetPositionWithLook;
import fr.neatmonster.nocheatplus.components.location.ISetPositionWithLook;

/**
 * Retrieve last position and look for a Bukkit Entity instance.
 * 
 * @author asofold
 *
 */
public interface IEntityAccessLastPositionAndLook {

    /**
     * Fetch the last position with look from an entity.
     * 
     * @param entity
     * @param location
     *            This instance gets updated by last coordinates and looking
     *            direction.
     */
    public void getPositionAndLook(Entity entity, ISetPositionWithLook location);

    /**
     * Set the last position with look of an entity.
     * 
     * @param entity
     *            The entity for which to set last position and look.
     * @param location
     *            The reference data to set the last position and look of the
     *            entity to.
     */
    public void setPositionAndLook(Entity entity, IGetPositionWithLook location);

}
