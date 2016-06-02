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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.NCPAPIProvider;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.ViolationData;
import fr.neatmonster.nocheatplus.checks.access.IViolationInfo;
import fr.neatmonster.nocheatplus.logging.Streams;

/**
 * After-check-failure hook manager integrated into NoCheatPlus.
 * 
 * @author asofold
 */
public final class NCPHookManager {
    /** Ids given to hooks. */
    private static int                                 maxHookId     = 0;

    /** Hook id to hook. */
    private final static Map<Integer, NCPHook>         allHooks      = new HashMap<Integer, NCPHook>();

    /** Mapping the check types to the hooks. */
    private static final Map<CheckType, List<NCPHook>> hooksByChecks = new HashMap<CheckType, List<NCPHook>>();

    private static Comparator<NCPHook> HookComparator = new Comparator<NCPHook>() {
        @Override
        public int compare(final NCPHook o1, final NCPHook o2) {
            final boolean s1 = o1 instanceof IStats;
            final boolean f1 = o1 instanceof IFirst;
            final boolean l1 = o1 instanceof ILast;
            final boolean s2 = o2 instanceof IStats;
            final boolean f2 = o2 instanceof IFirst;
            final boolean l2 = o2 instanceof ILast;
            if      (s1 && !s2) return l1 ? 1 : -1;
            else if (!s1 && s2) return l2 ? -1 : 1;
            else if (l2)        return -1;
            else if (l1)        return 1;
            else if (f1)        return -1;
            else if (f2)        return 1;
            else                return 0;
        }
    };

    static{
        // Fill the map to be sure that thread safety can be guaranteed.
        for (final CheckType type : CheckType.values()){
            if (APIUtils.needsSynchronization(type)) hooksByChecks.put(type, Collections.synchronizedList(new ArrayList<NCPHook>()));
            else hooksByChecks.put(type, new ArrayList<NCPHook>());
        }
    }

    /**
     * Register a hook for a specific check type (all, group, or an individual check).
     * 
     * @param checkType
     *            the check type
     * @param hook
     *            the hook
     * @return an id to identify the hook, will return the existing id if the hook was already present somewhere
     */
    public static Integer addHook(final CheckType checkType, final NCPHook hook) {
        final Integer hookId = getId(hook);
        addToMappings(checkType, hook);
        logHookAdded(hook);
        return hookId;
    }

    /**
     * Register a hook for several individual checks ids (all, group, or an individual checks).
     * 
     * @param checkTypes
     *            array of check types to register the hook for. If you pass null this hook will be registered for all
     *            checks
     * @param hook
     *            the hook
     * @return the hook id
     */
    public static Integer addHook(CheckType[] checkTypes, final NCPHook hook) {
        if (checkTypes == null)
            checkTypes = new CheckType[] {CheckType.ALL};
        final Integer hookId = getId(hook);
        for (final CheckType checkType : checkTypes)
            addToMappings(checkType, hook);
        logHookAdded(hook);
        return hookId;
    }

    /**
     * Add to the mapping for given check type, no extra actions or recursion.
     * 
     * @param checkType
     *            the check type
     * @param hook
     *            the hook
     */
    private static void addToMapping(final CheckType checkType, final NCPHook hook) {
        final List<NCPHook> hooks = hooksByChecks.get(checkType);
        if (!hooks.contains(hook)){
            if (!(hook instanceof ILast) && (hook instanceof IStats || hook instanceof IFirst)) hooks.add(0, hook);
            else hooks.add(hook);
            Collections.sort(hooks, HookComparator);
        }
    }

    /**
     * Add hook to the hooksByChecks mappings.<br>
     * Assumes that the hook already has been registered in the allHooks map.
     * 
     * @param checkType
     *            the check type
     * @param hook
     *            the hook
     */
    private static void addToMappings(final CheckType checkType, final NCPHook hook) {
        if (checkType == CheckType.ALL) {
            for (final CheckType refType : CheckType.values())
                addToMapping(refType, hook);
            return;
        }
        addToMapping(checkType, hook);
        for (final CheckType refType : CheckType.values())
            addToMappingsRecursively(checkType, refType, hook);
    }

