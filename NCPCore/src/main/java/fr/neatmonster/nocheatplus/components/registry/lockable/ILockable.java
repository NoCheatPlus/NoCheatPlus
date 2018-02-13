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
package fr.neatmonster.nocheatplus.components.registry.lockable;

/**
 * An instance that allows locking, either final, or locking and unlocking via a
 * secret.
 * 
 * @author asofold
 *
 */
public interface ILockable {

    // TODO: Use own registry exceptions.

    /**
     * Final permanent locking of the item. If a secret is set, the reference
     * will be set to null, provided permanent locking is supported. Not
     * supposed to be reversible.
     * 
     * @throws UnsupportedOperationException
     *             If locking without secret is not supported in the first
     *             place.
     * @throws IllegalStateException
     *             If locking is not supported due to custom side conditions.
     */
    public void lock();

    /**
     * Lock with setting a secret. Restrictions may apply for the nature of the
     * given secret. This may lead to overriding an internally stored secret, in
     * case the secret is valid for (override) locking, concerning the state the
     * ILockable instance is in - depending on implementation.
     * 
     * @param secret
     * @throws UnsupportedOperationException
     *             If locking with a secret is not supported at this moment.
     * @throws IllegalArgumentException
     *             If the secret does not fulfill the requirements (includes
     *             null).
     */
    public void lock(Object secret);

    /**
     * Unlock using the given secret. Restrictions may apply for the nature of
     * the given secret. This may lead to removal of the internally set secret,
     * depending on implementation.
     * 
     * @param secret
     * @throws UnsupportedOperationException
     *             If unlocking with a secret is not supported at this moment.
     * @throws IllegalArgumentException
     *             If the secret does not fulfill the requirements (includes
     *             null).
     */
    public void unlock(Object secret);

    /**
     * Test if is locked.
     * 
     * @return
     */
    public boolean isLocked();

    /**
     * Convenience: throw an IllegalStateException, if already locked.
     * 
     * @throws IllegalStateException
     *             If already locked.
     */
    public void throwIfLocked();

}
