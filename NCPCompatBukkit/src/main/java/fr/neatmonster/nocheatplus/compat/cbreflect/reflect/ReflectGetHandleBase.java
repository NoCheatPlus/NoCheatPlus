package fr.neatmonster.nocheatplus.compat.cbreflect.reflect;

import java.lang.reflect.Method;

import fr.neatmonster.nocheatplus.utilities.ReflectionUtil;

/**
 * Base for reflection on entities
 * 
 * @author asofold
 *
 * @param <BO>
 *            Bukkit object type for getHandle.
 */
public abstract class ReflectGetHandleBase <BO> {

    public final Method obcGetHandle;

    public ReflectGetHandleBase(ReflectBase base, Class<?> obcClass, Class<?> nmsClass) throws ClassNotFoundException {
        // getHandle
        obcGetHandle = ReflectionUtil.getMethodNoArgs(obcClass, "getHandle");
        // TODO: Consider throw in case of getHandle missing.
    }

    /**
     * Invoke getHandle on the bukkit object.
     * 
     * @param bukkitObject
     * @return
     */
    public Object getHandle(BO bukkitObject) {
        // TODO: CraftPlayer check (isAssignableFrom)?
        if (this.obcGetHandle == null) {
            fail();
        }
        final Object handle = ReflectionUtil.invokeMethodNoArgs(this.obcGetHandle, bukkitObject);
        if (handle == null) {
            fail();
        }
        return handle;
    }

    protected abstract void fail();

}
