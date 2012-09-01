package fr.neatmonster.nocheatplus.checks.chat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.actions.types.ActionList;
import fr.neatmonster.nocheatplus.checks.CheckConfig;
import fr.neatmonster.nocheatplus.checks.CheckConfigFactory;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.config.ConfPaths;
import fr.neatmonster.nocheatplus.config.ConfigFile;
import fr.neatmonster.nocheatplus.config.ConfigManager;
import fr.neatmonster.nocheatplus.players.Permissions;

/*
 * MM'""""'YMM dP                  dP   MM'""""'YMM                   .8888b oo          
 * M' .mmm. `M 88                  88   M' .mmm. `M                   88   "             
 * M  MMMMMooM 88d888b. .d8888b. d8888P M  MMMMMooM .d8888b. 88d888b. 88aaa  dP .d8888b. 
 * M  MMMMMMMM 88'  `88 88'  `88   88   M  MMMMMMMM 88'  `88 88'  `88 88     88 88'  `88 
 * M. `MMM' .M 88    88 88.  .88   88   M. `MMM' .M 88.  .88 88    88 88     88 88.  .88 
 * MM.     .dM dP    dP `88888P8   dP   MM.     .dM `88888P' dP    dP dP     dP `8888P88 
 * MMMMMMMMMMM                          MMMMMMMMMMM                                  .88 
 *                                                                               d8888P  
 */
/**
 * Configurations specific for the "chat" checks. Every world gets one of these assigned to it, or if a world doesn't
 * get it's own, it will use the "global" version.
 */
public class ChatConfig implements CheckConfig {

    /** The factory creating configurations. */
    public static final CheckConfigFactory factory   = new CheckConfigFactory() {
                                                         @Override
                                                         public final CheckConfig getConfig(final Player player) {
                                                             return ChatConfig.getConfig(player);
                                                         }
                                                     };

    /** The map containing the configurations per world. */
    private static Map<String, ChatConfig> worldsMap = new HashMap<String, ChatConfig>();

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

    public final boolean      colorCheck;
    public final ActionList   colorActions;
    
    public final boolean      globalChatCheck;
	public final float        globalChatFrequencyFactor;
	public final float        globalChatFrequencyWeight;
	public final double       globalChatLevel;
    public final ActionList   globalChatActions;

    public final boolean      noPwnageCheck;
    public final List<String> noPwnageExclusions;
    public final int          noPwnageLevel;

    public final boolean      noPwnageBannedCheck;
    public final long         noPwnageBannedTimeout;
    public final int          noPwnageBannedWeight;

    public final boolean      noPwnageCaptchaCheck;
    public final String       noPwnageCaptchaCharacters;
    public final int          noPwnageCaptchaLength;
    public final String       noPwnageCaptchaQuestion;
    public final String       noPwnageCaptchaSuccess;
    public final int          noPwnageCaptchaTries;
    public final ActionList   noPwnageCaptchaActions;

    public final boolean      noPwnageFirstCheck;
    public final long         noPwnageFirstTimeout;
    public final int          noPwnageFirstWeight;

    public final boolean      noPwnageGlobalCheck;
    public final long         noPwnageGlobalTimeout;
    public final int          noPwnageGlobalWeight;

    public final boolean      noPwnageMoveCheck;
    public final long         noPwnageMoveTimeout;
    public final int          noPwnageMoveWeight;

    public final boolean      noPwnageReloginCheck;
    public final String       noPwnageReloginKickMessage;
    public final long         noPwnageReloginTimeout;
    public final String       noPwnageReloginWarningMessage;
    public final int          noPwnageReloginWarningNumber;
    public final long         noPwnageReloginWarningTimeout;

    public final boolean      noPwnageRepeatCheck;
    public final long         noPwnageRepeatTimeout;
    public final int          noPwnageRepeatWeight;

    public final boolean      noPwnageSpeedCheck;
    public final long         noPwnageSpeedTimeout;
    public final int          noPwnageSpeedWeight;

