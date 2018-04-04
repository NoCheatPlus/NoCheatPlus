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
package fr.neatmonster.nocheatplus.worlds;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.bukkit.World;

import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.compat.AlmostBoolean;
import fr.neatmonster.nocheatplus.components.config.value.AlmostBooleanWithOverride;
import fr.neatmonster.nocheatplus.components.config.value.OverrideType;
import fr.neatmonster.nocheatplus.components.data.IDataOnReload;
import fr.neatmonster.nocheatplus.components.data.IDataOnRemoveSubCheckData;
import fr.neatmonster.nocheatplus.components.data.IDataOnWorldUnload;
import fr.neatmonster.nocheatplus.components.data.checktype.CheckNodeWithDebug;
import fr.neatmonster.nocheatplus.components.data.checktype.CheckTypeTree;
import fr.neatmonster.nocheatplus.components.data.checktype.CheckTypeTree.CheckTypeTreeNodeFactory;
import fr.neatmonster.nocheatplus.components.registry.factory.IFactoryOneRegistry;
import fr.neatmonster.nocheatplus.config.ConfigFile;
import fr.neatmonster.nocheatplus.utilities.ds.map.InstanceMapLOW;

/**
 * Data stored per world.
 * 
 * @author asofold
 *
 */
public class WorldData implements IWorldData {

    private static class WorldCheckTypeTreeNode extends CheckNodeWithDebug<WorldCheckTypeTreeNode> implements IWorldCheckNode {

        private static class AccessActive<WorldTypeCheckTreeNode> implements IConfigFlagAccess<WorldCheckTypeTreeNode> {

            @Override
            public AlmostBooleanWithOverride getConfigState(WorldCheckTypeTreeNode node) {
                return node.configActivation;
            }

            @Override
            public boolean getState(WorldCheckTypeTreeNode node) {
                return node.active;
            }

            @Override
            public void setState(WorldCheckTypeTreeNode node, boolean state) {
                node.active = state;
            }

            @Override
            public String getConfigPath(WorldCheckTypeTreeNode node) {
                return node.getCheckType().getConfigPathActive();
            }

            @Override
            public boolean getMissingParentState() {
                return true;
            }

        };

        private static class AccessLag<WorldTypeCheckTreeNode> implements IConfigFlagAccess<WorldCheckTypeTreeNode> {

            @Override
            public AlmostBooleanWithOverride getConfigState(WorldCheckTypeTreeNode node) {
                return node.configLag;
            }

            @Override
            public boolean getState(WorldCheckTypeTreeNode node) {
                return node.lag;
            }

            @Override
            public void setState(WorldCheckTypeTreeNode node, boolean state) {
                node.lag = state;
            }

            @Override
            public String getConfigPath(WorldCheckTypeTreeNode node) {
                return node.getCheckType().getConfigPathLag();
            }

            @Override
            public boolean getMissingParentState() {
                return true;
            }

        };

        @SuppressWarnings("rawtypes")
        private static AccessActive accessActive = new AccessActive();

        @SuppressWarnings("rawtypes")
        private static AccessLag accessLag = new AccessLag();

        /** The configuration value, featuring overriding. */
        private final AlmostBooleanWithOverride configActivation = new AlmostBooleanWithOverride();
        /** The pulled down actual state, may include parent nodes. */
        private boolean active = true;

        /** The configuration value, featuring overriding. */
        private final AlmostBooleanWithOverride configLag = new AlmostBooleanWithOverride();
        /** The pulled down actual state, may include parent nodes. */
        private boolean lag = false;

        WorldCheckTypeTreeNode(CheckType checkType,
                WorldCheckTypeTreeNode parent,
                CheckTypeTreeNodeFactory<WorldCheckTypeTreeNode> factory) {
            super(checkType, parent, factory);
        }

        /**
         * Set recursively.
         * @param configOverrideType
         */
        void setConfigOverrideType(final OverrideType configOverrideType) {
            this.configOverrideType = configOverrideType;
            for (final WorldCheckTypeTreeNode child: getChildren()) {
                child.setConfigOverrideType(configOverrideType);
            }
        }

        /**
         * Non-recursive in-place adjustments, no calls to update. Excludes
         * configOverrideType. Inherit overrides.
         * 
         * @param parentNode
         */
        void adjustToParent(final WorldCheckTypeTreeNode parentNode) {
            configOverrideType = OverrideType.DEFAULT;
            // Activation - replace SPECIFIC by DEFAULT, as this node inherits from a default.
            if (configActivation.setValue(parentNode.configActivation.getValue(), configOverrideType)) {
                active = parentNode.active;
            }
            if (configDebug.setValue(parentNode.configDebug.getValue(), configOverrideType)) {
                debug = parentNode.debug;
            }
            if (configLag.setValue(parentNode.configLag.getValue(), configOverrideType)) {
                lag = parentNode.lag;
            }
        }

        /**
         * General update to a potentially changed configuration.
         * 
         * @param rawConfiguration
         */
        void update(final ConfigFile rawConfiguration) {
            // TODO: A multi update method walking all nodes only once?
            updateActivation(rawConfiguration, true);
            updateDebug(rawConfiguration, true);
            updateLag(rawConfiguration, true);
            // TODO: contained configurations.
        }

