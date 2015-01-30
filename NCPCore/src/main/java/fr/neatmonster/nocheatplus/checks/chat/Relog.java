package fr.neatmonster.nocheatplus.checks.chat;

import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.combined.CombinedData;
import fr.neatmonster.nocheatplus.utilities.ColorUtil;

public class Relog extends Check {

	public Relog() {
		super(CheckType.CHAT_RELOG);
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
    public boolean unsafeLoginCheck(final Player player, final ChatConfig cc, final ChatData data) {
        boolean cancel = false;

        final long now = System.currentTimeMillis();

        final CombinedData cData = CombinedData.getData(player);
        
        // Enforce the player does not relog too fast.
        if (now - cData.lastLogoutTime < cc.relogTimeout) {
            if (now - data.relogWarningTime > cc.relogWarningTimeout)
                data.relogWarnings = 0;
            if (data.relogWarnings < cc.relogWarningNumber) {
                player.sendMessage(ColorUtil.replaceColors(cc.relogWarningMessage));
                data.relogWarningTime = now;
                data.relogWarnings++;
            } else{
                // Find out if we need to ban the player or not.
                data.relogVL += 1D;
                cancel = executeActions(player, (double) data.relogVL, 1D, cc.relogActions);
            }
        }
        // TODO: decrease relog vl ?

        return cancel;
    }

}
