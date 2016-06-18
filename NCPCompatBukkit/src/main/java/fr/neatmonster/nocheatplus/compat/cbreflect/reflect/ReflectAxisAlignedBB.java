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

import java.lang.reflect.Field;

import fr.neatmonster.nocheatplus.compat.cbreflect.reflect.ReflectHelper.ReflectFailureException;
import fr.neatmonster.nocheatplus.utilities.ReflectionUtil;
import fr.neatmonster.nocheatplus.utilities.Validate;

/**
 * AxisAlignedBB (nms).
 * 
 * @author asofold
 *
 */
public class ReflectAxisAlignedBB {

    public final Class<?> nmsClass;


    public final Field nms_minX;
    public final Field nms_minY;
    public final Field nms_minZ;
    public final Field nms_maxX;
    public final Field nms_maxY;
    public final Field nms_maxZ;

    /**
     * @param base
     * @throws ClassNotFoundException
     * @throws NullPointerException
     *             if not available.
     */
    public ReflectAxisAlignedBB(ReflectBase base) throws ClassNotFoundException {
        nmsClass = Class.forName(base.nmsPackageName + ".AxisAlignedBB");
        nms_minX = ReflectionUtil.getField(nmsClass, "a", double.class);
        nms_minY = ReflectionUtil.getField(nmsClass, "b", double.class);
        nms_minZ = ReflectionUtil.getField(nmsClass, "c", double.class);
        nms_maxX = ReflectionUtil.getField(nmsClass, "d", double.class);
        nms_maxY = ReflectionUtil.getField(nmsClass, "e", double.class);
        nms_maxZ = ReflectionUtil.getField(nmsClass, "f", double.class);
        Validate.validateNotNull(nms_minX, nms_minY, nms_minZ, nms_maxX, nms_maxY, nms_maxZ);
    }

    /**
     * Fill in all six values into the given array (order: minY, minY, minZ,
     * maxX, maxY, maxZ).
     * 
     * @param aabb
     * @param arr
     * @return The given array.
     */
    public double[] fillInValues(final Object aabb, final double[] arr) {
        try {
            arr[0] = nms_minX.getDouble(aabb);
            arr[1] = nms_minY.getDouble(aabb);
            arr[2] = nms_minZ.getDouble(aabb);
            arr[3] = nms_maxX.getDouble(aabb);
            arr[4] = nms_maxY.getDouble(aabb);
            arr[5] = nms_maxZ.getDouble(aabb);
            return arr;
        }
        catch (IllegalArgumentException e) {
            throw new ReflectFailureException();
        }
        catch (IllegalAccessException e) {
            throw new ReflectFailureException();
        }
    }

}
