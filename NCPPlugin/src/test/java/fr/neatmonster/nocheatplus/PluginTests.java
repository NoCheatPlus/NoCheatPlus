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
package fr.neatmonster.nocheatplus;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import fr.neatmonster.nocheatplus.compat.MCAccess;
import fr.neatmonster.nocheatplus.compat.blocks.changetracker.BlockChangeTracker;
import fr.neatmonster.nocheatplus.compat.bukkit.MCAccessBukkit;
import fr.neatmonster.nocheatplus.components.NoCheatPlusAPI;
import fr.neatmonster.nocheatplus.components.registry.ComponentRegistry;
import fr.neatmonster.nocheatplus.components.registry.DefaultGenericInstanceRegistry;
import fr.neatmonster.nocheatplus.components.registry.event.IGenericInstanceHandle;
import fr.neatmonster.nocheatplus.event.mini.EventRegistryBukkit;
import fr.neatmonster.nocheatplus.logging.LogManager;
import fr.neatmonster.nocheatplus.logging.StaticLog;
import fr.neatmonster.nocheatplus.permissions.PermissionRegistry;
import fr.neatmonster.nocheatplus.permissions.Permissions;
import fr.neatmonster.nocheatplus.permissions.RegisteredPermission;

public class PluginTests {

    /**
     * Dummy API, providing only a minimal subset of functionality for
     * offline-testing. Some methods do nothing, some throw an
     * UnsupportedOperationException, some will do something (set/get MCAccess).
     * 
     * @author asofold
     *
     */
    public static class UnitTestNoCheatPlusAPI implements NoCheatPlusAPI {

        private final DefaultGenericInstanceRegistry genericInstanceRegistry = new DefaultGenericInstanceRegistry();
        private final PermissionRegistry permissionRegistry = new PermissionRegistry(10000);

        public UnitTestNoCheatPlusAPI() {
            genericInstanceRegistry.registerGenericInstance(MCAccess.class, new MCAccessBukkit());
            for (RegisteredPermission rp : Permissions.getPermissions()) {
                permissionRegistry.addRegisteredPermission(rp);
            }
        }

        @Override
        public boolean addComponent(Object component) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void removeComponent(Object component) {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T> Collection<ComponentRegistry<T>> getComponentRegistries(Class<ComponentRegistry<T>> clazz) {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T> T registerGenericInstance(T instance) {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T, TI extends T> T registerGenericInstance(Class<T> registerFor, TI instance) {
            return genericInstanceRegistry.registerGenericInstance(registerFor, instance);
        }

        @Override
        public <T> T getGenericInstance(Class<T> registeredFor) {
            return genericInstanceRegistry.getGenericInstance(registeredFor);
        }

        @Override
        public <T> T unregisterGenericInstance(Class<T> registeredFor) {
            return genericInstanceRegistry.unregisterGenericInstance(registeredFor);
        }

        @Override
        public <T> IGenericInstanceHandle<T> getGenericInstanceHandle(Class<T> registeredFor) {
            return genericInstanceRegistry.getGenericInstanceHandle(registeredFor);
        }

        @Override
        public boolean addComponent(Object obj, boolean allowComponentFactory) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void addFeatureTags(String key, Collection<String> featureTags) {
            // IGNORE
        }

        @Override
        public void setFeatureTags(String key, Collection<String> featureTags) {
            // IGNORE
        }

        @Override
        public Map<String, Set<String>> getAllFeatureTags() {
            throw new UnsupportedOperationException();
        }

        @Override
        public int sendAdminNotifyMessage(String message) {
            StaticLog.logInfo("AdminNotifyMessage: " + message);
            return 1;
        }

        @Override
        public void sendMessageOnTick(String playerName, String message) {
            StaticLog.logInfo("sendMessageOnTick (-> " + playerName + "): " + message);
        }

        @Override
        public boolean allowLogin(String playerName) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int allowLoginAll() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void denyLogin(String playerName, long duration) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isLoginDenied(String playerName) {
            throw new UnsupportedOperationException();
        }

        @Override
        public String[] getLoginDeniedPlayers() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isLoginDenied(String playerName, long time) {
            throw new UnsupportedOperationException();
        }

        @Override
        public LogManager getLogManager() {
            // TODO: Maybe do implement a dummy log manager (with file?) 
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean hasFeatureTag(String key, String feature) {
            throw new UnsupportedOperationException();
        }

        @Override
        public BlockChangeTracker getBlockChangeTracker() {
            throw new UnsupportedOperationException();
        }

        @Override
        public EventRegistryBukkit getEventRegistry() {
            throw new UnsupportedOperationException();
        }

        @Override
        public PermissionRegistry getPermissionRegistry() {
            return permissionRegistry;
        }

    }

    public static void setUnitTestNoCheatPlusAPI(boolean force) {
        if (force || NCPAPIProvider.getNoCheatPlusAPI() == null) {
            NCPAPIProvider.setNoCheatPlusAPI(new UnitTestNoCheatPlusAPI());
        }
    }

}
