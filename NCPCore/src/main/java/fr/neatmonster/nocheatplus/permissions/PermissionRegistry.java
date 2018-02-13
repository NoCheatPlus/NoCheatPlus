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
package fr.neatmonster.nocheatplus.permissions;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import fr.neatmonster.nocheatplus.components.registry.exception.AlreadyRegisteredException;
import fr.neatmonster.nocheatplus.components.registry.exception.NotRegisteredException;
import fr.neatmonster.nocheatplus.utilities.ds.map.HashMapLOW;

/**
 * Keep registry information for permissions.
 * @author asofold
 *
 */
public class PermissionRegistry {

    // TODO: Interface / interfaces: consider read-only access IPermissionInfo, IPermissionPolicy etc.

    // TODO: Suffix permissions, because it's tedious to define constants for them. Might auto register within somewhere else.

    /*
     * TODO: Per world (rule/proxy) registries, with one central registry for
     * ids (per-world registries would proxy id registration, but have their own
     * rule settings). THUS, have the rule registry as a common super class, an
     * interface for the full thing.
     */

    private int nextId;
    private final Lock lock = new ReentrantLock();
    private PermissionSettings settings = new PermissionSettings(null, null, new PermissionPolicy());
    private final HashMapLOW<Integer, PermissionInfo> infosInt = new HashMapLOW<Integer, PermissionInfo>(lock, 100);
    /** No need to map to the strings in an extra step here. */
    private final HashMapLOW<String, PermissionInfo> infosString = new HashMapLOW<String, PermissionInfo>(lock, 100);

    // TODO: Might do lazy tasks for all player data regularly.
    /**
     * All registered permissions that are meant to be kept updated for players.
     * Guarantees are not to actually keep them updated, but might lazily update
     * them with world changing and logging on.
     */
    private final LinkedHashSet<RegisteredPermission> preferKeepUpdated = new LinkedHashSet<RegisteredPermission>();
    private RegisteredPermission[] preferKeepUpdatedWorld = new RegisteredPermission[0];
    private RegisteredPermission[] preferKeepUpdatedOffline = new RegisteredPermission[0];

    /**
     * 
     * @param nextId Next id to return with getId.
     */
    public PermissionRegistry(int nextId) {
        this.nextId = nextId;
    }

    /**
     * Internal put, must call under lock.
     * 
     * @param registeredPermission
     * @return
     */
    private PermissionInfo internalPut(final RegisteredPermission registeredPermission) {
        final PermissionInfo info = new PermissionInfo(registeredPermission);
        info.set(settings.getPermissionPolicy(registeredPermission));
        infosInt.put(registeredPermission.getId(), info);
        infosString.put(registeredPermission.getLowerCaseStringRepresentation(), info);
        return info;
    }

    /**
     * Convenience for setup: Add a preset permission. Ids should be below the
     * nextId passed in the constructor.
     * 
     * @param registeredPermission
     * @throws AlreadyRegisteredException
     *             If the id or the stringRepresentation are already registered.
     */
    public void addRegisteredPermission(final RegisteredPermission registeredPermission) {
        lock.lock();
        if (infosString.containsKey(registeredPermission.getLowerCaseStringRepresentation())) {
            lock.unlock();
            throw new AlreadyRegisteredException("String representation already registered: " 
                    + registeredPermission.getLowerCaseStringRepresentation());
        }
        if (infosInt.containsKey(registeredPermission.getId())) {
            lock.unlock();
            throw new AlreadyRegisteredException("Id already registered: " + registeredPermission.getId());
        }
        internalPut(registeredPermission);
        lock.unlock();
    }

    /**
     * Get a registered id or create a new id and PermissionInfo instance
     * (without invalidation policy set, etc.).Meant for setup, not for frequent
     * queries.
     * 
     * @param stringRepresentation
     * @return
     */
    public Integer getId(final String stringRepresentation) {
        return getOrCreatePermissionInfo(stringRepresentation).getRegisteredPermission().getId();
    }

    /**
     * 
     * @param id
     * @return A registered PermissionInfo instance, or null, in case there is
     *         no registration for that id.
     */
    public PermissionInfo getPermissionInfo(final Integer id) {
        return infosInt.get(id);
    }

