package fr.neatmonster.nocheatplus.hooks;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.checks.CheckType;

/**
 * API for exempting players of checks, checked before calculations are done.
 * 
 * @author asofold
 */
public class NCPExemptionManager {

    /** A map associating a check type with the unique ids of its exempted players. */
    private static final Map<CheckType, Set<UUID>> exempted          = new HashMap<CheckType, Set<UUID>>();

    static {
        clear();
    }

    /**
     * Remove all exemptions.
     */
    public static final void clear() {
        // Use put with a new map to keep entries to stay thread safe.
        for (final CheckType checkType : CheckType.values())
            if (APIUtils.needsSynchronization(checkType)) {
                exempted.put(checkType, Collections.synchronizedSet(new HashSet<UUID>()));
            }
            else {
                exempted.put(checkType, new HashSet<UUID>());
            }
    }

    /**
     * Exempt an entity from all checks permanently.
     * 
     * @param id
     *            The unique id.
     */
    public static final void exemptPermanently(final UUID id) {
        exemptPermanently(id, CheckType.ALL);
    }

    /**
     * Exempt an entity from the given check or check group permanently (only
     * until restart).
     * 
     * @param id
     *            The unique id.
     * @param checkType
     *            The check type.
     */
    public static final void exemptPermanently(final UUID id, final CheckType checkType) {
        exempted.get(checkType).add(id);
        for (final CheckType child : APIUtils.getChildren(checkType)) {
            exempted.get(child).add(id);
        }
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
     *            The player to exempt.
     * @param checkType
     *            The check type.
     */
    public static final void exemptPermanently(final Player player, final CheckType checkType) {
        exemptPermanently(player.getUniqueId(), checkType);
    }

    /**
     * Check if an entity is exempted from a check right now by entity id.
     * <hr>
     * This might help exempting NPCs from checks for all time, making
     * performance a lot better. A future purpose might be to exempt vehicles
     * and similar (including passengers) from checks.
     * 
     * @param id
     *            The unique id.
     * @param checkType
     *            This can be individual check types, as well as a check group
     *            like MOVING or ALL.
     * @return If the entity is exempted from checks right now.
     */
    public static final boolean isExempted(final UUID id, final CheckType checkType) {
        return exempted.get(checkType).contains(id);
    }

    /**
     * Check if a player is exempted from a check right now.
     * 
     * @param player
     *            The player to exempt from checks
     * @param checkType
     *            This can be individual check types, as well as a check group
     *            like MOVING or ALL.
     * @return If the player is exempted from the check right now.
     */
    public static final boolean isExempted(final Player player, final CheckType checkType) {
        return isExempted(player.getUniqueId(), checkType);
    }

    /**
     * Undo exempting an entity from all checks.
     * 
     * @param id
     *            The unique id.
     */
    public static final void unexempt(final UUID id) {
        unexempt(id, CheckType.ALL);
    }

    /**
     * Undo exempting an entity from a certain check, or check group, as given.
     * 
     * @param id
     *            The unique id.
     * @param checkType
     *            The check type.
     */
    public static final void unexempt(final UUID id,  final CheckType checkType) {
        exempted.get(checkType).remove(id);
        for (final CheckType child : APIUtils.getChildren(checkType)) {
            exempted.get(child).remove(id);
        }
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
     * Undo exempting a player form a certain check, or check group, as given.
     * 
     * @param player
     *            the player
     * @param checkType
     *            the check type
     */
    public static final void unexempt(final Player player, final CheckType checkType) {
        unexempt(player.getUniqueId(), checkType);
    }

}
