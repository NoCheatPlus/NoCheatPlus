package fr.neatmonster.nocheatplus.checks.chat;

import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.chat.analysis.LetterEngine;
import fr.neatmonster.nocheatplus.checks.chat.analysis.MessageLetterCount;
import fr.neatmonster.nocheatplus.checks.chat.analysis.WordLetterCount;
import fr.neatmonster.nocheatplus.hooks.NCPExemptionManager;

/**
 * Some alternative more or less advanced analysis methods.
 * @author mc_dev
 *
 */
public class GlobalChat extends Check{

	private final LetterEngine engine = new LetterEngine();
	
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
		
		// Test captcha in case nopwnage is disabled.
		if (captcha.shouldCheckCaptcha(cc, data)){
			captcha.checkCaptcha(player, message, cc, data, isMainThread);
			return true;
		}
		
		// Take time once:
		final long time = System.currentTimeMillis();
				
		boolean cancel = false;
		
		// Update the frequency interval weights.
		data.globalChatFrequency.update(time);
		
		// Score for this message (violation score).
		float score = 0;
		
		final MessageLetterCount letterCounts = new MessageLetterCount(message);
		
		final int msgLen = message.length();
		
		// (Following: random/made up criteria.)
		
		// TODO: Create tests for all methods with wordlists, fake chat (refactor for that).
		
		// Full message processing. ------------
		
		// Upper case.
		if (letterCounts.fullCount.upperCase > msgLen / 3){
			final float wUpperCase = 0.6f * letterCounts.fullCount.getUpperCaseRatio();
			score += wUpperCase;
		}
		
		// Letters vs. word length.
		if (msgLen > 4){
			final float fullRep = letterCounts.fullCount.getLetterCountRatio();
			// Long messages: very small and very big are bad !
			final float wRepetition = (float) msgLen / 15.0f * Math.abs(0.5f - fullRep);
			score += wRepetition;
			
			// Number of words vs. length of message
			final float fnWords = (float) letterCounts.words.length / (float) msgLen;
			if (fnWords > 0.75f){
				score += fnWords;
			}
		}
		
		// Per word checks. -------------------
		float wWords = 0.0f;
		final float avwLen = (float) msgLen / (float) letterCounts.words.length; 
		for (final WordLetterCount word: letterCounts.words){
			float wWord = 0.0f;
			final int wLen = word.word.length();
			// TODO: ? used letters vs. word length.
			
			// Length of word vs. av. word length.
			final float fLenAv = Math.abs(avwLen - (float) wLen) / avwLen;
			wWord += fLenAv;
			
			// Length of word vs. message length;
			final float fLenMsg = (float) wLen / (float) msgLen;
			wWord += fLenMsg;
			
			// Not letter:
			float notLetter = word.getNotLetterRatio();
			notLetter *= notLetter;
			wWord += notLetter;
			
			wWord *= wWord;
			wWords += wWord;
		}
		wWords /= (float) letterCounts.words.length;
		score += wWords;
		
		// Engine:
		if (cc.globalChatEngineCheck){
			final float wEngine = engine.feed(letterCounts);
			score += wEngine;
		}
		
//		System.out.println(score);
		
		// Wrapping it up. --------------------
		// Add weight to frequency counts.
		data.globalChatFrequency.add(time, score);
		final float accumulated = cc.globalChatFrequencyWeight * data.globalChatFrequency.getScore(cc.globalChatFrequencyFactor);
		
		if (score < 2.0f * cc.globalChatFrequencyWeight)
			// Reset the VL.
			data.globalChatVL = 0.0;
		
		if (accumulated > cc.globalChatLevel){
			if (captcha.shouldStartCaptcha(cc, data)){
				captcha.sendNewCaptcha(player, cc, data);
				cancel = true;
			}
			else{
				data.globalChatVL += accumulated / 10.0;
				if (executeActionsThreadSafe(player, data.globalChatVL, accumulated / 10.0, cc.globalChatActions, isMainThread))
					cancel = true;
			}
		}
		else
			data.globalChatVL *= 0.95;
		
		
		return cancel;
	}

}
