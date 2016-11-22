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

public class ReflectLivingEntity extends ReflectEntity {

    public final Method nmsGetHealth;

    public ReflectLivingEntity(ReflectBase base, ReflectAxisAlignedBB reflectAxisAlignedBB, ReflectDamageSource damageSource) throws ClassNotFoundException {
        this(base, reflectAxisAlignedBB, damageSource, Class.forName(base.obcPackageName + ".entity.CraftLivingEntity"), Class.forName(base.nmsPackageName + ".EntityLiving"));
    }

    public ReflectLivingEntity(ReflectBase base, ReflectAxisAlignedBB reflectAxisAlignedBB, ReflectDamageSource damageSource, Class<?> obcClass, Class<?> nmsClass) throws ClassNotFoundException {
        super(base, reflectAxisAlignedBB, damageSource, obcClass, nmsClass);
        this.nmsGetHealth = ReflectionUtil.getMethodNoArgs(nmsClass, "getHealth"); 
    }

}
