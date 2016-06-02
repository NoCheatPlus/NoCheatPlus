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

import fr.neatmonster.nocheatplus.utilities.ds.count.acceptdeny.IAcceptDenyCounter;

/**
 * Provide a means of controlling when workarounds should be able to apply,
 * enabling preconditions, mid-to-long-term side-conditions, as well as
 * statistics for how often a workaround has been used.
 * <hr>
 * The method use() must be called after all other preconditions have been met
 * for that (stage of) workaround, so that success means that the (stage of)
 * workaround does apply. The method canUse can be used to test if the
 * workaround would apply, aimed at cases where that is better performance-wise.
 * 
 * @author asofold
 *
 */
public interface IWorkaround {

    // TODO: Add setEnabled() ? Allow to configure workarounds (-> IConfigurableThing).

    public String getId();

    /**
     * Attempt to use the workaround, considering all preconditions and
     * side-conditions set. This will increase the accept counter in case of
     * returning true, or increase the deny counter in case of returning false.
     * It might also alter/use other counters based on the value to be returned
     * (stage counter, parent counter, other side conditions).
     * 
     * @return If actually can be used.
     */
    public boolean use();

    /**
     * Test if this (stage of) workaround would apply. This must not have any
     * side effect nor change any data. Intended use is a fast-denial check, to
     * be performed before other heavier to check pre-conditions are tested, as
     * well as being called from within use().
     * 
     * @return If a call returns false, use() must also return false (until
     *         conditions are changed). If a call returns true, future
     *         implementations might still return false on use().
     */
    public boolean canUse();
    
    /**
     * Retrieve the counter used for acceptance and denial with calling use().
     * This counter may or may not have a parent counter.
     * 
     * @return
     */
    public IAcceptDenyCounter getAllTimeCounter();

    /**
     * Serving as factory, retrieve a new instance of the same kind, in the
     * default state (not clone). Counters should be new counter instances,
     * except for parents.
     * 
     * @return
     */
    public IWorkaround getNewInstance();

}