    public final int          noPwnageWarnLevel;
    public final long         noPwnageWarnTimeout;
    public final boolean      noPwnageWarnOthersCheck;
    public final String       noPwnageWarnOthersMessage;
    public final boolean      noPwnageWarnPlayerCheck;
    public final String       noPwnageWarnPlayerMessage;

    public final ActionList   noPwnageActions;

    public final boolean      opInConsoleOnly;

    public final boolean      protectPlugins;

    /**
     * Instantiates a new chat configuration.
     * 
     * @param data
     *            the data
     */
    public ChatConfig(final ConfigFile data) {
        colorCheck = data.getBoolean(ConfPaths.CHAT_COLOR_CHECK);
        colorActions = data.getActionList(ConfPaths.CHAT_COLOR_ACTIONS, Permissions.CHAT_COLOR);
        
        globalChatCheck = data.getBoolean(ConfPaths.CHAT_GLOBALCHAT_CHECK);
        globalChatFrequencyFactor = (float) data.getDouble(ConfPaths.CHAT_GLOBALCHAT_FREQUENCY_FACTOR);
        globalChatFrequencyWeight = (float) data.getDouble(ConfPaths.CHAT_GLOBALCHAT_FREQUENCY_WEIGHT);
    	globalChatLevel = data.getDouble(ConfPaths.CHAT_GLOBALCHAT_LEVEL);
        globalChatActions = data.getActionList(ConfPaths.CHAT_GLOBALCHAT_ACTIONS, Permissions.CHAT_GLOBALCHAT);

        noPwnageCheck = data.getBoolean(ConfPaths.CHAT_NOPWNAGE_CHECK);
        noPwnageExclusions = data.getStringList(ConfPaths.CHAT_NOPWNAGE_EXCLUSIONS);
        noPwnageLevel = data.getInt(ConfPaths.CHAT_NOPWNAGE_LEVEL);

        noPwnageBannedCheck = data.getBoolean(ConfPaths.CHAT_NOPWNAGE_BANNED_CHECK);
        noPwnageBannedTimeout = data.getLong(ConfPaths.CHAT_NOPWNAGE_BANNED_TIMEOUT);
        noPwnageBannedWeight = data.getInt(ConfPaths.CHAT_NOPWNAGE_BANNED_WEIGHT);

        noPwnageCaptchaCheck = data.getBoolean(ConfPaths.CHAT_NOPWNAGE_CAPTCHA_CHECK);
        noPwnageCaptchaCharacters = data.getString(ConfPaths.CHAT_NOPWNAGE_CAPTCHA_CHARACTERS);
        noPwnageCaptchaLength = data.getInt(ConfPaths.CHAT_NOPWNAGE_CAPTCHA_LENGTH);
        noPwnageCaptchaQuestion = data.getString(ConfPaths.CHAT_NOPWNAGE_CAPTCHA_QUESTION);
        noPwnageCaptchaSuccess = data.getString(ConfPaths.CHAT_NOPWNAGE_CAPTCHA_SUCCESS);
        noPwnageCaptchaTries = data.getInt(ConfPaths.CHAT_NOPWNAGE_CAPTCHA_TRIES);
        noPwnageCaptchaActions = data.getActionList(ConfPaths.CHAT_NOPWNAGE_CAPTCHA_ACTIONS, Permissions.CHAT_NOPWNAGE_CAPTCHA);

        noPwnageFirstCheck = data.getBoolean(ConfPaths.CHAT_NOPWNAGE_FIRST_CHECK);
        noPwnageFirstTimeout = data.getLong(ConfPaths.CHAT_NOPWNAGE_FIRST_TIMEOUT);
        noPwnageFirstWeight = data.getInt(ConfPaths.CHAT_NOPWNAGE_FIRST_WEIGHT);

        noPwnageGlobalCheck = data.getBoolean(ConfPaths.CHAT_NOPWNAGE_GLOBAL_CHECK);
        noPwnageGlobalTimeout = data.getLong(ConfPaths.CHAT_NOPWNAGE_GLOBAL_TIMEOUT);
        noPwnageGlobalWeight = data.getInt(ConfPaths.CHAT_NOPWNAGE_GLOBAL_WEIGHT);

        noPwnageMoveCheck = data.getBoolean(ConfPaths.CHAT_NOPWNAGE_MOVE_CHECK);
        noPwnageMoveTimeout = data.getLong(ConfPaths.CHAT_NOPWNAGE_MOVE_TIMEOUT);
        noPwnageMoveWeight = data.getInt(ConfPaths.CHAT_NOPWNAGE_MOVE_WEIGHT);

        noPwnageReloginCheck = data.getBoolean(ConfPaths.CHAT_NOPWNAGE_RELOGIN_CHECK);
        noPwnageReloginKickMessage = data.getString(ConfPaths.CHAT_NOPWNAGE_RELOGIN_KICKMESSAGE);
        noPwnageReloginTimeout = data.getLong(ConfPaths.CHAT_NOPWNAGE_RELOGIN_TIMEOUT);
        noPwnageReloginWarningMessage = data.getString(ConfPaths.CHAT_NOPWNAGE_RELOGIN_WARNING_MESSAGE);
        noPwnageReloginWarningNumber = data.getInt(ConfPaths.CHAT_NOPWNAGE_RELOGIN_WARNING_NUMBER);
        noPwnageReloginWarningTimeout = data.getLong(ConfPaths.CHAT_NOPWNAGE_RELOGIN_WARNING_TIMEOUT);

        noPwnageRepeatCheck = data.getBoolean(ConfPaths.CHAT_NOPWNAGE_REPEAT_CHECK);
        noPwnageRepeatTimeout = data.getLong(ConfPaths.CHAT_NOPWNAGE_REPEAT_TIMEOUT);
        noPwnageRepeatWeight = data.getInt(ConfPaths.CHAT_NOPWNAGE_REPEAT_WEIGHT);

        noPwnageSpeedCheck = data.getBoolean(ConfPaths.CHAT_NOPWNAGE_SPEED_CHECK);
        noPwnageSpeedTimeout = data.getLong(ConfPaths.CHAT_NOPWNAGE_SPEED_TIMEOUT);
        noPwnageSpeedWeight = data.getInt(ConfPaths.CHAT_NOPWNAGE_SPEED_WEIGHT);

        noPwnageWarnLevel = data.getInt(ConfPaths.CHAT_NOPWNAGE_WARN_LEVEL);
        noPwnageWarnTimeout = data.getLong(ConfPaths.CHAT_NOPWNAGE_WARN_TIMEOUT);
        noPwnageWarnOthersCheck = data.getBoolean(ConfPaths.CHAT_NOPWNAGE_WARN_OTHERS_CHECK);
        noPwnageWarnOthersMessage = data.getString(ConfPaths.CHAT_NOPWNAGE_WARN_OTHERS_MESSAGE);
        noPwnageWarnPlayerCheck = data.getBoolean(ConfPaths.CHAT_NOPWNAGE_WARN_PLAYER_CHECK);
        noPwnageWarnPlayerMessage = data.getString(ConfPaths.CHAT_NOPWNAGE_WARN_PLAYER_MESSAGE);

        noPwnageActions = data.getActionList(ConfPaths.CHAT_NOPWNAGE_ACTIONS, Permissions.CHAT_NOPWNAGE);

        opInConsoleOnly = data.getBoolean(ConfPaths.MISCELLANEOUS_OPINCONSOLEONLY);

        protectPlugins = data.getBoolean(ConfPaths.MISCELLANEOUS_PROTECTPLUGINS);
    }

    /* (non-Javadoc)
     * @see fr.neatmonster.nocheatplus.checks.CheckConfig#isEnabled(fr.neatmonster.nocheatplus.checks.CheckType)
     */
    @Override
    public boolean isEnabled(final CheckType checkType) {
        switch (checkType) {
        case CHAT_COLOR:
            return colorCheck;
        case CHAT_GLOBALCHAT:
        	return globalChatCheck;
        case CHAT_NOPWNAGE:
            return noPwnageCheck;
        default:
            return true;
        }
    }
}
