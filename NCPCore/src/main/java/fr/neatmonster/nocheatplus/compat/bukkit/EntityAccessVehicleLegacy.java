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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.bukkit.entity.Entity;

import fr.neatmonster.nocheatplus.components.entity.IEntityAccessVehicle;

/**
 * Legacy access to vehicle entities (single passenger only).
 * 
 * @author asofold
 *
 */
public class EntityAccessVehicleLegacy implements IEntityAccessVehicle {

    private final List<Entity> nullPassenger;

    public EntityAccessVehicleLegacy() {
        List<Entity> lst = new ArrayList<Entity>(1);
        lst.add(null);
        nullPassenger = Collections.unmodifiableList(lst);
    }

    @SuppressWarnings("deprecation")
    @Override
    public List<Entity> getEntityPassengers(final Entity entity) {
        final Entity passenger = entity.getPassenger();
        return passenger == null ? nullPassenger : Arrays.asList(passenger);
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean addPassenger(final Entity entity, final Entity vehicle) {
        return vehicle.setPassenger(entity);
    }

}
