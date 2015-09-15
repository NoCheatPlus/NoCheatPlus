package fr.neatmonster.nocheatplus.checks.moving.model;

/**
 * Basic preset envelopes for moving off one medium.
 * 
 * @author asofold
 *
 */
public enum LiftOffEnvelope {
    /** Normal in-air lift off without any restrictions/specialties. */
    NORMAL(0.42, 1.35, 6, true),
    /** Weak or no limit moving off liquid near ground. */
    LIMIT_NEAR_GROUND(0.42, 1.35, 6, false), // TODO: 0.385 / not jump on top of 1 high wall from water.
    /** Simple calm water surface. */
    LIMIT_LIQUID(0.1, 0.27, 3, false), // TODO:
    //    /** Flowing water / strong(-est) limit. */
    //    LIMIT_LIQUID_STRONG(...), // TODO
    /** No jumping at all. */
    NO_JUMP(0.0, 0.0, 0, false); // TODO
    ;

    private double maxJumpGain;
    private double maxJumpHeight;
    private int maxJumpPhase;
    private boolean jumpEffectApplies;

    private LiftOffEnvelope(double maxJumpGain, double maxJumpHeight, int maxJumpPhase, boolean jumpEffectApplies) {
        this.maxJumpGain = maxJumpGain;
        this.maxJumpHeight = maxJumpHeight;
        this.maxJumpPhase = maxJumpPhase;
        this.jumpEffectApplies = jumpEffectApplies;
    }

    public double getMaxJumpGain(double jumpAmplifier) {
        // TODO: Count in effect level.
        if (jumpEffectApplies && jumpAmplifier != 0.0) {
            return Math.max(0.0, maxJumpGain + 0.2 * jumpAmplifier);
        }
        else {

            return maxJumpGain;
        }
    }

    public double getMaxJumpHeight(double jumpAmplifier) {
        // TODO: Count in effect level.
        if (jumpEffectApplies && jumpAmplifier > 0.0) {
            return 1.35 + 0.6 + jumpAmplifier - 1.0;
        } // TODO: < 0.0 ?
        else {

            return maxJumpHeight;
        }
    }

    public int getMaxJumpPhase(double jumpAmplifier) {
        if (jumpEffectApplies && jumpAmplifier > 0.0) {
            return (int) Math.round((0.5 + jumpAmplifier) * (double) maxJumpPhase);
        } // TODO: < 0.0 ?
        else {
            return maxJumpPhase;
        }
    }

    public boolean jumpEffectApplies() {
        return jumpEffectApplies;
    }

}
