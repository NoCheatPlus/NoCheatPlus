package fr.neatmonster.nocheatplus.hooks;

import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.IViolationInfo;

/*
 * M"""""""`YM MM'""""'YMM MM"""""""`YM M""MMMMM""MM                   dP       
 * M  mmmm.  M M' .mmm. `M MM  mmmmm  M M  MMMMM  MM                   88       
 * M  MMMMM  M M  MMMMMooM M'        .M M         `M .d8888b. .d8888b. 88  .dP  
 * M  MMMMM  M M  MMMMMMMM MM  MMMMMMMM M  MMMMM  MM 88'  `88 88'  `88 88888"   
 * M  MMMMM  M M. `MMM' .M MM  MMMMMMMM M  MMMMM  MM 88.  .88 88.  .88 88  `8b. 
 * M  MMMMM  M MM.     .dM MM  MMMMMMMM M  MMMMM  MM `88888P' `88888P' dP   `YP 
 * MMMMMMMMMMM MMMMMMMMMMM MMMMMMMMMMMM MMMMMMMMMMMM                            
 */
/**
 * Compatibility hooks have to implement this.<br>
 * NOTES: 
 * Some checks run asynchronously, the hooks using these also have to support processing in an extra thread, check with APIUtils.needsSynchronization(CheckType).
 * Hooks that can be called asynchronously must not register new hooks that might run asynchronously during processing (...).
 * 
 * @author asofold
 */
public interface NCPHook {

    /**
     * For logging purposes.
     * 
     * @return the hook name
     */
    public String getHookName();

    /**
     * For logging purposes.
     * 
     * @return the hook version
     */
    public String getHookVersion();

    /**
     * This is called on failure of a check.<br>
     * This is the minimal interface, it might later be extended by specific information like (target) locations and VL,
     * but with this a lot is possible already (see CNCP).<br>
     * See AbstractNCPHook for future compatibility questions.
     * 
     * @param checkType
     *            the check that failed
     * @param player
     *            the player that failed the check
     * @param info 
     *            Extended information about the violations.
     * @return if we need to cancel the check failure processing
     */
    public boolean onCheckFailure(CheckType checkType, Player player, IViolationInfo info);
}
