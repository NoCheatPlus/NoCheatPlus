package fr.neatmonster.nocheatplus.checks.chat;

import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.hooks.NCPExemptionManager;
import fr.neatmonster.nocheatplus.players.Permissions;

/**
 * Some alternative more or less advanced analysis methods.
 * @author mc_dev
 *
 */
public class GlobalChat extends Check{

	public GlobalChat() {
		super(CheckType.CHAT_GLOBALCHAT);
	}

	/**
	 * 
	 * @param player
	 * @param message
	 * @param captcha Used for starting captcha on failure.
	 * @return
	 */
	public boolean check(final Player player, final String message, final ICaptcha captcha) {
		// Take time once:
		final long time = System.currentTimeMillis();
		
		final ChatConfig cc = ChatConfig.getConfig(player);
		
		// Checking the player, actually.
		if (!cc.isEnabled(type) || NCPExemptionManager.isExempted(player, type))
			return false;
		
		final ChatData data = ChatData.getData(player);
	
		boolean cancel = false;
		
		data.globalChatFrequency.add(time);
		double score = cc.globalChatFrequencyWeight * data.globalChatFrequency.getScore(cc.globalChatFrequencyFactor);
		if (score < 2.0 * cc.globalChatFrequencyWeight) 
			// Reset the VL.
			data.globalChatVL = 0.0;
		
		// TODO Core checks....
		
		if (score > cc.globalChatLevel){
			if (captcha.shouldStartCaptcha(cc, data)){
				captcha.sendNewCaptcha(player, cc, data);
				cancel = true;
			}
			else{
				data.globalChatVL += score / 10.0;
				if (executeActionsThreadSafe(player, data.globalChatVL, score, cc.globalChatActions, Permissions.CHAT_GLOBALCHAT))
					cancel = true;
			}
		}
		else
			data.globalChatVL *= 0.95;
		
		return cancel;
	}

}
