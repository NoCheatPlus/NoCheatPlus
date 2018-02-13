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
 * Basic implementation to have a quick way to interact, customizable secret
 * locking supporting class type checks by default. This implementation is not
 * meant to be secure against an attacker, it's meant to help preventing
 * re-registration or accidental API-misuse.
 * <hr>
 * This implementation is not thread-safe (for locking).
 * 
 * @author asofold
 *
 */
public class BasicLockable implements ILockable {

    /*
     * TODO: Consider switching to a settings class with chaining (then
     * translate into flags for BasicLockable).
     */

    private static final int ALLOW_LOCK_NOSECRET = 0x01;
    private static final int ALLOW_LOCK_SECRET = 0x02;
    private static final int ALLOW_UNLOCK_SECRET = 0x04;
    /** Use identity for a set secret. */
    private static final int SECRET_IDENTITY = 0x08;
    private static final int EXACT_SECRET_TYPE = 0x10;
    private static final int REMOVE_SECRET_UNLOCK = 0x20;

    private boolean isLocked = false;
    private final int lockFlags;
    private final Class<?> lockSecretType;
    private Object lockSecret = null;

    /**
     * Start unlocked with no restrictions. A set secret will be removed on
     * unlock.
     */
    public BasicLockable() {
        this(true, true, true, false, true, void.class, false);
    }

    /**
     * Start with a specific secret set, but with no further restrictions
     * applying. Allows permanent locking. The secret is removed with unlock.
     * 
     * @param secret
     *            May be null, but note that a null secret with isLocked set,
     *            yields a permanently locked instance.
     * @param secretIdentity
     * @param isLocked
     */
    public BasicLockable(Object secret, boolean secretIdentity, boolean isLocked) {
        this(true, true, true, secretIdentity, true, void.class, false);
        this.lockSecret = secret;
        this.isLocked = isLocked;
    }

    /**
     * Lock down to the class of the given secret, set the secret from start.
     * Does not allow permanent locking. The secret is removed with unlock.
     * 
     * @param secret
     * @param secretIdentity
     * @param exactSecretType
     * @param isLocked
     */
    public BasicLockable(Object secret, boolean secretIdentity, boolean exactSecretType, boolean isLocked) {
        this(false, true, true, secretIdentity, true, secret.getClass(), exactSecretType);
        this.lockSecret = secret;
        this.isLocked = isLocked;
    }

    /**
     * Lock/Unlock with a secret only. Permanent locking is not supported. The
     * secret is removed with unlock.
     * 
     * @param secretIdentity
     * @param secretType
     * @param exactSecretType
     */
    public BasicLockable(boolean secretIdentity, Class<?> secretType, boolean exactSecretType) {
        this(false, true, true, secretIdentity, true, secretType, exactSecretType);
    }

    /**
     * Basic constructor.
     * 
     * @param allowLockNoSecret
     * @param allowLockSecret
     * @param allowUnlockSecret
     * @param secretIdentity
     * @param secretType
     *            Set to void.class in order to ignore this option. Setting to
     *            null yields an IllegalArgumentException.
     * @param exactSecretType
     * @throws IllegalArgumentException
     *             In case secretType is null.
     */
    public BasicLockable(boolean allowLockNoSecret, boolean allowLockSecret, 
            boolean allowUnlockSecret, boolean secretIdentity, boolean removeSecretUnlock,
            Class<?> secretType, boolean exactSecretType) {
        if (secretType == null) {
            throw new IllegalArgumentException("Can't pass null for secretType, use void.class instead, to ignore this option.");
        }
        this.lockSecretType = secretType;
        int lockFlags = setLockFlag(ALLOW_LOCK_NOSECRET, allowLockNoSecret, 0x00);
        lockFlags = setLockFlag(ALLOW_LOCK_SECRET, allowLockSecret, lockFlags);
        lockFlags = setLockFlag(ALLOW_UNLOCK_SECRET, allowUnlockSecret, lockFlags);
        lockFlags = setLockFlag(SECRET_IDENTITY, secretIdentity, lockFlags);
        lockFlags = setLockFlag(REMOVE_SECRET_UNLOCK, removeSecretUnlock, lockFlags);
        lockFlags = setLockFlag(EXACT_SECRET_TYPE, exactSecretType, lockFlags);
        this.lockFlags = lockFlags;
    }

    private final int setLockFlag(int flag, boolean value, int result) {
        if (value) {
            return result | flag;
        }
        else {
            return result & ~flag;
        }
    }

    private final boolean isLockFlagSet(int flag) {
        return (lockFlags & flag) == flag;
    }

    /**
     * 
     * @param givenSecret
     * @param isUnlock
     * @return
     */
    private final boolean isApplicableLockSecret(final Object givenSecret, final boolean isUnlock) {
        // Secret check.
        if (this.lockSecret == null) {
            // Check if is permanently locked.
            if (this.isLocked) {
                return false;
            }
        }
        else {
            if (isLockFlagSet(SECRET_IDENTITY)) {
                if (this.lockSecret != givenSecret) {
                    return false;
                }
            }
            else if (!this.lockSecret.equals(givenSecret)) {
                return false;
            }
        }
        // Type check.
        if (lockSecretType != void.class) {
            final Class<?> type = givenSecret.getClass();
            if (isLockFlagSet(EXACT_SECRET_TYPE)) {
                if (this.lockSecretType != type) {
                    return false;
                }
            }
            else if (!this.lockSecretType.isAssignableFrom(type)) {
                return false;
            }
        }
        return furtherLockingRestrictions(givenSecret, isUnlock);
    }

    /**
     * Called upon successful access checks (lock/unlock allowed so far). Note
     * that this is called for lock() with givenSecret set to null - otherwise a
     * given null secret would yield an IllegalArgumentException.
     * <hr>
     * Override for functionality (the default implementation always returns
     * true).
     * 
     * @param givenSecret
     * @param isUnlock
     * @return True to allow lock/unlock, false to prevent.
     * @throws IllegalStateException
     *             If custom side conditions are not met, and throwing
     *             IllegalStateException is preferred over an
     *             IllegalArgumentException.
     */
    protected boolean furtherLockingRestrictions(final Object givenSecret, final boolean isUnlock) {
        return true;
    }

    @Override
    public final void lock() {
        if (!isLockFlagSet(ALLOW_LOCK_NOSECRET)) {
            throw new UnsupportedOperationException();
        }
        if (!furtherLockingRestrictions(null, false)) {
            throw new IllegalStateException();
        }
        isLocked = true;
        // Change to permanently locked.
        this.lockSecret = null;
    }

    @Override
    public final void lock(final Object secret) {
        if (!isLockFlagSet(ALLOW_LOCK_SECRET)) {
            throw new UnsupportedOperationException();
        }
        if (secret == null || !isApplicableLockSecret(secret, false)) {
            throw new IllegalArgumentException();
        }
        this.lockSecret = secret;
        isLocked = true;
    }

    @Override
    public final void unlock(final Object secret) {
        if (!isLockFlagSet(ALLOW_UNLOCK_SECRET)) {
            throw new UnsupportedOperationException();
        }
        if (secret == null || !isApplicableLockSecret(secret, true)) {
            throw new IllegalArgumentException();
        }
        if (!isLocked) {
            return;
        }
        if (isLockFlagSet(REMOVE_SECRET_UNLOCK)) {
            this.lockSecret = null;
        }
        isLocked = false;
    }

    @Override
    public boolean isLocked() {
        return isLocked;
    }

    @Override
    public void throwIfLocked() {
        if (isLocked) {
            throw new IllegalStateException();
        }
    }

}
