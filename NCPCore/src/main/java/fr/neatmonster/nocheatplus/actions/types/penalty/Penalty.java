package fr.neatmonster.nocheatplus.actions.types.penalty;

import org.bukkit.entity.Player;

/**
 * A simple static penalty. If this penalty applies must be determined
 * externally (PenaltyAction).
 * 
 * @author asofold
 *
 */
public interface Penalty {

    /**
     * Effects that apply only to a player. Usually executed on
     * 
     * @return
     */
    public boolean hasPlayerEffects();

    /**
     * Test if there are input-specific effects, other than with Player instance
     * input.
     * 
     * @return If true, this instance must implement InputSpecificPenalty as
     *         well. Applying input specific penalties might only be possible
     *         within the surrounding context of creation of ViolationData, i.e.
     *         during the event handling. Input-specific effects will not apply
     *         within ViolationData.executeActions, be it within the TickTask
     *         (requestActionsExecution) or during handling a primary-thread
     *         check failure.
     */
    public boolean hasInputSpecificEffects();

    /**
     * Apply player-specific effects. Executed within
     * ViolationData.executeActions, extra to input-specific effects (likely
     * before those, if within the primary thread, or within the TickTask for
     * off-primary-thread checks).
     * 
     * @param player
     */
    public void apply(Player player);

}
