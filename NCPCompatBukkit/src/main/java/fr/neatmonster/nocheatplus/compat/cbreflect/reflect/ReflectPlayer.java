package fr.neatmonster.nocheatplus.compat.cbreflect.reflect;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import fr.neatmonster.nocheatplus.utilities.ReflectionUtil;

public class ReflectPlayer extends ReflectLivingEntity {

    // Not sure: Living/Human entity.
    public final Field nmsDeathTicks;
    public final Field nmsInvulnerableTicks;

    public final Method nmsGetAttributeInstance; // TODO: LivingEntity

    public ReflectPlayer(ReflectBase base, ReflectDamageSource damageSource) throws ClassNotFoundException {
        this(base, damageSource, Class.forName(base.obcPackageName + ".entity.CraftPlayer"), Class.forName(base.nmsPackageName + ".EntityPlayer"));
    }

    public ReflectPlayer(ReflectBase base, ReflectDamageSource damageSource, Class<?> obcClass, Class<?> nmsClass) throws ClassNotFoundException {
        super(base, damageSource, obcClass, nmsClass);
        // TODO: invulnerable etc.
        // deathTicks
        nmsDeathTicks = ReflectionUtil.getField(nmsClass, "deathTicks", int.class);
        nmsInvulnerableTicks = ReflectionUtil.getField(nmsClass, "invulnerableTicks", int.class);

        Method method;
        try {
            Class<?> clazzIAttribute = Class.forName(base.nmsPackageName + ".IAttribute");
            method = ReflectionUtil.getMethod(nmsClass, "getAttributeInstance", clazzIAttribute); 
        } catch (ClassNotFoundException e) {
            method = null;
        }
        nmsGetAttributeInstance = method;
    }

}
