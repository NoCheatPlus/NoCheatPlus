package fr.neatmonster.nocheatplus.checks.chat;

import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerEvent;

import fr.neatmonster.nocheatplus.actions.ParameterName;
import fr.neatmonster.nocheatplus.actions.types.ActionList;
import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.ViolationData;
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
public class NoPwnage extends Check {

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

        for (final Player player : Bukkit.getOnlinePlayers()){
        	final ChatData data = ChatData.getData(player);
        	synchronized(data){
        		data.noPwnageLastLocation = player.getLocation();
        	}
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
    
    	if (!isEnabled(player)) return false;
    	
        final ChatConfig cc = ChatConfig.getConfig(player);
        final ChatData data = ChatData.getData(player);
        
        synchronized(data){ // [keep related to ChatData/NoPwnage/Color used lock.]
        	return unsafeLoginCheck(player, cc, data);
        }
    }

    /**
     * Check (Join), only call from synchronized code. 
     * @param player
     * @param cc
     * @param dataFactory
     * @return
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
                 player.sendMessage(replaceColors(cc.noPwnageReloginWarningMessage));
                 data.noPwnageReloginWarningTime = now;
                 data.noPwnageReloginWarnings++;
             } else if (now - data.noPwnageReloginWarningTime < cc.noPwnageReloginWarningTimeout)
                 // Find out if we need to ban the player or not.
            	 // TODO: extra actions / VL ?
                 cancel = executeActionsThreadSafe(player, data.noPwnageVL, cc.noPwnageActions, true);
         }

         // Store his location and some other dataFactory.
         data.noPwnageLastLocation = player.getLocation();
         data.noPwnageJoinTime = now;

         return cancel;
	}

	/**
     * Checks a player (chat).
     * 
     * @param player
     *            the player
     * @param event
     *            the event
     * @return true, if successful
     */
    public boolean check(final Player player, final PlayerEvent event, final boolean isMainThread) {
    	
    	if (isMainThread && !isEnabled(player)) return false;
    	
        final ChatConfig cc = ChatConfig.getConfig(player);
        final ChatData data = ChatData.getData(player);
        
        synchronized(data){ // [keep related to ChatData/NoPwnage/Color used lock.]
        	return unsafeCheck(player, event, isMainThread, cc, data);
        }
    }
    
