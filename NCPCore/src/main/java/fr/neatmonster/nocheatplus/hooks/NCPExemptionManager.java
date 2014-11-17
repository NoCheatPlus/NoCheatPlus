package fr.neatmonster.nocheatplus.hooks;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.components.NCPListener;
import fr.neatmonster.nocheatplus.logging.StaticLog;
import fr.neatmonster.nocheatplus.utilities.StringUtil;

/**
 * API for exempting players of checks, checked before calculations are done.
 * 
 * @author asofold
 */
public class NCPExemptionManager {

    /** A map associating a check type with the entity ids of its exempted players. */
    private static final Map<CheckType, Set<Integer>> exempted          = new HashMap<CheckType, Set<Integer>>();

    /** A map associating the registered player with their entity id. */
    private static final Map<String, Integer>         registeredPlayers = new HashMap<String, Integer>();

    static {
        clear();
    }

    /**
     * Remove all exemptions.
     */
    public static final void clear() {
        registeredPlayers.clear();
        // Use put with a new map to keep entries to stay thread safe.
        for (final CheckType checkType : CheckType.values())
            if (APIUtils.needsSynchronization(checkType))
                exempted.put(checkType, Collections.synchronizedSet(new HashSet<Integer>()));
            else
                exempted.put(checkType, new HashSet<Integer>());
    }

    /**
     * Exempt an entity from all checks permanently.
     * 
     * @param entityId
     *            the entity id
     */
    public static final void exemptPermanently(final int entityId) {
        exemptPermanently(entityId, CheckType.ALL);
    }

    /**
     * Exempt an entity from the given check or check group permanently (only until restart).
     * 
     * @param entityId
     *            the entity id
     * @param checkType
     *            the check type
     */
    public static final void exemptPermanently(final int entityId, final CheckType checkType) {
        final Integer id = entityId;
        exempted.get(checkType).add(id);
        for (final CheckType child : APIUtils.getChildren(checkType))
            exempted.get(child).add(id);
    }

    /**
     * Exempt a player form all checks permanently.
     * 
     * @param player
     *            the player
     */
    public static final void exemptPermanently(final Player player) {
        exemptPermanently(player, CheckType.ALL);
    }

    /**
     * Exempt a player from a check or check group permanently.
     * 
     * @param player
     *            the player
     * @param checkType
     *            the check type
     */
    public static final void exemptPermanently(final Player player, final CheckType checkType) {
        exemptPermanently(player.getEntityId(), checkType);
    }

	/**
	 * This should be registered before all other listeners of NoCheatPlus.
	 * 
	 * NOTE: For internal use only, DO NOT CALL FROM OUTSIDE.
	 * 
	 * @return the listener
	 */
	public static Listener getListener() {
		return new NCPListener() {
			@EventHandler(priority = EventPriority.LOWEST)
			public void onPlayerJoin(final PlayerJoinEvent event) {
				NCPExemptionManager.registerPlayer(event.getPlayer());
			}

			@EventHandler(priority = EventPriority.MONITOR)
			public void onPlayerQuit(final PlayerQuitEvent event) {
				NCPExemptionManager.tryToRemove(event.getPlayer());
			}

			@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
			public void onPlayerKick(final PlayerKickEvent event) {
				NCPExemptionManager.tryToRemove(event.getPlayer());
			}
		};
	}

    /**
     * Check if an entity is exempted from a check right now by entity id.
     * <hr>
     * This might help exempting NPCs from checks for all time, making performance a lot better. A future purpose might
     * be to exempt vehicles and similar (including passengers) from checks.
     * 
     * @param entityId
     *            the entity id to exempt from checks
     * @param checkType
     *            the type of check to exempt the player from. This can be individual check types, as well as a check
     *            group like MOVING or ALL
     * @return if the entity is exempted from checks right now
     */
    public static final boolean isExempted(final int entityId, final CheckType checkType) {
        return exempted.get(checkType).contains(entityId);
    }

    /**
     * Check if a player is exempted from a check right now.
     * 
     * @param player
     *            the player to exempt from checks
     * @param checkType
     *            the type of check to exempt the player from. This can be individual check types, as well as a check
     *            group like MOVING or ALL
     * @return if the player is exempted from the check right now
     */
    public static final boolean isExempted(final Player player, final CheckType checkType) {
        return isExempted(player.getEntityId(), checkType);
    }
    
