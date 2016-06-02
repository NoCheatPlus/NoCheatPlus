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

public class ReflectAttributeModifier {

    /** (Custom naming.) */
    public Method nmsGetOperation;
    /** (Custom naming.) */
    public Method nmsGetValue;
    
    public ReflectAttributeModifier(ReflectBase base) throws ClassNotFoundException {
        Class<?> clazz = Class.forName(base.nmsPackageName + ".AttributeModifier");
     // TODO: Scan in a more future proof way.
        nmsGetOperation = ReflectionUtil.getMethodNoArgs(clazz, "c", int.class);
        nmsGetValue = ReflectionUtil.getMethodNoArgs(clazz, "d", double.class);
    }

}
