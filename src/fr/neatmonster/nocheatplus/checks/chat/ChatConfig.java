package fr.neatmonster.nocheatplus.checks.chat;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.actions.ActionList;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.access.AsyncCheckConfig;
import fr.neatmonster.nocheatplus.checks.access.CheckConfigFactory;
import fr.neatmonster.nocheatplus.checks.access.ICheckConfig;
import fr.neatmonster.nocheatplus.checks.chat.analysis.engine.EnginePlayerConfig;
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
public class ChatConfig extends AsyncCheckConfig {

    /** The factory creating configurations. */
    public static final CheckConfigFactory factory   = new CheckConfigFactory() {
                                                         @Override
                                                         public final ICheckConfig getConfig(final Player player) {
                                                             return ChatConfig.getConfig(player);
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

    public final boolean      colorCheck;
    public final ActionList   colorActions;
    
    public final boolean      globalChatCheck;
	public final boolean      globalChatGlobalCheck;
	public final boolean      globalChatPlayerCheck;
	public final EnginePlayerConfig globalChatEnginePlayerConfig;
	public final float        globalChatFrequencyFactor;
	public final float        globalChatFrequencyWeight;
	public final float        globalChatGlobalWeight;
	public final float        globalChatPlayerWeight;
	public final double       globalChatLevel;
	public boolean            globalChatEngineMaximum;
	public final boolean      globalChatDebug;
    public final ActionList   globalChatActions;

    public final boolean      noPwnageCheck;
	public final boolean      noPwnageDebug;
    public final int          noPwnageLevel;
    public final float        noPwnageVLFactor;

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
//    public final long         noPwnageSpeedTimeout;
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
     * @param config
     *            the data
     */
    public ChatConfig(final ConfigFile config) {
    	super(new String[]{
    	    	Permissions.CHAT_COLOR,
    	    	Permissions.CHAT_GLOBALCHAT,
    	    	Permissions.CHAT_NOPWNAGE,
    	    	Permissions.CHAT_NOPWNAGE_CAPTCHA,
    	    });
        colorCheck = config.getBoolean(ConfPaths.CHAT_COLOR_CHECK);
        colorActions = config.getActionList(ConfPaths.CHAT_COLOR_ACTIONS, Permissions.CHAT_COLOR);
        
        globalChatCheck = config.getBoolean(ConfPaths.CHAT_GLOBALCHAT_CHECK);
    	globalChatGlobalCheck = config.getBoolean(ConfPaths.CHAT_GLOBALCHAT_GL_CHECK, true);
    	globalChatPlayerCheck = config.getBoolean(ConfPaths.CHAT_GLOBALCHAT_PP_CHECK, true);
        globalChatEnginePlayerConfig = new EnginePlayerConfig(config);
        globalChatFrequencyFactor = (float) config.getDouble(ConfPaths.CHAT_GLOBALCHAT_FREQUENCY_FACTOR);
        globalChatFrequencyWeight = (float) config.getDouble(ConfPaths.CHAT_GLOBALCHAT_FREQUENCY_WEIGHT);
        globalChatGlobalWeight = (float) config.getDouble(ConfPaths.CHAT_GLOBALCHAT_GL_WEIGHT, 1.0);
        globalChatPlayerWeight = (float) config.getDouble(ConfPaths.CHAT_GLOBALCHAT_PP_WEIGHT, 1.0);
    	globalChatLevel = config.getDouble(ConfPaths.CHAT_GLOBALCHAT_LEVEL);
    	globalChatEngineMaximum = config.getBoolean(ConfPaths.CHAT_GLOBALCHAT_ENGINE_MAXIMUM, true);
    	globalChatDebug = config.getBoolean(ConfPaths.CHAT_GLOBALCHAT_DEBUG, false);
        globalChatActions = config.getActionList(ConfPaths.CHAT_GLOBALCHAT_ACTIONS, Permissions.CHAT_GLOBALCHAT);

        noPwnageCheck = config.getBoolean(ConfPaths.CHAT_NOPWNAGE_CHECK);
        noPwnageDebug = config.getBoolean(ConfPaths.CHAT_NOPWNAGE_DEBUG, false);
        noPwnageLevel = config.getInt(ConfPaths.CHAT_NOPWNAGE_LEVEL);
        // VL decreasing factor, hidden option.
        noPwnageVLFactor = (float) config.getDouble(ConfPaths.CHAT_NOPWNAGE_VL_FACTOR, 0.9);

        noPwnageBannedCheck = config.getBoolean(ConfPaths.CHAT_NOPWNAGE_BANNED_CHECK);
        noPwnageBannedTimeout = config.getLong(ConfPaths.CHAT_NOPWNAGE_BANNED_TIMEOUT);
        noPwnageBannedWeight = config.getInt(ConfPaths.CHAT_NOPWNAGE_BANNED_WEIGHT);

        noPwnageCaptchaCheck = config.getBoolean(ConfPaths.CHAT_NOPWNAGE_CAPTCHA_CHECK);
        noPwnageCaptchaCharacters = config.getString(ConfPaths.CHAT_NOPWNAGE_CAPTCHA_CHARACTERS);
        noPwnageCaptchaLength = config.getInt(ConfPaths.CHAT_NOPWNAGE_CAPTCHA_LENGTH);
        noPwnageCaptchaQuestion = config.getString(ConfPaths.CHAT_NOPWNAGE_CAPTCHA_QUESTION);
        noPwnageCaptchaSuccess = config.getString(ConfPaths.CHAT_NOPWNAGE_CAPTCHA_SUCCESS);
        noPwnageCaptchaTries = config.getInt(ConfPaths.CHAT_NOPWNAGE_CAPTCHA_TRIES);
        noPwnageCaptchaActions = config.getActionList(ConfPaths.CHAT_NOPWNAGE_CAPTCHA_ACTIONS, Permissions.CHAT_NOPWNAGE_CAPTCHA);

        noPwnageFirstCheck = config.getBoolean(ConfPaths.CHAT_NOPWNAGE_FIRST_CHECK);
        noPwnageFirstTimeout = config.getLong(ConfPaths.CHAT_NOPWNAGE_FIRST_TIMEOUT);
        noPwnageFirstWeight = config.getInt(ConfPaths.CHAT_NOPWNAGE_FIRST_WEIGHT);

        noPwnageGlobalCheck = config.getBoolean(ConfPaths.CHAT_NOPWNAGE_GLOBAL_CHECK);
        noPwnageGlobalTimeout = config.getLong(ConfPaths.CHAT_NOPWNAGE_GLOBAL_TIMEOUT);
        noPwnageGlobalWeight = config.getInt(ConfPaths.CHAT_NOPWNAGE_GLOBAL_WEIGHT);

        noPwnageMoveCheck = config.getBoolean(ConfPaths.CHAT_NOPWNAGE_MOVE_CHECK);
        noPwnageMoveTimeout = config.getLong(ConfPaths.CHAT_NOPWNAGE_MOVE_TIMEOUT);
        noPwnageMoveWeight = config.getInt(ConfPaths.CHAT_NOPWNAGE_MOVE_WEIGHT);

        noPwnageReloginCheck = config.getBoolean(ConfPaths.CHAT_NOPWNAGE_RELOGIN_CHECK);
        noPwnageReloginKickMessage = config.getString(ConfPaths.CHAT_NOPWNAGE_RELOGIN_KICKMESSAGE);
        noPwnageReloginTimeout = config.getLong(ConfPaths.CHAT_NOPWNAGE_RELOGIN_TIMEOUT);
        noPwnageReloginWarningMessage = config.getString(ConfPaths.CHAT_NOPWNAGE_RELOGIN_WARNING_MESSAGE);
        noPwnageReloginWarningNumber = config.getInt(ConfPaths.CHAT_NOPWNAGE_RELOGIN_WARNING_NUMBER);
        noPwnageReloginWarningTimeout = config.getLong(ConfPaths.CHAT_NOPWNAGE_RELOGIN_WARNING_TIMEOUT);

        noPwnageRepeatCheck = config.getBoolean(ConfPaths.CHAT_NOPWNAGE_REPEAT_CHECK);
        noPwnageRepeatTimeout = config.getLong(ConfPaths.CHAT_NOPWNAGE_REPEAT_TIMEOUT);
        noPwnageRepeatWeight = config.getInt(ConfPaths.CHAT_NOPWNAGE_REPEAT_WEIGHT);

        noPwnageSpeedCheck = config.getBoolean(ConfPaths.CHAT_NOPWNAGE_SPEED_CHECK);
//        noPwnageSpeedTimeout = data.getLong(ConfPaths.CHAT_NOPWNAGE_SPEED_TIMEOUT);
        noPwnageSpeedWeight = config.getInt(ConfPaths.CHAT_NOPWNAGE_SPEED_WEIGHT);

        noPwnageWarnLevel = config.getInt(ConfPaths.CHAT_NOPWNAGE_WARN_LEVEL);
        noPwnageWarnTimeout = config.getLong(ConfPaths.CHAT_NOPWNAGE_WARN_TIMEOUT);
        noPwnageWarnOthersCheck = config.getBoolean(ConfPaths.CHAT_NOPWNAGE_WARN_OTHERS_CHECK);
        noPwnageWarnOthersMessage = config.getString(ConfPaths.CHAT_NOPWNAGE_WARN_OTHERS_MESSAGE);
        noPwnageWarnPlayerCheck = config.getBoolean(ConfPaths.CHAT_NOPWNAGE_WARN_PLAYER_CHECK);
        noPwnageWarnPlayerMessage = config.getString(ConfPaths.CHAT_NOPWNAGE_WARN_PLAYER_MESSAGE);

        noPwnageActions = config.getActionList(ConfPaths.CHAT_NOPWNAGE_ACTIONS, Permissions.CHAT_NOPWNAGE);

        opInConsoleOnly = config.getBoolean(ConfPaths.MISCELLANEOUS_OPINCONSOLEONLY);

        protectPlugins = config.getBoolean(ConfPaths.MISCELLANEOUS_PROTECTPLUGINS);
    }

    /* (non-Javadoc)
     * @see fr.neatmonster.nocheatplus.checks.ICheckConfig#isEnabled(fr.neatmonster.nocheatplus.checks.CheckType)
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
