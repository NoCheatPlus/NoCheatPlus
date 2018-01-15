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
package fr.neatmonster.nocheatplus.hooks;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.NCPAPIProvider;
import fr.neatmonster.nocheatplus.checks.CheckType;

/**
 * API for exempting players of checks, checked before calculations are done.
 * 
 * @author asofold
 */
public class NCPExemptionManager {

    private static ExemptionSettings settings = new ExemptionSettings();

    /**
     * A map associating a check type with the unique ids of its exempted
     * players.
     */
    private static final Map<CheckType, Set<UUID>> exempted          = new HashMap<CheckType, Set<UUID>>();

    static {
        clear();
    }

    /**
     * Get the current settings.
     * 
     * @return
     */
    public static ExemptionSettings getExemptionSettings() {
        return settings;
    }

    /**
     * Set the settings to apply from that moment on. <br>
     * Note that there is no cleanup, thus you should perform cleanup yourself,
     * in case of passing a custom sub class of ExemptionSettings, for the case
     * of your plugin disabling or NPC disabling. The given ExemptionSettings
     * instance will be registered as a generic instance for the
     * ExemptionSettings class.
     * 
     * @param settings
     *            If null, the default implementation will be used, otherwise
     *            the instance is stored as is (see note about cleanup for
     *            custom implementations).
     */
    public static void setExemptionSettings(ExemptionSettings settings) {
        NCPExemptionManager.settings = settings;
        NCPAPIProvider.getNoCheatPlusAPI().registerGenericInstance(ExemptionSettings.class, settings);
    }

    /**
     * Remove all exemptions.
     */
    public static final void clear() {
        // Use put with a new map to keep entries to stay thread safe.
        for (final CheckType checkType : CheckType.values()) {
            if (APIUtils.needsSynchronization(checkType)) {
                exempted.put(checkType, Collections.synchronizedSet(new HashSet<UUID>()));
            }
            else {
                exempted.put(checkType, new HashSet<UUID>());
            }
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
     * and similar (including passengers) from checks. Includes players, note
     * that this can not check for exemption by meta data.
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
     * Check for exemption, including meta data. Convenience method, testing for
     * primary thread.
     * 
     * @see #isExempted(Player, CheckType, boolean)
     * @param player
     * @param checkType
     * @return
     */
    public static final boolean isExempted(final Player player, final CheckType checkType) {
        return isExempted(player, checkType,  Bukkit.isPrimaryThread());
    }

    /**
     * Check if a player is exempted from a check right now. This also checks
     * for exemption by meta data, iff it's called from within execution of the
     * primary thread. Wild card exemption for NPCs is also checked.
     * 
     * @param player
     *            The player to exempt from checks
     * @param checkType
     *            This can be individual check types, as well as a check group
     *            like MOVING or ALL.
     * @param isPrimaryThread
     *            If set to true, this has to be the primary server thread, as
     *            returned by Bukkit.isPrimaryThread(). If set to false,
     *            meta data can't be checked!
     * @return If the player is exempted from the check right now.
     */
    public static final boolean isExempted(final Player player, final CheckType checkType,
            final boolean isPrimaryThread) {
        return isExempted(player.getUniqueId(), checkType) 
                || settings.isExemptedBySettings(player, isPrimaryThread);
    }

    /**
     * Undo exempting an entity from all checks. Includes players, note that
     * exemption by meta data is not removed here.
     * 
     * @param id
     *            The unique id.
     */
    public static final void unexempt(final UUID id) {
        unexempt(id, CheckType.ALL);
    }

    /**
     * Undo exempting an entity from a certain check, or check group, as given.
     * Note that exemption by meta data is not removed here.
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
     * Undo exempting a player from all checks. Note that exemption by meta data
     * is not removed here.
     * 
     * @param player
     *            the player
     */
    public static final void unexempt(final Player player) {
        unexempt(player, CheckType.ALL);
    }

    /**
     * Undo exempting a player form a certain check, or check group, as given.
     * Note that exemption by meta data is not removed here.
     * 
     * @param player
     *            the player
     * @param checkType
     *            the check type
     */
    public static final void unexempt(final Player player, final CheckType checkType) {
        // TODO: Consider settings for removing meta data as well.
        unexempt(player.getUniqueId(), checkType);
    }

}
