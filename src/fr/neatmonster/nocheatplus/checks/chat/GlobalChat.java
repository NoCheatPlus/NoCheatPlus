package fr.neatmonster.nocheatplus.checks.chat;

import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.hooks.NCPExemptionManager;

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
	 * Start analysis.
	 * @param player
	 *   		The player who issued the message.
	 * @param message
	 * 			The message to check.
	 * @param captcha 
	 * 			Used for starting captcha on failure, if configured so.
	 * @return
	 */
	public boolean check(final Player player, final String message, final ICaptcha captcha, boolean isMainThread) {
		
		final ChatConfig cc = ChatConfig.getConfig(player);
		
		if (isMainThread && !isEnabled(player)) return false;
		if (!isMainThread && (!cc.isEnabled(type) || NCPExemptionManager.isExempted(player, type)))
			return false;

		final ChatData data = ChatData.getData(player);
		
		synchronized (data) {
			return unsafeCheck(player, message, captcha, cc, data, isMainThread);
		}	
	}

	/**
	 * Check without further synchronization.
	 * @param player
	 * @param message
	 * @param captcha
	 * @param cc
	 * @param data
	 * @param isMainThread 
	 * @return
	 */
	private boolean unsafeCheck(final Player player, final String message, final ICaptcha captcha,
			final ChatConfig cc, final ChatData data, boolean isMainThread) {
		// Take time once:
		final long time = System.currentTimeMillis();
				
		boolean cancel = false;
		
		// Update the frequency interval weights.
		data.globalChatFrequency.update(time);
		double score = 0;
		if (score < cc.globalChatFrequencyWeight)
			// Reset the VL.
			data.globalChatVL = 0.0;
		
		// Weight of this chat message.
		float weight = 1.0f;
		
		final MessageLetterCount letterCounts = new MessageLetterCount(message);
		
		final int length = message.length();
		// Upper case.
		if (length > 8 && letterCounts.fullCount.upperCase > length / 4){
			weight += 0.6 * letterCounts.fullCount.getUpperCaseRatio();
		}
		
		// ? for words individually ?
		
		// Repetition of characters.
		if (length > 4){
			final float fullRep = letterCounts.fullCount.getLetterRatio();
			score += (float) length / 15.0 * Math.abs(0.5 - fullRep); // Very small and very big are bad !
		}
	
		// TODO Core checks....
		
		// Add weight to frequency counts.
		data.globalChatFrequency.add(time, weight);
		score +=  cc.globalChatFrequencyWeight * data.globalChatFrequency.getScore(cc.globalChatFrequencyFactor);
				
		if (score > cc.globalChatLevel){
			if (captcha.shouldStartCaptcha(cc, data)){
				captcha.sendNewCaptcha(player, cc, data);
				cancel = true;
			}
			else{
				data.globalChatVL += score / 10.0;
				if (executeActionsThreadSafe(player, data.globalChatVL, score, cc.globalChatActions, isMainThread))
					cancel = true;
			}
		}
		else
			data.globalChatVL *= 0.95;
		
		
		return cancel;
	}

}
