package fr.neatmonster.nocheatplus.compat.spigotcb1_9_R1;

import org.bukkit.craftbukkit.v1_9_R1.entity.CraftEntity;
import org.bukkit.entity.Entity;

import fr.neatmonster.nocheatplus.components.location.IEntityAccessLastPositionAndLook;
import fr.neatmonster.nocheatplus.components.location.ISetPositionWithLook;

public class EntityAccessLastPositionAndLook implements IEntityAccessLastPositionAndLook {

    @Override
    public void setPositionAndLook(final Entity entity, final ISetPositionWithLook location) {
        // TODO: Error handling / conventions.
        final net.minecraft.server.v1_9_R1.Entity nmsEntity = ((CraftEntity) entity).getHandle();
        location.setX(nmsEntity.lastX);
        location.setY(nmsEntity.lastY);
        location.setZ(nmsEntity.lastZ);
        location.setYaw(nmsEntity.lastYaw);
        location.setPitch(nmsEntity.lastPitch);
    }
}
