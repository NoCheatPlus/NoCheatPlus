package fr.neatmonster.nocheatplus.compat.cbreflect.reflect;

import java.lang.reflect.Field;

import org.bukkit.entity.Entity;

import fr.neatmonster.nocheatplus.NCPAPIProvider;
import fr.neatmonster.nocheatplus.components.location.IEntityAccessLastPositionAndLook;
import fr.neatmonster.nocheatplus.components.location.IGetPositionWithLook;
import fr.neatmonster.nocheatplus.components.location.ISetPositionWithLook;
import fr.neatmonster.nocheatplus.logging.Streams;
import fr.neatmonster.nocheatplus.utilities.ReflectionUtil;
import fr.neatmonster.nocheatplus.utilities.Validate;

/**
 * Access last position and look. This must be able to work independently of
 * MCAccess. Should be registered as generic instance for
 * IEntityAccessLastPositionAndLook.
 * 
 * @author asofold
 *
 */
public class ReflectEntityLastPositionAndLook extends ReflectGetHandleBase<Entity> implements IEntityAccessLastPositionAndLook {

    public final Field lastX, lastY, lastZ, lastYaw, lastPitch;

    public ReflectEntityLastPositionAndLook() throws ClassNotFoundException {
        this(new ReflectBase());
    }

    public ReflectEntityLastPositionAndLook(ReflectBase base) throws ClassNotFoundException {
        this(base, Class.forName(base.obcPackageName + ".entity.CraftEntity"), Class.forName(base.nmsPackageName + ".Entity"));
    }

    public ReflectEntityLastPositionAndLook(ReflectBase base, Class<?> obcClass, Class<?> nmsClass) throws ClassNotFoundException, NoSuchFieldError {
        super(base, obcClass, nmsClass);
        lastX = ReflectionUtil.getField(nmsClass, "lastX", double.class);
        lastY = ReflectionUtil.getField(nmsClass, "lastY", double.class);
        lastZ = ReflectionUtil.getField(nmsClass, "lastZ", double.class);
        lastYaw = ReflectionUtil.getField(nmsClass, "lastYaw", float.class);
        lastPitch = ReflectionUtil.getField(nmsClass, "lastPitch", float.class);
        Validate.validateNotNull(lastX, lastY, lastZ, lastYaw, lastPitch);
    }

    @Override
    public void getPositionAndLook(final Entity entity, final ISetPositionWithLook location) {
        try {
            performGet(entity, location);
        }
        catch (Throwable t) {
            NCPAPIProvider.getNoCheatPlusAPI().getLogManager().warning(Streams.STATUS, "Could not retrieve last position and look for Entity: " + entity.getClass().getName());
        }
    }

    private void performGet(final Entity entity, final ISetPositionWithLook location) throws IllegalArgumentException, IllegalAccessException {
        final Object nmsObject = getHandle(entity);
        location.setX(lastX.getDouble(nmsObject));
        location.setY(lastY.getDouble(nmsObject));
        location.setZ(lastZ.getDouble(nmsObject));
        location.setYaw(lastYaw.getFloat(nmsObject));
        location.setPitch(lastPitch.getFloat(nmsObject));
    }

    @Override
    public void setPositionAndLook(Entity entity, IGetPositionWithLook location) {
        try {
            performSet(entity, location);
        }
        catch (Throwable t) {
            NCPAPIProvider.getNoCheatPlusAPI().getLogManager().warning(Streams.STATUS, "Could not set last position and look for Entity: " + entity.getClass().getName());
        }
    }

    private void performSet(Entity entity, IGetPositionWithLook location) throws IllegalArgumentException, IllegalAccessException {
        final Object nmsObject = getHandle(entity);
        lastX.setDouble(nmsObject, location.getX());
        lastY.setDouble(nmsObject, location.getY());
        lastZ.setDouble(nmsObject, location.getZ());
        lastYaw.setFloat(nmsObject, location.getYaw());
        lastPitch.setFloat(nmsObject, location.getPitch());
    }

    @Override
    protected void fail() {
        throw new RuntimeException("Not available."); // To be caught internally.
    }

}
