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
import java.lang.reflect.Method;

import org.bukkit.entity.Entity;

import fr.neatmonster.nocheatplus.utilities.ReflectionUtil;

/**
 * Reflection for entity.
 * @author asofold
 *
 */
public class ReflectEntityDamage extends ReflectGetHandleBase<Entity> {

    public final Field nmsDead;

    public final Method nmsDamageEntity;

    public final boolean nmsDamageEntityInt;

    public ReflectEntityDamage(ReflectBase base, ReflectDamageSource damageSource) throws ClassNotFoundException {
        this(base, damageSource, Class.forName(base.obcPackageName + ".entity.CraftEntity"), Class.forName(base.nmsPackageName + ".Entity"));
    }

    public ReflectEntityDamage(ReflectBase base, ReflectDamageSource damageSource, Class<?> obcClass, Class<?> nmsClass) throws ClassNotFoundException {
        // base
        super(base, obcClass, nmsClass);

        // dead
        nmsDead = ReflectionUtil.getField(nmsClass, "dead", boolean.class);

        // damageEntity(...)
        nmsDamageEntity = ReflectionUtil.getMethod(nmsClass, "damageEntity", 
                new Class<?>[]{damageSource.nmsClass, float.class}, new Class<?>[]{damageSource.nmsClass, int.class});
        if (nmsDamageEntity != null) {
            nmsDamageEntityInt = nmsDamageEntity.getParameterTypes()[1] == int.class;
        } else {
            nmsDamageEntityInt = true; // Uncertain.
        }
    }

    @Override
    protected void fail() {
        // Unused.
    }

}
