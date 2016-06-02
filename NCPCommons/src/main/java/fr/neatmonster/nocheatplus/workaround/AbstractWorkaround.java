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
package fr.neatmonster.nocheatplus.workaround;

import fr.neatmonster.nocheatplus.utilities.ds.count.acceptdeny.AcceptDenyCounter;
import fr.neatmonster.nocheatplus.utilities.ds.count.acceptdeny.IAcceptDenyCounter;
import fr.neatmonster.nocheatplus.utilities.ds.count.acceptdeny.ICounterWithParent;

/**
 * Implementing the minimum features for counting use.
 * 
 * @author asofold
 *
 */
public abstract class AbstractWorkaround implements IWorkaround {

    private final String id;
    private final AcceptDenyCounter allTimeCounter;

    /**
     * 
     * @param id
     */
    public AbstractWorkaround(String id) {
        this.id = id;
        this.allTimeCounter = new AcceptDenyCounter();
    }

    @Override
    public String getId() {
        return id;
    }

    /**
     * Check if the workaround can be used by relaying that decision to
     * testUse(false). The allTimeCounter is updated here.
     */
    @Override
    public boolean use() {
        if (testUse(true)) {
            allTimeCounter.accept();
            return true;
        }
        else {
            allTimeCounter.deny();
            return false;
        }
    }

    /**
     * Simply relay to testUse(true).
     */
    @Override
    public boolean canUse() {
        return testUse(false);
    }


    @Override
    public IAcceptDenyCounter getAllTimeCounter() {
        return allTimeCounter;
    }

    /**
     * Override to check for pre/side-conditions.
     * 
     * @param isUse
     *            If set to false, this must not alter any data, nor have any
     *            other side conditions. If set to true, this is called from
     *            within use(), meant to update stage counters and similar. Note
     *            that the allTimeCounter is updated from within use, depending
     *            on the result of calling testUse(true).
     * @return
     */
    public abstract boolean testUse(boolean isUse);

    /**
     * Override the parent counters in instance with the ones set in this
     * instance, where feasible, if possible. (By default, only the
     * allTimeCounter is checked for a parent.)
     * 
     * @param instance
     * @return The given instance.
     */
    <W extends IWorkaround> W setParentCounters(final W instance) {
        final IAcceptDenyCounter allTimeParent = this.allTimeCounter.getParentCounter();
        if (allTimeParent != null) {
            final IAcceptDenyCounter instanceAllTime = instance.getAllTimeCounter();
            if ((instance instanceof ICounterWithParent)) {
                ((ICounterWithParent) instanceAllTime).setParentCounter(allTimeParent);
            }
        }
        return instance;
    }

}
