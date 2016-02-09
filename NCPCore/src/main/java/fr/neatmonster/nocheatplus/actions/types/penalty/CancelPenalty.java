package fr.neatmonster.nocheatplus.actions.types.penalty;

import org.bukkit.entity.Player;

/**
 * This penalty does nothing. It's presence solely indicates that an action is
 * to be canceled or rolled back.
 * 
 * @author asofold
 *
 */
public final class CancelPenalty implements Penalty {
    
    // TODO: Consider putting a static instance somewhere instead (check with ==).

    @Override
    public final boolean hasPlayerEffects() {
        return false;
    }

    @Override
    public final boolean hasInputSpecificEffects() {
        return false;
    }

    @Override
    public final void apply(Player player) {
    }

}
