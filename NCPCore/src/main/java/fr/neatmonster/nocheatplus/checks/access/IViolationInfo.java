package fr.neatmonster.nocheatplus.checks.access;

import fr.neatmonster.nocheatplus.actions.ParameterHolder;

/**
 * Access interface for extended information about violations.
 * 
 * @author mc_dev
 *
 */
public interface IViolationInfo extends ParameterHolder {

    /**
     * Get the violation level just added by this violation.
     * 
     * @return
     */
    public double getAddedVl();

    /**
     * Get the total violation level the player has right now. This is not the
     * value shown with "/ncp info <player>", but the value used for actions.
     * 
     * @return
     */
    public double getTotalVl();

    /**
     * Check if a cancel would happen.
     * 
     * @return
     * @deprecated The concept of cancel has been changed to be contained in
     *             penalties, use willCancel instead.
     */
    public boolean hasCancel();

    /**
     * Test, if the evaluation of penalties will lead to canceling. This may be
     * overridden, if violation processing is cancelled by a hook.
     * 
     * @return
     */
    public boolean willCancel();

    /**
     * Test if there is any instances of (Generic)LogAction set in the
     * applicable actions.
     * 
     * @return
     */
    public boolean hasLogAction();

}
