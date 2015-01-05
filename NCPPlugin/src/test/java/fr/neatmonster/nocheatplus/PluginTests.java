package fr.neatmonster.nocheatplus;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import fr.neatmonster.nocheatplus.compat.MCAccess;
import fr.neatmonster.nocheatplus.compat.bukkit.MCAccessBukkit;
import fr.neatmonster.nocheatplus.components.ComponentRegistry;
import fr.neatmonster.nocheatplus.components.NoCheatPlusAPI;
import fr.neatmonster.nocheatplus.logging.LogManager;
import fr.neatmonster.nocheatplus.logging.StaticLog;

public class PluginTests {

    /**
     * Dummy API, providing only a minimal subset of functionality for offline-testing. Some methods do nothing, some throw an UnsupportedOperationException, some will do something (set/get MCAccess).
     * @author web4web1
     *
     */
    public static class DummyNoCheatPlusAPI implements NoCheatPlusAPI {

        private MCAccess mcAccess = new MCAccessBukkit();

        @Override
        public void setMCAccess(MCAccess mcAccess) {
            this.mcAccess = mcAccess;
        }

        @Override
        public MCAccess getMCAccess() {
            return mcAccess;
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
            throw new UnsupportedOperationException();
        }

        @Override
        public <T> T getGenericInstance(Class<T> registeredFor) {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T> T unregisterGenericInstance(Class<T> registeredFor) {
            throw new UnsupportedOperationException();
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


    }

    public static void setDummNoCheatPlusAPI(boolean force) {
        if (force || NCPAPIProvider.getNoCheatPlusAPI() == null) {
            NCPAPIProvider.setNoCheatPlusAPI(new DummyNoCheatPlusAPI());
        }
    }

}
