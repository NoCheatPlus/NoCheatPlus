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
