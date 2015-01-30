package fr.neatmonster.nocheatplus.checks.chat;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.actions.ActionList;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.access.ACheckConfig;
import fr.neatmonster.nocheatplus.checks.access.CheckConfigFactory;
import fr.neatmonster.nocheatplus.checks.access.ICheckConfig;
import fr.neatmonster.nocheatplus.checks.chat.analysis.engine.EnginePlayerConfig;
import fr.neatmonster.nocheatplus.config.ConfPaths;
import fr.neatmonster.nocheatplus.config.ConfigFile;
import fr.neatmonster.nocheatplus.config.ConfigManager;
import fr.neatmonster.nocheatplus.permissions.Permissions;
import fr.neatmonster.nocheatplus.utilities.ColorUtil;

/**
 * Configurations specific for the "chat" checks. Every world gets one of these assigned to it, or if a world doesn't
 * get it's own, it will use the "global" version.
 */
public class ChatConfig extends ACheckConfig {

    /** The factory creating configurations. */
    public static final CheckConfigFactory factory   = new CheckConfigFactory() {
        @Override
        public final ICheckConfig getConfig(final Player player) {
            return ChatConfig.getConfig(player);
        }

        @Override
        public void removeAllConfigs() {
            clear(); // Band-aid.
        }
    };

    /** The map containing the configurations per world. */
    private static final Map<String, ChatConfig> worldsMap = new HashMap<String, ChatConfig>();

    /**
     * Clear all the configurations.
     */
    public static void clear() {
        synchronized (worldsMap) {
            worldsMap.clear();
        }
    }

    /**
     * Gets the configuration for a specified player.
     * 
     * @param player
     *            the player
     * @return the configuration
     */
    public static ChatConfig getConfig(final Player player) {
        synchronized (worldsMap) {
            if (!worldsMap.containsKey(player.getWorld().getName()))
                worldsMap.put(player.getWorld().getName(),
                        new ChatConfig(ConfigManager.getConfigFile(player.getWorld().getName())));
            return worldsMap.get(player.getWorld().getName());
        }
    }

    public final boolean      captchaCheck;
    public final String       captchaCharacters;
    public final int          captchaLength;
    public final String       captchaQuestion;
    public final String       captchaSuccess;
    public final int          captchaTries;
    public final ActionList   captchaActions;

    public final boolean      colorCheck;
    public final ActionList   colorActions;

    public final boolean      commandsCheck;
    public final double       commandsLevel;
    public final int          commandsShortTermTicks;
    public final double       commandsShortTermLevel;
    public final ActionList   commandsActions;

    public final boolean      textCheck;
    public final boolean      textGlobalCheck;
    public final boolean      textPlayerCheck;
    public final EnginePlayerConfig textEnginePlayerConfig;
    public final float        textFreqNormFactor;
    public final float        textFreqNormWeight;
    public final float        textFreqNormMin;
    public final double       textFreqNormLevel;
    public final ActionList   textFreqNormActions;
    public final float        textFreqShortTermFactor;
    public final float        textFreqShortTermWeight;
    public final float        textFreqShortTermLevel;
    public final float        textFreqShortTermMin;
    public final ActionList   textFreqShortTermActions;
    public final float        textMessageLetterCount;
    public final float        textMessageUpperCase;
    public final float        textMessagePartition;
    public final float        textMsgRepeatCancel;
    public final float        textMsgAfterJoin;
    public final float        textMsgRepeatSelf;
    public final float        textMsgRepeatGlobal;
    public final float        textMsgNoMoving;

    // words
    public final float        textMessageLengthAv;
    public final float        textMessageLengthMsg;
    public final float        textMessageNoLetter;
    public final float        textGlobalWeight;
    public final float        textPlayerWeight;
    public final boolean      textEngineMaximum;
    public final boolean	  textAllowVLReset;
    public final boolean      textDebug;

    public final boolean      chatWarningCheck;
    public final float        chatWarningLevel;
    public final String       chatWarningMessage;
    public final long         chatWarningTimeout;

    public final boolean      loginsCheck;
    public final boolean      loginsPerWorldCount;
    public final int          loginsSeconds;
    public final int          loginsLimit;
    public final String       loginsKickMessage;
    public final long         loginsStartupDelay;

    public final boolean      consoleOnlyCheck;
    public final String		  consoleOnlyMessage;


    public final boolean      relogCheck;
    public final String       relogKickMessage;
    public final long         relogTimeout;
    public final String       relogWarningMessage;
    public final int          relogWarningNumber;
    public final long         relogWarningTimeout;
    public final ActionList   relogActions;

