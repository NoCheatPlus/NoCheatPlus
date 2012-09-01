package fr.neatmonster.nocheatplus.checks.chat;

import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.actions.ParameterName;
import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.ViolationData;
import fr.neatmonster.nocheatplus.hooks.NCPExemptionManager;
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
public class NoPwnage extends Check implements ICaptcha{

    /** The last message which caused ban said. */
    private String       lastBanCausingMessage;

    /** The time it was when the last message which caused ban was said. */
    private long         lastBanCausingMessageTime;

    /** The last message said. */
    private String       lastGlobalMessage;

    /** The time it was when the last message was said. */
    private long         lastGlobalMessageTime;

    /** The random number generator. */
    private final Random random = new Random();

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
     * @param event
     *            the event
     * @param isMainThread
     *            is the thread the main thread
     * @return If to cancel the event.
     */
    public boolean check(final Player player, final String message, final boolean isMainThread) {
        if (isMainThread && !isEnabled(player))
            return false;

        final ChatConfig cc = ChatConfig.getConfig(player);
        final ChatData data = ChatData.getData(player);

        if (!isMainThread && (!cc.isEnabled(type) || NCPExemptionManager.isExempted(player, type)))
            return false;

        // Keep related to ChatData/NoPwnage/Color used lock.
        synchronized (data) {
            return unsafeCheck(player, message, isMainThread, cc, data);
        }
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

    /* (non-Javadoc)
     * @see fr.neatmonster.nocheatplus.checks.Check#getParameter(fr.neatmonster.nocheatplus.actions.ParameterName, org.bukkit.entity.Player)
     */
    @Override
    public String getParameter(final ParameterName wildcard, final ViolationData violationData) {
        if (wildcard == ParameterName.IP)
            return violationData.player.getAddress().toString().substring(1).split(":")[0];
        else
            return super.getParameter(wildcard, violationData);
    }

    /**
     * Only to be called form synchronized code.
     * 
     * @param player
     *            the player
     * @param message
     *            the message
     * @param isMainThread
     *            the is main thread
     * @param cc
     *            the cc
     * @param data
     *            the data
     * @return If to cancel the event.
     */
    private boolean unsafeCheck(final Player player, final String message, final boolean isMainThread,
            final ChatConfig cc, final ChatData data) {
        boolean cancel = false;

        // Don't not check excluded messages/commands.
        for (final String exclusion : cc.noPwnageExclusions)
            if (message.startsWith(exclusion))
                return false;

        final long now = System.currentTimeMillis();

        if (shouldCheckCaptcha(cc, data)) {
            checkCaptcha(player, message, cc, data, isMainThread);
            // Cancel the event.
            return true;
        }

        int suspicion = 0;
        // NoPwnage will remember the last message that caused someone to get banned. If a player repeats that
        // message within "timeout" milliseconds, the suspicion will be increased by "weight".
        if (cc.noPwnageBannedCheck && now - lastBanCausingMessageTime < cc.noPwnageBannedTimeout
                && CheckUtils.isSimilar(message, lastBanCausingMessage, 0.8f))
            suspicion += cc.noPwnageBannedWeight;

        // NoPwnage will check if a player sends his first message within "timeout" milliseconds after his login. If
        // he does, increase suspicion by "weight".
        if (cc.noPwnageFirstCheck && now - data.noPwnageJoinTime < cc.noPwnageFirstTimeout)
            suspicion += cc.noPwnageFirstWeight;

        // NoPwnage will check if a player repeats a message that has been sent by another player just before,
        // within "timeout". If he does, suspicion will be increased by "weight".
        if (cc.noPwnageGlobalCheck && now - lastGlobalMessageTime < cc.noPwnageGlobalTimeout
                && CheckUtils.isSimilar(message, lastGlobalMessage, 0.8f))
            suspicion += cc.noPwnageGlobalWeight;

        // NoPwnage will check if a player sends messages too fast. If a message is sent within "timeout"
        // milliseconds after the previous message, increase suspicion by "weight".
        if (cc.noPwnageSpeedCheck && now - data.noPwnageLastMessageTime < cc.noPwnageSpeedTimeout)
            suspicion += cc.noPwnageSpeedWeight;

        // NoPwnage will check if a player repeats his messages within the "timeout" timeframe. Even if the message
        // is a bit different, it will be counted as being a repetition. The suspicion is increased by "weight".
        if (cc.noPwnageRepeatCheck && now - data.noPwnageLastMessageTime < cc.noPwnageRepeatTimeout
                && CheckUtils.isSimilar(message, data.noPwnageLastMessage, 0.8f))
            suspicion += cc.noPwnageRepeatWeight;

        // NoPwnage will check if a player moved within the "timeout" timeframe. If he did not move, the suspicion will
        // be increased by "weight" value.
        if (cc.noPwnageMoveCheck && now - data.noPwnageLastMovedTime > cc.noPwnageMoveTimeout)
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
            if (shouldStartCaptcha(cc, data)) {
                sendNewCaptcha(player, cc, data);
                cancel = true;
            } else {
                lastBanCausingMessage = message;
                data.noPwnageLastWarningTime = lastBanCausingMessageTime = now;
                if (cc.noPwnageWarnOthersCheck)
                    Bukkit.broadcastMessage(CheckUtils.replaceColors(cc.noPwnageWarnOthersMessage.replace("[player]",
                            player.getName())));

                // Increment the violation level.
                data.noPwnageVL += suspicion / 10D;

                // Find out if we need to kick the player or not.
                cancel = executeActionsThreadSafe(player, data.noPwnageVL, suspicion / 10D, cc.noPwnageActions,
                        isMainThread);
            }
        else
            // Reduce the violation level.
            data.noPwnageVL *= 0.95D;

        // Store the message and some other data.
        data.noPwnageLastMessage = message;
        data.noPwnageLastMessageTime = now;
        lastGlobalMessage = message;
        lastGlobalMessageTime = now;

        return cancel;
    }

    @Override
    public void checkCaptcha(Player player, String message, ChatConfig cc, ChatData data, boolean isMainThread) {
    	// Correct answer to the captcha?
        if (message.equals(data.noPwnageGeneratedCaptcha)) {
            // Yes, clear his data and do not worry anymore about him.
            data.clearNoPwnageData();
            data.noPwnageHasStartedCaptcha = false;
            player.sendMessage(CheckUtils.replaceColors(cc.noPwnageCaptchaSuccess));
        } else {
        	// Increment his tries number counter.
            data.noPwnageCaptchTries++;
            data.captchaVL ++;
            // Does he failed too much times?
            if (data.noPwnageCaptchTries > cc.noPwnageCaptchaTries) {
                // Find out if we need to kick the player or not.
                executeActionsThreadSafe(player, data.captchaVL, 1, cc.noPwnageCaptchaActions, 
                		isMainThread);
                // reset in case of reconnection allowed.
                if (!player.isOnline()) 
                	data.noPwnageCaptchTries = 0;
            }

            // Display the question again (if not kicked).
            if (player.isOnline())
            	sendCaptcha(player, cc, data);
        }
	}

    @Override
	public void sendNewCaptcha(Player player, ChatConfig cc, ChatData data) {
    	// Display a captcha to the player.
        data.noPwnageGeneratedCaptcha = "";
        for (int i = 0; i < cc.noPwnageCaptchaLength; i++)
            data.noPwnageGeneratedCaptcha += cc.noPwnageCaptchaCharacters.charAt(random
                    .nextInt(cc.noPwnageCaptchaCharacters.length()));
        sendCaptcha(player, cc, data);
        data.noPwnageHasStartedCaptcha = true;
	}

    @Override
	public void sendCaptcha(Player player, ChatConfig cc, ChatData data) {
		player.sendMessage(CheckUtils.replaceColors(cc.noPwnageCaptchaQuestion.replace("[captcha]",
                data.noPwnageGeneratedCaptcha)));
	}

    @Override
	public boolean shouldStartCaptcha(ChatConfig cc, ChatData data) {
		return cc.noPwnageCaptchaCheck && !data.noPwnageHasStartedCaptcha;
	}

    @Override
	public boolean shouldCheckCaptcha(ChatConfig cc, ChatData data) {
		return cc.noPwnageCaptchaCheck && data.noPwnageHasStartedCaptcha;
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

        // NoPwnage will remember the time when a player leaves the server. If he returns within "time" milliseconds, he
        // will get warned. If he has been warned "warnings" times already, the "commands" will be executed for him.
        // Warnings get removed if the time of the last warning was more than "timeout" milliseconds ago.
        if (cc.noPwnageReloginCheck && now - data.noPwnageLeaveTime < cc.noPwnageReloginTimeout) {
            if (now - data.noPwnageReloginWarningTime > cc.noPwnageReloginWarningTimeout)
                data.noPwnageReloginWarnings = 0;
            if (data.noPwnageReloginWarnings < cc.noPwnageReloginWarningNumber) {
                player.sendMessage(CheckUtils.replaceColors(cc.noPwnageReloginWarningMessage));
                data.noPwnageReloginWarningTime = now;
                data.noPwnageReloginWarnings++;
            } else if (now - data.noPwnageReloginWarningTime < cc.noPwnageReloginWarningTimeout)
                // Find out if we need to ban the player or not.
                cancel = executeActionsThreadSafe(player, data.noPwnageVL, data.noPwnageVL, cc.noPwnageActions, true);
        }

        // Store his joining time.
        data.noPwnageJoinTime = now;

        return cancel;
    }
}