        /**
         * Update to activation states.
         */
        void update() {
            // TODO: A multi update method walking all nodes only once?
            updateActivation(true);
            updateDebug(true);
            updateLag(true);
            // TODO: contained configurations.
        }

        @SuppressWarnings("unchecked")
        private void updateLag(final ConfigFile rawConfiguration, 
                final boolean forceUpdateChildren) {
            update(rawConfiguration, forceUpdateChildren, accessLag);
        }

        @SuppressWarnings("unchecked")
        private void updateLag(final boolean forceUpdateChildren) {
            update(forceUpdateChildren, accessLag);
        }

        @SuppressWarnings("unchecked")
        void overrideCheckActivation(
                final AlmostBoolean active, final OverrideType overrideType, 
                final boolean overrideChildren) {
            override(active, 
                    overrideType, overrideChildren, accessActive);
        }

        /**
         * Just update the check activation property.
         * 
         * @param rawConfiguration
         * @param forceUpdateChildren
         *            If set to true, children activation state will be
         *            force-updated recursively. Otherwise, children will only
         *            be updated, if their activation depends on the parent.
         */
        @SuppressWarnings("unchecked")
        void updateActivation(final ConfigFile rawConfiguration, 
                final boolean forceUpdateChildren) {
            update(rawConfiguration, forceUpdateChildren, accessActive);
        }

        /**
         * Just update the activation. 
         * @param forceUpdateChildren
         */
        @SuppressWarnings({"unchecked" })
        void updateActivation( 
                final boolean forceUpdateChildren) {
            update(forceUpdateChildren, accessActive);
        }

        @Override
        public boolean isCheckActive() {
            return active;
        }

        @Override
        public OverrideType getOverrideTypeDebug() {
            return configDebug.getOverrideType();
        }

        @Override
        public boolean shouldAdjustToLag() {
            return lag;
        }

    }

    private static class WorldCheckTypeTree extends CheckTypeTree<WorldCheckTypeTreeNode> {

        private OverrideType configOverrideType = OverrideType.DEFAULT;

        @Override
        protected WorldCheckTypeTreeNode newNode(CheckType checkType,
                WorldCheckTypeTreeNode parent,
                CheckTypeTreeNodeFactory<WorldCheckTypeTreeNode> factory) {
            return new WorldCheckTypeTreeNode(checkType, parent, factory);
        }

        void setConfigOverrideType(OverrideType configOverrideType) {
            if (this.configOverrideType  != configOverrideType) {
                this.configOverrideType = configOverrideType;
                getNode(CheckType.ALL).setConfigOverrideType(configOverrideType);
            }
        }

    }

    ///////////////////
    // Instance.
    ///////////////////

    /** World wide lock ;). */
    private final Lock lock = new ReentrantLock();

    WorldData parent = null;
    private final Collection<WorldData> children = new LinkedHashSet<WorldData>();

    private ConfigFile rawConfiguration = null;

    private final WorldCheckTypeTree checkTypeTree = new WorldCheckTypeTree();

    private final String worldNameLowerCase;
    private WorldIdentifier worldIdentifier = null;

    private IFactoryOneRegistry<WorldFactoryArgument> factoryRegistry;
    private final InstanceMapLOW dataCache = new InstanceMapLOW(lock, 25);

    WorldData(final String worldName, 
            final IFactoryOneRegistry<WorldFactoryArgument> factoryRegistry) {
        this(worldName, null, factoryRegistry);
    }

    WorldData(final String worldName, final WorldData parent, 
            final IFactoryOneRegistry<WorldFactoryArgument> factoryRegistry) {
        // TODO: ILockable ?
        this.parent = parent;
        this.worldNameLowerCase = worldName == null ? null : worldName.toLowerCase(); // Locale.ENGLISH ?
        this.factoryRegistry = factoryRegistry;
        if (parent == null) {
            checkTypeTree.setConfigOverrideType(OverrideType.SPECIFIC);
        }
        else {
            checkTypeTree.setConfigOverrideType(OverrideType.DEFAULT);
            adjustToParent(parent);
        }
    }

    /**
     * Adjust specific overrides and update.
     * 
     * @param parent
     */
    void adjustToParent(final WorldData parent) {
        // This may be called during runtime.
        this.parent = parent;
        this.rawConfiguration = parent.rawConfiguration;
        checkTypeTree.setConfigOverrideType(OverrideType.DEFAULT);
        for (final CheckType checkType : CheckType.values()) {
            checkTypeTree.getNode(checkType).adjustToParent(
                    parent.checkTypeTree.getNode(checkType));
        }
        // Force update (custom overrides might be persistent, just not on object creation).
        checkTypeTree.getNode(CheckType.ALL).update();
        // TODO: What if children exist?
    }

    /**
     * Must be under external lock for now.
     * @param childData
     */
    void addChild(WorldData childData) {
        /*
         * TODO: Locking or not. -> if we never call WorldDataManager.something
         * from in here. extra lock is feasible anyway.
         */
        this.children.add(childData);
    }

