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
package fr.neatmonster.nocheatplus.compat.cbreflect.reflect;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import fr.neatmonster.nocheatplus.compat.cbreflect.reflect.ReflectHelper.ReflectFailureException;
import fr.neatmonster.nocheatplus.utilities.ReflectionUtil;

public class ReflectWorld {

    /** World (nms) */
    public final Class<?> nmsClass;

    public final Method obcGetHandle;

    public final Method nmsGetType;

    // nms - WorldServer: Used as IBlockAccess as well.

    public ReflectWorld(ReflectBase base, ReflectMaterial reflectMaterial, 
            ReflectBlockPosition reflectBlockPosition) throws ClassNotFoundException {
        Class<?> obcClass = Class.forName(base.obcPackageName + ".CraftWorld");
        obcGetHandle = ReflectionUtil.getMethodNoArgs(obcClass, "getHandle");

        // IBlockData getType(BlockPosition): fail-safe.
        ReflectIBlockData reflectIBlockData = null;
        Class<?> nmsClass = null;
        try {
            reflectIBlockData = new ReflectIBlockData(base, reflectMaterial);
            nmsClass = Class.forName(base.nmsPackageName + ".World");
        }
        catch (Throwable t) {};
        this.nmsClass = nmsClass;
        if (reflectIBlockData == null || nmsClass == null) {
            nmsGetType = null;
        }
        else {
            Method method = ReflectionUtil.getMethod(nmsClass, "getType", reflectBlockPosition.nmsClass);
            if (method.getReturnType() == reflectIBlockData.nmsClass) {
                this.nmsGetType = method;
            }
            else {
                this.nmsGetType = null;
            }
        }
    }

    /**
     * 
     * @param nmsWorld
     * @param blockPosition
     * @return
     * @throws ReflectFailureException
     */
    public Object nms_getType(final Object nmsWorld, final Object blockPosition) {
        if (nmsGetType == null) {
            throw new ReflectFailureException();
        }
        try {
            return nmsGetType.invoke(nmsWorld, blockPosition);
        } catch (IllegalAccessException e) {
            throw new ReflectFailureException();
        } catch (IllegalArgumentException e) {
            throw new ReflectFailureException();
        } catch (InvocationTargetException e) {
            throw new ReflectFailureException();
        }
    }

}
