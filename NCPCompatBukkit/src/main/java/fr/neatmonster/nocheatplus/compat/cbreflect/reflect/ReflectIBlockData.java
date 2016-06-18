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
