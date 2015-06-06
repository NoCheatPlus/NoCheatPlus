package fr.neatmonster.nocheatplus.compat.cbreflect.reflect;

import java.lang.reflect.Method;
import java.util.UUID;

import fr.neatmonster.nocheatplus.utilities.ReflectionUtil;

public class ReflectAttributeInstance {

    /** (Custom naming.) */
    public final Method nmsGetBaseValue;
    public final Method nmsGetValue;
    
    /** (Custom naming.) */
    public final Method nmsGetAttributeModifier;

    public ReflectAttributeInstance(ReflectBase base) throws ClassNotFoundException {
        Class<?> clazz = Class.forName(base.nmsPackageName + ".AttributeInstance");
        // Base value.
        Method method = ReflectionUtil.getMethodNoArgs(clazz, "b", double.class);
        if (method == null) {
            // TODO: Consider to search (as long as only two exist).
            method = ReflectionUtil.getMethodNoArgs(clazz, "getBaseValue", double.class);
            if (method == null) {
                method = ReflectionUtil.getMethodNoArgs(clazz, "getBase", double.class);
            }
        }
        nmsGetBaseValue = method;
        // Value (final value).
        method = ReflectionUtil.getMethodNoArgs(clazz, "getValue", double.class);
        if (method == null) {
            // TODO: Consider to search (as long as only two exist).
            method = ReflectionUtil.getMethodNoArgs(clazz, "e", double.class); // 1.6.1
        }
        nmsGetValue = method;
        // Get AttributeModifier.
        // TODO: If name changes: scan.
        method = ReflectionUtil.getMethod(clazz, "a", UUID.class);
        if (method == null) {
            method = ReflectionUtil.getMethod(clazz, "getAttributeModifier", UUID.class);
            if (method == null) {
                method = ReflectionUtil.getMethod(clazz, "getModifier", UUID.class);
            }
        }
        nmsGetAttributeModifier = method;
    }

}
