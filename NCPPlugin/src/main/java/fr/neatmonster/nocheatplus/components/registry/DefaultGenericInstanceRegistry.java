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
package fr.neatmonster.nocheatplus.components.registry;

import java.util.LinkedList;
import java.util.List;

import fr.neatmonster.nocheatplus.components.registry.event.GenericInstanceHandle.ReferenceCountHandle;
import fr.neatmonster.nocheatplus.components.registry.event.IGenericInstanceHandle;
import fr.neatmonster.nocheatplus.components.registry.event.IGenericInstanceRegistryListener;
import fr.neatmonster.nocheatplus.components.registry.event.IUnregisterGenericInstanceRegistryListener;
import fr.neatmonster.nocheatplus.components.registry.exception.RegistrationLockedException;
import fr.neatmonster.nocheatplus.logging.details.IGetStreamId;
import fr.neatmonster.nocheatplus.logging.details.ILogString;
import fr.neatmonster.nocheatplus.utilities.ds.corw.LinkedHashMapCOW;

public class DefaultGenericInstanceRegistry implements GenericInstanceRegistry, IUnregisterGenericInstanceRegistryListener {

    // TODO: Test cases.

    /**
     * Hold registration information for a class, as well as some convenience
     * functionality.
     * 
     * @author asofold
     *
     */
    public static class Registration<T> {

        private static final long DENY_OVERRIDE_INSTANCE = 0x01;
        private static final long DENY_REMOVE_INSTANCE = 0x02;

        // TODO: unique handles + use

        private final GenericInstanceRegistry registry;
        private final IUnregisterGenericInstanceRegistryListener unregister;
        private final Class<T> registeredFor;
        private T instance = null;
        /** Always kept registered, thus the reference count is ignored. */
        private ReferenceCountHandle<T> uniqueHandle = null;

        private long accessFlags = 0L;

        private final List<IGenericInstanceRegistryListener<T>> listeners = new LinkedList<IGenericInstanceRegistryListener<T>>();

        public Registration(Class<T> registeredFor, T instance,
                GenericInstanceRegistry registry, IUnregisterGenericInstanceRegistryListener unregister) {
            this.registry = registry;
            this.unregister = unregister;
            this.registeredFor = registeredFor;
            this.instance = instance;
        }

        public void denyOverrideInstance() {
            accessFlags |= DENY_OVERRIDE_INSTANCE;
        }

        public void denyRemoveInstance() {
            accessFlags |= DENY_REMOVE_INSTANCE;
        }

        /**
         * Call for unregistering this instance officially. Listeners and
         * handles may be kept,
         * 
         * @return The previously registered instance.
         */
        public T unregisterInstance() {
            if ((accessFlags & DENY_REMOVE_INSTANCE) != 0) {
                throw new RegistrationLockedException();
            }
            T oldInstance = this.instance;
            this.instance = null;
            if (!listeners.isEmpty()) {
                for (IGenericInstanceRegistryListener<T> listener : listeners) {
                    ((IGenericInstanceRegistryListener<T>) listener).onGenericInstanceRemove(registeredFor, oldInstance);
                }
            }
            return oldInstance;
        }

        /**
         * Call on register.
         * 
         * @param instance
         *            The previously registered instance.
         * @return
         */
        public T registerInstance(T instance) {
            if ((accessFlags & DENY_OVERRIDE_INSTANCE) != 0) {
                throw new RegistrationLockedException();
            }
            T oldInstance = this.instance;
            this.instance = instance;
            if (!listeners.isEmpty()) {
                if (oldInstance == null) {
                    for (IGenericInstanceRegistryListener<T> listener : listeners) {
                        listener.onGenericInstanceOverride(registeredFor, instance, oldInstance);
                    }
                }
                else {
                    for (IGenericInstanceRegistryListener<T> listener : listeners) {
                        listener.onGenericInstanceRegister(registeredFor, instance);
                    }
                }
            }
            return oldInstance;
        }

        public IGenericInstanceHandle<T> getHandle() {
            if (uniqueHandle != null && uniqueHandle.isDisabled()) {
                unregisterListener(uniqueHandle);
            }
            if (uniqueHandle == null) {
                uniqueHandle = new ReferenceCountHandle<T>(registeredFor, registry, unregister);
                this.listeners.add(uniqueHandle);
            }
            return uniqueHandle.getNewHandle();
        }

