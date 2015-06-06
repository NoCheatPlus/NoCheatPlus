package fr.neatmonster.nocheatplus.compat.cbreflect.reflect;

import java.lang.reflect.Method;

import fr.neatmonster.nocheatplus.utilities.ReflectionUtil;

public class ReflectWorld {
    
    public final Method obcGetHandle;
    
    // nms - WorldServer: Used as IBlockAccess as well.

    public ReflectWorld(ReflectBase base) throws ClassNotFoundException {
        Class<?> obcClass = Class.forName(base.obcPackageName + ".CraftWorld");
        obcGetHandle = ReflectionUtil.getMethodNoArgs(obcClass, "getHandle");
    }

}
