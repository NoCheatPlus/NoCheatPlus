/*
 * This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package fr.neatmonster.nocheatplus.event.mini;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;

import fr.neatmonster.nocheatplus.NCPAPIProvider;
import fr.neatmonster.nocheatplus.components.registry.feature.ComponentWithName;
import fr.neatmonster.nocheatplus.components.registry.order.RegistrationOrder;
import fr.neatmonster.nocheatplus.components.registry.order.RegistrationOrder.RegisterEventsWithOrder;
import fr.neatmonster.nocheatplus.components.registry.order.RegistrationOrder.RegisterMethodWithOrder;
import fr.neatmonster.nocheatplus.logging.StaticLog;
import fr.neatmonster.nocheatplus.logging.Streams;
import fr.neatmonster.nocheatplus.utilities.StringUtil;

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

    protected class AutoListener<E> implements MiniListenerWithOrder<E>, ComponentWithName {

        private final Class<E> eventClass;
        private final Object listener;
        private final Method method;
        private final RegistrationOrder order;
        private final P basePriority;

        private AutoListener(final Class<E> eventClass, 
                final Object listener, final Method method, 
                final RegistrationOrder order, final P basePriority) {
            this.eventClass = eventClass;
            this.listener = listener;
            this.method = method;
            this.order = order;
            this.basePriority = basePriority;
        }

        @Override
        public void onEvent(final E event) {
            try {
                method.invoke(listener, event);
            }
            catch (InvocationTargetException e) {
                onException(event, e);
            }
            catch (IllegalArgumentException e) {
                onException(event, e);
            }
            catch (IllegalAccessException e) {
                onException(event, e);
            }
        }

        private void onException(final E event, final Throwable t) {
            final StringBuilder builder = new StringBuilder(1024);
            builder.append("Exception:\n");
            builder.append(StringUtil.throwableToString(t));
            Throwable cause = t.getCause();
            while (cause != null) {
                builder.append("caused by:\n");
                builder.append(StringUtil.throwableToString(t.getCause()));
                cause = cause.getCause();
            }
            StaticLog.logOnce(Level.SEVERE, 
                    "Exception with " + getComponentName() + ", processing " + event.getClass().getName() + ": " + t.getClass().getSimpleName(), 
                    builder.toString());
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

        @Override
        public String getComponentName() {
            return "AutoListener(" + listener.getClass().getName() +"." + method.getName() + "/" + eventClass.getName() + "/" + basePriority + ")";
        }
    }

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
        MiniListener<E> miniListener = getMiniListener(listener, method, order, basePriority);
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
    @SuppressWarnings("unchecked")
    protected <E extends EB> MiniListener<E> getMiniListener(final Object listener, 
            final Method method, final RegistrationOrder order, final P basePriority) {
        return new AutoListener<E>((Class<E>) method.getParameterTypes()[0], 
                listener, method, order, basePriority);
    }

    protected boolean check_and_prepare_method(final Method method) {
        try {
            if (!method.getReturnType().equals(void.class)) {
                return false;
            }
            Class<?>[] parameters = method.getParameterTypes();
            if (parameters.length != 1) {
                return false;
            }
            if (!Modifier.isPublic(method.getModifiers())) {
                // TODO: Specific log.
                return false;
            }
            if (!method.isAccessible()) {
                // TODO: Can this be minimized?
                method.setAccessible(true);
            }
            return true;
        } catch (Throwable t) {
            return false;
        }
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
        /*
         * TODO: Collect checked methods first. If methods fail checking, could
         * prevent register any. Since complex hooks/plugins may register
         * multiple listeners, it's probably better to register what works, but
         * pass success/failure state to the caller, thinking ahead of a
         * registry context (policy: unregister all if any listener method fails
         * to register).
         */
        for (Method method : listenerClass.getMethods()) {
            if (shouldBeEventHandler(method)) {
                MiniListener<? extends EB> miniListener = null;
                if (check_and_prepare_method(method)) {
                    miniListener = register(listener, method, 
                            getPriority(method, defaultPriority), order, 
                            getIgnoreCancelled(method, defaultIgnoreCancelled));
                }
                if (miniListener == null) {
                    // TODO: ReflectionUtil.toStringSpecialCase(Method) -> With type parameters (simple).
                    NCPAPIProvider.getNoCheatPlusAPI().getLogManager().severe(Streams.STATUS, 
                            "Could not register event listener: " + listener.getClass().getName()
                            + "#" + method.getName());
                } else {
                    listeners.add(miniListener);
                }
            }
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
