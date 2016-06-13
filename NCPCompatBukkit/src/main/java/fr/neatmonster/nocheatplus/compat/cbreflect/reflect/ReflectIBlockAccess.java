package fr.neatmonster.nocheatplus.compat.cbreflect.reflect;

public class ReflectIBlockAccess {

    public final Class<?> nmsClass;

    public ReflectIBlockAccess(ReflectBase base) throws ClassNotFoundException {
        nmsClass = Class.forName(base.nmsPackageName + ".IBlockAccess");
    }

}
