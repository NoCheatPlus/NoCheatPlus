package fr.neatmonster.nocheatplus.checks.chat;

import fr.neatmonster.nocheatplus.actions.types.ActionList;
import fr.neatmonster.nocheatplus.checks.CheckConfig;
import fr.neatmonster.nocheatplus.config.ConfPaths;
import fr.neatmonster.nocheatplus.config.ConfigFile;
import fr.neatmonster.nocheatplus.players.informations.Permissions;

/**
 * Configurations specific for the "Chat" checks
 * Every world gets one of these assigned to it, or if a world doesn't get
 * it's own, it will use the "global" version
 * 
 */
public class ChatConfig extends CheckConfig {

    public final boolean    opByConsoleOnly;
    public final boolean    protectPlugins;

    public final boolean    noPwnageCheck;
    public final boolean    noPwnageWarnPlayers;
    public final boolean    noPwnageWarnOthers;
    public final int        noPwnageWarnLevel;
    public final long       noPwnageWarnTimeout;
    public final int        noPwnageBanLevel;
    public final ActionList noPwnageActions;

    public final boolean    noPwnageMoveCheck;
    public final int        noPwnageMoveWeightBonus;
    public final int        noPwnageMoveWeightMalus;
    public final long       noPwnageMoveTimeout;

    public final boolean    noPwnageSpeedCheck;
    public final int        noPwnageSpeedWeight;
    public final long       noPwnageSpeedTimeout;

    public final boolean    noPwnageFirstCheck;
    public final int        noPwnageFirstWeight;
    public final long       noPwnageFirstTimeout;

    public final boolean    noPwnageRepeatCheck;
    public final int        noPwnageRepeatWeight;
    public final long       noPwnageRepeatTimeout;

    public final boolean    noPwnageGlobalCheck;
    public final int        noPwnageGlobalWeight;
    public final long       noPwnageGlobalTimeout;

    public final boolean    noPwnageBannedCheck;
    public final int        noPwnageBannedWeight;
    public final long       noPwnageBannedTimeout;

    public final boolean    noPwnageRelogCheck;
    public final long       noPwnageRelogTime;
    public final int        noPwnageRelogWarnings;
    public final long       noPwnageRelogTimeout;

    public final boolean    noPwnageCaptchaCheck;
    public final int        noPwnageCaptchaLength;
    public final String     noPwnageCaptchaCharacters;
    public final int        noPwnageCaptchaTries;

    public final String     noPwnageMessagesKick;
    public final String     noPwnageMessagesCaptchaQuestion;
    public final String     noPwnageMessagesCaptchaSuccess;
    public final String     noPwnageMessagesWarnPlayer;
    public final String     noPwnageMessagesWarnOthers;
    public final String     noPwnageMessagesWarnRelog;

    public final boolean    arrivalsLimitCheck;
    public final int        arrivalsLimitPlayersLimit;
    public final long       arrivalsLimitTimeframe;
    public final long       arrivalsLimitCooldownDelay;
    public final long       arrivalsLimitNewTime;
    public final String     arrivalsLimitKickMessage;
    public final ActionList arrivalsLimitActions;

    public final boolean    colorCheck;
    public final ActionList colorActions;

