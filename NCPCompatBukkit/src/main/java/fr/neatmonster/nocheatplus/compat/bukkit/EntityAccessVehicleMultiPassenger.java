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
package fr.neatmonster.nocheatplus.compat.bukkit;

import java.util.List;

import org.bukkit.entity.Entity;

import fr.neatmonster.nocheatplus.components.entity.IEntityAccessVehicle;
import fr.neatmonster.nocheatplus.utilities.ReflectionUtil;

public class EntityAccessVehicleMultiPassenger implements IEntityAccessVehicle {

    public EntityAccessVehicleMultiPassenger() {
        // TODO: List<Entity>
        if (ReflectionUtil.getMethodNoArgs(Entity.class, "getPassengers", List.class) == null) {
            throw new RuntimeException("Not supported.");
        }
    }

    @Override
    public List<Entity> getEntityPassengers(final Entity entity) {
        return entity.getPassengers();
    }

    @Override
    public boolean addPassenger(final Entity entity, final Entity vehicle) {
        return vehicle.addPassenger(entity);
    }

}
