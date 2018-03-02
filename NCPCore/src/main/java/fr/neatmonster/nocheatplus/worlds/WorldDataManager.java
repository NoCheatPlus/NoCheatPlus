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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.world.WorldUnloadEvent;

import fr.neatmonster.nocheatplus.NCPAPIProvider;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.compat.AlmostBoolean;
import fr.neatmonster.nocheatplus.components.NoCheatPlusAPI;
import fr.neatmonster.nocheatplus.components.config.ICheckConfig;
import fr.neatmonster.nocheatplus.components.config.IConfig;
import fr.neatmonster.nocheatplus.components.config.value.OverrideType;
import fr.neatmonster.nocheatplus.components.data.ICheckData;
import fr.neatmonster.nocheatplus.components.data.IData;
import fr.neatmonster.nocheatplus.components.data.IDataOnReload;
import fr.neatmonster.nocheatplus.components.data.IDataOnWorldUnload;
import fr.neatmonster.nocheatplus.components.registry.factory.IFactoryOne;
import fr.neatmonster.nocheatplus.components.registry.factory.RichFactoryRegistry;
import fr.neatmonster.nocheatplus.components.registry.factory.RichFactoryRegistry.CheckRemovalSpec;
import fr.neatmonster.nocheatplus.components.registry.feature.INotifyReload;
import fr.neatmonster.nocheatplus.components.registry.order.RegistrationOrder.RegisterMethodWithOrder;
import fr.neatmonster.nocheatplus.config.ConfigFile;
import fr.neatmonster.nocheatplus.config.DefaultConfig;
import fr.neatmonster.nocheatplus.event.mini.MiniListener;
import fr.neatmonster.nocheatplus.utilities.ds.map.HashMapLOW;

public class WorldDataManager implements IWorldDataManager, INotifyReload {

    static class IWorldDataEntry implements Entry<String, IWorldData> {

        private final Entry<String, WorldData> entry;

        IWorldDataEntry(Entry<String, WorldData> entry) {
            this.entry = entry;
        }

        @Override
        public String getKey() {
            return entry.getKey();
        }

        @Override
        public IWorldData getValue() {
            return entry.getValue();
        }

        @Override
        public IWorldData setValue(IWorldData value) {
            throw new UnsupportedOperationException();
        }

    }

    static class IWorldDataIterator implements Iterator<Entry<String, IWorldData>> {

        private final Iterator<Entry<String, WorldData>> iterator;

