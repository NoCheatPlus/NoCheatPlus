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

/**
 * Somehow access entity dimensions. (MCAccess extends this for now.)
 * 
 * @author asofold
 *
 */
public interface IEntityAccessDimensions {

    /**
     * Return some width (rather the full bounding box width).
     * 
     * @param entity
     * @return
     */
    public double getWidth(Entity entity);

    /**
     * Get height of an entity (attack relevant, the maximal "thing" found).
     * 
     * @param entity
     * @return
     */
    public double getHeight(Entity entity);

}
