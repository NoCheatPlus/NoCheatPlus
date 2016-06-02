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
