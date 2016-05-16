package fr.neatmonster.nocheatplus.compat.cbdev;

import org.bukkit.craftbukkit.v1_9_R2.entity.CraftEntity;
import org.bukkit.entity.Entity;

import fr.neatmonster.nocheatplus.components.location.IEntityAccessLastPositionAndLook;
import fr.neatmonster.nocheatplus.components.location.IGetPositionWithLook;
import fr.neatmonster.nocheatplus.components.location.ISetPositionWithLook;
import fr.neatmonster.nocheatplus.utilities.ReflectionUtil;

public class EntityAccessLastPositionAndLook implements IEntityAccessLastPositionAndLook {

    public EntityAccessLastPositionAndLook() {
        ReflectionUtil.checkMembers(net.minecraft.server.v1_9_R2.Entity.class, double.class, new String[] {
                "lastX", "lastY", "lastZ"
        });
        ReflectionUtil.checkMembers(net.minecraft.server.v1_9_R2.Entity.class, float.class, new String[] {
                "lastYaw", "lastPitch"
        });
    }

    @Override
    public void getPositionAndLook(final Entity entity, final ISetPositionWithLook location) {
        // TODO: Error handling / conventions.
        final net.minecraft.server.v1_9_R2.Entity nmsEntity = ((CraftEntity) entity).getHandle();
        location.setX(nmsEntity.lastX);
        location.setY(nmsEntity.lastY);
        location.setZ(nmsEntity.lastZ);
        location.setYaw(nmsEntity.lastYaw);
        location.setPitch(nmsEntity.lastPitch);
    }

    @Override
    public void setPositionAndLook(Entity entity, IGetPositionWithLook location) {
        final net.minecraft.server.v1_9_R2.Entity nmsEntity = ((CraftEntity) entity).getHandle();
        nmsEntity.lastX = location.getX();
        nmsEntity.lastY = location.getY();
        nmsEntity.lastZ = location.getZ();
        nmsEntity.lastYaw = location.getYaw();
        nmsEntity.lastPitch = location.getPitch();
    }

}
