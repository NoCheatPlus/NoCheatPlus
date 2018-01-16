package fr.neatmonster.nocheatplus.event.mini;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;

import fr.neatmonster.nocheatplus.NCPAPIProvider;
import fr.neatmonster.nocheatplus.components.registry.order.RegistrationOrder;
import fr.neatmonster.nocheatplus.components.registry.order.RegistrationOrder.RegisterEventsWithOrder;
import fr.neatmonster.nocheatplus.components.registry.order.RegistrationOrder.RegisterMethodWithOrder;
import fr.neatmonster.nocheatplus.logging.Streams;

/**
 * Support for registering multiple event-handler methods at once.<br>
 * <br>
 * 
 * The MultiListenerRegistry allows passing a defaultOrder, as well as the
 * per-class annotation
 * {@link fr.neatmonster.nocheatplus.components.registry.order.RegistrationOrder.RegisterEventsWithOrder},
 * and the per-method annotation
 * {@link fr.neatmonster.nocheatplus.components.registry.order.RegistrationOrder.RegisterMethodWithOrder}.
 * <br>
 * Priority (FCFS): RegisterMethodWithOrder, RegisterEventsWithOrder,
 * defaultOrder
 * 
 * <br>
 * <br>
 * For alternatives and more details and conventions see:
 * {@link fr.neatmonster.nocheatplus.event.mini.MiniListenerRegistry}<br>
 * 
 * 
 * @author asofold
 * 
 * @param <EB>
 *            Event base class, e.g. Event for Bukkit.
 * @param <P>
 *            Priority class, e.g. EventPriority for Bukkit.
 */
public abstract class MultiListenerRegistry<EB, P> extends MiniListenerRegistry<EB, P> {

    /**
     * 
     * @param method
     * @param basePriority
     * @param defaultOrder
     *            Default order, taken from the Listener, iff the annotation
     *            RegisterEventsWithOrder was present. The
     *            RegisterMethodWithOrder annotation is processed here.
     * @param ignoreCancelled
     * @return
     */
    @SuppressWarnings("unchecked")
    protected <E extends EB> MiniListener<E> register(Object listener, Method method, P basePriority, 
            RegistrationOrder defaultOrder, boolean ignoreCancelled) {
        RegistrationOrder order = null;
        if (method.getClass().isAnnotationPresent(RegisterMethodWithOrder.class)) {
            order = new RegistrationOrder(method.getClass().getAnnotation(RegisterMethodWithOrder.class));
        }
        if (order == null) {
            order = defaultOrder;
        }
        MiniListener<E> miniListener = getMiniListener(listener, method, order);
        if (listener == null) {
            // TODO: Throw rather.
            return null;
        }
        register((Class<E>) method.getParameterTypes()[0], miniListener, 
                basePriority, defaultOrder, ignoreCancelled);
        return miniListener;
    }

    /**
     * Auxiliary method to get a MiniListener instance for a given method.
     * 
     * @param method
     * @return
     */
    protected <E extends EB> MiniListener<E> getMiniListener(final Object listener, final Method method, final RegistrationOrder order) {
        try {
            if (!method.getReturnType().equals(void.class)) {
                return null;
            }
            Class<?>[] parameters = method.getParameterTypes();
            if (parameters.length != 1) {
                return null;
            }
            @SuppressWarnings({ "unchecked", "unused" })
            Class<E> eventClass = (Class<E>) parameters[0];
            if (!method.isAccessible()) {
                method.setAccessible(true);
            }
        } catch (Throwable t) {
            return null;
        }
        MiniListener<E> miniListener = new MiniListenerWithOrder<E>() {
            @Override
            public void onEvent(E event) {
                try {
                    method.invoke(listener, event);
                } catch (IllegalArgumentException e) {
                    throw new RuntimeException(e);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                } catch (InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public RegistrationOrder getRegistrationOrder() {
                /*
                 * Return the given instance of RegistrationOrder, assuming
                 * it'll be copied upon registration. Typically the registry
                 * can't and shouldn't distinguish if this comes from an
                 * external source anyway.
                 */
                return order;
            }
        };
        return miniListener;
    }

    /**
     * Listener registration, checking methods for
     * {@link org.bukkit.event.EventHandler}. Supports passing a defaultOrder,
     * as well as the per-class annotation
     * {@link fr.neatmonster.nocheatplus.components.registry.order.RegistrationOrder.RegisterEventsWithOrder},
     * and the per-method annotation
     * {@link fr.neatmonster.nocheatplus.components.registry.order.RegistrationOrder.RegisterMethodWithOrder}.
     * <br>
     * Priority (FCFS): RegisterMethodWithOrder, RegisterEventsWithOrder,
     * defaultOrder
     * 
     * @param listener
     *            All internally created MiniListener instances will be attached
     *            to the listener by default.
     * @return Collection of MiniListener instances for attaching.
     */
    protected Collection<MiniListener<? extends EB>> register(Object listener, 
            P defaultPriority, RegistrationOrder defaultOrder, boolean defaultIgnoreCancelled) {
        Collection<MiniListener<? extends EB>> listeners = new ArrayList<MiniListener<? extends EB>>();
        Class<?> listenerClass = listener.getClass();
        RegistrationOrder order = null;
        if (listenerClass.isAnnotationPresent(RegisterEventsWithOrder.class)) {
            order = new RegistrationOrder(listenerClass.getAnnotation(RegisterEventsWithOrder.class));
        }
        if (order == null) {
            order = defaultOrder;
        }
        int shouldBe = 0;
        for (Method method : listenerClass.getMethods()) {
            if (shouldBeEventHandler(method)) {
                shouldBe ++;
                MiniListener<? extends EB> miniListener = register(listener, method, 
                        getPriority(method, defaultPriority), order, 
                        getIgnoreCancelled(method, defaultIgnoreCancelled));
                if (miniListener == null) {
                    NCPAPIProvider.getNoCheatPlusAPI().getLogManager().severe(Streams.STATUS, 
                            "Could not register event listener: " + listener.getClass().getName()
                            + "#" + method.getName());
                } else {
                    listeners.add(miniListener);
                }
            }
        }
        if (shouldBe > listeners.size()) {
            // TODO: Unregister and throw ? Should perhaps depend on configuration.
        }
        if (!listeners.isEmpty()) {
            attach(listeners, listener);
        }
        return listeners;
    }

    /**
     * Solely meant for check for annotations (not return type etc., use
     * getMiniListener for that).
     * 
     * @param method
     * @return
     */
    protected abstract boolean shouldBeEventHandler(Method method);

    /**
     * This is meant to process platform specific annotations.
     * 
     * @param method
     * @return
     */
    protected abstract boolean getIgnoreCancelled(Method method, boolean defaultIgnoreCancelled);

    /**
     * This is meant to process platform specific annotations.
     * 
     * @param method
     * @return
     */
    protected abstract P getPriority(Method method, P defaultPriority);

}
