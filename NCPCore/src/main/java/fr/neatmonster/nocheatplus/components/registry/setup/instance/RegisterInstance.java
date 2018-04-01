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
package fr.neatmonster.nocheatplus.components.registry.setup.instance;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import fr.neatmonster.nocheatplus.NCPAPIProvider;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.components.config.ICheckConfig;
import fr.neatmonster.nocheatplus.components.config.IConfig;
import fr.neatmonster.nocheatplus.components.data.ICheckData;
import fr.neatmonster.nocheatplus.components.data.IData;
import fr.neatmonster.nocheatplus.components.data.IDataOnRemoveSubCheckData;
import fr.neatmonster.nocheatplus.components.registry.factory.IFactoryOne;
import fr.neatmonster.nocheatplus.components.registry.factory.IRichFactoryRegistry;
import fr.neatmonster.nocheatplus.components.registry.setup.IDoRegister;
import fr.neatmonster.nocheatplus.components.registry.setup.RegistrationContext;
import fr.neatmonster.nocheatplus.players.IPlayerDataManager;
import fr.neatmonster.nocheatplus.utilities.CheckTypeUtil;

/**
 * Base class.
 * 
 * @author asofold
 *
 * @param <T>
 *            Instance type.
 * @param <A>
 *            Argument type for IFactoryOne.
 */
public abstract class RegisterInstance<T, A> implements IDoRegister {

    /**
     * Allow registering with multiple registries.
     * 
     * @author asofold
     *
     */
    protected static interface IDoRegisterWithRegistry {
        void doRegister(IRichFactoryRegistry<?> factoryRegistry);
    }

    ////////////////////
    // Instance
    ////////////////////

    protected final RegistrationContext registrationContext;
    protected final IRichFactoryRegistry<A> factoryRegistry;
    protected final Class<T> type;
    protected IFactoryOne<A, T> factory = null;
    protected boolean registerConfigTypesPlayer = false;
    protected boolean registerDataTypesPlayer = false;
    /**
     * Types that may be registered as types for players as well, for the case
     * this is a per world type (with or without factory).
     */
    protected final List<IDoRegisterWithRegistry> genericDataItems = new LinkedList<IDoRegisterWithRegistry>();
    /**
     * Types that may be registered as types for players as well, for the case
     * this is a per world type (with or without factory).
     */
    protected final List<IDoRegisterWithRegistry> genericConfigItems = new LinkedList<IDoRegisterWithRegistry>();

    /** Standard items. */
    protected final List<IDoRegister> items = new LinkedList<IDoRegister>();

    public RegisterInstance(RegistrationContext registrationContext, Class<T> type, 
            IRichFactoryRegistry<A> factoryRegistry) {
        this.registrationContext = registrationContext;
        this.factoryRegistry = factoryRegistry;
        this.type = type;
    }


    //////////////////////////////
    // Abstract
    //////////////////////////////


    //////////////////////////////
    // Setter (chaining).
    //////////////////////////////

    /**
     * 
     * @param factory
     * @return This instance for chaining.
     */
    public RegisterInstance<T,A> factory(IFactoryOne<A, T> factory) {
        this.factory = factory;
        return this;
    }

    /**
     * This does not check for data/config types, to apply with
     * registreXYTypesAB.
     * 
     * @param checkType
     * @param withDescendantCheckTypes
     * @param groupTypes
     * @return
     */
    public RegisterInstance<T,A> addToGroups(final CheckType checkType, 
            final boolean withDescendantCheckTypes, 
            final Class<? super T>... groupTypes) {
        items.add(new IDoRegister() {
            @Override
            public void doRegister() {
                if (withDescendantCheckTypes) {
                    factoryRegistry.addToGroups(
                            CheckTypeUtil.getWithDescendants(checkType), 
                            type, groupTypes);
                }
                else {
                    factoryRegistry.addToGroups(checkType, type, groupTypes);
                }
            }
        });
        return this;
    }

    /**
     * Register standard config types with the player registry as well (aiming
     * at per world configuration types).
     * <hr>
     * <ul>
     * <li>IConfig</li>
     * <li>ICheckConfig</li>
     * </ul>
     * 
     * @return
     */
    public RegisterInstance<T,A> registerConfigTypesPlayer() {
        registerConfigTypesPlayer = true;
        return this;
    }

    /**
     * Register standard config types with the player registry as well (aiming
     * at per world configuration types).
     * <hr>
     * <ul>
     * <li>IConfig</li>
     * <li>ICheckConfig</li>
     * </ul>
     * 
     * @param checkType
     * @param withDescendantCheckTypes
     * @return
     */
    public RegisterInstance<T,A> registerConfigTypesPlayer(
            final CheckType checkType, 
            final boolean withDescendantCheckTypes) {
        registerConfigTypesPlayer();
        final Collection<CheckType> checkTypes = withDescendantCheckTypes ? CheckTypeUtil.getWithDescendants(checkType) 
                : Arrays.asList(checkType);
        if (IConfig.class.isAssignableFrom(type)) {
            genericConfigItems.add(new IDoRegisterWithRegistry() {
                @SuppressWarnings("unchecked")
                @Override
                public void doRegister(IRichFactoryRegistry<?> factoryRegistry) {
                    factoryRegistry.addToGroups(checkTypes, 
                            (Class<? extends IConfig>) type, 
                            IConfig.class);
                }
            });
        }
        if (ICheckConfig.class.isAssignableFrom(type)) {
            genericConfigItems.add(new IDoRegisterWithRegistry() {
                @SuppressWarnings("unchecked")
                @Override
                public void doRegister(IRichFactoryRegistry<?> factoryRegistry) {
                    factoryRegistry.addToGroups(checkTypes, 
                            (Class<? extends ICheckConfig>) type, 
                            ICheckConfig.class);
                }
            });
        }
        return this;
    }

