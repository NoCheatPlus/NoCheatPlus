package fr.neatmonster.nocheatplus.event.mini;

import fr.neatmonster.nocheatplus.components.registry.order.IGetRegistrationOrder;

/**
 * Convenience interface to have RegistrationOrder bundled this way.
 * 
 * @author asofold
 *
 * @param <E>
 */
public interface MiniListenerWithOrder<E> extends MiniListener<E>, IGetRegistrationOrder {

}
