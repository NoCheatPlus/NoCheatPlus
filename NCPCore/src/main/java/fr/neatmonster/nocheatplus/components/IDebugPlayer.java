package fr.neatmonster.nocheatplus.components;

import org.bukkit.entity.Player;

/**
 * Convenient player-specific debug messages with standard format.
 * 
 * @author asofold
 *
 */
public interface IDebugPlayer {

    /**
     * Output a message for a player with the standard format (see
     * CheckUtils.debug(Player, CheckType, String).
     * 
     * @param player
     *            May be null.
     * @param message
     */
    public void debug(Player player, String message);

}
