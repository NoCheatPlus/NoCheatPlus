package fr.neatmonster.nocheatplus.checks.access;

import fr.neatmonster.nocheatplus.actions.ParameterHolder;

/**
 * Access interface for extended information about violations.
 * @author mc_dev
 *
 */
public interface IViolationInfo extends ParameterHolder{
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

    }
