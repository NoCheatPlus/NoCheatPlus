package fr.neatmonster.nocheatplus.checks.chat;

import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.combined.CombinedData;
import fr.neatmonster.nocheatplus.utilities.CheckUtils;

public class Relog extends Check {

	public Relog() {
		super(CheckType.CHAT_RELOG);
	}
	
    /**
     * Checks a player (join).
     * 
     * Only called from the main thread.
     * 
     * @param player
     *            the player
     * @return true, if successful
     */
    public boolean checkLogin(final Player player) {
        if (!isEnabled(player))
            return false;

        final ChatConfig cc = ChatConfig.getConfig(player);
        final ChatData data = ChatData.getData(player);

        // Keep related to ChatData/NoPwnage/Color used lock.
        synchronized (data) {
            return unsafeLoginCheck(player, cc, data);
        }
    }
	
	
	   /**
     * Check (Join), only call from synchronized code.
     * 
     * @param player
     *            the player
     * @param cc
     *            the cc
     * @param data
     *            the data
     * @return true, if successful
     */
    private boolean unsafeLoginCheck(final Player player, final ChatConfig cc, final ChatData data) {
        boolean cancel = false;

        final long now = System.currentTimeMillis();

        final CombinedData cData = CombinedData.getData(player);
        
        // NoPwnage will remember the time when a player leaves the server. If he returns within "time" milliseconds, he
        // will get warned. If he has been warned "warnings" times already, the "commands" will be executed for him.
        // Warnings get removed if the time of the last warning was more than "timeout" milliseconds ago.
        if (now - cData.lastLogoutTime < cc.relogTimeout) {
            if (now - data.relogWarningTime > cc.relogWarningTimeout)
                data.relogWarnings = 0;
            if (data.relogWarnings < cc.relogWarningNumber) {
                player.sendMessage(CheckUtils.replaceColors(cc.relogWarningMessage));
                data.relogWarningTime = now;
                data.relogWarnings++;
            } else{
                // Find out if we need to ban the player or not.
                data.relogVL += 1D;
                cancel = executeActions(player, (double) data.relogVL, 1D, cc.relogActions, true);
            }
        }
        // TODO: decrease relog vl ?

        return cancel;
    }

}
