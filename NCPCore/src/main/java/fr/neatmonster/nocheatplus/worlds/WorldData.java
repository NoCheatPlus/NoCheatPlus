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

import org.bukkit.World;

import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.compat.AlmostBoolean;
import fr.neatmonster.nocheatplus.components.config.value.AlmostBooleanWithOverride;
import fr.neatmonster.nocheatplus.components.config.value.OverrideType;
import fr.neatmonster.nocheatplus.components.data.checktype.CheckNodeWithDebug;
import fr.neatmonster.nocheatplus.components.data.checktype.CheckTypeTree;
import fr.neatmonster.nocheatplus.components.data.checktype.CheckTypeTree.CheckTypeTreeNodeFactory;
import fr.neatmonster.nocheatplus.components.registry.DefaultGenericInstanceRegistry;
import fr.neatmonster.nocheatplus.components.registry.GenericInstanceRegistry;
import fr.neatmonster.nocheatplus.components.registry.event.IGenericInstanceHandle;
import fr.neatmonster.nocheatplus.config.ConfigFile;

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
        }

        /**
         * General update to a potentially changed configuration.
         * 
         * @param rawConfiguration
         */
        void update(final ConfigFile rawConfiguration) {
            // TODO: A multi update method walking all nodes only once.
            updateActivation(rawConfiguration, true);
            updateDebug(rawConfiguration, true);
            updateLag(rawConfiguration, true);
            // TODO: contained configurations.
        }

        @SuppressWarnings("unchecked")
        private void updateLag(final ConfigFile rawConfiguration, 
                final boolean forceUpdateChildren) {
            configFlagUpdate(rawConfiguration, forceUpdateChildren, accessLag);
        }

        @SuppressWarnings("unchecked")
        void overrideCheckActivation(final ConfigFile rawConfiguration, 
                final AlmostBoolean active, final OverrideType overrideType, 
                final boolean overrideChildren) {
            configFlagOverride(rawConfiguration, active, 
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

            configFlagUpdate(rawConfiguration, forceUpdateChildren, accessActive);
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

    //    /** World wide lock ;). */
    //    private final Lock lock = new ReentrantLock();

    WorldData parent = null;
    private final Collection<WorldData> children = new LinkedHashSet<WorldData>();
    private final GenericInstanceRegistry dataRegistry = new DefaultGenericInstanceRegistry();

    private ConfigFile rawConfiguration = null;

    private final WorldCheckTypeTree checkTypeTree = new WorldCheckTypeTree();

    private final String worldNameLowerCase;
    private WorldIdentifier worldIdentifier = null;

    WorldData(String worldName) {
        this(worldName, null);
    }

    WorldData(String worldName, WorldData parent) {
        // TODO: ILockable ?
        this.parent = parent;
        this.worldNameLowerCase = worldName.toLowerCase(); // Locale.ENGLISH ?
        if (parent == null) {
            checkTypeTree.setConfigOverrideType(OverrideType.SPECIFIC);
        }
        else {
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
        // Force update.
        checkTypeTree.getNode(CheckType.ALL).update(rawConfiguration);
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

    @Override
    public ConfigFile getRawConfiguration() {
        return rawConfiguration;
    }

    void update(final ConfigFile rawConfiguration) {
        // TODO: Locking ?
        this.rawConfiguration = rawConfiguration;
        if (this.parent != null && rawConfiguration != this.parent.rawConfiguration) {
            this.parent = null;
            this.parent.removeChild(this);
            this.checkTypeTree.getNode(CheckType.ALL).setConfigOverrideType(OverrideType.SPECIFIC);
        }
        this.update();
    }

    void update() {
        // TODO: Locking ?
        checkTypeTree.getNode(CheckType.ALL).update(rawConfiguration);
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
    public void overrideCheckActivation(final CheckType checkType, 
            final AlmostBoolean active, final OverrideType overrideType, 
            final boolean overrideChildren) {
        // TODO: Concept for locking.
        checkTypeTree.getNode(checkType).overrideCheckActivation(rawConfiguration, active, overrideType, overrideChildren);
    }

    @Override
    public IWorldCheckNode getCheckNode(final CheckType checkType) {
        return checkTypeTree.getNode(checkType);
    }

    @Override
    public <T> T getGenericInstance(final Class<T> registeredFor) {
        // TODO: Factories.
        return dataRegistry.getGenericInstance(registeredFor);
    }

    @Override
    public <T> IGenericInstanceHandle<T> getGenericInstanceHandle(final Class<T> registeredFor) {
        // TODO: Factories.
        return dataRegistry.getGenericInstanceHandle(registeredFor);
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

}
