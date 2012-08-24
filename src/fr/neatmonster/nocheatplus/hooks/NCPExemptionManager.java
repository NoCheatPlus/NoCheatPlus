package fr.neatmonster.nocheatplus.hooks;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import fr.neatmonster.nocheatplus.checks.CheckType;

/**
 * API for exempting players of checks, checked before calculations are done.
 * 
 * @author mc_dev
 *
 */
public class NCPExemptionManager {
	
	/**
	 * CheckType -> Entity id -> Exemption info.
	 * 
	 * TODO: opt: move these to checks individually for even faster access.
	 */
	private static final Map<CheckType, Set<Integer>> exempted = new HashMap<CheckType, Set<Integer>>();
	
	/**
	 * Registered players (exact name) -> entity id (last time registered). 
	 */
	private static final Map<String, Integer> registeredPlayers = new HashMap<String, Integer>();
	
	static{
		clear();
	}
	
	/**
	 * Check if a player is exempted from a check right now.
	 * 
	 * @param player
	 * 		The player to exempt from checks.
	 * @param checkType
	 * 		The type of check to exempt the player from. This can be individual check types, as well as a check group like MOVING or ALL.
	 * @return
	 * 		If the player is exempted from the check right now.
	 */
	public static final boolean isExempted(final Player player, final CheckType checkType){
		return isExempted(player.getEntityId(), checkType);
	}
	
	/**
	 * Check if an entity is exempted from a check by entity id right now.
	 * <hr>
	 * This might help exempting NPCs from checks for all time, making performance a lot better. A future purpose might be to exempt vehicles and similar (including passengers) from checks.
	 * @param id
	 * 		Entity id to exempt from checks.
	 * @param checkType
	 * 		The type of check to exempt the player from. This can be individual check types, as well as a check group like MOVING or ALL.
	 * @return
	 * 		If the entity is exempted from checks right now.
	 */
	public static final boolean isExempted(final int id, final CheckType checkType){
		return exempted.get(checkType).contains(id);
	}
	
	/**
	 * Remove all exemptions.
	 */
	public static final void clear(){
		registeredPlayers.clear();
		// Use put with a new map to keep entries to stay thread safe.
		for (final CheckType checkType : CheckType.values()){
			if (APIUtil.needsSynchronization(checkType))
				exempted.put(checkType, Collections.synchronizedSet(new HashSet<Integer>(10)));
			else 
				exempted.put(checkType, new HashSet<Integer>(10));
		}
		
	}
	
	/**
	 * This should be registered before all other listeners of NCP (!).
	 * 
	 * NOTE: For internal use only, DO NOT CALL FROM OUTSIDE.
	 * @return
	 */
	public static Listener getListener(){
		return new Listener() {
			@SuppressWarnings("unused")
			@EventHandler(priority=EventPriority.LOWEST)
			final void onJoin(final PlayerJoinEvent event){
				NCPExemptionManager.registerPlayer(event.getPlayer());
			}
			@SuppressWarnings("unused")
			@EventHandler(priority=EventPriority.MONITOR)
			final void onJoin(final PlayerQuitEvent event){
				NCPExemptionManager.checkRemovePlayer(event.getPlayer());
			}
		};
	}

	/**
	 * Check if the registeredPlayers mapping can be removed for a player, i.e. no exemptions are present.
	 * @param player
	 */
	protected static final void checkRemovePlayer(final Player player) {
		if (!registeredPlayers.containsKey(player.getName())) return;
		final Integer id = player.getEntityId();
		for (final CheckType checkType : CheckType.values()){
			// Check if player is exempted from something.
			if (isExempted(id, checkType)) return;
		}
		// No return = remove player.
		registeredPlayers.remove(player.getName());
	}

	/**
	 * Register current entity id for the player.
	 * @param player
	 */
	public static final void registerPlayer(final Player player) {
		final int newId = player.getEntityId();
		final String name = player.getName();
		
		final Integer registeredId = registeredPlayers.get(name);
		if (registeredId == null){
			// Was not registered.
			registeredPlayers.put(name, newId);
		}
		else if (newId == registeredId.intValue()){
			// No change.
		}
		else {
			// Player was registered under another id (needs exchange).
			for (final Set<Integer> set : exempted.values()){
				if (set.remove(registeredId)){
					// replace.
					set.add(newId);
				}
			}
			registeredPlayers.put(name, newId);
		}
	}
	
	/**
	 * Remove all exempting a player.
	 * @param player
	 */
	public static final void unExempt(final Player player){
		unExempt(player, CheckType.ALL);
	}
	
	/**
	 * Undo exempting a player form a certain check, or check group, as given.
	 * @param player
	 * @param checkType
	 */
	public static final void unExempt(final Player player, final CheckType checkType){
		unExempt(player.getEntityId(), checkType);
	}
	
	/**
	 * Undo exempting an entity by entity id from all checks.
	 * @param entityId
	 */
	public static final void unExempt(final int entityId){
		unExempt(entityId, CheckType.ALL);
	}
	
	/**
	 * Undo exempting an entity by entity id from a certain check type, also check groups, etc.
	 * @param entityId
	 * @param checkType
	 */
	public static final void unExempt(final int entityId, final CheckType checkType){
		final Integer id = entityId;
		exempted.get(checkType).remove(id);
		for (final CheckType child : APIUtil.getChildren(checkType)){
			exempted.get(child).remove(id);
		}
	}
	
	/**
	 * Exempt a player form all checks.
	 * @param player
	 */
	public static final void exemptPermanently(final Player player){
		exemptPermanently(player, CheckType.ALL);
	}
			
	/**
	 * Exempt a player from a check or check group permanently.
	 * @param player
	 * @param checkType
	 */
	public static final void exemptPermanently(final Player player, final CheckType checkType){
		exemptPermanently(player.getEntityId(), checkType);
	}
	
	/**
	 * exempt an entity from all checks, by entity id. 
	 * @param entityId
	 */
	public static final void exemptPermanently(final int entityId){
		exemptPermanently(entityId, CheckType.ALL);
	}
	
	/**
	 * Exempt an entity by entity id from the given check or check group permanently (only until restart).
	 * @param entityId
	 * @param checkType
	 */
	public static final void exemptPermanently(final int entityId, final CheckType checkType){
		final Integer id = entityId;
		exempted.get(checkType).add(id);
		for (final CheckType child : APIUtil.getChildren(checkType)){
			exempted.get(child).add(id);
		}
	}
	
}
