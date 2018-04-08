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
package fr.neatmonster.nocheatplus.actions.types.penalty;

import java.util.Collection;
import java.util.Random;

/**
 * Internal data representation, managing probabilities, and complex decisions
 * with multiple penalties.
 * 
 * @author asofold
 *
 */
public class PenaltyNode {

    // TODO: Might switch to float for probability or not?

    // TODO: Might add a parsing method (recursive).


    /** Random instance to use. May be null, in case probability is 1. */
    private final Random random;
    /** The probability for this node to apply. */
    public final double probability;
    /** Penalty to apply when this node applies. */
    private final Penalty<?> penalty;
    /** Child nodes to test when this node applies. */
    private final PenaltyNode[] childNodes;
    /** Indicate that the result is set with the first child node that applies. */
    private final boolean abortOnApply;

    /**
     * Convenience: Simple penalty that always applies with no child nodes.
     * @param random
     * @param probability
     * @param penalty
     */
    public PenaltyNode(Random random, Penalty<?> penalty) {
        this(random, 1.0, penalty, null, false);
    }

    /**
     * Convenience: Simple penalty with no child nodes.
     * @param random
     * @param probability
     * @param penalty
     */
    public PenaltyNode(Random random, double probability, Penalty<?> penalty) {
        this(random, probability, penalty, null, false);
    }

    /**
     * 
     * @param random
     * @param probability
     * @param penalty
     * @param childNodes
     *            May be null.
     * @param abortOnApply
     */
    public PenaltyNode(Random random, double probability, Penalty<?> penalty, Collection<PenaltyNode> childNodes, boolean abortOnApply) {
        this.random = random;
        this.probability = probability;
        this.penalty = penalty;
        this.childNodes = childNodes == null ? new PenaltyNode[0] : childNodes.toArray(new PenaltyNode[childNodes.size()]);
        this.abortOnApply = abortOnApply;
    }

    /**
     * On the spot evaluation of an applicable path, filling in all applicable
     * penalties into the results collection. This does test
     * 
     * @param results
     * @return If this node applies (, which does not necessarily mean that
     *         anything has been appended to results).
     */
    public boolean evaluate(final IPenaltyList results) {
        if (probability < 1.0 && random.nextDouble() > probability) {
            // This node does not apply
            return false;
        }
        if (penalty != null) {
            penalty.addToPenaltyList(results);
        }
        for (int i = 0 ; i < childNodes.length; i++) {
            if (childNodes[i].evaluate(results) && abortOnApply) {
                break;
            }
        }
        return true;
    }

}
