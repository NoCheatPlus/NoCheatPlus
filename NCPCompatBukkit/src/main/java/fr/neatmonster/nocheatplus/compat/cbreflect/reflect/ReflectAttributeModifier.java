package fr.neatmonster.nocheatplus.compat.cbreflect.reflect;

import java.lang.reflect.Method;

import fr.neatmonster.nocheatplus.utilities.ReflectionUtil;

public class ReflectAttributeModifier {

    /** (Custom naming.) */
    public Method nmsGetOperation;
    /** (Custom naming.) */
    public Method nmsGetValue;
    
    public ReflectAttributeModifier(ReflectBase base) throws ClassNotFoundException {
        Class<?> clazz = Class.forName(base.nmsPackageName + ".AttributeModifier");
     // TODO: Scan in a more future proof way.
        nmsGetOperation = ReflectionUtil.getMethodNoArgs(clazz, "c", int.class);
        nmsGetValue = ReflectionUtil.getMethodNoArgs(clazz, "d", double.class);
    }

}
