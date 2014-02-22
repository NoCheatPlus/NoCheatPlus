package fr.neatmonster.nocheatplus.hooks;

import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.access.IViolationInfo;

/**
 * Extend this class for maximum future compatibility.<br>
 * Especially the onCheckFailure method might get extended with check specific arguments, this class will provide
 * compatibility with older method signatures, where possible.
 * 
 * @author asofold
 */
public abstract class AbstractNCPHook implements NCPHook {
    /**
     * 
     * @deprecated See new signature in NCPHook.
     * @param checkType
     * @param player
     * @return
     */
    public boolean onCheckFailure(CheckType checkType, Player player){
        // Implemented because of API change.
        return false;
    }

    @Override
    public boolean onCheckFailure(final CheckType checkType, final Player player, final IViolationInfo info) {
        // Kept for compatibility reasons.
        return onCheckFailure(checkType, player);
    }
    
}