    /**
     * Must be under external lock for now.
     * @param childData
     */
    void removeChild(WorldData childData) {
        /*
         * TODO: Locking or not. -> if we never call WorldDataManager.something
         * from in here. extra lock is feasible anyway.
         */
        this.children.remove(childData);
    }

    /**
     * Must be under external lock for now.
     */
    void clearChildren() {
        // Change to specific configuration.
        this.children.clear();
    }

    void update(final ConfigFile rawConfiguration) {
        // TODO: Locking ?
        this.rawConfiguration = rawConfiguration;
        if (this.parent != null && rawConfiguration != this.parent.rawConfiguration) {
            this.parent.removeChild(this);
            this.parent = null;
            this.checkTypeTree.getNode(CheckType.ALL).setConfigOverrideType(OverrideType.SPECIFIC);
        }
        this.update();
        // TODO: Propagate to children?
    }

    void update() {
        // TODO: Locking ?
        // TODO: Distinguish updateByConfig and update().
        checkTypeTree.getNode(CheckType.ALL).update(rawConfiguration);
        // TODO: Propagate to children?
    }

    @Override
    public void overrideCheckActivation(final CheckType checkType, 
            final AlmostBoolean active, final OverrideType overrideType, 
            final boolean overrideChildren) {
        // TODO: Concept for locking.
        checkTypeTree.getNode(checkType).overrideCheckActivation(
                active, overrideType, overrideChildren);
        // TODO: Propagate to children?
    }

    // TODO: overrideDebug?

    @Override
    public ConfigFile getRawConfiguration() {
        return rawConfiguration;
    }

    @Override
    public boolean isCheckActive(final CheckType checkType) {
        return checkTypeTree.getNode(checkType).active;
    }

    @Override
    public boolean isDebugActive(final CheckType checkType) {
        return getCheckNode(checkType).isDebugActive();
    }

    @Override
    public IWorldCheckNode getCheckNode(final CheckType checkType) {
        return checkTypeTree.getNode(checkType);
    }

    @Override
    public <T> T getGenericInstance(final Class<T> registeredFor) {
        T instance = dataCache.get(registeredFor);
        if (instance == null) {
            instance = factoryRegistry.getNewInstance(registeredFor, 
                    new WorldFactoryArgument(this));
            if (instance != null) {
                final T newInstance = dataCache.putIfAbsent(registeredFor, instance);
                return newInstance == null ? instance : newInstance;
            }
        }
        return instance;
    }

    @Override
    public String getWorldNameLowerCase() {
        return worldNameLowerCase;
    }

    public void updateWorldIdentifier(final WorldIdentifier worldIdentifier) {
        if (this.worldNameLowerCase.equals(worldIdentifier.lowerCaseName)) {
            // Prefer to keep the initial instance.
            if (this.worldIdentifier == null || !this.worldIdentifier.equals(worldIdentifier)) {
                this.worldIdentifier = worldIdentifier;
            }
        }
        else {
            throw new IllegalArgumentException("Lower case names must match.");
        }
    }

    public void updateWorldIdentifier(final World world) {
        updateWorldIdentifier(new WorldIdentifier(world.getName(), world.getUID()));
    }

    @Override
    public WorldIdentifier getWorldIdentifier() {
        return worldIdentifier;
    }

    @Override
    public boolean shouldAdjustToLag(CheckType checkType) {
        return getCheckNode(checkType).shouldAdjustToLag();
    }

    /**
     * Remove from cache.
     */
    @Override
    public <T> void removeGenericInstance(final Class<T> type) {
        dataCache.remove(type);
    }

    @Override
    public void removeAllGenericInstances(final Collection<Class<?>> types) {
        if (dataCache.isEmpty()) {
            return;
        }
        dataCache.remove(types);
    }

    @Override
    public void removeSubCheckData(
            final Collection<Class<? extends IDataOnRemoveSubCheckData>> types,
            final Collection<CheckType> checkTypes
            ) {
        final Collection<Class<?>> removeTypes = new LinkedList<Class<?>>();
        for (final Class<? extends IDataOnRemoveSubCheckData> type : types) {
            final IDataOnRemoveSubCheckData impl = dataCache.get(type);
            if (impl != null) {
                if (impl.dataOnRemoveSubCheckData(checkTypes)) {
                    removeTypes.add(type);
                }
            }
        }
        if (!removeTypes.isEmpty()) {
            dataCache.remove(removeTypes);
        }
    }

    public void onWorldUnload(final World world, final Collection<Class<? extends IDataOnWorldUnload>> types) {
        for (final Class<? extends IDataOnWorldUnload> type : types) {
            final IDataOnWorldUnload instance = dataCache.get(type);
            if (instance != null && instance.dataOnWorldUnload(world, this)) {
                dataCache.remove(type);
            }
        }
    }

    public void onReload(final Collection<Class<? extends IDataOnReload>> types) {
        // (Might collect types in a set.)
        for (final Class<? extends IDataOnReload> type : types) {
            final IDataOnReload instance = dataCache.get(type);
            if (instance != null && instance.dataOnReload(this)) {
                dataCache.remove(type);
            }
        }
    }

}