    /**
     * Only to be called form synchronized code.
     * @param player
     * @param event
     * @param isMainThread
     * @param cc
     * @param dataFactory
     * @return
     */
    private boolean  unsafeCheck(final Player player, final PlayerEvent event, final boolean isMainThread, final ChatConfig cc, final ChatData data) {
    	data.noPwnageVL = 0D;

        boolean cancel = false;

        if (!data.noPwnageHasFilledCaptcha) {
            String message = "";
            if (event instanceof AsyncPlayerChatEvent)
                message = ((AsyncPlayerChatEvent) event).getMessage();
            else if (event instanceof PlayerCommandPreprocessEvent)
                message = ((PlayerCommandPreprocessEvent) event).getMessage();
            final boolean isCommand = event instanceof PlayerCommandPreprocessEvent;
            final long now = System.currentTimeMillis();

            if (cc.noPwnageCaptchaCheck && data.noPwnageHasStartedCaptcha) {
                // Correct answer to the captcha?
                if (message.equals(data.noPwnageGeneratedCaptcha)) {
                    // Yes, clear his dataFactory and do not worry anymore about him.
                    data.clearNoPwnageData();
                    data.noPwnageHasFilledCaptcha = true;
                    player.sendMessage(replaceColors(cc.noPwnageCaptchaSuccess));
                } else {
                    // Does he failed too much times?
                    if (data.noPwnageCaptchTries > cc.noPwnageCaptchaTries)
                        // Find out if we need to ban the player or not.
                    	// TODO: Extra captcha actions / VL ?
                        cancel = executeActionsThreadSafe(player, data.noPwnageVL, cc.noPwnageActions, isMainThread);

                    // Increment his tries number counter.
                    data.noPwnageCaptchTries++;

                    // Display the question again.
                    player.sendMessage(replaceColors(cc.noPwnageCaptchaQuestion.replace("[captcha]",
                            data.noPwnageGeneratedCaptcha)));
                }

                // Cancel the event and return.
                if (event instanceof AsyncPlayerChatEvent)
                    ((AsyncPlayerChatEvent) event).setCancelled(true);
                else if (event instanceof PlayerCommandPreprocessEvent)
                    ((PlayerCommandPreprocessEvent) event).setCancelled(true);
                return cancel;
            }

            if (data.noPwnageLastLocation == null)
                data.noPwnageLastLocation = player.getLocation();
            else if (!data.noPwnageLastLocation.equals(player.getLocation())) {
                data.noPwnageLastLocation = player.getLocation();
                data.noPwnageLastMovedTime = now;
            }

            // NoPwnage will remember the last message that caused someone to get banned. If a player repeats that
            // message within "timeout" milliseconds, the suspicion will be increased by "weight".
            if (!isCommand && cc.noPwnageBannedCheck && now - lastBanCausingMessageTime < cc.noPwnageBannedTimeout
                    && CheckUtils.isSimilar(message, lastBanCausingMessage, 0.8f))
                data.noPwnageVL += cc.noPwnageBannedWeight;

            // NoPwnage will check if a player sends his first message within "timeout" milliseconds after his login. If
            // he does, increase suspicion by "weight".
            if (cc.noPwnageFirstCheck && now - data.noPwnageJoinTime < cc.noPwnageFirstTimeout)
                data.noPwnageVL += cc.noPwnageFirstWeight;

            // NoPwnage will check if a player repeats a message that has been sent by another player just before,
            // within "timeout". If he does, suspicion will be increased by "weight".
            if (!isCommand && cc.noPwnageGlobalCheck && now - lastGlobalMessageTime < cc.noPwnageGlobalTimeout
                    && CheckUtils.isSimilar(message, lastGlobalMessage, 0.8f))
                data.noPwnageVL += cc.noPwnageGlobalWeight;

            // NoPwnage will check if a player sends messages too fast. If a message is sent within "timeout"
            // milliseconds after the previous message, increase suspicion by "weight".
            if (cc.noPwnageSpeedCheck && now - data.noPwnageLastMessageTime < cc.noPwnageSpeedTimeout)
                data.noPwnageVL += cc.noPwnageSpeedWeight;

            // NoPwnage will check if a player repeats his messages within the "timeout" timeframe. Even if the message
            // is a bit different, it will be counted as being a repetition. The suspicion is increased by "weight".
            if (!isCommand && cc.noPwnageRepeatCheck && now - data.noPwnageLastMessageTime < cc.noPwnageRepeatTimeout
                    && CheckUtils.isSimilar(message, data.noPwnageLastMessage, 0.8f))
                data.noPwnageVL += cc.noPwnageRepeatWeight;

            // NoPwnage will check if a player moved within the "timeout" timeframe. If he did move, the suspicion will
            // be reduced by the "weightbonus" value. If he did not move, the suspicion will be increased by
            // "weightmalus" value.
            if (cc.noPwnageMoveCheck && now - data.noPwnageLastMovedTime < cc.noPwnageMoveTimeout)
                data.noPwnageVL -= cc.noPwnageMoveWeightBonus;
            else
                data.noPwnageVL += cc.noPwnageMoveWeightMalus;

            // Should a player that reaches the "warnLevel" get a text message telling him that he is under suspicion of
            // being a bot.
            boolean warned = false;
            if (cc.noPwnageWarnPlayerCheck && now - data.noPwnageLastWarningTime < cc.noPwnageWarnTimeout) {
                data.noPwnageVL += 100;
                warned = true;
            }

            if (cc.noPwnageWarnPlayerCheck && data.noPwnageVL > cc.noPwnageWarnLevel && !warned) {
                player.sendMessage(replaceColors(cc.noPwnageWarnPlayerMessage));
                data.noPwnageLastWarningTime = now;
            } else if (data.noPwnageVL > cc.noPwnageLevel)
                if (cc.noPwnageCaptchaCheck && !data.noPwnageHasStartedCaptcha) {
                    // Display a captcha to the player.
                    for (int i = 0; i < cc.noPwnageCaptchaLength; i++)
                        data.noPwnageGeneratedCaptcha += cc.noPwnageCaptchaCharacters.charAt(random
                                .nextInt(cc.noPwnageCaptchaCharacters.length()));
                    player.sendMessage(replaceColors(cc.noPwnageCaptchaQuestion.replace("[captcha]",
                            data.noPwnageGeneratedCaptcha)));
                    data.noPwnageHasStartedCaptcha = true;
                    if (event instanceof AsyncPlayerChatEvent)
                        ((AsyncPlayerChatEvent) event).setCancelled(true);
                    else if (event instanceof PlayerCommandPreprocessEvent)
                        ((PlayerCommandPreprocessEvent) event).setCancelled(true);
                } else {
                    lastBanCausingMessage = message;
                    data.noPwnageLastWarningTime = lastBanCausingMessageTime = now;
                    if (cc.noPwnageWarnOthersCheck)
                        Bukkit.broadcastMessage(replaceColors(cc.noPwnageWarnOthersMessage.replace("[player]",
                                player.getName())));
                    if (event instanceof AsyncPlayerChatEvent)
                        ((AsyncPlayerChatEvent) event).setCancelled(true);
                    else if (event instanceof PlayerCommandPreprocessEvent)
                        ((PlayerCommandPreprocessEvent) event).setCancelled(true);

                    // Find out if we need to ban the player or not.
                    cancel = executeActionsThreadSafe(player, data.noPwnageVL, cc.noPwnageActions, isMainThread);
                }

            // Store the message and some other dataFactory.
            data.noPwnageLastMessage = message;
            data.noPwnageLastMessageTime = now;
            lastGlobalMessage = message;
            lastGlobalMessageTime = now;
        }

        return cancel;
	}
    
    /**
     * Execute actions from another thread (not the main thread).<br>
     * This does not use extra synchronization.
     * @param player
     * @param actions 
     * @param VL 
     * @return
     */
    public final boolean executeActionsThreadSafe(final Player player, double VL, ActionList actions, boolean isMainThread){
        final boolean cancel = super.executeActionsThreadSafe(player, VL, actions, isMainThread);
        if (cancel) ChatData.getData(player).clearNoPwnageData();
        return cancel;
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
    
}
