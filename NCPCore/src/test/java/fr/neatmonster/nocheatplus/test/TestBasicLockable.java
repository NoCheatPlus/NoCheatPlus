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
package fr.neatmonster.nocheatplus.test;

import static org.junit.Assert.fail;

import org.junit.Test;

import fr.neatmonster.nocheatplus.components.registry.lockable.BasicLockable;
import fr.neatmonster.nocheatplus.components.registry.lockable.ILockable;

public class TestBasicLockable {

    static class Dummy {

        Dummy equalsOther;

        Dummy() {
        }

        Dummy(Dummy equalsOther) {
            this.equalsOther = equalsOther;
        }

        @Override
        public boolean equals(Object obj) {
            return obj == this || this.equalsOther != null && this.equalsOther == obj;
        }

    }

    static class DummMY extends Dummy {}

    private BasicLockable getLocked() {
        BasicLockable lock = new BasicLockable();
        try {
            lock.lock();
        }
        catch (Exception e) {
            fail("lock() should work here");
        }
        return lock;
    }

    /**
     * Attempt to lock an already permanently locked item with a secret.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testFailChangeLockNoSecret() {
        BasicLockable lock = getLocked();
        lock.lock(new Object());
    }

    /**
     * Lock twice permanently (expect no exception).
     */
    @Test
    public void testLockTwice() {
        BasicLockable lock = getLocked();
        lock.lock();
    }

    @Test
    public void testPermanentLockOverrideSecret() {
        BasicLockable lock = new BasicLockable();
        lock.lock(lock);
        lock.lock();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSecretTypeIdentity() {
        BasicLockable lock = new BasicLockable(new Dummy(), true, true);
        lock.lock(new Dummy());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSecretTypeExactFail1() {
        BasicLockable lock = new BasicLockable(new Dummy(), false, true, false);
        lock.lock(new Dummy());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSecretTypeExactFail2() {
        BasicLockable lock = new BasicLockable(new Dummy(), false, true, false);
        lock.lock(new DummMY());
    }

    @Test
    public void testSecretTypeSubClass() {
        BasicLockable lock = new BasicLockable(true, Dummy.class, false);
        lock.lock(new DummMY());
    }

    private void checkIsLocked(ILockable lockable, boolean expected) {
        if (lockable.isLocked() ^ expected) {
            fail("Expect lock to be " + (expected ? "locked" : "not locked") + ", instead it's " + (expected ? "not locked." : "locked."));
        }
    }

    @Test
    public void testConstructorsIsLocked() {
        // Default
        checkIsLocked(new BasicLockable(), false);
        //
        checkIsLocked(new BasicLockable(new Dummy(), true, true), true);
        checkIsLocked(new BasicLockable(new Dummy(), true, false), false);
        //
        checkIsLocked(new BasicLockable(true, Dummy.class, true), false);
        //
        checkIsLocked(new BasicLockable(new Dummy(), true, true, true), true);
        checkIsLocked(new BasicLockable(new Dummy(), true, true, false), false);
        //
        checkIsLocked(new BasicLockable(true, true, true, true, true, void.class, false), false);
    }

    @Test
    public void testUnlock() {
        Dummy secret1 = new Dummy();
        Dummy secret2 = new Dummy(secret1);
        Dummy secret3 = new DummMY();
        // secretIdentity
        BasicLockable lock = new BasicLockable(secret2, true, true);
        lock.unlock(secret2);
        checkIsLocked(lock, false);
        lock.lock();
        // Unlock with different instance (equals).
        lock = new BasicLockable(false, Dummy.class, false);
        lock.lock(secret2);
        lock.unlock(secret1);
        checkIsLocked(lock, false);
        // secretTypeExact
        lock = new BasicLockable(false, Dummy.class, true);
        lock.lock(secret1);
        lock.unlock(secret1);
        lock.lock(secret2);
        lock.unlock(secret2);
        // !secretTypeExact with sub class.
        lock = new BasicLockable(false, Dummy.class, false);
        lock.lock(secret1);
        lock.unlock(secret1);
        lock.lock(secret3);
        lock.unlock(secret3);
    }

    @Test
    public void testUnlockWithSecretRemoval() {
        Dummy secret = new Dummy();
        BasicLockable lock = new BasicLockable(null, true, false);
        lock.lock(secret);
        lock.unlock(secret);
        secret = new DummMY();
        lock.lock(secret);
        lock.unlock(secret);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUnlockAfterPermanentLockFail() {
        Dummy secret = new Dummy();
        BasicLockable lock = new BasicLockable(secret, true, true);
        lock.lock();
        lock.unlock(secret);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUnlockWithWrongSecretFail() {
        Dummy secret = new Dummy();
        BasicLockable lock = new BasicLockable(secret, true, true);
        lock.unlock(new Dummy());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUnlockwithSubClassFail() {
        BasicLockable lock = new BasicLockable(false, Dummy.class, true);
        lock.lock(new Dummy());
        lock.unlock(new Dummy());
        lock.lock(new DummMY());
    }

}
