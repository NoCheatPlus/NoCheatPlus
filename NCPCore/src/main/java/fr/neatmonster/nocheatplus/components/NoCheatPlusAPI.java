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
package fr.neatmonster.nocheatplus.components;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import fr.neatmonster.nocheatplus.actions.ActionFactoryFactory;
import fr.neatmonster.nocheatplus.compat.blocks.changetracker.BlockChangeTracker;
import fr.neatmonster.nocheatplus.components.registry.ComponentRegistry;
import fr.neatmonster.nocheatplus.components.registry.ComponentRegistryProvider;
import fr.neatmonster.nocheatplus.components.registry.GenericInstanceRegistry;
import fr.neatmonster.nocheatplus.components.registry.setup.RegistrationContext;
import fr.neatmonster.nocheatplus.event.mini.EventRegistryBukkit;
import fr.neatmonster.nocheatplus.logging.LogManager;
import fr.neatmonster.nocheatplus.permissions.PermissionRegistry;
import fr.neatmonster.nocheatplus.players.IPlayerDataManager;
import fr.neatmonster.nocheatplus.worlds.IWorldDataManager;



/**
 * ComponentRegistry:
 * <li>Supported components: Listener, TickListener, PermStateReceiver,
 * INotifyReload, INeedConfig, IRemoveData, MCAccessHolder, ConsistencyChecker,
 * JoinLeaveListener, IDisableListener</li>
 * <li>ComponentRegistry instances will be registered as sub registries unless
 * you use the addComponent(Object, boolean) method appropriately.</li>
 * <li>IHoldSubComponents instances will be registered in the next tick
 * (scheduled task), those added within onEnable will get registered after
 * looping in onEnable.</li>
 * <li>JoinLeaveListeners are called on EventPriority.LOW.</li>
 * <li>Registering components should be done during onEnable or any time while
 * the plugin is enabled, all components will be unregistered in onDisable.</li>
 * <li>References to all components will be held until onDisable is
 * finished.</li>
 * <li>Event registration and unregistering via passing a
 * {@link org.bukkit.event.Listener} is possible, see
 * {@link fr.neatmonster.nocheatplus.event.mini.EventRegistryBukkit} and
 * {@link fr.neatmonster.nocheatplus.event.mini.MiniListenerRegistry} for
 * details on how to apply
 * {@link fr.neatmonster.nocheatplus.components.registry.order.RegistrationOrder}
 * for controlling the order of event processing and further options. Fetch the
 * event registry via {@link #getEventRegistry()}</li>
 * <hr>
 * Not sure about all the login-denial API, some of those might get removed.
 * <hr>
 * NOTE: Class names for implementations of the NoCheatPlusAPI which aim at unit
 * tests, where server access might not work, should start with "UnitTest".
 * <hr>
 * 
 * @author asofold
 *
 */
public interface NoCheatPlusAPI extends ComponentRegistry<Object>, ComponentRegistryProvider, GenericInstanceRegistry    {

    /**
     * By default addComponent(Object) will register ComponentFactories as well.
     * @param obj
     * @param allowComponentFactory If to allow registering ComponentFactories.
     * @return
     */
    public boolean addComponent(Object obj, boolean allowComponentFactory);

    /**
     * Tell NCP that certain features are present, e.g. for display with the
     * "ncp version" command. Version tags get cleared with disabling the
     * plugin.
     * 
     * @param key
     * @param featureTags
     */
    public void addFeatureTags(String key, Collection<String> featureTags);

    /**
     * Tell NCP that certain features are present, e.g. for display with the
     * "ncp version" command. Overrides all present definitions for the given
     * key. Version tags get cleared with disabling the plugin.
     * 
     * @param key
     * @param featureTags
     */
    public void setFeatureTags(String key, Collection<String> featureTags);

    /**
     * Test if an entry has been made.
     * @param key
     * @param feature
     * @return
     */
    public boolean hasFeatureTag(String key, String feature);

    /**
     * Get a map with all feature tags that have been set.
     * @return
     */
    public Map<String, Set<String>> getAllFeatureTags();

    /**
     * Send all players with the nocheatplus.admin.notify permission a message.<br>
     * This will act according to configuration (stored permissions and/or permission subscriptions).
     * 
     * @param message
     * @return Number of players messaged.
     */
    public int sendAdminNotifyMessage(final String message);