    /**
     * Instantiates a new chat configuration.
     * 
     * @param config
     *            the data
     */
    public ChatConfig(final ConfigFile config) {
        super(config, ConfPaths.CHAT, new String[]{
                // Only the permissions needed for async. checking.
                Permissions.CHAT_COLOR,
                Permissions.CHAT_TEXT,
                Permissions.CHAT_CAPTCHA,
        });

        captchaCheck = config.getBoolean(ConfPaths.CHAT_CAPTCHA_CHECK);
        captchaCharacters = config.getString(ConfPaths.CHAT_CAPTCHA_CHARACTERS);
        captchaLength = config.getInt(ConfPaths.CHAT_CAPTCHA_LENGTH);
        captchaQuestion = config.getString(ConfPaths.CHAT_CAPTCHA_QUESTION);
        captchaSuccess = config.getString(ConfPaths.CHAT_CAPTCHA_SUCCESS);
        captchaTries = config.getInt(ConfPaths.CHAT_CAPTCHA_TRIES);
        captchaActions = config.getOptimizedActionList(ConfPaths.CHAT_CAPTCHA_ACTIONS, Permissions.CHAT_CAPTCHA);

        colorCheck = config.getBoolean(ConfPaths.CHAT_COLOR_CHECK);
        colorActions = config.getOptimizedActionList(ConfPaths.CHAT_COLOR_ACTIONS, Permissions.CHAT_COLOR);

        commandsCheck = config.getBoolean(ConfPaths.CHAT_COMMANDS_CHECK);
        commandsLevel = config.getDouble(ConfPaths.CHAT_COMMANDS_LEVEL);
        commandsShortTermTicks = config.getInt(ConfPaths.CHAT_COMMANDS_SHORTTERM_TICKS);
        commandsShortTermLevel = config.getDouble(ConfPaths.CHAT_COMMANDS_SHORTTERM_LEVEL);;
        commandsActions = config.getOptimizedActionList(ConfPaths.CHAT_COMMANDS_ACTIONS, Permissions.CHAT_COMMANDS);


        textCheck = config.getBoolean(ConfPaths.CHAT_TEXT_CHECK);
        textGlobalCheck = config.getBoolean(ConfPaths.CHAT_TEXT_GL_CHECK, true);
        textPlayerCheck = config.getBoolean(ConfPaths.CHAT_TEXT_PP_CHECK, true);
        textEnginePlayerConfig = new EnginePlayerConfig(config);
        textFreqNormMin = (float) config.getDouble(ConfPaths.CHAT_TEXT_FREQ_NORM_MIN);
        textFreqNormFactor = (float) config.getDouble(ConfPaths.CHAT_TEXT_FREQ_NORM_FACTOR);
        textFreqNormWeight = (float) config.getDouble(ConfPaths.CHAT_TEXT_FREQ_NORM_WEIGHT);
        textFreqShortTermFactor = (float) config.getDouble(ConfPaths.CHAT_TEXT_FREQ_SHORTTERM_FACTOR);
        textFreqShortTermWeight = (float) config.getDouble(ConfPaths.CHAT_TEXT_FREQ_SHORTTERM_WEIGHT);
        textFreqShortTermLevel = (float) config.getDouble(ConfPaths.CHAT_TEXT_FREQ_SHORTTERM_LEVEL);
        textFreqShortTermMin = (float) config.getDouble(ConfPaths.CHAT_TEXT_FREQ_SHORTTERM_MIN);
        textFreqShortTermActions = config.getOptimizedActionList(ConfPaths.CHAT_TEXT_FREQ_SHORTTERM_ACTIONS, Permissions.CHAT_TEXT);
        textMessageLetterCount = (float) config.getDouble(ConfPaths.CHAT_TEXT_MSG_LETTERCOUNT);
        textMessagePartition = (float) config.getDouble(ConfPaths.CHAT_TEXT_MSG_PARTITION);
        textMessageUpperCase = (float) config.getDouble(ConfPaths.CHAT_TEXT_MSG_UPPERCASE);
        textMsgRepeatCancel = (float) config.getDouble(ConfPaths.CHAT_TEXT_MSG_REPEATCANCEL);
        textMsgAfterJoin = (float) config.getDouble(ConfPaths.CHAT_TEXT_MSG_AFTERJOIN); 
        textMsgRepeatSelf = (float) config.getDouble(ConfPaths.CHAT_TEXT_MSG_REPEATSELF); 
        textMsgRepeatGlobal = (float) config.getDouble(ConfPaths.CHAT_TEXT_MSG_REPEATGLOBAL); 
        textMsgNoMoving = (float) config.getDouble(ConfPaths.CHAT_TEXT_MSG_NOMOVING); 

        textMessageLengthAv = (float) config.getDouble(ConfPaths.CHAT_TEXT_MSG_WORDS_LENGTHAV);
        textMessageLengthMsg = (float) config.getDouble(ConfPaths.CHAT_TEXT_MSG_WORDS_LENGTHMSG);
        textMessageNoLetter = (float) config.getDouble(ConfPaths.CHAT_TEXT_MSG_WORDS_NOLETTER);
        textGlobalWeight = (float) config.getDouble(ConfPaths.CHAT_TEXT_GL_WEIGHT, 1.0);
        textPlayerWeight = (float) config.getDouble(ConfPaths.CHAT_TEXT_PP_WEIGHT, 1.0);
        textFreqNormLevel = config.getDouble(ConfPaths.CHAT_TEXT_FREQ_NORM_LEVEL);
        textEngineMaximum = config.getBoolean(ConfPaths.CHAT_TEXT_ENGINE_MAXIMUM, true);
        textDebug = config.getBoolean(ConfPaths.CHAT_TEXT_DEBUG, false);
        textFreqNormActions = config.getOptimizedActionList(ConfPaths.CHAT_TEXT_FREQ_NORM_ACTIONS, Permissions.CHAT_TEXT);
        textAllowVLReset = config.getBoolean(ConfPaths.CHAT_TEXT_ALLOWVLRESET);

        chatWarningCheck = config.getBoolean(ConfPaths.CHAT_WARNING_CHECK);
        chatWarningLevel = (float) config.getDouble(ConfPaths.CHAT_WARNING_LEVEL);
        chatWarningMessage = config.getString(ConfPaths.CHAT_WARNING_MESSAGE);
        chatWarningTimeout = config.getLong(ConfPaths.CHAT_WARNING_TIMEOUT) * 1000;

        loginsCheck = config.getBoolean(ConfPaths.CHAT_LOGINS_CHECK);
        loginsPerWorldCount = config.getBoolean(ConfPaths.CHAT_LOGINS_PERWORLDCOUNT);
        loginsSeconds = config.getInt(ConfPaths.CHAT_LOGINS_SECONDS);
        loginsLimit = config.getInt(ConfPaths.CHAT_LOGINS_LIMIT);
        loginsKickMessage =  config.getString(ConfPaths.CHAT_LOGINS_KICKMESSAGE);
        loginsStartupDelay = config.getInt(ConfPaths.CHAT_LOGINS_STARTUPDELAY) * 1000;

        relogCheck = config.getBoolean(ConfPaths.CHAT_RELOG_CHECK);
        relogKickMessage = config.getString(ConfPaths.CHAT_RELOG_KICKMESSAGE);
        relogTimeout = config.getLong(ConfPaths.CHAT_RELOG_TIMEOUT);
        relogWarningMessage = config.getString(ConfPaths.CHAT_RELOG_WARNING_MESSAGE);
        relogWarningNumber = config.getInt(ConfPaths.CHAT_RELOG_WARNING_NUMBER);
        relogWarningTimeout = config.getLong(ConfPaths.CHAT_RELOG_WARNING_TIMEOUT);
        relogActions = config.getOptimizedActionList(ConfPaths.CHAT_RELOG_ACTIONS, Permissions.CHAT_RELOG);

        consoleOnlyCheck = config.getBoolean(ConfPaths.PROTECT_COMMANDS_CONSOLEONLY_ACTIVE);
        consoleOnlyMessage = ColorUtil.replaceColors(config.getString(ConfPaths.PROTECT_COMMANDS_CONSOLEONLY_MSG));

    }

    /* (non-Javadoc)
     * @see fr.neatmonster.nocheatplus.checks.ICheckConfig#isEnabled(fr.neatmonster.nocheatplus.checks.CheckType)
     */
    @Override
    public boolean isEnabled(final CheckType checkType) {
        switch (checkType) {
            case CHAT_COLOR:
                return colorCheck;
            case CHAT_TEXT:
                return textCheck;
            case CHAT_COMMANDS:
                return commandsCheck;
            case CHAT_CAPTCHA:
                return captchaCheck;
            case CHAT_RELOG:
                return relogCheck;
            case CHAT_LOGINS:
                return loginsCheck;
            default:
                return true;
        }
    }
}
