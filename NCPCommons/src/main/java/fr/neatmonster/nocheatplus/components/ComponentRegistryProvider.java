package fr.neatmonster.nocheatplus.components;

import java.util.Collection;

/**
 * An object allowing to get ComponentRegistry implementations of a specific type.
 * This class is not specialized to maintain flexibility.
 * @author mc_dev
 *
 */
public interface ComponentRegistryProvider{
	
	/**
	 * Get all available specialized ComponentFactory instances matching the given signature. This is not meant as a factory method but for more efficient registration for the case of the regestry being present. 
	 * @param clazz
	 * @return Some collection, empty collection in case no matches are found.
	 */
	public <T> Collection<ComponentRegistry<T>> getComponentRegistries(Class<ComponentRegistry<T>> clazz);
	
}
