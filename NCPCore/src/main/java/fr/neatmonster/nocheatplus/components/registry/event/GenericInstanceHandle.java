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
package fr.neatmonster.nocheatplus.components.registry.event;

import fr.neatmonster.nocheatplus.components.registry.GenericInstanceRegistry;

/**
 * Default implementation for retrieving a IGenericInstanceHandle from a
 * registry.
 * 
 * @author asofold
 *
 * @param <T>
 */
public class GenericInstanceHandle<T> implements IGenericInstanceRegistryListener<T>, IGenericInstanceHandle<T> {

    // TODO: <? extends T> ?

    /**
     * Delegates getHandle, disables the parent only once (meant for reference
     * counting).
     * 
     * @author asofold
     *
     * @param <T>
     */
    public static class ParentDelegateHandle<T> implements IGenericInstanceHandle<T> {

        private final IGenericInstanceHandle<T> parent;
        private boolean disabled = false;

        public ParentDelegateHandle(Class<T> registeredFor, GenericInstanceRegistry registry,
                IGenericInstanceHandle<T> parent) {
            this.parent = parent;
        }

        @Override
        public T getHandle() {
            if (disabled) {
                throw new RuntimeException("Already disabled.");
            }
            else {
                return parent.getHandle();
            }
        }

        @Override
        public void disableHandle() {
            if (!disabled) {
                disabled = true;
                parent.disableHandle();
            }
        }

    }

    /**
     * Allow fetching PrarentDelegate instances, increasing reference count with
     * each returned one. Really unregister only with reaching a count of zero
     * on disableHandle. This way only one instance needs to be updated. This
     * doesn't self-register as listener.
     * 
     * @author asofold
     *
     * @param <T>
     */
    public static class ReferenceCountHandle<T> extends GenericInstanceHandle<T> {

        private int references = 0;

        public ReferenceCountHandle(Class<T> registeredFor, GenericInstanceRegistry registry,
                IUnregisterGenericInstanceRegistryListener unregister) {
            super(registeredFor, registry, unregister);
        }

        @Override
        public void disableHandle() {
            if (references > 0) {
                references --;
            }
            // Only really unregister once.
            if (references == 0) {
                super.disableHandle();
            }
        }

        /**
         * Retrieve a new instance referencing this one.
         * @return
         * @throws RuntimeException If already disabled.
         */
        public IGenericInstanceHandle<T> getNewHandle() {
            if (isDisabled()) {
                throw new RuntimeException("Already disabled.");
            }
            references ++;
            return new ParentDelegateHandle<T>(getRegisteredFor(), getRegistry(), this);
        }

        public int getNumberOfReferences() {
            return references;
        }

    }

    private GenericInstanceRegistry registry;
    private IUnregisterGenericInstanceRegistryListener unregister;
    private Class<T> registeredFor;
    private T handle = null;
    private boolean initialized = false;
    private boolean disabled = false;

    /**
     * Note that this doesn't register with the registry, as the registry may
     * return unique handles on request rather.
     * 
     * @param registeredFor
     * @param registry
     * @param unregister
     */
    public GenericInstanceHandle(Class<T> registeredFor, GenericInstanceRegistry registry, IUnregisterGenericInstanceRegistryListener unregister) {
        this.registry = registry;
        this.unregister = unregister;
        this.registeredFor = registeredFor;
    }

    private T fetchHandle() {
        return registry.getGenericInstance(registeredFor);
    }

    @Override
    public void onGenericInstanceRegister(Class<T> registerFor, T instance) {
        this.handle = instance;
        initialized = true;
    }

    @Override
    public void onGenericInstanceOverride(Class<T> registerFor, T newInstance, T oldInstance) {
        this.handle = newInstance;
        initialized = true;
    }

    @Override
    public void onGenericInstanceRemove(Class<T> registerFor, T oldInstance) {
        this.handle = null;
        initialized = true;
    }

    @Override
    public T getHandle() {
        if (initialized) {
            return handle;
        }
        else if (disabled) {
            throw new RuntimeException("Already disabled.");
        }
        else {
            return fetchHandle();
        }
    }

    @Override
    public void disableHandle() {
        if (unregister != null) {
            disabled = true;
            initialized = false;
            handle = null;
            registeredFor = null;
            registry = null;
            if (unregister != null) {
                unregister.unregisterGenericInstanceRegistryListener(registeredFor, this);
            }
            unregister = null;
        }
    }

    public Class<T> getRegisteredFor() {
        return registeredFor;
    }

    public GenericInstanceRegistry getRegistry() {
        return registry;
    }

    public IUnregisterGenericInstanceRegistryListener getUnregister() {
        return unregister;
    }

    public boolean isDisabled() {
        return disabled;
    }

}
