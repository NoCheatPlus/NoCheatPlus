package fr.neatmonster.nocheatplus.hooks;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

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
 * NOTE: Solution for async: Query the related checks all under the same lock (!), like ChatData for chat.
 * 
 * TODO: Check if the above note is valid! :p
 * 
 * @author mc_dev
 *
 */
public class NCPExemptionManager {
	
	/**
	 * CheckType -> Entity id -> Exemption info (<0 = times, 0 = permanently, >0 = timestamp).
	 * 
	 * TODO: opt: move these to checks individually for even faster access.
	 */
	private static final Map<CheckType, Map<Integer, Long>> exempted = new HashMap<CheckType, Map<Integer,Long>>();
	
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
	 * NOTE: Do not set consume to true for outside access, it is for internal use only and might break other plugins intentions.
	 * @param player
	 * 		The player to exempt from checks.
	 * @param checkType
	 * 		The type of check to exempt the player from. This can be individual check types, as well as a check group like MOVING or ALL.
	 * @param consume
	 * 		Flag to indicate if to count down if a player is exempted from a number of such checks - do not set to true on external calls, it could break other plugins intentions.
	 * @return
	 * 		If the player is exempted from the check right now.
	 */
	public static final boolean isExempted(final Player player, final CheckType checkType, final boolean consume){
		return isExempted(player.getEntityId(), checkType, consume);
	}
	
	/**
	 * Check if an entity is exempted from a check by entity id right now.
	 * 
	 * NOTE: Do not set consume to true for outside access, it is for internal use only and might break other plugins intentions.
	 * <hr>
	 * This might help exempting NPCs from checks for all time, making performance a lot better. A future purpose might be to exempt vehicles and similar (including passengers) from checks.
	 * @param id+
	 * 		Entity id to exempt from checks.
	 * @param checkType
	 * 		The type of check to exempt the player from. This can be individual check types, as well as a check group like MOVING or ALL.
	 * @param consume
	 * 		Flag to indicate if to count down if a player is exempted from a number of such checks - do not set to true on external calls, it could break other plugins intentions.
	 * @return
	 * 		If the entity is exempted from checks right now.
	 */
	public static final boolean isExempted(final int id, final CheckType checkType, final boolean consume){
		final Map<Integer, Long> map = exempted.get(checkType);
		final Long spec = map.get(id);
		if (spec == null) 
			return false;
		else if (spec == 0){
			// Exempted from checks permanently.
			return true;
		}
		else if (spec < 0){
			// Exempted for a number of checks.
			long val = spec.longValue() + 1;
			if (consume){
				if (val == 0)
					map.remove(id);
				else 
					map.put(id, val);
			}
			return 
					true;
		}
		else {
			// (spec > 0) Exempted for a period of time.
			final long ts = System.currentTimeMillis(); // TODO: maybe make argument, but might be used very seldom.
			if (ts > spec){
				// Expired
				if (consume)
					map.remove(id);
				return false;
			}
			else
				return true;
		}
	}
	
	/**
	 * Remove all exemptions.
	 */
	public static final void clear(){
		// Use put with a new map to keep entries to stay thread safe.
		for (final CheckType checkType : CheckType.values()){
			if (APIUtil.needsSynchronization(checkType))
				exempted.put(checkType, new Hashtable<Integer, Long>(10));
			else 
				exempted.put(checkType, new HashMap<Integer, Long>(10));
		}
		registeredPlayers.clear();
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
			if (isExempted(id, checkType, false)) return;
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
			final long ts = System.currentTimeMillis();
			for (final Map<Integer, Long> map : exempted.values()){
				final Long entry = map.remove(registeredId);
				if (entry == null) 
					continue;
				else{
					// replace if not expired.
					if (entry <= 0 || ts <= entry)
						map.put(newId, entry);
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
	 * Undo exempting an entity (or player by entity id) from checks.
	 * @param entityId
	 */
	public static final void unExempt(final int entityId){
		unExempt(entityId, CheckType.ALL);
	}
	
	/**
	 * Undo exempting an entity (or player by entity id) from a certain check type, also check groups, etc.
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
	 * Exempt a player from a check n times (english?).
	 * @param player
	 * @param checkType
	 * @param n
	 */
	public static final void exemptTimes(final Player player, final CheckType checkType, final int n){
		exemptTimes(player.getEntityId(), checkType, n); // mind n :p
	}
	
	/**
	 * Exempt an entity by entity id from a check n times, could be a check group, etc.
	 * @param entityId
	 * @param checkType
	 * @param n
	 */
	public static final void exemptTimes(final int entityId, final CheckType checkType, final int n){
		if (n <= 0) throw new IllegalArgumentException("Bad number given: " + n);
		exempt(entityId, checkType, -n);
	}
	
	/**
	 * Exempt a player from a check or check group etc. for a given duration in milliseconds (i am curious if this is ever used by anyone/anything).
	 * @param player
	 * @param checkType
	 * @param millis
	 */
	public static final void exemptMillis(final Player player, final CheckType checkType, final long millis){
		exemptMillis(player.getEntityId(), checkType, millis);
	}
	
	/**
	 * Exempt the player from the check or check group for the given duration in milliseconds.
	 * @param entityId
	 * @param checkType
	 * @param millis
	 */
	public static final void exemptMillis(final int entityId, final CheckType checkType, final long millis){
		if (millis <= 0) throw new IllegalArgumentException("Bad duration given: " + millis);
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
	 * Exempt an entity by entity id from the given check or check group permanently (only until restart).
	 * @param entityId
	 * @param checkType
	 */
	public static final void exemptPermanently(final int entityId, final CheckType checkType){
		exempt(entityId, checkType, 0);
	}
	
	/**
	 * Generic exempting.
	 * @param entityId
	 * @param checkType
	 * @param spec <0 = times, ==0 = permanently, >0 = timestamp.
	 */
	private static final void exempt(final int entityId, final CheckType checkType, long spec){
		final Integer id = entityId;
		exempt(id, spec, exempted.get(checkType));
		for (final CheckType child : APIUtil.getChildren(checkType)){
			exempt(id, spec, exempted.get(child));
		}
	}
	
	/**
	 * Auxiliary, using the corresponding map.
	 * @param id
	 * @param spec
	 * @param map
	 */
	private static final void exempt(final Integer id, final long spec, final Map<Integer, Long> map){
		final Long old = map.get(id);
		if (old == null)
			map.put(id, spec);
		else if (old == 0) 
			return;
		else if (spec == 0)
			map.put(id, spec);
		else if (old < 0 ){
			if (spec > 0 || spec < old) 
				map.put(id, spec);
		}
		else{
			// old > 0
			if (spec > old)
				map.put(id, spec);
		}
	}
	
}