        IWorldDataIterator(Iterator<Entry<String, WorldData>> iterator) {
            this.iterator = iterator;
        }

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public Entry<String, IWorldData> next() {
            return new IWorldDataEntry(iterator.next());
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    // TODO: debug flags like activation?
    // TODO: Factory registration.

    /** Global access lock for this registry. Also used for ConfigFile editing. */
    private final Lock lock = new ReentrantLock();

    /** Lower case name to WorldData map. */
    // TODO: ID-name pairs / mappings?
    private final HashMapLOW<String, WorldData> worldDataMap = new HashMapLOW<String, WorldData>(lock, 10);
    private Map<String, ConfigFile> rawConfigurations = new HashMap<String, ConfigFile>(); // COW
    private final RichFactoryRegistry<WorldFactoryArgument> factoryRegistry = new RichFactoryRegistry<WorldFactoryArgument>(lock);

    private final MiniListener<?>[] miniListeners = new MiniListener<?>[] {
        /*
         * TODO: Constants in a class 'ListenerTags', plus a plan
         * (system.data.player.nocheatplus, system.nocheatplus.data ??,
         * nocheatplus.system.data.player...). (RegistryTags for other?).
         */
        new MiniListener<WorldUnloadEvent>() {
            @EventHandler(priority = EventPriority.LOWEST)
            @RegisterMethodWithOrder(tag = "system.nocheatplus.worlddatamanager", beforeTag = "(system\\.nocheatplus\\.playerdatamanager|feature.*)")
            @Override
            public void onEvent(final WorldUnloadEvent event) {
                onWorldUnload(event);
            }
        },
    };


    public WorldDataManager() {
        // TODO: ILockable
        // TODO: Create a default node with some basic settings.
        createDefaultWorldData();
        // (Call support.) 
        factoryRegistry.createAutoGroup(IDataOnReload.class);
        factoryRegistry.createAutoGroup(IDataOnWorldUnload.class);
        // Data/config removal.
        factoryRegistry.createAutoGroup(IData.class);
        factoryRegistry.createAutoGroup(IConfig.class);
        factoryRegistry.createAutoGroup(ICheckData.class);
        factoryRegistry.createAutoGroup(ICheckConfig.class);
    }

    @Override
    public IWorldData getDefaultWorldData() {
        return internalGetDefaultWorldData();
    }

    @Override
    public IWorldData getWorldData(final String worldName) {
        return internalGetWorldData(worldName.toLowerCase());
    }

    @Override
    public IWorldData getWorldData(final World world) {
        final WorldData worldData = internalGetWorldData(world.getName().toLowerCase());
        if (worldData.getWorldIdentifier() == null) {
            worldData.updateWorldIdentifier(world);
        }
        return worldData;
    }

    /**
     * Internal get or create for the lower case world name.
     * 
     * @param lowerCaseWorldName
     *            In case of the default WorldData, null is used internally.
     * @return
     */
    private WorldData internalGetWorldData(final String lowerCaseWorldName) {
        final WorldData worldData = worldDataMap.get(lowerCaseWorldName);
        if (worldData != null) {
            return worldData;
        }
        else {
            return createWorldData(lowerCaseWorldName);
        }
    }

    private WorldData internalGetDefaultWorldData() {
        // TODO: Store the instance extra to the map.
        return worldDataMap.get(null);
    }

    private void createDefaultWorldData() {
        lock.lock();
        if (worldDataMap.containsKey(null)) {
            lock.unlock();
            return;
        }
        ConfigFile config = rawConfigurations.get(null);
        if (config == null) {
            config = new DefaultConfig();
        }
        worldDataMap.put(null, new WorldData(null, this));
        updateWorldData(null, config);
        lock.unlock();
    }

    /**
     * Create if not present (check under lock).
     * @param worldName
     * @return
     */
    private WorldData createWorldData(final String worldName) {
        return updateWorldData(worldName, getDefaultWorldData().getRawConfiguration());
    }

    @Override
    public Iterator<Entry<String, IWorldData>> getWorldDataIterator() {
        return new IWorldDataIterator(worldDataMap.iterator());
    }

    @Override
    public Iterable<Entry<String, IWorldData>> getWorldDataIterable() {
        final Iterator<Entry<String, IWorldData>> iterator = getWorldDataIterator();
        return new Iterable<Map.Entry<String, IWorldData>>() {
            @Override
            public Iterator<Entry<String, IWorldData>> iterator() {
                return iterator;
            }
        };
    }

    /**
     * The given ConfigFile instances are stored within WorldData.
     * 
     * @param rawWorldConfigs
     */
    public void applyConfiguration(final Map<String, ConfigFile> rawWorldConfigs) {
        // TODO: ILockable
        /*
         * Minimal locking is used, to prevent deadlocks, in case WorldData
         * instances will hold individual locks.
         */
        lock.lock();
        final Map<String, ConfigFile> rawConfigurations = new LinkedHashMap<String, ConfigFile>(rawWorldConfigs.size());
        for (final Entry<String, ConfigFile> entry : rawWorldConfigs.entrySet()) {
            final String worldName = entry.getKey();
            rawConfigurations.put(worldName == null ? null : worldName.toLowerCase(), entry.getValue());
        }
        this.rawConfigurations = rawConfigurations;
        final ConfigFile defaultConfig = this.rawConfigurations.get(null);
        final WorldData defaultWorldData = internalGetDefaultWorldData(); // Always the same instance.
        defaultWorldData.update(defaultConfig);
        lock.unlock(); // From here on, new instances have a proper config set.
        // Update all given
        for (final Entry<String, ConfigFile> entry : rawConfigurations.entrySet()) {
            final String worldName = entry.getKey();
            if (worldName != null) {
                /*
                 * Children adding and removal for defaultWorldData is handled
                 * in updateWorldData.
                 */
                updateWorldData(worldName, entry.getValue());
            }
        }
        // Update all that are not contained and don't point to the default configuration.
        // TODO: Consider deleting world nodes, unless the world is actually loaded.
        for (final Entry<String, WorldData> entry : worldDataMap.iterable()) {
            final String worldName = entry.getKey();
            if (worldName != null 
                    && !rawConfigurations.containsKey(worldName)) {
                final WorldData ref = entry.getValue();
                lock.lock();
                defaultWorldData.addChild(ref); // Redundant calls are ok.
                ref.adjustToParent(defaultWorldData); // Inherit specific overrides and more.
                lock.unlock();
            }
        }
    }

    /**
     * Remove world data that is inherited from default, but for which no world
     * is loaded. Note that calling this may conflict with external plugins
     * preparing settings for just not yet loaded worlds. Intended to be called
     * with rare tasks rather.
     * <hr/>
     * Primary thread only.
     * <hr/>
     */
    public void removeOfflineInheritedWorldData() {
        final Set<String> worldNames = new HashSet<String>();
        for (World world : Bukkit.getWorlds()) {
            worldNames.add(world.getName().toLowerCase());
        }
        final List<String> remove = new LinkedList<String>();
        for (final Entry<String, WorldData> entry : worldDataMap.iterable()) {
            final String lcName = entry.getKey();
            if (!rawConfigurations.containsKey(lcName) && ! worldNames.contains(lcName)) {
                remove.add(lcName);
            }
        }
        worldDataMap.remove(remove); // Same lock is used (where necessary).
    }

    @Override
    public void updateAllWorldData() {
        // TODO: ILockable / move to an access object ?
        lock.lock();
        final WorldData defaultWorldData = internalGetDefaultWorldData();
        defaultWorldData.update();
        for (final Entry<String, WorldData> entry : worldDataMap.iterable()) {
            final WorldData ref = entry.getValue();
            if (ref != defaultWorldData) {
                ref.update();
            }
        }
        lock.unlock();
    }

    /**
     * Create if not existent (under lock). In any case simply call
     * WorldData.update (under lock). Updates children and parent relation with
     * the default WorldData instance.
     * 
     * @param worldName
     * @param rawConfiguration
     * @return
     */
    private WorldData updateWorldData(final String worldName, 
            final ConfigFile rawConfiguration) {
        final WorldData defaultWorldData = internalGetDefaultWorldData();
        lock.lock(); // TODO: Might lock outside (pro/con).
        final String lcName = worldName == null ? null : worldName.toLowerCase();
        WorldData data = worldDataMap.get(lcName);
        boolean skipUpdate = false;
        if (data == null) {
            data = new WorldData(worldName, defaultWorldData, factoryRegistry);
            worldDataMap.put(data.getWorldNameLowerCase(), data);
            if (rawConfiguration == defaultWorldData.getRawConfiguration()) {
                defaultWorldData.addChild(data);
                skipUpdate = true;
            }
        }
        if (!skipUpdate) {
            data.update(rawConfiguration); // Parent/child state is updated  here.
        }
        lock.unlock();
        return data;
    }

    @Override
    public void overrideCheckActivation(final CheckType checkType, final AlmostBoolean active, 
            final OverrideType overrideType, final boolean overrideChildren) {
        lock.lock();
        final IWorldData defaultWorldData = getDefaultWorldData();
        defaultWorldData.overrideCheckActivation(checkType, active, overrideType, overrideChildren);
        // Override flags.
        // TODO: If possible, skip derived from default, since default data is done first.
        for (final Entry<String, WorldData> entry : worldDataMap.iterable()) {
            final IWorldData worldData = entry.getValue();
            if (worldData != defaultWorldData) {
                worldData.overrideCheckActivation(checkType, active, overrideType, overrideChildren);
            }
        }
        lock.unlock();
    }

    @Override
    public boolean isActiveAnywhere(final CheckType checkType) {
        for (final Entry<String, WorldData> entry : worldDataMap.iterable()) {
            if (entry.getValue().isCheckActive(checkType)) {
                return true;
            }
        }
        return false;
    }

    public void updateWorldIdentifier(World world) {
        internalGetWorldData(world.getName().toLowerCase()).updateWorldIdentifier(world);
    }

    @Override
    public IWorldData getWorldDataSafe(Player player) {
        // (Hope the JIT specializes for odd impl.)
        try {
            return getWorldData(player.getWorld());
        }
        catch (UnsupportedOperationException e) {
            // Proxy/Fake/Packet.
            return getDefaultWorldData();
        }
    }

    @Override
    public <T> void registerFactory(Class<T> registerFor,
            IFactoryOne<WorldFactoryArgument, T> factory) {
        factoryRegistry.registerFactory(registerFor, factory);
    }

    @Override
    public <G> void createAutoGroup(Class<G> groupType) {
        factoryRegistry.createAutoGroup(groupType);
    }

    @Override
    public <T> Collection<Class<? extends T>> getGroupedTypes(
            Class<T> groupType) {
        return factoryRegistry.getGroupedTypes(groupType);
    }

    @Override
    public <T> Collection<Class<? extends T>> getGroupedTypes(
            Class<T> groupType, CheckType checkType) {
        return factoryRegistry.getGroupedTypes(groupType, checkType);
    }

    @Override
    public <I> void addToGroups(Class<I> itemType,
            Class<? super I>... groupTypes) {
        factoryRegistry.addToGroups(itemType, groupTypes);
    }

    @Override
    public void addToExistingGroups(Class<?> itemType) {
        factoryRegistry.addToExistingGroups(itemType);
    }

    @Override
    public <I> void addToGroups(CheckType checkType, Class<I> itemType,
            Class<? super I>... groupTypes) {
        factoryRegistry.addToGroups(checkType, itemType, groupTypes);
    }

    @Override
    public <I> void addToExistingGroups(CheckType checkType,
            Class<I> itemType) {
        factoryRegistry.addToExistingGroups(checkType, itemType);
    }

    @Override
    public <I> void addToGroups(Collection<CheckType> checkTypes,
            Class<I> itemType, Class<? super I>... groupTypes) {
        factoryRegistry.addToGroups(checkTypes, itemType, groupTypes);
    }

    @Override
    public <I> void addToExistingGroups(Collection<CheckType> checkTypes,
            Class<I> itemType) {
        factoryRegistry.addToExistingGroups(checkTypes, itemType);
    }

    @Override
    public <G> void createGroup(Class<G> groupType) {
        factoryRegistry.createGroup(groupType);
    }

    @Override
    public <T> T getNewInstance(Class<T> registeredFor,
            WorldFactoryArgument arg) {
        return factoryRegistry.getNewInstance(registeredFor, arg);
    }

    /**
     * Initializing with online players.
     */
    public void onEnable() {
        final NoCheatPlusAPI api = NCPAPIProvider.getNoCheatPlusAPI();
        for (final MiniListener<?> listener : miniListeners) {
            api.addComponent(listener, false);
        }
        api.addComponent(this);
    }

    private void onWorldUnload(WorldUnloadEvent event) {
        final Collection<Class<? extends IDataOnWorldUnload>> types = factoryRegistry.getGroupedTypes(IDataOnWorldUnload.class);
        final World world = event.getWorld();
        final WorldData worldData = worldDataMap.get(world.getName().toLowerCase());
        worldData.onWorldUnload(event.getWorld(), types);
        // TODO: Minimize, clear cache / remove ?
    }

    @Override
    public void onReload() {
        final Collection<Class<? extends IDataOnReload>> types = factoryRegistry.getGroupedTypes(IDataOnReload.class);
        for (final Entry<String, WorldData> entry : worldDataMap.iterable()) {
            entry.getValue().onReload(types);
        }
    }

    @Override
    public void clearData(CheckType checkType) {
        final CheckRemovalSpec removalSpec = new CheckRemovalSpec(checkType, true, this);
        final boolean hasComplete = !removalSpec.completeRemoval.isEmpty();
        final boolean hasSub = !removalSpec.subCheckRemoval.isEmpty();
        if (hasComplete || hasSub) {
            for (final Entry<String, WorldData> entry : worldDataMap.iterable()) {
                final WorldData worldData = entry.getValue();
                if (hasComplete) {
                    worldData.removeAllGenericInstances(removalSpec.completeRemoval);
                }
                if (hasSub) {
                    worldData.removeSubCheckData(removalSpec.subCheckRemoval, removalSpec.checkTypes);
                }
            }
        }
    }

    @Override
    public void removeCachedConfigs() {
        final Collection<Class<?>> types = new LinkedHashSet<Class<?>>(
                factoryRegistry.getGroupedTypes(IConfig.class));
        if (!types.isEmpty()) {
            for (final Entry<String, WorldData> entry : worldDataMap.iterable()) {
                entry.getValue().removeAllGenericInstances(types);
            }
        }
    }

}
