package fr.neatmonster.nocheatplus.components;

/**
 * A registry for unique instances of any class type.<br>
 * Currently there is no specification for what happens with registering for an
 * already registered class, neither if exceptions are thrown, nor if
 * dependencies will use those then.
 * 
 * @author dev1mc
 *
 */
public interface GenericInstanceRegistry {
	
	/**
	 * Register the instance by its own class.
	 * @param instance
	 */
	public <T> T registerGenericInstance(T instance);
	
	/**
	 * Register an instance under for a super-class. 
	 * @todo The registry implementation might specify if overriding is allowed.
	 * @param registerAs
	 * @param instance
	 * @return The previously registered instance. If none was registered, null is returned.
	 */
	public <T, TI extends T> T registerGenericInstance(Class<T> registerFor, TI instance);
	
	/**
	 * Retrieve the instance registered for the given class.
	 * @param registeredBy
	 * @return The instance, or null, if none is registered.
	 */
	public <T> T getGenericInstance(Class<T> registeredFor);
	
	/**
	 * Remove a registration. The registry implementation might specify id removing is allowed.
	 * @param registeredFor
	 * @return The previously registered instance. If none was registered, null is returned.
	 */
	public <T> T unregisterGenericInstance(Class<T> registeredFor);
	
}
