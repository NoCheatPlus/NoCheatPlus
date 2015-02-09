package fr.neatmonster.nocheatplus.components;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import fr.neatmonster.nocheatplus.logging.LogManager;



/**
 * ComponentRegistry: 
 * <li>Supported components: Listener, TickListener, PermStateReceiver, INotifyReload, INeedConfig, IRemoveData, MCAccessHolder, ConsistencyChecker, JoinLeaveListener, DisableListener</li>
 * <li>ComponentRegistry instances will be registered as sub registries unless you use the addComponent(Object, boolean) method appropriately. </li>
 * <li>IHoldSubComponents instances will be registered in the next tick (scheduled task), those added within onEnable will get registered after looping in onEnable.</li>
 * <li>JoinLeaveListeners are called on EventPriority.LOW.</li>
 * <li>Registering components should be done during onEnable or any time while the plugin is enabled, all components will be unregistered in onDisable.</li>
 * <li>References to all components will be held until onDisable is finished.</li> 
 * <li>Interfaces checked for managed listeners: IHaveMethodOrder (method), ComponentWithName (tag)</li>
 * <hr>
 * Not sure about all the login-denial API, some of those might get removed.
 * @author mc_dev
 *
 */
public interface NoCheatPlusAPI extends ComponentRegistry<Object>, ComponentRegistryProvider, GenericInstanceRegistry, MCAccessHolder {

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

}
