package fr.neatmonster.nocheatplus.checks;

/**
 * Access interface for extended information about violations.
 * @author mc_dev
 *
 */
public interface IViolationInfo {
    /**
     * Get the violation level just added by this violation.
     * @return
     */
    public double getAddedVl();
    /**
     * Get the total violation level the player has right now. This is not the value shown with "/ncp info <player>", but the value used for actions.
     * @return
     */
    public double getTotalVl();
    /**
     * Check if the actions contain a cancel action.
     * @return
     */
    boolean hasCancel();
    /**
     * Check if any of the actions needs parameters.
     * @return If true, actions are likely to contian command or logging actions.
     */
    boolean needsParameters();
    }
