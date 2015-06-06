package fr.neatmonster.nocheatplus.compat.cbreflect.reflect;

import java.lang.reflect.Field;

import fr.neatmonster.nocheatplus.utilities.ReflectionUtil;

public class ReflectGenericAttributes {

    public final Object nmsMOVEMENT_SPEED;

    public ReflectGenericAttributes(ReflectBase base) throws ClassNotFoundException {
        Class<?> clazz = Class.forName(base.nmsPackageName + ".GenericAttributes");
        Field field = ReflectionUtil.getField(clazz, "MOVEMENT_SPEED", null);
        if (field != null) {
            nmsMOVEMENT_SPEED = ReflectionUtil.get(field, clazz, null);
        }
        else {
            nmsMOVEMENT_SPEED = null;
        }
    }

}