    /**
     * Add to mappings if checkType is a parent in the tree structure leading to refType.
     * 
     * @param checkType
     * @param refType
     * @param hook
     * @return If the
     */
    private static boolean addToMappingsRecursively(final CheckType checkType, final CheckType refType,
            final NCPHook hook) {
        if (refType.getParent() == null)
            return false;
        else if (refType.getParent() == checkType) {
            addToMapping(refType, hook);
            return true;
        } else if (addToMappingsRecursively(checkType, refType.getParent(), hook)) {
            addToMapping(refType, hook);
            return true;
        } else
            return false;
    }

    /**
     * Call the hooks for the specified check type and player.
     * 
     * @param checkType
     *            the check type
     * @param player
     *            the player
     * @param hooks
     *            the hooks
     * @return true, if a hook as decided to cancel the VL processing
     */
    private static final boolean applyHooks(final CheckType checkType, final Player player, final IViolationInfo info, final List<NCPHook> hooks) {
        for (int i = 0; i < hooks.size(); i++) {
            final NCPHook hook = hooks.get(i);
            try {
                if (hook.onCheckFailure(checkType, player, info) && !(hook instanceof IStats))
                    return true;
            } catch (final Throwable t) {
                // TODO: maybe distinguish some exceptions here (interrupted ?).
                logHookFailure(checkType, player, hook, t);
            }
        }
        return false;
    }

    /**
     * Get a collection of all hooks.
     * 
     * @return all the hooks
     */
    public static Collection<NCPHook> getAllHooks() {
        final List<NCPHook> hooks = new LinkedList<NCPHook>();
        hooks.addAll(allHooks.values());
        return hooks;
    }

    /**
     * Get the hook description.
     * 
     * @param hook
     *            the hook
     * @return the hook description
     */
    private static final String getHookDescription(final NCPHook hook) {
        return hook.getHookName() + " [" + hook.getHookVersion() + "]";
    }

    /**
     * Get hooks by their hook name.
     * 
     * @param hookName
     *            case sensitive (exact match)
     * @return the collection of NCP hooks matching the hook name
     */
    public static Collection<NCPHook> getHooksByName(final String hookName) {
        final List<NCPHook> hooks = new LinkedList<NCPHook>();
        for (final Integer refId : allHooks.keySet()) {
            final NCPHook hook = allHooks.get(refId);
            if (hook.getHookName().equals(hookName) && !hooks.contains(hook))
                hooks.add(hook);
        }
        return hooks;
    }

    /**
     * For registration purposes only.
     * 
     * @param hook
     *            the hook
     * @return unique id associated with that hook (returns an existing id if hook is already present)
     */
    private static Integer getId(final NCPHook hook) {
        if (hook == null)
            // Just in case.
            throw new NullPointerException("Hooks must not be null.");
        Integer id = null;
        for (final Integer refId : allHooks.keySet())
            if (hook == allHooks.get(refId)) {
                id = refId;
                break;
            }
        if (id == null) {
            id = getNewHookId();
            allHooks.put(id, hook);
        }
        return id;
    }

    /**
     * Gets the new hook id.
     * 
     * @return the new hook id
     */
    private static Integer getNewHookId() {
        maxHookId++;
        return maxHookId;
    }

    /**
     * Log that a hook was added.
     * 
     * @param hook
     *            the hook
     */
    private static final void logHookAdded(final NCPHook hook) {
        NCPAPIProvider.getNoCheatPlusAPI().getLogManager().info(Streams.STATUS, "Added hook: " + getHookDescription(hook) + ".");
    }

    /**
     * Log that a hook failed.
     * 
     * @param checkType
     *            the check type
     * @param player
     *            the player
     * @param hook
     *            the hook
     * @param throwable
     *            the throwable
     */
    private static final void logHookFailure(final CheckType checkType, final Player player, final NCPHook hook, final Throwable t) {
        // TODO: might accumulate failure rate and only log every so and so seconds or disable hook if spamming (leads
        // to NCP spam though)?
        final StringBuilder builder = new StringBuilder(1024);
        builder.append("Hook " + getHookDescription(hook) + " encountered an unexpected exception:\n");
        builder.append("Processing: ");
        if (checkType.getParent() != null) {
            builder.append("Parent " + checkType.getParent() + " ");
        }
        builder.append("Check " + checkType);
        builder.append(" Player " + player.getName());
        builder.append("\n");
        builder.append("Exception (" + t.getClass().getSimpleName() + "): " + t.getMessage() + "\n");
        for (final StackTraceElement el : t.getStackTrace()) {
            builder.append(el.toString());
        }
        NCPAPIProvider.getNoCheatPlusAPI().getLogManager().severe(Streams.STATUS, builder.toString());
    }

