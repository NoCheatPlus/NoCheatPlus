package fr.neatmonster.nocheatplus.compat.cbreflect.reflect;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import fr.neatmonster.nocheatplus.compat.cbreflect.reflect.ReflectHelper.ReflectFailureException;
import fr.neatmonster.nocheatplus.utilities.ReflectionUtil;

/**
 * IBlockData (nms).
 * 
 * @author asofold
 *
 */
public class ReflectIBlockData {

    public final Class<?> nmsClass;

    public final Method nmsGetMaterial;

    /**
     * 
     * @param base
     * @param reflectMaterial
     * @throws ClassNotFoundException
     * @throws ReflectFailureException
     *             If not available.
     */
    public ReflectIBlockData(ReflectBase base, ReflectMaterial reflectMaterial) throws ClassNotFoundException {
        nmsClass = Class.forName(base.nmsPackageName + ".IBlockData");
        nmsGetMaterial = ReflectionUtil.getMethodNoArgs(nmsClass, "getMaterial", reflectMaterial.nmsClass);
        if (nmsGetMaterial == null) {
            throw new ReflectFailureException();
        }
    }

    /**
     * 
     * @param iBlockData
     *            IBlockData instance.
     * @return
     * @throws ReflectFailureException
     *             On failures.
     */
    public Object nms_getMaterial(final Object iBlockData) {
        try {
            return nmsGetMaterial.invoke(iBlockData);
        } catch (IllegalAccessException e) {
            throw new ReflectFailureException();
        } catch (IllegalArgumentException e) {
            throw new ReflectFailureException();
        } catch (InvocationTargetException e) {
            throw new ReflectFailureException();
        }
    }

}