    /**
     * 
     * @param stringRepresentation
     * @return Note that storage is case insensitive, thus the
     *         stringRepresentation registered first always stays in storage.
     */
    public PermissionInfo getOrCreatePermissionInfo(final String stringRepresentation) {
        PermissionInfo info = infosString.get(RegisteredPermission.toLowerCaseStringRepresentation(stringRepresentation));
        if (info == null) {
            lock.lock();
            // Must check again (asynchronicity).
            info = infosString.get(RegisteredPermission.toLowerCaseStringRepresentation(stringRepresentation));
            if (info != null) {
                lock.unlock();
                return info;
            }
            final RegisteredPermission registeredPermission;
            try {
                registeredPermission = new RegisteredPermission(nextId, stringRepresentation);
            }
            catch (NullPointerException e) {
                lock.unlock();
                throw e;
            }
            info = internalPut(registeredPermission);
            lock.unlock();
        }
        return info;
    }

    public RegisteredPermission getOrRegisterPermission(final String stringRepresentation) {
        return getOrCreatePermissionInfo(stringRepresentation).getRegisteredPermission();
    }

    /**
     * (Primary thread only.)
     * 
     * @param settings
     * @return All registered permissions for which the policy has changed (by
     *         checking isPolicyEquivalent).
     */
    public Set<RegisteredPermission> updateSettings(final PermissionSettings settings) {
        final Set<RegisteredPermission> changed = new LinkedHashSet<RegisteredPermission>();
        // Ensure outdated policies don't get applied from here on.
        lock.lock(); 
        this.settings = settings;
        lock.unlock();
        // Since we can't know rule changes at this stage, all have to be updated.
        // (Lazy iteration, we'll hit all previously registered ones. Asynchronous registration shouldn't happen anyway.)
        final Iterator<Entry<Integer, PermissionInfo>> it = infosInt.iterator();
        while (it.hasNext()) {
            final PermissionInfo info = it.next().getValue();
            final PermissionPolicy newPolicy = settings.getPermissionPolicy(info.getRegisteredPermission());
            if (!info.isPolicyEquivalent(newPolicy)) {
                changed.add(info.getRegisteredPermission());
            }
            // Still set in either case, in case we missed something (cheap).
            info.set(newPolicy);
        }
        arrangePreferKeepUpdated();
        return changed;
    }

    /**
     * (Primary thread only.)
     */
    public void arrangePreferKeepUpdated() {
        final List<RegisteredPermission> preferKeepUpdatedWorld = new LinkedList<RegisteredPermission>();
        final List<RegisteredPermission> preferKeepUpdatedOffline = new LinkedList<RegisteredPermission>();
        // (No permanent updating yet.)
        for (final RegisteredPermission registeredPermission : this.preferKeepUpdated) {
            final PermissionInfo info = infosInt.get(registeredPermission.getId());
            switch (info.fetchingPolicy()) {
                case FALSE:
                case TRUE:
                    // Skip only these.
                    continue;
                case ALWAYS:
                case INTERVAL:
                    // Update as often as makes sense in this context.
                    // TODO: Might later run lazy tasks permanently for online players.
                    preferKeepUpdatedOffline.add(registeredPermission);
                    preferKeepUpdatedWorld.add(registeredPermission);
                    break;
                default:
                    if (info.invalidationOffline()) {
                        preferKeepUpdatedOffline.add(registeredPermission);
                    }
                    else if (info.invalidationWorld()) { // TODO: 'else' as long as world includes offline.
                        preferKeepUpdatedOffline.add(registeredPermission); // TODO: as long as world includes offline.
                        preferKeepUpdatedWorld.add(registeredPermission);
                    }
                    break;
            }
        }
        this.preferKeepUpdatedWorld = preferKeepUpdatedWorld.toArray(new RegisteredPermission[preferKeepUpdatedWorld.size()]);
        this.preferKeepUpdatedOffline = preferKeepUpdatedOffline.toArray(new RegisteredPermission[preferKeepUpdatedOffline.size()]);
    }

    /**
     * Permissions will be sorted into areas, depending on the set policies.
     * However that will only happen with either calling
     * arrangePreferKeepUpdated() or updateSettings(PermissionSettings).
     * 
     * @param registeredPermissions
     */
    public void preferKeepUpdated(final RegisteredPermission... registeredPermissions) {
        for (final RegisteredPermission registeredPermission : registeredPermissions) {
            final PermissionInfo info = infosInt.get(registeredPermission.getId());
            if (info == null) {
                throw new NotRegisteredException("Id not registered: " + registeredPermission.getId());
            }
            if (info.getRegisteredPermission() != registeredPermission) {
                throw new AlreadyRegisteredException("RegisteredPermission instances should be identical.");
            }
            preferKeepUpdated.add(info.getRegisteredPermission()); // Add the already registered object.
        }
    }

    public RegisteredPermission[] getPreferKeepUpdatedWorld() {
        return preferKeepUpdatedWorld;
    }

    public RegisteredPermission[] getPreferKeepUpdatedOffline() {
        return preferKeepUpdatedOffline;
    }

}