    /**
     * Thread-safe method to send a message to a player in a scheduled task. The scheduling preserves order of messages.
     * @param playerName
     * @param message
     */
    public void sendMessageOnTick(final String playerName, final String message);


    /**
     * Allow login (remove from deny login map).
     * @param playerName
     * @return If player was denied to login.
     */
    public boolean allowLogin(String playerName);

    /**
     * Remove all players from the allow login set.
     * @return Number of players that had actually been denied to login.
     */
    public int allowLoginAll();

    /**
     * Deny the player to login. This will also remove expired entries.
     * @param playerName
     * @param duration Duration from now on, in milliseconds.
     */
    public void denyLogin(String playerName, long duration);

    /**
     * Check if player is denied to login right now. 
     * @param playerName
     * @return
     */
    public boolean isLoginDenied(String playerName);

    /**
     * Get the names of all players who are denied to log in at present.
     * @return
     */
    public String[] getLoginDeniedPlayers();

    /**
     * Check if a player is denied to login at a certain point of time.
     * @param playerName
     * @param currentTimeMillis
     * @return
     */
    public boolean isLoginDenied(String playerName, long time);

    /**
     * Get the central access point for logging (LogManager),
     * @return
     */
    public LogManager getLogManager();

    /**
     * Get the block change tracker (pistons, other).
     * @return
     */
    public BlockChangeTracker getBlockChangeTracker();

    /**
     * Get the registry to register events with the
     * {@link org.bukkit.plugin.PluginManager}, e.g. for the case that
     * {@link #addComponent(Object)} and similar is not sufficient/appropriate.
     * <br>
     * <br>
     * For details see
     * {@link fr.neatmonster.nocheatplus.event.mini.EventRegistryBukkit} and
     * {@link fr.neatmonster.nocheatplus.event.mini.MiniListenerRegistry}.
     * <hr>
     * 
     * @return
     */
    public EventRegistryBukkit getEventRegistry();

    /**
     * Get the internal permission registry, holding internal id mappings and
     * caching policies. (This is detached from Bukkit.)
     * 
     * @return
     */
    // TODO: Remove in favor of per world permission registries (!).
    public PermissionRegistry getPermissionRegistry();

    /**
     * Get the WorldDataManager, which stores per-world data and configuration.
     * 
     * @return
     */
    public IWorldDataManager getWorldDataManager();

    /**
     * Get the PlayerDataManager, which stores per player data and
     * configuration.
     * 
     * @return
     */
    public IPlayerDataManager getPlayerDataManager();

    /**
     * Get a new registration context instance for registration with
     * {@link NoCheatPlusAPI#register(RegistrationContext)}.
     * 
     * @return
     */
    public RegistrationContext newRegistrationContext();

    /**
     * Do use {@link NoCheatPlusAPI#newRegistrationContext()} for future
     * compatibility.
     * 
     * @param context
     */
    public void register(RegistrationContext context);

    /**
     * Get the registered factory for retrieving config-dependent ActionFactory
     * instances.
     * 
     * @return
     */
    public ActionFactoryFactory getActionFactoryFactory();

    /**
     * Register a factory for retrieving config-dependent ActionFactory
     * instances. The given instance will be the one returned by
     * {@link #getActionFactoryFactory()}. Pass null to reset to default.
     * <hr/>
     * For all stored raw configurations,
     * {@link fr.neatmonster.nocheatplus.config.ConfigFile#setActionFactory(ActionFactoryFactory)})
     * will be called.<br/>
     * To ensure that configurations are newly created with altered actions, you
     * should first call
     * {@link fr.neatmonster.nocheatplus.worlds.IWorldDataManager#removeCachedConfigs()}
     * and finally
     * {@link fr.neatmonster.nocheatplus.players.IPlayerDataManager#removeCachedConfigs()}
     * <hr/>
     * To Hook into NCP for setting the factories, you could register a
     * INotifyReload instance with the NoCheatPlusAPI using the annotation
     * SetupOrder (to be deprecated, later: RegisterWithOrder) with a larger
     * negative value (-1000, see INotifyReload javadoc).
     * <hr/>
     * 
     * @param actionFactoryFactory
     *            The instance to set. Pass null to reset to the default
     *            factory.
     * @return The previously registered instance.
     */
    public ActionFactoryFactory setActionFactoryFactory(final ActionFactoryFactory actionFactoryFactory);

}