    /**
     * Check if a player is exempted from a check right now by player name.
     * @param playerName
     * 			The exact player name.
     * @param checkType
     *            the type of check to exempt the player from. This can be individual check types, as well as a check
     *            group like MOVING or ALL
     * @return  if the player is exempted from the check right now
     */
    public static final boolean isExempted(final String playerName, final CheckType checkType) {
    	final Integer entityId = registeredPlayers.get(playerName);
    	if (entityId == null) return false;
        return isExempted(entityId, checkType);
    }

    /**
     * Register current entity id for the player.
     * 
     * @param player
     *            the player
     */
    public static final void registerPlayer(final Player player) {
        final int entityId = player.getEntityId();
        final String name = player.getName();

        final Integer registeredId = registeredPlayers.get(name);
        if (registeredId == null)
            // Player wasn't registered.
            registeredPlayers.put(name, entityId);
        else if (entityId != registeredId.intValue()) {
            // Player was registered under another id (needs exchange).
            for (final Set<Integer> set : exempted.values())
                if (set.remove(registeredId))
                    // Replace.
                    set.add(entityId);
            registeredPlayers.put(name, entityId);
        }
    }

    /**
     * Check if the registeredPlayers mapping can be removed for a player, i.e. no exemptions are present.
     * 
     * @param player
     *            the player
     */
    protected static final void tryToRemove(final Player player) {
        if (!registeredPlayers.containsKey(player.getName()))
            return;
        final Integer entityId = player.getEntityId();
        for (final CheckType checkType : CheckType.values())
            // Check if player is exempted from something.
            if (isExempted(entityId, checkType))
                // If they are, we can't remove them so we return.
                return;
        registeredPlayers.remove(player.getName());
    }

    /**
     * Undo exempting an entity from all checks.
     * 
     * @param entityId
     *            the entity id
     */
    public static final void unexempt(final int entityId) {
        unexempt(entityId, CheckType.ALL);
    }

    /**
     * Undo exempting an entity from a certain check, or check group, as given.
     * 
     * @param entityId
     *            the entity id
     * @param checkType
     *            the check type
     */
    public static final void unexempt(final int entityId, final CheckType checkType) {
        final Integer id = entityId;
        exempted.get(checkType).remove(id);
        for (final CheckType child : APIUtils.getChildren(checkType))
            exempted.get(child).remove(id);
    }

    /**
     * Undo exempting a player from all checks.
     * 
     * @param player
     *            the player
     */
    public static final void unexempt(final Player player) {
        unexempt(player, CheckType.ALL);
    }
    
    /**
     * Undo exempting a player from all checks.
     * 
     * @param playerName
     *            the players exact name
     */
    public static final void unexempt(final String playerName) {
        unexempt(playerName, CheckType.ALL);
    }

    /**
     * Undo exempting a player form a certain check, or check group, as given.
     * 
     * @param player
     *            the player
     * @param checkType
     *            the check type
     */
    public static final void unexempt(final Player player, final CheckType checkType) {
        unexempt(player.getEntityId(), checkType);
    }
    
    /**
     * Undo exempting a player form a certain check, or check group, as given.
     * 
     * @param playerName
     *            the exact player name.
     * @param checkType
     *            the check type
     */
    public static final void unexempt(final String playerName, final CheckType checkType) {
    	final Integer entityId = registeredPlayers.get(playerName);
    	if (entityId != null) unexempt(entityId, checkType);
    }
    
    /**
     * Check Entity-id mappings, for internal use.
     * @param onlinePlayers
     */
    public static void checkConsistency(final Player[] onlinePlayers){
    	int wrong = 0;
    	for (int i = 0; i < onlinePlayers.length; i++){
    		final Player player = onlinePlayers[i];
    		final int id = player.getEntityId();
    		final String name = player.getName();
    		final Integer presentId = registeredPlayers.get(name);
    		if (presentId == null){
    			// TODO: Could complain.
    		}
    		else if (id != presentId.intValue()){
    			wrong ++;
    			registerPlayer(player);
    		}
    		// TODO: Consider also checking if numbers don't match.
    	}
    	if (wrong != 0){
    		final List<String> details = new LinkedList<String>();
    		if (wrong != 0){
    			details.add("wrong entity-ids (" + wrong + ")");
    		}
    		StaticLog.logWarning("[NoCheatPlus] ExemptionManager inconsistencies: " + StringUtil.join(details, " | "));
    	}
    }

}
