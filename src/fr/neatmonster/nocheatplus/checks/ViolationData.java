package fr.neatmonster.nocheatplus.checks;

import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.actions.types.ActionList;

/*
 * M""MMMMM""M oo          dP            dP   oo                   M""""""'YMM            dP            
 * M  MMMMM  M             88            88                        M  mmmm. `M            88            
 * M  MMMMP  M dP .d8888b. 88 .d8888b. d8888P dP .d8888b. 88d888b. M  MMMMM  M .d8888b. d8888P .d8888b. 
 * M  MMMM' .M 88 88'  `88 88 88'  `88   88   88 88'  `88 88'  `88 M  MMMMM  M 88'  `88   88   88'  `88 
 * M  MMP' .MM 88 88.  .88 88 88.  .88   88   88 88.  .88 88    88 M  MMMM' .M 88.  .88   88   88.  .88 
 * M     .dMMM dP `88888P' dP `88888P8   dP   dP `88888P' dP    dP M       .MM `88888P8   dP   `88888P8 
 * MMMMMMMMMMM                                                     MMMMMMMMMMM                          
 */
/**
 * Violation specific dataFactory, for executing actions.<br>
 * This is meant to capture a violation incident in a potentially thread safe way.
 * 
 * @author asofold
 */
public class ViolationData {

    /** The check. */
    public final Check      check;

    /** The player. */
    public final Player     player;

    /** The violation level. */
    public final double     VL;

    /** The actions to be executed. */
    public final ActionList actions;

    /** The bypassing permission. */
    public final String     bypassPermission;

    /**
     * Instantiates a new violation data.
     * 
     * @param check
     *            the check
     * @param player
     *            the player
     * @param VL
     *            the vL
     * @param actions
     *            the actions
     */
    public ViolationData(final Check check, final Player player, final double VL, final ActionList actions) {
        this(check, player, VL, actions, null);
    }

    /**
     * Instantiates a new violation data.
     * 
     * @param check
     *            the check
     * @param player
     *            the player
     * @param VL
     *            the vL
     * @param actions
     *            the actions
     * @param bypassPermission
     *            the permission to bypass the execution, if not null
     */
    public ViolationData(final Check check, final Player player, final double VL, final ActionList actions,
            final String bypassPermission) {
        this.check = check;
        this.player = player;
        this.VL = VL;
        this.actions = actions;
        this.bypassPermission = bypassPermission;
    }

}
