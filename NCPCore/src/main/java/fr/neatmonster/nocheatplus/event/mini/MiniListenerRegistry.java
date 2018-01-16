package fr.neatmonster.nocheatplus.event.mini;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import fr.neatmonster.nocheatplus.components.registry.order.IGetRegistrationOrder;
import fr.neatmonster.nocheatplus.components.registry.order.RegistrationOrder;
import fr.neatmonster.nocheatplus.components.registry.order.RegistrationOrder.RegisterEventsWithOrder;
import fr.neatmonster.nocheatplus.components.registry.order.RegistrationOrder.RegisterWithOrder;

/**
 * 
 * One registry for MiniListener instances.<br>
 * 
 * <br>
 * <br>
 * Supports registration of MiniListener instances with order (FCFS):
 * <ul>
 * <li>Implement the interface
 * {@link fr.neatmonster.nocheatplus.components.registry.order.IGetRegistrationOrder}</li>
 * <li>Annotation
 * {@link fr.neatmonster.nocheatplus.components.registry.order.RegistrationOrder.RegisterEventsWithOrder}</li>
 * <li>Annotation
 * {@link fr.neatmonster.nocheatplus.components.registry.order.RegistrationOrder.RegisterWithOrder}</li>
 * <li>the given defaultOrder</li>
 * </ul>
 * (Method annotations are not supported for the MiniListener class.) <br>
 * <hr>
 * 
 * @param <EB>
 *            Event base type, e.g. Event (Bukkit).
 * @param <P>
 *            Priority type of the underlying event system, e.g. EventPriority
 *            (Bukkit).
 * 
 * @author asofold
 * 
 */
public abstract class MiniListenerRegistry<EB, P> {

    public static interface NodeFactory<EB, P> {
        public <E extends EB> MiniListenerNode<E, P> newNode(Class<E> eventClass, P basePriority);
    }

    ///////////////
    // Instance.
    ///////////////

    /**
     * Override for efficient stuff.
     */
    protected NodeFactory<EB, P> nodeFactory = new NodeFactory<EB, P>() {
        @Override
        public <E extends EB> MiniListenerNode<E, P> newNode(Class<E> eventClass, P basePriority) {
            return new MiniListenerNode<E, P>(basePriority);
        }
    };

    /**
     * Map event class -> base priority -> node. Note that this does no merging
     * based on super-classes like the Bukkit implementation of the Listener
     * registry would do.
     */
    protected final Map<Class<? extends EB>, Map<P, MiniListenerNode<? extends EB, P>>> classMap = new HashMap<Class<? extends EB>, Map<P, MiniListenerNode<? extends EB, P>>>();

    /**
     * Store attached MiniListener instances by anchor objects.
     */
    protected final Map<Object, Set<MiniListener<? extends EB>>> attachments = new HashMap<Object, Set<MiniListener<? extends EB>>>();

    public void attach(MiniListener<? extends EB>[] listeners, Object anchor) {
        attach(Arrays.asList(listeners), anchor);
    }

    public void attach(Collection<MiniListener<? extends EB>> listeners, Object anchor) {
        for (MiniListener<? extends EB> listener : listeners) {
            attach(listener, anchor);
        }
    }

    /**
     * "Attach" a listener to an object, such that the listener is removed if
     * removeAttachment is called.<br>
     * Note that removing a MiniListener will also remove the attachment.
     * 
     * @param listener
     * @param anchor
     */
    public <E extends EB> void attach(MiniListener<E> listener, Object anchor) {
        if (listener == null) {
            throw new NullPointerException("Must not be null: listener");
        } else if (anchor == null) {
            throw new NullPointerException("Must not be null: anchor");
        } else if (anchor.equals(listener)) {
            throw new IllegalArgumentException("Must not be equal: listener and anchor");
        }
        Set<MiniListener<? extends EB>> attached = attachments.get(anchor);
        if (attached == null) {
            attached = new HashSet<MiniListener<? extends EB>>();
            attachments.put(anchor, attached);
        }
        attached.add(listener);
    }

    /**
     * Convenience method, e.g. for use with Listener registration and plugins
     * to remove all attachments on plugin-disable.
     * 
     * @param registeredAnchor
     * @param otherAnchor
     */
    public void inheritAttached(Object registeredAnchor, Object otherAnchor) {
        // TODO: More signatures (Collection/Array).
        if (registeredAnchor == null) {
            throw new NullPointerException("Must not be null: registeredAnchor");
        } else if (otherAnchor == null) {
            throw new NullPointerException("Must not be null: newAnchor");
        }
        if (registeredAnchor.equals(otherAnchor)) {
            throw new IllegalArgumentException("Must not be equal: registeredAnchor and newAnchor");
        }
        Set<MiniListener<? extends EB>> attached = attachments.get(registeredAnchor);
        if (attached == null) {
            // TODO: throw something or return value or ignore?
        } else {
            Set<MiniListener<? extends EB>> attached2 = attachments.get(otherAnchor);
            if (attached2 == null) {
                attached2 = new HashSet<MiniListener<? extends EB>>();
                attachments.put(otherAnchor, attached2);
            }
            attached2.addAll(attached);
        }
    }

