package fr.neatmonster.nocheatplus.compat.cbreflect.reflect;

import java.lang.reflect.Constructor;

import fr.neatmonster.nocheatplus.utilities.ReflectionUtil;

public class ReflectBlockPosition {
    
    public final Class<?> nmsClass;

    public final Constructor<?> new_nmsBlockPosition;

    public ReflectBlockPosition(ReflectBase base) throws ClassNotFoundException {
        nmsClass = Class.forName(base.nmsPackageName + ".BlockPosition");
        new_nmsBlockPosition = ReflectionUtil.getConstructor(nmsClass, int.class, int.class, int.class);
    }

}