        public void unregisterListener(IGenericInstanceRegistryListener<T> listener) {
            IGenericInstanceHandle<T> disable = null; 
            if (listener == uniqueHandle) {
                disable = uniqueHandle;
                uniqueHandle = null;
            }
            this.listeners.remove(listener);
            if (disable != null) {
                disable.disableHandle();
            }
        }

        public T getInstance() {
            return (T) instance;
        }

        public boolean canBeRemoved() {
            return instance == null && uniqueHandle == null && listeners.isEmpty();
        }

        public void clear() {
            instance = null;
            if (uniqueHandle != null) {
                uniqueHandle.disableHandle();
                uniqueHandle = null;
            }
            listeners.clear();
        }

    }

    /*
     * TODO: Not sure about thread-safety here. Registration might later contain
     * lots of objects and we might like to do some kind of opportunistic
     * skipping of the copying, e.g. if no handles have been fetched yet, OR
     * change implementation on the fly after 'activating' the registry.
     */
    private final LinkedHashMapCOW<Class<?>, Registration<?>> registrations = new LinkedHashMapCOW<Class<?>, Registration<?>>();

    private ILogString logger = null;

    private IGetStreamId selectStream;

    private String logPrefix;

    public void setLogger(ILogString logger, IGetStreamId selectStream, String logPrefix) {
        this.logger = logger;
        this.selectStream = selectStream;
        this.logPrefix = logPrefix;
    }

    @SuppressWarnings("unchecked")
    private <T> Registration<T> getRegistration(Class<T> registeredFor, boolean create) {
        Registration<T> registration = (Registration<T>) registrations.get(registeredFor);
        if (registration == null && create) {
            // Create empty.
            registration = new Registration<T>(registeredFor, null, this, this);
            this.registrations.put(registeredFor, registration);
        }
        return registration;
    }

    @Override
    public <T> void unregisterGenericInstanceRegistryListener(Class<T> registeredFor, IGenericInstanceRegistryListener<T> listener) {
        Registration<T> registration = getRegistration(registeredFor, false);
        if (registration != null) {
            registration.unregisterListener(listener);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T registerGenericInstance(T instance) {
        return registerGenericInstance((Class<T>) instance.getClass(), instance);
    }

    @Override
    public <T, TI extends T> T registerGenericInstance(Class<T> registerFor, TI instance) {
        Registration<T> registration = getRegistration(registerFor, true);
        T registered = registration.registerInstance(instance);
        if (registered != null) {
            logRegistryEvent("Registered (override) for " + registerFor.getName() + ": " + instance.getClass().getName());
        }
        else {
            logRegistryEvent("Registered for " + registerFor.getName() + ": " + instance.getClass().getName());
        }
        return registered;
    }

    @Override
    public <T> T getGenericInstance(Class<T> registeredFor) {
        Registration<T> registration = getRegistration(registeredFor, false);
        return registration == null ? null : registration.getInstance();
    }

    @Override
    public <T> T unregisterGenericInstance(Class<T> registeredFor) {
        Registration<T> registration = getRegistration(registeredFor, false);
        T registered = registration == null ? null : registration.unregisterInstance();
        if (registered != null) {
            logRegistryEvent("Unregister, remove mapping for: " + registeredFor.getName());
        }
        else {
            logRegistryEvent("Unregister, no mapping present for: " + registeredFor.getName());
        }
        // Repeat getting for removal test.
        if (registrations.containsKey(registeredFor) && getRegistration(registeredFor, false).canBeRemoved()) {
            registrations.remove(registeredFor);
        }
        return registered;
    }

    @Override
    public <T> IGenericInstanceHandle<T> getGenericInstanceHandle(Class<T> registeredFor) {
        return getRegistration(registeredFor, true).getHandle();
    }

    public void clear() {
        // TODO: consider fire unregister or add a removal method ?
        for (final Registration<?> registration : registrations.values()) {
            registration.clear();
        }
        registrations.clear();
        logRegistryEvent("Registry cleared.");
    }

    /**
     * Convenience method to lock a registration vs. changing.
     * 
     * @param registeredFor
     */
    public void denyChangeExistingRegistration(Class<?> registeredFor) {
        Registration<?> registration = this.getRegistration(registeredFor, false);
        if (registration != null) {
            registration.denyOverrideInstance();
            registration.denyRemoveInstance();
        }
    }

    protected void logRegistryEvent(String message) {
        if (logger != null) {
            logger.info(selectStream.getStreamId(), logPrefix == null ? message : logPrefix + message);
        }
    }

}
