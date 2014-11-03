package fr.neatmonster.nocheatplus.checks.chat;

import java.util.Random;

import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.permissions.Permissions;
import fr.neatmonster.nocheatplus.utilities.ColorUtil;

/**
 * NOTE: EARLY REFACTORING STATE, MOST METHODS NEED SYNC OVER DATA !
 * @author mc_dev
 *
 */
public class Captcha extends Check implements ICaptcha{
	
    public Captcha() {
		super(CheckType.CHAT_CAPTCHA);
	}

	/** The random number generator. */
    private final Random random = new Random();

    
    @Override
    public void checkCaptcha(Player player, String message, ChatConfig cc, ChatData data, boolean isMainThread) {
    	// Correct answer to the captcha?
        if (message.equals(data.captchaGenerated)) {
            // Yes, clear their data and do not worry anymore about them.
            data.reset();
            data.captchaStarted = false;
            player.sendMessage(ColorUtil.replaceColors(cc.captchaSuccess));
        } else {
        	// Increment their tries number counter.
            data.captchTries++;
            data.captchaVL ++;
            // Have they failed too man times?
            if (data.captchTries > cc.captchaTries) {
                // Find out if we need to kick the player or not.
                executeActions(player, data.captchaVL, 1, cc.captchaActions, isMainThread);
                // (Resetting captcha tries is done on quit/kick).
            }

            // Display the question again (if not kicked).
            if (player.isOnline()) {
            	sendCaptcha(player, cc, data);
            }
        }
	}

    @Override
	public void sendNewCaptcha(Player player, ChatConfig cc, ChatData data) {
    	// Display a captcha to the player.
        generateCaptcha(cc, data, true);
        sendCaptcha(player, cc, data);
        data.captchaStarted = true;
	}

    @Override
    public void generateCaptcha(ChatConfig cc, ChatData data, boolean reset) {
    	if (reset) data.captchTries = 0;
    	final char[] chars = new char[cc.captchaLength];
        for (int i = 0; i < cc.captchaLength; i++)
            chars[i] = cc.captchaCharacters.charAt(random
                    .nextInt(cc.captchaCharacters.length()));
        data.captchaGenerated = new String(chars);
	}
    
    @Override
    public void resetCaptcha(Player player){
    	ChatData data = ChatData.getData(player);
    	synchronized (data) {
    		resetCaptcha(ChatConfig.getConfig(player), data);
		}
    }
    
    @Override
    public void resetCaptcha(ChatConfig cc, ChatData data){
    	data.captchTries = 0;
    	if (shouldCheckCaptcha(cc, data) || shouldStartCaptcha(cc, data)){
    		generateCaptcha(cc, data, true);
    	}
    }

	@Override
	public void sendCaptcha(Player player, ChatConfig cc, ChatData data) {
		player.sendMessage(ColorUtil.replaceColors(cc.captchaQuestion.replace("[captcha]",
                data.captchaGenerated)));
	}

    @Override
	public boolean shouldStartCaptcha(ChatConfig cc, ChatData data) {
		return cc.captchaCheck && !data.captchaStarted && !data.hasCachedPermission(Permissions.CHAT_CAPTCHA);
	}

    @Override
	public boolean shouldCheckCaptcha(ChatConfig cc, ChatData data) {
		return cc.captchaCheck && data.captchaStarted  && !data.hasCachedPermission(Permissions.CHAT_CAPTCHA);
	}
}
