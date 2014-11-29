package fr.neatmonster.nocheatplus.clients.motd;

import org.bukkit.entity.Player;

/**
 * Setup for all "motd" sending for enabling/disabling client mod features.
 * @author mc_dev
 *
 */
public abstract class ClientMOTD {

    /**
     * Extend / manipulate the message based on permissions and the allowAll setting.
     * @param message
     * @param player
     * @param allowAll
     * @return The message to send / process further.
     */
    public abstract String onPlayerJoin(String message, Player player, boolean allowAll);
}
