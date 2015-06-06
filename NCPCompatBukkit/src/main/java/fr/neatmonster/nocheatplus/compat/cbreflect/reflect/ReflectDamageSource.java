package fr.neatmonster.nocheatplus.compat.cbreflect.reflect;

import java.lang.reflect.Field;

import fr.neatmonster.nocheatplus.utilities.ReflectionUtil;

public class ReflectDamageSource {
    
    public final Class<?> nmsClass;

    public final Object nmsFALL;

    public ReflectDamageSource(ReflectBase base) throws ClassNotFoundException {
        Class<?> nmsClass = Class.forName(base.nmsPackageName + ".DamageSource");
        this.nmsClass = nmsClass;
        Field field = ReflectionUtil.getField(nmsClass, "FALL", nmsClass);
        nmsFALL = field == null ? null : ReflectionUtil.get(field, nmsClass, null);
    }

}
