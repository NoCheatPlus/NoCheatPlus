/*
 * This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package fr.neatmonster.nocheatplus.checks.chat;

import fr.neatmonster.nocheatplus.actions.ActionList;
import fr.neatmonster.nocheatplus.checks.access.ACheckConfig;
import fr.neatmonster.nocheatplus.checks.chat.analysis.engine.EnginePlayerConfig;
import fr.neatmonster.nocheatplus.config.ConfPaths;
import fr.neatmonster.nocheatplus.config.ConfigFile;
import fr.neatmonster.nocheatplus.permissions.Permissions;
import fr.neatmonster.nocheatplus.permissions.RegisteredPermission;
import fr.neatmonster.nocheatplus.utilities.ColorUtil;
import fr.neatmonster.nocheatplus.worlds.IWorldData;

/**
 * Configurations specific for the "chat" checks. Every world gets one of these assigned to it, or if a world doesn't
 * get it's own, it will use the "global" version.
 */
public class ChatConfig extends ACheckConfig {


    private static final RegisteredPermission[] preferKeepUpdatedPermissions = new RegisteredPermission[]{
            // Only the permissions needed for async. checking.
            Permissions.CHAT_COLOR,
            Permissions.CHAT_TEXT,
            Permissions.CHAT_CAPTCHA,
            // TODO: COMMANDS, in case of handleascommand?
    };

    public static RegisteredPermission[] getPreferKeepUpdatedPermissions() {
        return preferKeepUpdatedPermissions;
    }

    public final boolean      captchaSkipCommands;
    public final String       captchaCharacters;
    public final int          captchaLength;
    public final String       captchaQuestion;
    public final String       captchaSuccess;
    public final int          captchaTries;
    public final ActionList   captchaActions;

    public final ActionList   colorActions;

    public final double       commandsLevel;
    public final int          commandsShortTermTicks;
    public final double       commandsShortTermLevel;
    public final ActionList   commandsActions;

    // TODO: Sub check types ?
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

    public final boolean      loginsPerWorldCount;
    public final int          loginsSeconds;
    public final int          loginsLimit;
    public final String       loginsKickMessage;
    public final long         loginsStartupDelay;

    public final boolean      consoleOnlyCheck;
    public final String		  consoleOnlyMessage;

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
    public ChatConfig(final IWorldData worldData) {
        super(worldData);
        final ConfigFile config = worldData.getRawConfiguration();

        captchaSkipCommands = config.getBoolean(ConfPaths.CHAT_CAPTCHA_SKIP_COMMANDS);
        captchaCharacters = config.getString(ConfPaths.CHAT_CAPTCHA_CHARACTERS);
        captchaLength = config.getInt(ConfPaths.CHAT_CAPTCHA_LENGTH);
        captchaQuestion = config.getString(ConfPaths.CHAT_CAPTCHA_QUESTION);
        captchaSuccess = config.getString(ConfPaths.CHAT_CAPTCHA_SUCCESS);
        captchaTries = config.getInt(ConfPaths.CHAT_CAPTCHA_TRIES);
        captchaActions = config.getOptimizedActionList(ConfPaths.CHAT_CAPTCHA_ACTIONS, Permissions.CHAT_CAPTCHA);

        colorActions = config.getOptimizedActionList(ConfPaths.CHAT_COLOR_ACTIONS, Permissions.CHAT_COLOR);

        commandsLevel = config.getDouble(ConfPaths.CHAT_COMMANDS_LEVEL);
        commandsShortTermTicks = config.getInt(ConfPaths.CHAT_COMMANDS_SHORTTERM_TICKS);
        commandsShortTermLevel = config.getDouble(ConfPaths.CHAT_COMMANDS_SHORTTERM_LEVEL);;
        commandsActions = config.getOptimizedActionList(ConfPaths.CHAT_COMMANDS_ACTIONS, Permissions.CHAT_COMMANDS);

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

        loginsPerWorldCount = config.getBoolean(ConfPaths.CHAT_LOGINS_PERWORLDCOUNT);
        loginsSeconds = config.getInt(ConfPaths.CHAT_LOGINS_SECONDS);
        loginsLimit = config.getInt(ConfPaths.CHAT_LOGINS_LIMIT);
        loginsKickMessage =  config.getString(ConfPaths.CHAT_LOGINS_KICKMESSAGE);
        loginsStartupDelay = config.getInt(ConfPaths.CHAT_LOGINS_STARTUPDELAY) * 1000;

        relogKickMessage = config.getString(ConfPaths.CHAT_RELOG_KICKMESSAGE);
        relogTimeout = config.getLong(ConfPaths.CHAT_RELOG_TIMEOUT);
        relogWarningMessage = config.getString(ConfPaths.CHAT_RELOG_WARNING_MESSAGE);
        relogWarningNumber = config.getInt(ConfPaths.CHAT_RELOG_WARNING_NUMBER);
        relogWarningTimeout = config.getLong(ConfPaths.CHAT_RELOG_WARNING_TIMEOUT);
        relogActions = config.getOptimizedActionList(ConfPaths.CHAT_RELOG_ACTIONS, Permissions.CHAT_RELOG);

        consoleOnlyCheck = config.getBoolean(ConfPaths.PROTECT_COMMANDS_CONSOLEONLY_ACTIVE);
        consoleOnlyMessage = ColorUtil.replaceColors(config.getString(ConfPaths.PROTECT_COMMANDS_CONSOLEONLY_MSG));

    }

}
