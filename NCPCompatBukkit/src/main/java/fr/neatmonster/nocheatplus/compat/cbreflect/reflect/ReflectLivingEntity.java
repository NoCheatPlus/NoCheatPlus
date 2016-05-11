package fr.neatmonster.nocheatplus.compat.cbreflect.reflect;

import java.lang.reflect.Method;

import fr.neatmonster.nocheatplus.utilities.ReflectionUtil;

public class ReflectLivingEntity extends ReflectEntityDamage {

    public final Method nmsGetHealth;

    public ReflectLivingEntity(ReflectBase base, ReflectDamageSource damageSource) throws ClassNotFoundException {
        this(base, damageSource, Class.forName(base.obcPackageName + ".entity.CraftLivingEntity"), Class.forName(base.nmsPackageName + ".EntityLiving"));
    }

    public ReflectLivingEntity(ReflectBase base, ReflectDamageSource damageSource, Class<?> obcClass, Class<?> nmsClass) throws ClassNotFoundException {
        super(base, damageSource, obcClass, nmsClass);
        this.nmsGetHealth = ReflectionUtil.getMethodNoArgs(nmsClass, "getHealth"); 
    }

}