    /**
     * Unregister all attached MiniListener instances for a given anchor.
     * 
     * @param anchor
     */
    public void unregisterAttached(Object anchor) {
        // TODO: Consider more signatures for Collection + Array.
        Set<MiniListener<? extends EB>> attached = attachments.get(anchor);
        if (attached != null) {
            for (MiniListener<? extends EB> listener : new ArrayList<MiniListener<? extends EB>>(attached)) {
                unregister(listener);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public <E extends EB> void unregister(MiniListener<E> listener) {
        // TODO: Consider allowing to pinpoint by priority?
        /*
         * Somewhat inefficient, as all attachments and all priority levels are checked,
         * this might/should be improved by adding extra mappings (consider check class by reflection).
         */
        // Remove listener registrations.
        for (Map<P, MiniListenerNode<? extends EB, P>> prioMap : classMap.values()) {
            for (MiniListenerNode<? extends EB, P> node : prioMap.values()) {
                try {
                    ((MiniListenerNode<E, P>) node).removeMiniListener(listener);
                } catch (ClassCastException e) {
                    // TODO: Log ?
                }
            }
        }
        // Remove attachment references.
        Iterator<Entry<Object, Set<MiniListener<? extends EB>>>> it = attachments.entrySet().iterator();
        while (it.hasNext()) {
            Entry<Object, Set<MiniListener<? extends EB>>> entry = it.next();
            Set<MiniListener<? extends EB>> attached = entry.getValue();
            attached.remove(listener); // TODO: can throw?
            if (attached.isEmpty()) {
                it.remove();
            }
        }
    }

    /**
     * Clear all listeners and attachments. The events stay registered in the
     * underlying event system (TBD if not: unregister).
     */
    public void clear() {
        attachments.clear();
        for (Map<P, MiniListenerNode<? extends EB, P>> prioMap : classMap.values()) {
            for (MiniListenerNode<? extends EB, P> node : prioMap.values()) {
                node.clear();
            }
        }
    }

    /**
     * Register a MiniListener instance for the given event class and base
     * priority. Further ignoreCancelled controls if cancelled events are
     * ignored and it's possible to influence the order of processing for
     * registered listeners.<br>
     * <br>
     * Supports registration of MiniListener instances with order (FCFS):
     * <ul>
     * <li>Implement the interface
     * {@link fr.neatmonster.nocheatplus.components.registry.order.IGetRegistrationOrder}</li>
     * <li>Annotation
     * {@link fr.neatmonster.nocheatplus.components.registry.order.RegistrationOrder.RegisterEventsWithOrder}</li>
     * <li>Annotation
     * {@link fr.neatmonster.nocheatplus.components.registry.order.RegistrationOrder.RegisterWithOrder}</li>
     * <li>the given defaultOrder</li>
     * </ul>
     * (Method annotations are not supported for the MiniListener class.) <br>
     * <hr>
     * 
     * @param eventClass
     * @param listener
     * @param basePriority
     *            Priority for the underlying event system.
     * @param defaultOrder
     *            Default registration order (secondary priority). This comes
     *            last comparing to supported types of order (annotations,
     *            interfaces), assuming the default order to be more general,
     *            e.g. originating from a MultiListenerRegistry.
     * @param ignoreCancelled
     */
    public <E extends EB> void register(Class<E> eventClass, MiniListener<E> listener, 
            P basePriority, RegistrationOrder defaultOrder, boolean ignoreCancelled) {
        // TODO: Can/should the eventClass be read from listener parameters [means constraints on MiniListener?] ?
        RegistrationOrder order = null;
        if (listener instanceof IGetRegistrationOrder) {
            order = ((IGetRegistrationOrder) listener).getRegistrationOrder();
        }
        if (order == null) {
            Class<?> listenerClass = listener.getClass();
            if (listenerClass.isAnnotationPresent(RegisterEventsWithOrder.class)) {
                order = new RegistrationOrder(listenerClass.getAnnotation(RegisterEventsWithOrder.class));
            }
            else if (listenerClass.isAnnotationPresent(RegisterWithOrder.class)) {
                order = new RegistrationOrder(listenerClass.getAnnotation(RegisterWithOrder.class));
            }
            else {
                order = defaultOrder;
            }
        }
        // TODO: Accept RegisterEventsWithOrder (and RegisterWithOrder) with listener.
        // TODO: Accept IRegisterWithOrder with listener.
        Map<P, MiniListenerNode<? extends EB, P>> prioMap = classMap.get(eventClass);
        if (prioMap == null) {
            prioMap = new HashMap<P, MiniListenerNode<? extends EB, P>>();
            classMap.put(eventClass, prioMap);
        }
        // TODO: Concept for when to cast.
        @SuppressWarnings("unchecked")
        MiniListenerNode<E, P> node = (MiniListenerNode<E, P>) prioMap.get(basePriority);
        if (node == null) {
            node = nodeFactory.newNode(eventClass, basePriority);
            // TODO: Consider try-catch.
            registerNode(eventClass, node, basePriority);
            prioMap.put(basePriority, node);
        }
        node.addMiniListener(listener, ignoreCancelled, order);
    }

    /**
     * Register a MiniListenerNode instance with the underlying event-system
     * (unique nodes are ensured in register(...)). <br>
     * Note that the node is put to the internals map after this has been
     * called, to be able to recover from errors.
     * 
     * @param eventClass
     * @param node
     * @param basePriority
     */
    protected abstract <E extends EB> void registerNode(Class<E> eventClass, 
            MiniListenerNode<E, P> node, P basePriority);

}
