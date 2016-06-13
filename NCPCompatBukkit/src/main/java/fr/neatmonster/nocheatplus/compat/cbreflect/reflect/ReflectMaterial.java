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

import java.lang.reflect.Method;

import fr.neatmonster.nocheatplus.utilities.ReflectionUtil;

public class ReflectMaterial {

    public final Class<?> nmsClass;

    public final Method nmsIsLiquid;
    public final Method nmsIsSolid;

    public ReflectMaterial(ReflectBase base) throws ClassNotFoundException {
        nmsClass = Class.forName(base.nmsPackageName + ".Material");
        nmsIsLiquid = ReflectionUtil.getMethodNoArgs(nmsClass, "isLiquid", boolean.class);
        nmsIsSolid = ReflectionUtil.getMethodNoArgs(nmsClass, "isSolid", boolean.class);
    }

}
