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
package fr.neatmonster.nocheatplus.actions.types;

import fr.neatmonster.nocheatplus.actions.AbstractActionList;
import fr.neatmonster.nocheatplus.actions.Action;
import fr.neatmonster.nocheatplus.actions.ActionData;
import fr.neatmonster.nocheatplus.penalties.IPenaltyList;
import fr.neatmonster.nocheatplus.penalties.PenaltyNode;

/**
 * Penalty action, referencing a certain penalty defined elsewhere. <br>
 * Contained penalties are executed if passing the probability test, possibly in
 * an uncertain order, concerning:
 * <ul>
 * <li>Cancel penalties are always directly evaluated and applied to the context
 * of checking, e.g. the event that is being processed.</li>
 * <li>
 * Player-specific penalties are always executed within
 * ViolationData.executeActions in the primary thread. Note that for
 * off-primary-thread checks, this will be within the the TickTask.</li>
 * <li>Penalties that need specific extra input might be processed in different
 * places, e.g. the event listener, likely after executeActions has been called,
 * no guarantee.</li>
 * </ul>
 * 
 * @author asofold
 *
 * @param <D>
 * @param <L>
 */
public class PenaltyAction<D extends ActionData, L extends AbstractActionList<D, L>> extends Action<D, L> {

    private final PenaltyNode rootNode;
    private final String penaltyId;

    // TODO: executesAlways +- making sense.

    /**
     * 
     * @param penaltyId
     *            If null, toString should be overridden.
     * @param rootNode
     */
    public PenaltyAction(String penaltyId, PenaltyNode rootNode) {
        // TODO: name vs toString (!).
        super("penalty", 0, 0);
        this.rootNode = rootNode;
        this.penaltyId = penaltyId;

    }

    /**
     * Fill in applicable penalties to results.
     * 
     * @param results
     */
    public void evaluate(final IPenaltyList results) {
        if (this.rootNode != null) {
            this.rootNode.evaluate(results);
        }
    }

    @Override
    public void execute(D violationData) {
        // Does nothing (!).
    }

    @Override
    public String toString() {
        return "penalty:" + penaltyId;
    }

    public PenaltyNode getPenaltyNode() {
        return rootNode;
    }

}
