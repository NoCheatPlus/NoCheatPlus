package fr.neatmonster.nocheatplus.compat.bukkit;

import java.util.Arrays;
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

    @SuppressWarnings("deprecation")
    @Override
    public List<Entity> getEntityPassengers(final Entity entity) {
        return Arrays.asList(entity.getPassenger());
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean addPassenger(final Entity entity, final Entity vehicle) {
        return vehicle.setPassenger(entity);
    }

}
