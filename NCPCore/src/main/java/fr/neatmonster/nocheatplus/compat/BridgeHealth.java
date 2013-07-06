package fr.neatmonster.nocheatplus.compat;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityRegainHealthEvent;

import fr.neatmonster.nocheatplus.logging.LogUtil;
import fr.neatmonster.nocheatplus.utilities.ReflectionUtil;

/**
 * Utility class with static access methods to bridge compatibility issues, such as arising from changes in Bukkit from MC 1.5.2 to 1.6.1.
 * @author mc_dev
 *
 */
public class BridgeHealth {
	
	/** For debugging purposes. TODO: Reset on shutdown !? */
	private static Set<String> failures = new HashSet<String>(); 
	
	/**
	 * This method is meant to be called on API that changed from int to double.<br>
	 * NOTE: Might get changed to return a Number instance.
	 * @param obj Object to call the method on.
	 * @param methodName
	 * @param RuntimeException with reason Will be thrown if no recovery from present methods is possible. If not null the first call gets logged as "API incompatibility".
	 * @return
	 */
	public static final double getDoubleOrInt(final Object obj, final String methodName, final Throwable reason){
		if (reason != null){
			final String tag = obj.getClass().getName() + "." + methodName;
			if (failures.add(tag)){
				// New entry.
				LogUtil.logWarning("[NoCheatPlus] API incompatibility detected: " + tag);
			}
		}
		final Object o1 = ReflectionUtil.invokeMethodNoArgs(obj, methodName, double.class, int.class);
		if (o1 instanceof Number){
			return ((Number) o1).doubleValue();
		}
		else{
			String message = "Expect method " + methodName + " in " + obj.getClass() + " with return type double or int.";
			if (reason == null){
				throw new RuntimeException(message);
			}
			else{
				throw new RuntimeException(message, reason);
			}
		}
	}
	
	/**
	 * Get the amount of health added with the event.
	 * @param event
	 * @return
	 * @throws RuntimeException, in case of an AbstractMethodError without success on recovery attempts.
	 */
	public static double getAmount(final EntityRegainHealthEvent event){
		try{
			return event.getAmount();
		}
		catch(AbstractMethodError e){
			return getDoubleOrInt(event, "getAmount", e);
		}
	}
	
	/**
	 * Get the health for an entity (LivingEntity).
	 * @param entity
	 * @return
	 * @throws RuntimeException, in case of an AbstractMethodError without success on recovery attempts.
	 */
	public static double getHealth(final LivingEntity entity){
		try{
			return entity.getHealth();
		}
		catch(AbstractMethodError e){
			return getDoubleOrInt(entity, "getHealth", e);
		}
	}
	
	/**
	 * Get the maximum health for an entity (LivingEntity).
	 * @param entity
	 * @return
	 * @throws RuntimeException, in case of an AbstractMethodError without success on recovery attempts.
	 */
	public static double getMaxHealth(final LivingEntity entity){
		try{
			return entity.getMaxHealth();
		}
		catch(AbstractMethodError e){
			return getDoubleOrInt(entity, "getMaxHealth", e);
		}
	}
	
}
