package fr.neatmonster.nocheatplus.compat.cbreflect.reflect;

import java.lang.reflect.Method;

import fr.neatmonster.nocheatplus.utilities.ReflectionUtil;

public class ReflectMaterial {
    
    public final Method nmsIsLiquid;
    public final Method nmsIsSolid;

    public ReflectMaterial(ReflectBase base) throws ClassNotFoundException {
        Class<?> nmsClass = Class.forName(base.nmsPackageName + ".Material");
        nmsIsLiquid = ReflectionUtil.getMethodNoArgs(nmsClass, "isLiquid", boolean.class);
        nmsIsSolid = ReflectionUtil.getMethodNoArgs(nmsClass, "isSolid", boolean.class);
    }

}
