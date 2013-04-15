package fr.neatmonster.nocheatplus.components;

import java.util.Collection;

/**
 * This component queues other components to automatically register "later", i.e. with an unspecified but finite delay, depending on specification coming with the implementation.
 * The convention is to add this as a component "officially", even if only the sub-components are used.
 * @author mc_dev
 *
 */
public interface IHoldSubComponents {
	
	/**
	 * This is to be called after the specified delay after registering the implementation in a registry.
	 * It is recommended to not return the same elements again on a second call, for the case of delegating to further registries supporting this interface.
	 * @return Always a collection, may be empty, should be empty on the second call.
	 */
	public Collection<Object> getSubComponents();
}