    /**
     * Register standard data types with the player registry as well (aiming at
     * per world data).
     * <hr>
     * <ul>
     * <li>IData</li>
     * <li>ICheckData</li>
     * </ul>
     * 
     * @return
     */
    public RegisterInstance<T,A> registerDataTypesPlayer() {
        registerDataTypesPlayer = true;
        return this;
    }

    /**
     * Register standard data types with the player registry as well (aiming at
     * per world data).
     * <hr>
     * <ul>
     * <li>IData</li>
     * <li>ICheckData</li>
     * </ul>
     * 
     * @param checkType
     * @param withDescendantCheckTypes
     * @return
     */
    public RegisterInstance<T,A> registerDataTypesPlayer(
            final CheckType checkType, 
            final boolean withDescendantCheckTypes) {
        registerDataTypesPlayer();
        final Collection<CheckType> checkTypes = withDescendantCheckTypes ? CheckTypeUtil.getWithDescendants(checkType) 
                : Arrays.asList(checkType);
        if (IData.class.isAssignableFrom(type)) {
            genericConfigItems.add(new IDoRegisterWithRegistry() {
                @SuppressWarnings("unchecked")
                @Override
                public void doRegister(IRichFactoryRegistry<?> factoryRegistry) {
                    factoryRegistry.addToGroups(checkTypes, 
                            (Class<? extends IData>) type, 
                            IData.class);
                }
            });
        }
        if (ICheckData.class.isAssignableFrom(type)) {
            genericConfigItems.add(new IDoRegisterWithRegistry() {
                @SuppressWarnings("unchecked")
                @Override
                public void doRegister(IRichFactoryRegistry<?> factoryRegistry) {
                    factoryRegistry.addToGroups(checkTypes, 
                            (Class<? extends ICheckData>) type, 
                            ICheckData.class);
                }
            });
        }
        return this;
    }

    /**
     * Register for sub check removal.
     * 
     * @param checkType
     * @param withDescendantCheckTypes
     * @return
     */
    public RegisterInstance<T,A> removeSubCheckData(
            final CheckType checkType, 
            final boolean withDescendantCheckTypes) {
        if (!IDataOnRemoveSubCheckData.class.isAssignableFrom(type)) {
            throw new UnsupportedOperationException();
        }
        final Collection<CheckType> checkTypes = withDescendantCheckTypes ? CheckTypeUtil.getWithDescendants(checkType) 
                : Arrays.asList(checkType);
        items.add(new IDoRegister() {
            @SuppressWarnings("unchecked")
            @Override
            public void doRegister() {
                factoryRegistry.addToGroups(checkTypes, 
                        (Class<? extends IDataOnRemoveSubCheckData>) type, 
                        IDataOnRemoveSubCheckData.class);
            }
        });
        return this;
    }


    //////////////////////////////
    // Other functionality.
    //////////////////////////////

    public RegistrationContext registrationContext() {
        return registrationContext;
    }

    public RegistrationContext context() {
        return registrationContext();
    }

    @Override
    public void doRegister() {
        if (factory != null) {
            factoryRegistry.registerFactory(type, factory);
        }
        for (final IDoRegister item : items) {
            item.doRegister();
        }
        for (final IDoRegisterWithRegistry item : genericDataItems) {
            item.doRegister(factoryRegistry);
        }
        final IPlayerDataManager pdMan = NCPAPIProvider.getNoCheatPlusAPI().getPlayerDataManager();
        if (registerConfigTypesPlayer) {
            registerConfigTypesPlayer(pdMan);
        }
        if (registerDataTypesPlayer) {
            registerDataTypesPlayer(pdMan);
        }
    }

    @SuppressWarnings("unchecked")
    protected void registerConfigTypesPlayer(final IPlayerDataManager pdMan) {
        if (IConfig.class.isAssignableFrom(type)) {
            pdMan.addToGroups((Class<? extends IConfig>) type, IConfig.class);
        }
        if (ICheckConfig.class.isAssignableFrom(type)) {
            pdMan.addToGroups((Class<? extends ICheckConfig>) type, ICheckConfig.class);
        }
        for (final IDoRegisterWithRegistry item : genericConfigItems) {
            item.doRegister(pdMan);
        }
    }

    @SuppressWarnings("unchecked")
    protected void registerDataTypesPlayer(final IPlayerDataManager pdMan) {
        if (IData.class.isAssignableFrom(type)) {
            pdMan.addToGroups((Class<? extends IData>) type, IData.class);
        }
        if (ICheckData.class.isAssignableFrom(type)) {
            pdMan.addToGroups((Class<? extends ICheckData>) type, ICheckData.class);
        }
        for (final IDoRegisterWithRegistry item : genericDataItems) {
            item.doRegister(pdMan);
        }
    }

}
