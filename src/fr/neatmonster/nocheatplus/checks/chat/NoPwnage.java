package fr.neatmonster.nocheatplus.checks.chat;

import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.actions.ParameterName;
import fr.neatmonster.nocheatplus.checks.AsyncCheck;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.ViolationData;
import fr.neatmonster.nocheatplus.checks.combined.CombinedData;
import fr.neatmonster.nocheatplus.utilities.CheckUtils;

/*
 * M"""""""`YM          MM"""""""`YM                                                
 * M  mmmm.  M          MM  mmmmm  M                                                
 * M  MMMMM  M .d8888b. M'        .M dP  dP  dP 88d888b. .d8888b. .d8888b. .d8888b. 
 * M  MMMMM  M 88'  `88 MM  MMMMMMMM 88  88  88 88'  `88 88'  `88 88'  `88 88ooood8 
 * M  MMMMM  M 88.  .88 MM  MMMMMMMM 88.88b.88' 88    88 88.  .88 88.  .88 88.  ... 
 * M  MMMMM  M `88888P' MM  MMMMMMMM 8888P Y8P  dP    dP `88888P8 `8888P88 `88888P' 
 * MMMMMMMMMMM          MMMMMMMMMMMM                                   .88          
 *                                                                 d8888P           
 */
/**
 * The NoPwnage check will try to detect "spambots" (like the ones created by the PWN4G3 software).
 */
public class NoPwnage extends AsyncCheck{

    /** The last message which caused ban said. */
    private String       lastBanCausingMessage;

    /** The time it was when the last message which caused ban was said. */
    private long         lastBanCausingMessageTime;

    /** The last message said. */
    private String       lastGlobalMessage;

    /** The time it was when the last message was said. */
    private long         lastGlobalMessageTime;
    
    /**
     * Instantiates a new no pwnage check.
     */
    public NoPwnage() {
        super(CheckType.CHAT_NOPWNAGE);
    }