    /**
     * Log that a hook was removed.
     * 
     * @param hook
     *            the hook
     */
    private static final void logHookRemoved(final NCPHook hook) {
        NCPAPIProvider.getNoCheatPlusAPI().getLogManager().info(Streams.STATUS, "Removed hook: " + getHookDescription(hook) + ".");
    }

    /**
     * Removes all the hooks.
     * 
     * @return the collection
     */
    public static Collection<NCPHook> removeAllHooks() {
        final Collection<NCPHook> hooks = getAllHooks();
        for (final NCPHook hook : hooks)
            removeHook(hook);
        return hooks;
    }

    /**
     * Remove from internal mappings, both allHooks and hooksByChecks.
     * 
     * @param hook
     *            the hook
     * @param hookId
     *            the hook id
     */
    private static void removeFromMappings(final NCPHook hook, final Integer hookId) {
        allHooks.remove(hookId);
        for (final CheckType checkId : hooksByChecks.keySet()) {
            hooksByChecks.get(checkId).remove(hook);
        }
    }

    /**
     * Remove a hook by its hook id (returned on adding hooks).
     * 
     * @param hookId
     *            if present, null otherwise
     * @return the NCP hook
     */
    public static NCPHook removeHook(final Integer hookId) {
        final NCPHook hook = allHooks.get(hookId);
        if (hook == null)
            return null;
        removeFromMappings(hook, hookId);
        logHookRemoved(hook);
        return hook;
    }

    /**
     * Remove a hook.
     * 
     * @param hook
     *            the hook
     * @return hook id if present, null otherwise
     */
    public static Integer removeHook(final NCPHook hook) {
        Integer hookId = null;
        for (final Integer refId : allHooks.keySet())
            if (hook == allHooks.get(refId)) {
                hookId = refId;
                break;
            }
        if (hookId == null)
            return null;
        removeFromMappings(hook, hookId);
        logHookRemoved(hook);
        return hookId;
    }

    /**
     * Remove a collection of hooks.
     * 
     * @param hooks
     *            the hooks
     * @return a set of the removed hooks ids, same order as the given collection (hooks).
     */
    public static Set<Integer> removeHooks(final Collection<NCPHook> hooks) {
        final Set<Integer> ids = new LinkedHashSet<Integer>();
        for (final NCPHook hook : hooks) {
            final Integer id = removeHook(hook);
            if (id != null)
                ids.add(id);
        }
        return ids;
    }

    /**
     * Remove hooks by their name (case sensitive, exact match).
     * 
     * @param hookName
     *            the hook name
     * @return the collection of NCP hooks removed
     */
    public static Collection<NCPHook> removeHooks(final String hookName) {
        final Collection<NCPHook> hooks = getHooksByName(hookName);
        if (hooks.isEmpty())
            return null;
        removeHooks(hooks);
        return hooks;
    }

    /**
     * This is called by checks when players fail them.
     * 
     * @param checkType
     *            the check type
     * @param player
     *            the player that fails the check
     * @return if we should cancel the VL processing
     */
    public static final boolean shouldCancelVLProcessing(final ViolationData violationData) {
        // Checks for hooks registered for this event, parent groups or ALL will be inserted into the list.
        // Return true as soon as one hook returns true. Test hooks, if present.
        final CheckType type = violationData.check.getType();
        final List<NCPHook> hooksCheck = hooksByChecks.get(type);
        if (!hooksCheck.isEmpty()){
            if (APIUtils.needsSynchronization(type)){
                synchronized (hooksCheck) {
                    return applyHooks(type, violationData.player, violationData, hooksCheck);
                }
            }
            else{
                return applyHooks(type, violationData.player, violationData, hooksCheck);
            }
        }   
        return false;
    }
}
