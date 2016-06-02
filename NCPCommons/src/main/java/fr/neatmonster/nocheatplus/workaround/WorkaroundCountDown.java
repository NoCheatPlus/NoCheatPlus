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
import fr.neatmonster.nocheatplus.utilities.ds.count.acceptdeny.IResettableAcceptDenyCounter;

/**
 * Count down to 0, use is only possible if the currentCounter is greater than
 * 0. An initialCounter of 0 together with setting the currentCount manually,
 * allows to activate a workaround on base of a precondition. Also keep track of
 * denyUseCount (all time + stage). Parent counters are not supported for the
 * stage counter.
 * 
 * @author asofold
 *
 */
public class WorkaroundCountDown extends AbstractWorkaround implements IStagedWorkaround {

    /** The start value for the count down. */
    private final int initialCount;
    /** The current state of the count down. */
    private int currentCount;
    /** Counter for the current stage, resetting with resetConditions. */
    private final IResettableAcceptDenyCounter stageCounter;

    /**
     * 
     * @param id
     * @param initialCount
     */
    public WorkaroundCountDown(String id, int initialCount) {
        super(id);
        this.initialCount = initialCount;
        this.currentCount = initialCount;
        this.stageCounter =  new AcceptDenyCounter();
    }

    /**
     * Set the current count down value.
     * @param currentCount
     */
    public void setCurrentCount(int currentCount) {
        this.currentCount = currentCount;
    }

    /**
     * Get the current count down value. 
     */
    public int getCurrentCount() {
        return currentCount;
    }

    @Override
    public void resetConditions() {
        currentCount = initialCount;
        stageCounter.resetCounter();
    }

    @Override
    public WorkaroundCountDown getNewInstance() {
        return setParentCounters(new WorkaroundCountDown(getId(), initialCount));
    }

    @Override
    public IAcceptDenyCounter getStageCounter() {
        return stageCounter;
    }

    @Override
    public boolean testUse(final boolean isUse) {
        if (!isUse) {
            return currentCount > 0;
        }
        else {
            if (currentCount > 0) {
                currentCount --;
                stageCounter.accept();
                return true;
            } else {
                stageCounter.deny();
                return false;
            }
        }
    }

}