    /**
     * Checks a player (chat).
     * 
     * @param player
     *            the player
     * @param captcha 
     * @param event
     *            the event
     * @param isMainThread
     *            is the thread the main thread
     * @return If to cancel the event.
     */
    public boolean check(final Player player, final String message, final Captcha captcha, final boolean isCommand, 
    		final boolean isMainThread) {

        final ChatConfig cc = ChatConfig.getConfig(player);
        final ChatData data = ChatData.getData(player);

        // Keep related to ChatData/NoPwnage/Color used lock.
        synchronized (data) {
            return unsafeCheck(player, message, captcha, isCommand, isMainThread, cc, data);
        }
    }

    
    /**
     * Only to be called form synchronized code.
     * 
     * @param player
     *            the player
     * @param message
     *            the message
     * @param captcha 
     * @param isMainThread
     *            the is main thread
     * @param cc
     *            the cc
     * @param data
     *            the data
     * @return If to cancel the event.
     */
    private boolean unsafeCheck(final Player player, final String message, final Captcha captcha, final boolean isCommand,
    		final boolean isMainThread, final ChatConfig cc, final ChatData data) {
        boolean cancel = false;

        final long now = System.currentTimeMillis();

        if (captcha.shouldCheckCaptcha(cc, data)) {
            captcha.checkCaptcha(player, message, cc, data, isMainThread);
            // Cancel the event.
            return true;
        }
        
        CombinedData cData = CombinedData.getData(player);

        int suspicion = 0;
        // NoPwnage will remember the last message that caused someone to get banned. If a player repeats that
        // message within "timeout" milliseconds, the suspicion will be increased by "weight".
        if (!isCommand && cc.noPwnageBannedCheck && now - lastBanCausingMessageTime < cc.noPwnageBannedTimeout
                && CheckUtils.isSimilar(message, lastBanCausingMessage, 0.8f))
            suspicion += cc.noPwnageBannedWeight;

        // NoPwnage will check if a player sends his first message within "timeout" milliseconds after his login. If
        // he does, increase suspicion by "weight".
        if (cc.noPwnageFirstCheck && now - cData.lastJoinTime < cc.noPwnageFirstTimeout)
            suspicion += cc.noPwnageFirstWeight;

        // NoPwnage will check if a player repeats a message that has been sent by another player just before,
        // within "timeout". If he does, suspicion will be increased by "weight".
        if (!isCommand && cc.noPwnageGlobalCheck && now - lastGlobalMessageTime < cc.noPwnageGlobalTimeout
                && CheckUtils.isSimilar(message, lastGlobalMessage, 0.8f))
            suspicion += cc.noPwnageGlobalWeight;

        // NoPwnage will check if a player sends messages too fast. If a message is sent within "timeout"
        // milliseconds after the previous message, increase suspicion by "weight".
        // TODO: update description or method :p
        if (cc.noPwnageSpeedCheck){
        	// First add old messages score:
        	data.noPwnageSpeed.update(now);
        	suspicion += data.noPwnageSpeed.getScore(0.7f) * cc.noPwnageSpeedWeight;
        	// Then add this message.
        	data.noPwnageSpeed.add(now, 1.0f);
        	
        }
            

        // NoPwnage will check if a player repeats his messages within the "timeout" timeframe. Even if the message
        // is a bit different, it will be counted as being a repetition. The suspicion is increased by "weight".
        if (!isCommand && cc.noPwnageRepeatCheck && now - data.noPwnageLastMessageTime < cc.noPwnageRepeatTimeout
                && CheckUtils.isSimilar(message, data.noPwnageLastMessage, 0.8f))
            suspicion += cc.noPwnageRepeatWeight;

        // NoPwnage will check if a player moved within the "timeout" timeframe. If he did not move, the suspicion will
        // be increased by "weight" value.
        if (!isCommand && cc.noPwnageMoveCheck && now - cData.lastMoveTime > cc.noPwnageMoveTimeout)
            suspicion += cc.noPwnageMoveWeight;

        // Should a player that reaches the "warnLevel" get a text message telling him that he is under suspicion of
        // being a bot.
        boolean warned = false;
        if (cc.noPwnageWarnPlayerCheck && now - data.noPwnageLastWarningTime < cc.noPwnageWarnTimeout) {
            suspicion += 100;
            warned = true;
        }

        if (cc.noPwnageWarnPlayerCheck && suspicion > cc.noPwnageWarnLevel && !warned) {
            player.sendMessage(CheckUtils.replaceColors(cc.noPwnageWarnPlayerMessage));
            data.noPwnageLastWarningTime = now;
        } else if (suspicion > cc.noPwnageLevel)
            if (captcha.shouldStartCaptcha(cc, data)) {
                captcha.sendNewCaptcha(player, cc, data);
                cancel = true;
            } else {
                lastBanCausingMessage = message;
                data.noPwnageLastWarningTime = lastBanCausingMessageTime = now;
                if (cc.noPwnageWarnOthersCheck)
                    Bukkit.broadcastMessage(CheckUtils.replaceColors(cc.noPwnageWarnOthersMessage.replace("[player]",
                            player.getName())));

                // Increment the violation level.
                data.noPwnageVL += (double) suspicion / 10D ;

                // Find out if we need to kick the player or not.
                cancel = executeActions(player, data.noPwnageVL, suspicion / 10D, cc.noPwnageActions,
                        isMainThread);
            }
        else
            // Reduce the violation level. <- Done automatically by queue.
            data.noPwnageVL *= 0.95D;

        // Store the message and some other data.
        data.noPwnageLastMessage = message;
        data.noPwnageLastMessageTime = now;
        lastGlobalMessage = message;
        lastGlobalMessageTime = now;
        
        if (cc.noPwnageDebug){
        	final String msg = "[NoCheatPlus][nopwnage]  Message ("+player.getName()+"/"+message.length()+"): suspicion="+suspicion +", vl="+CheckUtils.fdec3.format(data.noPwnageVL);
        	CheckUtils.scheduleOutput(msg);
        }

        return cancel;
    }




    
	@Override
	protected Map<ParameterName, String> getParameterMap(final ViolationData violationData) {
		final Map<ParameterName, String> parameters = super.getParameterMap(violationData);
		parameters.put(ParameterName.IP, violationData.player.getAddress().toString().substring(1).split(":")[0]);
		return parameters;
	}

}
