package fr.neatmonster.nocheatplus.compat.cbreflect.reflect;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import fr.neatmonster.nocheatplus.utilities.ReflectionUtil;

/**
 * Reflection for entity.
 * @author asofold
 *
 */
public class ReflectEntity {

    public final Field nmsDead;

    public final Method obcGetHandle;
    
    public final Method nmsDamageEntity;
    
    public final boolean nmsDamageEntityInt;

    public ReflectEntity(ReflectBase base, ReflectDamageSource damageSource) throws ClassNotFoundException {
        this(base, damageSource, Class.forName(base.obcPackageName + ".entity.CraftEntity"), Class.forName(base.nmsPackageName + ".Entity"));

    }

    public ReflectEntity(ReflectBase base, ReflectDamageSource damageSource, Class<?> obcClass, Class<?> nmsClass) throws ClassNotFoundException {
        // getHandle
        obcGetHandle = ReflectionUtil.getMethodNoArgs(obcClass, "getHandle");
        // TODO: Consider throw in case of getHandle missing.
        
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

}
