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
    private final Penalty penalty;
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
    public PenaltyNode(Random random, Penalty penalty) {
        this(random, 1.0, penalty, null, false);
    }

    /**
     * Convenience: Simple penalty with no child nodes.
     * @param random
     * @param probability
     * @param penalty
     */
    public PenaltyNode(Random random, double probability, Penalty penalty) {
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
    public PenaltyNode(Random random, double probability, Penalty penalty, Collection<PenaltyNode> childNodes, boolean abortOnApply) {
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
    public boolean evaluate(final Collection<Penalty> results) {
        if (probability < 1.0 && random.nextDouble() > probability) {
            // This node does not apply
            return false;
        }
        if (penalty != null) {
            results.add(penalty);
        }
        for (int i = 0 ; i < childNodes.length; i++) {
            if (childNodes[i].evaluate(results) && abortOnApply) {
                break;
            }
        }
        return true;
    }

}