    public ChatConfig(final ConfigFile data) {

        opByConsoleOnly = data.getBoolean(ConfPaths.MISCELLANEOUS_OPBYCONSOLEONLY);
        protectPlugins = data.getBoolean(ConfPaths.MISCELLANEOUS_PROTECTPLUGINS);

        noPwnageCheck = data.getBoolean(ConfPaths.CHAT_NOPWNAGE_CHECK);
        noPwnageWarnPlayers = data.getBoolean(ConfPaths.CHAT_NOPWNAGE_WARNPLAYERS);
        noPwnageWarnOthers = data.getBoolean(ConfPaths.CHAT_NOPWNAGE_WARNOTHERS);
        noPwnageWarnLevel = data.getInt(ConfPaths.CHAT_NOPWNAGE_WARNLEVEL);
        noPwnageWarnTimeout = data.getLong(ConfPaths.CHAT_NOPWNAGE_WARNTIMEOUT);
        noPwnageBanLevel = data.getInt(ConfPaths.CHAT_NOPWNAGE_BANLEVEL);
        noPwnageActions = data.getActionList(ConfPaths.CHAT_NOPWNAGE_ACTIONS, Permissions.CHAT_NOPWNAGE);

        noPwnageMoveCheck = data.getBoolean(ConfPaths.CHAT_NOPWNAGE_MOVE_CHECK);
        noPwnageMoveWeightBonus = data.getInt(ConfPaths.CHAT_NOPWNAGE_MOVE_WEIGHTBONUS);
        noPwnageMoveWeightMalus = data.getInt(ConfPaths.CHAT_NOPWNAGE_MOVE_WEIGHTMALUS);
        noPwnageMoveTimeout = data.getLong(ConfPaths.CHAT_NOPWNAGE_MOVE_TIMEOUT);

        noPwnageSpeedCheck = data.getBoolean(ConfPaths.CHAT_NOPWNAGE_SPEED_CHECK);
        noPwnageSpeedWeight = data.getInt(ConfPaths.CHAT_NOPWNAGE_SPEED_WEIGHT);
        noPwnageSpeedTimeout = data.getLong(ConfPaths.CHAT_NOPWNAGE_SPEED_TIMEOUT);

        noPwnageFirstCheck = data.getBoolean(ConfPaths.CHAT_NOPWNAGE_FIRST_CHECK);
        noPwnageFirstWeight = data.getInt(ConfPaths.CHAT_NOPWNAGE_FIRST_WEIGHT);
        noPwnageFirstTimeout = data.getLong(ConfPaths.CHAT_NOPWNAGE_FIRST_TIMEOUT);

        noPwnageRepeatCheck = data.getBoolean(ConfPaths.CHAT_NOPWNAGE_REPEAT_CHECK);
        noPwnageRepeatWeight = data.getInt(ConfPaths.CHAT_NOPWNAGE_REPEAT_WEIGHT);
        noPwnageRepeatTimeout = data.getInt(ConfPaths.CHAT_NOPWNAGE_REPEAT_TIMEOUT);

        noPwnageGlobalCheck = data.getBoolean(ConfPaths.CHAT_NOPWNAGE_GLOBAL_CHECK);
        noPwnageGlobalWeight = data.getInt(ConfPaths.CHAT_NOPWNAGE_GLOBAL_WEIGHT);
        noPwnageGlobalTimeout = data.getLong(ConfPaths.CHAT_NOPWNAGE_GLOBAL_TIMEOUT);

        noPwnageBannedCheck = data.getBoolean(ConfPaths.CHAT_NOPWNAGE_BANNED_CHECK);
        noPwnageBannedWeight = data.getInt(ConfPaths.CHAT_NOPWNAGE_BANNED_WEIGHT);
        noPwnageBannedTimeout = data.getLong(ConfPaths.CHAT_NOPWNAGE_BANNED_TIMEOUT);

        noPwnageRelogCheck = data.getBoolean(ConfPaths.CHAT_NOPWNAGE_RELOG_CHECK);
        noPwnageRelogTime = data.getLong(ConfPaths.CHAT_NOPWNAGE_RELOG_TIME);
        noPwnageRelogWarnings = data.getInt(ConfPaths.CHAT_NOPWNAGE_RELOG_WARNINGS);
        noPwnageRelogTimeout = data.getLong(ConfPaths.CHAT_NOPWNAGE_RELOG_TIMEOUT);

        noPwnageCaptchaCheck = data.getBoolean(ConfPaths.CHAT_NOPWNAGE_CAPTCHA_CHECK);
        noPwnageCaptchaLength = data.getInt(ConfPaths.CHAT_NOPWNAGE_CAPTCHA_LENGTH);
        noPwnageCaptchaCharacters = data.getString(ConfPaths.CHAT_NOPWNAGE_CAPTCHA_CHARACTERS);
        noPwnageCaptchaTries = data.getInt(ConfPaths.CHAT_NOPWNAGE_CAPTCHA_TRIES);

        noPwnageMessagesKick = data.getString(ConfPaths.CHAT_NOPWNAGE_MESSAGES_KICK);
        noPwnageMessagesCaptchaQuestion = data.getString(ConfPaths.CHAT_NOPWNAGE_MESSAGES_CAPTCHAQUESTION);
        noPwnageMessagesCaptchaSuccess = data.getString(ConfPaths.CHAT_NOPWNAGE_MESSAGES_CAPTCHASUCCESS);
        noPwnageMessagesWarnPlayer = data.getString(ConfPaths.CHAT_NOPWNAGE_MESSAGES_WARNPLAYER);
        noPwnageMessagesWarnOthers = data.getString(ConfPaths.CHAT_NOPWNAGE_MESSAGES_WARNOTHERS);
        noPwnageMessagesWarnRelog = data.getString(ConfPaths.CHAT_NOPWNAGE_MESSAGES_WARNRELOG);

        arrivalsLimitCheck = data.getBoolean(ConfPaths.CHAT_ARRIVALSLIMIT_CHECK);
        arrivalsLimitPlayersLimit = data.getInt(ConfPaths.CHAT_ARRIVALSLIMIT_PLAYERSLIMIT);
        arrivalsLimitTimeframe = data.getLong(ConfPaths.CHAT_ARRIVALSLIMIT_TIMEFRAME);
        arrivalsLimitCooldownDelay = data.getLong(ConfPaths.CHAT_ARRIVALSLIMIT_COOLDOWNDELAY);
        arrivalsLimitNewTime = data.getLong(ConfPaths.CHAT_ARRIVALSLIMIT_NEWTIME);
        arrivalsLimitKickMessage = data.getString(ConfPaths.CHAT_ARRIVALSLIMIT_KICKMESSAGE);
        arrivalsLimitActions = data.getActionList(ConfPaths.CHAT_ARRIVALSLIMIT_ACTIONS, Permissions.CHAT_ARRIVALSLIMIT);

        colorCheck = data.getBoolean(ConfPaths.CHAT_COLOR_CHECK);
        colorActions = data.getActionList(ConfPaths.CHAT_COLOR_ACTIONS, Permissions.CHAT_COLOR);
    }
}
