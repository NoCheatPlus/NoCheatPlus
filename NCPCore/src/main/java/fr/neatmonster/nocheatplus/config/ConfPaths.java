package fr.neatmonster.nocheatplus.config;

/**
 * Paths for the configuration options. Making everything final static prevents accidentally modifying any of these.
 */
public abstract class ConfPaths {
	
    // Sub-paths that are used with different path prefixes potentially.
	// TODO: These might better be in another class.
    public static final String SUB_DEBUG 								 = "debug";
    public static final String SUB_IGNOREPASSABLE                        = "ignorepassable";
	public static final String SUB_ALLOWINSTANTBREAK                     = "allowinstantbreak";
	public static final String SUB_OVERRIDEFLAGS					 	 = "overrideflags";
    public static final String SUB_LAG       							 = "lag";
	
	// General.
	public static final String SAVEBACKCONFIG                            = "savebackconfig";
	
	// Configuration version.
	@GlobalConfig // TODO: Per file versions should also be supported. Better with per-path comparison?
	public static final String CONFIGVERSION							 = "configversion.";
	public static final String CONFIGVERSION_NOTIFY						 = CONFIGVERSION + "notify";
	/** Build number of the build for which the default config was first created (DefaultConfig.buildNumber), updated with first save. */
	public static final String CONFIGVERSION_CREATED					 = CONFIGVERSION + "created";
	/** Build number of the build for which the default config was first created (DefaultConfig.buildNumber), updated with each save. */
	public static final String CONFIGVERSION_SAVED						 = CONFIGVERSION + "saved";

	@GlobalConfig
    private static final String LOGGING                                  = "logging.";
    public static final String  LOGGING_ACTIVE                           = LOGGING + "active";
    public static final String  LOGGING_DEBUG                            = LOGGING + "debug";
    public static final String  LOGGING_MAXQUEUESIZE                     = LOGGING + "maxqueuesize";
    
    private static final String LOGGING_BACKEND							 = LOGGING + "backend.";
    private static final String LOGGING_BACKEND_CONSOLE					 = LOGGING_BACKEND + "console.";
    public static final String  LOGGING_BACKEND_CONSOLE_ACTIVE           = LOGGING_BACKEND_CONSOLE + "active";
    public static final String  LOGGING_BACKEND_CONSOLE_PREFIX			 = LOGGING_BACKEND_CONSOLE + "prefix";
    private static final String LOGGING_BACKEND_FILE					 = LOGGING_BACKEND + "file.";
    public static final String  LOGGING_BACKEND_FILE_ACTIVE	        	 = LOGGING_BACKEND_FILE + "active";
    public static final String  LOGGING_BACKEND_FILE_FILENAME	         = LOGGING_BACKEND_FILE + "filename";
    public static final String  LOGGING_BACKEND_FILE_PREFIX				 = LOGGING_BACKEND_FILE + "prefix";
    private static final String LOGGING_BACKEND_INGAMECHAT				 = LOGGING_BACKEND + "ingamechat.";
    public static final String  LOGGING_BACKEND_INGAMECHAT_ACTIVE      	 = LOGGING_BACKEND_INGAMECHAT + "active";
    public static final String  LOGGING_BACKEND_INGAMECHAT_SUBSCRIPTIONS = LOGGING_BACKEND_INGAMECHAT + "subscriptions";
    public static final String  LOGGING_BACKEND_INGAMECHAT_PREFIX		 = LOGGING_BACKEND_INGAMECHAT + "prefix";

	@GlobalConfig
	private static final String MISCELLANEOUS = "miscellaneous.";
	//public static final String  MISCELLANEOUS_CHECKFORUPDATES				= MISCELLANEOUS + "checkforupdates";
	//public static final String  MISCELLANEOUS_UPDATETIMEOUT					= MISCELLANEOUS + "updatetimeout";

	/** TEMP: hidden flag to disable all lag adaption with one flag. */
	public static final String MISCELLANEOUS_LAG						 	= MISCELLANEOUS + "lag";

	// Extended data-related settings.
    @GlobalConfig
    private static final String DATA                                     = "data.";
    // Expired data removal.
    private static final String DATA_EXPIRATION                          = DATA + "expiration.";
    public static final String  DATA_EXPIRATION_ACTIVE                   = DATA_EXPIRATION + "active";
	public static final String  DATA_EXPIRATION_DURATION                 = DATA_EXPIRATION + "duration";
	public static final String  DATA_EXPIRATION_DATA                     = DATA_EXPIRATION + "data";
	public static final String  DATA_EXPIRATION_HISTORY                  = DATA_EXPIRATION + "history";
	// Consistency checking.
	private static final String DATA_CONSISTENCYCHECKS					 = DATA + "consistencychecks.";
	public static final  String DATA_CONSISTENCYCHECKS_CHECK			 = DATA_CONSISTENCYCHECKS + "active";
	public static final  String DATA_CONSISTENCYCHECKS_INTERVAL			 = DATA_CONSISTENCYCHECKS + "interval";
	public static final  String DATA_CONSISTENCYCHECKS_MAXTIME			 = DATA_CONSISTENCYCHECKS + "maxtime";
	/**
	 * This might not might not be used by checks. <br>
	 * Used by: DataMan/Player-instances
	 * 
	 */
	public static final  String DATA_CONSISTENCYCHECKS_SUPPRESSWARNINGS  = DATA_CONSISTENCYCHECKS + "suppresswarnings";
	
	private static final String PROTECT									 = "protection.";
	// Clients settings.
	private static final String PROTECT_CLIENTS							 = PROTECT + "clients.";
	@GlobalConfig
	private static final String PROTECT_CLIENTS_MOTD					 = PROTECT_CLIENTS + "motd.";
	public static final String  PROTECT_CLIENTS_MOTD_ACTIVE				 = PROTECT_CLIENTS_MOTD + "active";
	public static final String  PROTECT_CLIENTS_MOTD_ALLOWALL			 = PROTECT_CLIENTS_MOTD + "allowall";
	// Other commands settings
	@GlobalConfig
	private static final String PROTECT_COMMANDS						 = PROTECT + "commands.";
	private static final String PROTECT_COMMANDS_CONSOLEONLY			 = PROTECT_COMMANDS + "consoleonly.";
	public  static final String PROTECT_COMMANDS_CONSOLEONLY_ACTIVE		 = PROTECT_COMMANDS_CONSOLEONLY + "active";
	public  static final String PROTECT_COMMANDS_CONSOLEONLY_MSG		 = PROTECT_COMMANDS_CONSOLEONLY + "message";
	public  static final String PROTECT_COMMANDS_CONSOLEONLY_CMDS		 = PROTECT_COMMANDS_CONSOLEONLY + "commands";
	// Plugins settings.
	private static final String PROTECT_PLUGINS							 = PROTECT + "plugins.";
	@GlobalConfig
	private static final String PROTECT_PLUGINS_HIDE					 = PROTECT_PLUGINS + "hide.";
	public static  final String PROTECT_PLUGINS_HIDE_ACTIVE				 = PROTECT_PLUGINS_HIDE + "active";
	private static final String PROTECT_PLUGINS_HIDE_NOCOMMAND			 = PROTECT_PLUGINS_HIDE + "unknowncommand.";
	public static  final String PROTECT_PLUGINS_HIDE_NOCOMMAND_MSG		 = PROTECT_PLUGINS_HIDE_NOCOMMAND + "message";
	public static  final String PROTECT_PLUGINS_HIDE_NOCOMMAND_CMDS		 = PROTECT_PLUGINS_HIDE_NOCOMMAND + "commands";
	private static final String PROTECT_PLUGINS_HIDE_NOPERMISSION		 = PROTECT_PLUGINS_HIDE + "nopermission.";
	public static  final String PROTECT_PLUGINS_HIDE_NOPERMISSION_MSG	 = PROTECT_PLUGINS_HIDE_NOPERMISSION + "message";
	public static  final String PROTECT_PLUGINS_HIDE_NOPERMISSION_CMDS	 = PROTECT_PLUGINS_HIDE_NOPERMISSION + "commands";
	
	// Checks!
    private static final String CHECKS                                   = "checks.";
    /** Debug flag to debug all checks (!), individual sections debug flags override this, if present. */
	public static final  String CHECKS_DEBUG							 = CHECKS + SUB_DEBUG;
    public static final String  BLOCKBREAK                               = CHECKS + "blockbreak.";
    
	public static final String  BLOCKBREAK_DEBUG						 = BLOCKBREAK + "debug";
    

    private static final String BLOCKBREAK_DIRECTION                     = BLOCKBREAK + "direction.";
    public static final String  BLOCKBREAK_DIRECTION_CHECK               = BLOCKBREAK_DIRECTION + "active";
    public static final String  BLOCKBREAK_DIRECTION_ACTIONS             = BLOCKBREAK_DIRECTION + "actions";

    private static final String BLOCKBREAK_FASTBREAK                     = BLOCKBREAK + "fastbreak.";
    public static final String  BLOCKBREAK_FASTBREAK_CHECK               = BLOCKBREAK_FASTBREAK + "active";
    public static final String  BLOCKBREAK_FASTBREAK_STRICT				 = BLOCKBREAK_FASTBREAK + "strict";
	public static final  String BLOCKBREAK_FASTBREAK_DEBUG               = BLOCKBREAK_FASTBREAK + "debug";
    private static final String BLOCKBREAK_FASTBREAK_BUCKETS             = BLOCKBREAK + "buckets.";
	public static final String  BLOCKBREAK_FASTBREAK_BUCKETS_CONTENTION  = BLOCKBREAK_FASTBREAK_BUCKETS + "contention";
	@GlobalConfig
	public static final String  BLOCKBREAK_FASTBREAK_BUCKETS_N           = BLOCKBREAK_FASTBREAK_BUCKETS + "number";
	@GlobalConfig
	public static final String  BLOCKBREAK_FASTBREAK_BUCKETS_DUR         = BLOCKBREAK_FASTBREAK_BUCKETS + "duration";
	public static final String  BLOCKBREAK_FASTBREAK_BUCKETS_FACTOR      = BLOCKBREAK_FASTBREAK_BUCKETS + "factor";
	public static final String  BLOCKBREAK_FASTBREAK_DELAY               = BLOCKBREAK_FASTBREAK + "delay";
	public static final String  BLOCKBREAK_FASTBREAK_GRACE               = BLOCKBREAK_FASTBREAK + "grace";
	public static final String  BLOCKBREAK_FASTBREAK_MOD_SURVIVAL        = BLOCKBREAK_FASTBREAK + "intervalsurvival";
    public static final String  BLOCKBREAK_FASTBREAK_ACTIONS             = BLOCKBREAK_FASTBREAK + "actions";
    
	private static final String BLOCKBREAK_FREQUENCY                     = BLOCKBREAK + "frequency.";
	public static final String  BLOCKBREAK_FREQUENCY_CHECK               = BLOCKBREAK_FREQUENCY + "active";
	public static final String  BLOCKBREAK_FREQUENCY_MOD_CREATIVE        = BLOCKBREAK_FREQUENCY + "intervalcreative";
	public static final String  BLOCKBREAK_FREQUENCY_MOD_SURVIVAL        = BLOCKBREAK_FREQUENCY + "intervalsurvival";
	private static final String BLOCKBREAK_FREQUENCY_BUCKETS             = BLOCKBREAK_FREQUENCY + "buckets.";
	@GlobalConfig
	public static final String  BLOCKBREAK_FREQUENCY_BUCKETS_DUR         = BLOCKBREAK_FREQUENCY_BUCKETS + "duration";
	public static final String  BLOCKBREAK_FREQUENCY_BUCKETS_FACTOR      = BLOCKBREAK_FREQUENCY_BUCKETS + "factor";
	@GlobalConfig
	public static final String  BLOCKBREAK_FREQUENCY_BUCKETS_N           = BLOCKBREAK_FREQUENCY_BUCKETS + "number";
	private static final String BLOCKBREAK_FREQUENCY_SHORTTERM           = BLOCKBREAK_FREQUENCY + "shortterm.";
	public static final String  BLOCKBREAK_FREQUENCY_SHORTTERM_LIMIT     = BLOCKBREAK_FREQUENCY_SHORTTERM + "limit";
	public static final String  BLOCKBREAK_FREQUENCY_SHORTTERM_TICKS     = BLOCKBREAK_FREQUENCY_SHORTTERM + "ticks";
	public static final String  BLOCKBREAK_FREQUENCY_ACTIONS             = BLOCKBREAK_FREQUENCY + "actions";

    private static final String BLOCKBREAK_NOSWING                       = BLOCKBREAK + "noswing.";
    public static final String  BLOCKBREAK_NOSWING_CHECK                 = BLOCKBREAK_NOSWING + "active";
    public static final String  BLOCKBREAK_NOSWING_ACTIONS               = BLOCKBREAK_NOSWING + "actions";

    private static final String BLOCKBREAK_REACH                         = BLOCKBREAK + "reach.";
    public static final String  BLOCKBREAK_REACH_CHECK                   = BLOCKBREAK_REACH + "active";
    public static final String  BLOCKBREAK_REACH_ACTIONS                 = BLOCKBREAK_REACH + "actions";
    
    private static final String BLOCKBREAK_WRONGBLOCK                    = BLOCKBREAK + "wrongblock.";
	public static final String  BLOCKBREAK_WRONGBLOCK_CHECK              = BLOCKBREAK_WRONGBLOCK + "active";
	public static final String  BLOCKBREAK_WRONGBLOCK_LEVEL              = BLOCKBREAK_WRONGBLOCK + "level";
	public static final String  BLOCKBREAK_WRONGBLOCK_ACTIONS            = BLOCKBREAK_WRONGBLOCK + "actions";

    public static final String BLOCKINTERACT                            = CHECKS + "blockinteract.";

    private static final String BLOCKINTERACT_DIRECTION                  = BLOCKINTERACT + "direction.";
    public static final String  BLOCKINTERACT_DIRECTION_CHECK            = BLOCKINTERACT_DIRECTION + "active";
    public static final String  BLOCKINTERACT_DIRECTION_ACTIONS          = BLOCKINTERACT_DIRECTION + "actions";

    private static final String BLOCKINTERACT_REACH                      = BLOCKINTERACT + "reach.";
    public static final String  BLOCKINTERACT_REACH_CHECK                = BLOCKINTERACT_REACH + "active";
    public static final String  BLOCKINTERACT_REACH_ACTIONS              = BLOCKINTERACT_REACH + "actions";
    
    private static final String BLOCKINTERACT_SPEED						 = BLOCKINTERACT + "speed.";
	public static final String BLOCKINTERACT_SPEED_CHECK				 = BLOCKINTERACT_SPEED + "active";
	public static final String BLOCKINTERACT_SPEED_INTERVAL				 = BLOCKINTERACT_SPEED + "interval";
	public static final String BLOCKINTERACT_SPEED_LIMIT				 = BLOCKINTERACT_SPEED + "limit";
	public static final String BLOCKINTERACT_SPEED_ACTIONS				 = BLOCKINTERACT_SPEED + "actions";
    
    private static final String BLOCKINTERACT_VISIBLE					 = BLOCKINTERACT + "visible.";
	public static final String  BLOCKINTERACT_VISIBLE_CHECK				 = BLOCKINTERACT_VISIBLE + "active";
	public static final String  BLOCKINTERACT_VISIBLE_ACTIONS			 = BLOCKINTERACT_VISIBLE + "actions";

    // BLOCKPLACE
    public static final String  BLOCKPLACE                               = CHECKS + "blockplace.";
    
    private static final String BLOCKPLACE_AGAINST						 = BLOCKPLACE + "against.";
    public static final String  BLOCKPLACE_AGAINST_CHECK				 = BLOCKPLACE_AGAINST + "active";
	public static final String BLOCKPLACE_AGAINST_ACTIONS				 = BLOCKPLACE_AGAINST + "actions";
    
    private static final String BLOCKPLACE_AUTOSIGN						 = BLOCKPLACE + "autosign.";
	public static final String  BLOCKPLACE_AUTOSIGN_CHECK				 = BLOCKPLACE_AUTOSIGN + "active";
	public static final String  BLOCKPLACE_AUTOSIGN_ACTIONS				 = BLOCKPLACE_AUTOSIGN + "actions";

    private static final String BLOCKPLACE_DIRECTION                     = BLOCKPLACE + "direction.";
    public static final String  BLOCKPLACE_DIRECTION_CHECK               = BLOCKPLACE_DIRECTION + "active";
    public static final String  BLOCKPLACE_DIRECTION_ACTIONS             = BLOCKPLACE_DIRECTION + "actions";

    private static final String BLOCKPLACE_FASTPLACE                     = BLOCKPLACE + "fastplace.";
    public static final String  BLOCKPLACE_FASTPLACE_CHECK               = BLOCKPLACE_FASTPLACE + "active";
	public static final String  BLOCKPLACE_FASTPLACE_LIMIT               = BLOCKPLACE_FASTPLACE + "limit";
	private static final String BLOCKPLACE_FASTPLACE_SHORTTERM			 = BLOCKPLACE_FASTPLACE + "shortterm.";
	public static final String  BLOCKPLACE_FASTPLACE_SHORTTERM_TICKS	 = BLOCKPLACE_FASTPLACE_SHORTTERM + "ticks";
	public static final String  BLOCKPLACE_FASTPLACE_SHORTTERM_LIMIT	 = BLOCKPLACE_FASTPLACE_SHORTTERM + "limit";
    public static final String  BLOCKPLACE_FASTPLACE_ACTIONS             = BLOCKPLACE_FASTPLACE + "actions";

    private static final String BLOCKPLACE_NOSWING                       = BLOCKPLACE + "noswing.";
    public static final String  BLOCKPLACE_NOSWING_CHECK                 = BLOCKPLACE_NOSWING + "active";
    public static final String  BLOCKPLACE_NOSWING_EXCEPTIONS			 = BLOCKPLACE_NOSWING + "exceptions";
    public static final String  BLOCKPLACE_NOSWING_ACTIONS               = BLOCKPLACE_NOSWING + "actions";

    private static final String BLOCKPLACE_REACH                         = BLOCKPLACE + "reach.";
    public static final String  BLOCKPLACE_REACH_CHECK                   = BLOCKPLACE_REACH + "active";
    public static final String  BLOCKPLACE_REACH_ACTIONS                 = BLOCKPLACE_REACH + "actions";

    private static final String BLOCKPLACE_SPEED                         = BLOCKPLACE + "speed.";
    public static final String  BLOCKPLACE_SPEED_CHECK                   = BLOCKPLACE_SPEED + "active";
    public static final String  BLOCKPLACE_SPEED_INTERVAL                = BLOCKPLACE_SPEED + "interval";
    public static final String  BLOCKPLACE_SPEED_ACTIONS                 = BLOCKPLACE_SPEED + "actions";

    public static final String  CHAT                                     = CHECKS + "chat.";

    private static final String CHAT_CAPTCHA                    = CHAT + "captcha.";
    public static final String  CHAT_CAPTCHA_CHECK              = CHAT_CAPTCHA + "active";
    public static final String  CHAT_CAPTCHA_CHARACTERS         = CHAT_CAPTCHA + "characters";
    public static final String  CHAT_CAPTCHA_LENGTH             = CHAT_CAPTCHA + "length";
    public static final String  CHAT_CAPTCHA_QUESTION           = CHAT_CAPTCHA + "question";
    public static final String  CHAT_CAPTCHA_SUCCESS            = CHAT_CAPTCHA + "success";
    public static final String  CHAT_CAPTCHA_TRIES              = CHAT_CAPTCHA + "tries";
    public static final String  CHAT_CAPTCHA_ACTIONS            = CHAT_CAPTCHA + "actions";
	
    private static final String CHAT_COLOR                               = CHAT + "color.";
    public static final String  CHAT_COLOR_CHECK                         = CHAT_COLOR + "active";
    public static final String  CHAT_COLOR_ACTIONS                       = CHAT_COLOR + "actions";
    
    private static final String CHAT_COMMANDS                            = CHAT + "commands.";
    public static final String  CHAT_COMMANDS_CHECK                      = CHAT_COMMANDS + "active";
    @GlobalConfig
    public static final String  CHAT_COMMANDS_EXCLUSIONS                 = CHAT_COMMANDS + "exclusions";
    @GlobalConfig
    public static final String CHAT_COMMANDS_HANDLEASCHAT                = CHAT_COMMANDS + "handleaschat";
    public static final String  CHAT_COMMANDS_LEVEL                      = CHAT_COMMANDS + "level";
    private static final String CHAT_COMMANDS_SHORTTERM                  = CHAT_COMMANDS + "shortterm.";
    public static final String  CHAT_COMMANDS_SHORTTERM_TICKS            = CHAT_COMMANDS_SHORTTERM + "ticks";
    public static final String  CHAT_COMMANDS_SHORTTERM_LEVEL            = CHAT_COMMANDS_SHORTTERM + "level";
    public static final String  CHAT_COMMANDS_ACTIONS                    = CHAT_COMMANDS + "actions";
    
    // Text
    private static final String CHAT_TEXT                                 = CHAT + "text.";
    public static final String CHAT_TEXT_CHECK                            = CHAT_TEXT + "active";
	public static final String CHAT_TEXT_DEBUG                       = CHAT_TEXT + "debug";
	public static final String CHAT_TEXT_ENGINE_MAXIMUM              = CHAT_TEXT + "maximum";
	public static final String CHAT_TEXT_ALLOWVLRESET				 = CHAT_TEXT + "allowvlreset";
    public static final String CHAT_TEXT_FREQ                        = CHAT_TEXT + "frequency.";
    public static final String CHAT_TEXT_FREQ_NORM                   = CHAT_TEXT_FREQ + "normal.";
	public static final String CHAT_TEXT_FREQ_NORM_FACTOR            = CHAT_TEXT_FREQ_NORM + "factor";
	public static final String CHAT_TEXT_FREQ_NORM_LEVEL             = CHAT_TEXT_FREQ_NORM + "level";
	public static final String CHAT_TEXT_FREQ_NORM_WEIGHT            = CHAT_TEXT_FREQ_NORM + "weight";
	public static final String CHAT_TEXT_FREQ_NORM_MIN               = CHAT_TEXT_FREQ_NORM + "minimum";
    public static final String CHAT_TEXT_FREQ_NORM_ACTIONS           = CHAT_TEXT_FREQ_NORM + "actions";
	private static final String CHAT_TEXT_FREQ_SHORTTERM             = CHAT_TEXT_FREQ + "shortterm.";
    public static final String CHAT_TEXT_FREQ_SHORTTERM_FACTOR       = CHAT_TEXT_FREQ_SHORTTERM + "factor";
    public static final String CHAT_TEXT_FREQ_SHORTTERM_LEVEL        = CHAT_TEXT_FREQ_SHORTTERM + "level";
    public static final String CHAT_TEXT_FREQ_SHORTTERM_WEIGHT       = CHAT_TEXT_FREQ_SHORTTERM + "weight";
    public static final String  CHAT_TEXT_FREQ_SHORTTERM_MIN         = CHAT_TEXT_FREQ_SHORTTERM + "minimum";
    public static final String CHAT_TEXT_FREQ_SHORTTERM_ACTIONS      = CHAT_TEXT_FREQ_SHORTTERM + "actions";
    
	// (Some of the following paths must be public for generic config reading.)
	// Per message checks.
	private static final String CHAT_TEXT_MSG                      = CHAT_TEXT + "message.";
    public static final String  CHAT_TEXT_MSG_LETTERCOUNT          = CHAT_TEXT_MSG + "lettercount";
    public static final String  CHAT_TEXT_MSG_PARTITION            = CHAT_TEXT_MSG + "partition";
	public static final String  CHAT_TEXT_MSG_UPPERCASE            = CHAT_TEXT_MSG + "uppercase";
	
    public static final String CHAT_TEXT_MSG_REPEATCANCEL          = CHAT_TEXT_MSG + "repeatviolation";
    public static final String CHAT_TEXT_MSG_AFTERJOIN             = CHAT_TEXT_MSG + "afterjoin";
    public static final String CHAT_TEXT_MSG_REPEATSELF            = CHAT_TEXT_MSG + "repeatself";
    public static final String CHAT_TEXT_MSG_REPEATGLOBAL          = CHAT_TEXT_MSG + "repeatglobal";
    public static final String CHAT_TEXT_MSG_NOMOVING              = CHAT_TEXT_MSG + "nomoving";
	
	private static final String CHAT_TEXT_MSG_WORDS                = CHAT_TEXT_MSG + "words.";
	public static final String  CHAT_TEXT_MSG_WORDS_LENGTHAV       = CHAT_TEXT_MSG_WORDS + "lengthav";
	public static final String  CHAT_TEXT_MSG_WORDS_LENGTHMSG      = CHAT_TEXT_MSG_WORDS + "lengthmsg";
	public static final String  CHAT_TEXT_MSG_WORDS_NOLETTER       = CHAT_TEXT_MSG_WORDS + "noletter";
    // Extended global checks.
    private static final String CHAT_TEXT_GL                       = CHAT_TEXT + "global.";
	public static final String CHAT_TEXT_GL_CHECK                  = CHAT_TEXT_GL + "active";
	public static final String CHAT_TEXT_GL_WEIGHT                 = CHAT_TEXT_GL + "weight";
	@GlobalConfig
    public static final String CHAT_TEXT_GL_WORDS                  = CHAT_TEXT_GL + "words.";
	public static final String CHAT_TEXT_GL_WORDS_CHECK            = CHAT_TEXT_GL_WORDS + "active";
	@GlobalConfig
	public static final String CHAT_TEXT_GL_PREFIXES               = CHAT_TEXT_GL + "prefixes.";
	public static final String CHAT_TEXT_GL_PREFIXES_CHECK         = CHAT_TEXT_GL_PREFIXES + "active";
	@GlobalConfig
	public static final String CHAT_TEXT_GL_SIMILARITY             = CHAT_TEXT_GL + "similarity.";
	public static final String CHAT_TEXT_GL_SIMILARITY_CHECK       = CHAT_TEXT_GL_SIMILARITY + "active";
	// Extended per player checks.
	private static final String CHAT_TEXT_PP                       = CHAT_TEXT + "player.";
	public static final String CHAT_TEXT_PP_CHECK                  = CHAT_TEXT_PP + "active";
	public static final String CHAT_TEXT_PP_WEIGHT                 = CHAT_TEXT_PP + "weight";
	@GlobalConfig
	public static final String CHAT_TEXT_PP_PREFIXES               = CHAT_TEXT_PP + "prefixes.";
	public static final String CHAT_TEXT_PP_PREFIXES_CHECK         = CHAT_TEXT_PP_PREFIXES + "active";
	@GlobalConfig
	public static final String CHAT_TEXT_PP_WORDS                  = CHAT_TEXT_PP + "words.";
	public static final String CHAT_TEXT_PP_WORDS_CHECK            = CHAT_TEXT_PP_WORDS + "active";
	@GlobalConfig
	public static final String CHAT_TEXT_PP_SIMILARITY             = CHAT_TEXT_PP + "similarity.";
	public static final String CHAT_TEXT_PP_SIMILARITY_CHECK       = CHAT_TEXT_PP_SIMILARITY + "active";
	
    private static final String CHAT_WARNING                             = CHAT + "warning.";
    public static final String  CHAT_WARNING_CHECK                       = CHAT_WARNING + "active";
    public static final String  CHAT_WARNING_LEVEL                       = CHAT_WARNING + "level";
    public static final String  CHAT_WARNING_MESSAGE                     = CHAT_WARNING + "message";
    public static final String  CHAT_WARNING_TIMEOUT                     = CHAT_WARNING + "timeout";
	
    // NOT YET IN USE
    private static final String CHAT_LOGINS                              = CHAT + "logins.";
    public static final String  CHAT_LOGINS_CHECK                        = CHAT_LOGINS + "active";
    public static final String  CHAT_LOGINS_PERWORLDCOUNT                = CHAT_LOGINS + "perworldcount";
    public static final String  CHAT_LOGINS_SECONDS                      = CHAT_LOGINS + "seconds";
    public static final String  CHAT_LOGINS_LIMIT                        = CHAT_LOGINS + "limit";
    public static final String  CHAT_LOGINS_KICKMESSAGE                  = CHAT_LOGINS + "kickmessage";
    public static final String  CHAT_LOGINS_STARTUPDELAY                 = CHAT_LOGINS + "startupdelay";
    
    private static final String CHAT_RELOG                               = CHAT + "relog.";
    public static final String  CHAT_RELOG_CHECK                         = CHAT_RELOG + "active";
    public static final String  CHAT_RELOG_KICKMESSAGE                   = CHAT_RELOG + "kickmessage";
    public static final String  CHAT_RELOG_TIMEOUT                       = CHAT_RELOG + "timeout";
    private static final String CHAT_RELOG_WARNING                       = CHAT_RELOG + "warning.";
    public static final String  CHAT_RELOG_WARNING_MESSAGE               = CHAT_RELOG_WARNING + "message";
    public static final String  CHAT_RELOG_WARNING_NUMBER                = CHAT_RELOG_WARNING + "number";
    public static final String  CHAT_RELOG_WARNING_TIMEOUT               = CHAT_RELOG_WARNING + "timeout";
    public static final String  CHAT_RELOG_ACTIONS                       = CHAT_RELOG + "actions";

    /*
     * Combined !
     */
    public static final String  COMBINED                                 = CHECKS + "combined.";
	
    private static final String COMBINED_BEDLEAVE						 = COMBINED + "bedleave.";
    public static final String  COMBINED_BEDLEAVE_CHECK					 = COMBINED_BEDLEAVE + "active";
	public static final String  COMBINED_BEDLEAVE_ACTIONS				 = COMBINED_BEDLEAVE + "actions";
	
	private static final String COMBINED_ENDERPEARL						 = COMBINED + "enderpearl.";
	public static final String  COMBINED_ENDERPEARL_CHECK				 = COMBINED_ENDERPEARL + "active";
	public static final String  COMBINED_ENDERPEARL_PREVENTCLICKBLOCK	 = COMBINED_ENDERPEARL + "preventclickblock";
	
    private static final String COMBINED_IMPROBABLE                      = COMBINED + "improbable.";
	public static final String  COMBINED_IMPROBABLE_CHECK                = COMBINED_IMPROBABLE + "active";
	public static final String  COMBINED_IMPROBABLE_LEVEL                = COMBINED_IMPROBABLE + "level";
	
//	private static final String COMBINED_IMPROBABLE_CHECKS               = COMBINED_IMPROBABLE + "options.";               
//	public static final String  COMBINED_IMPROBABLE_FASTBREAK_CHECK      = COMBINED_IMPROBABLE_CHECKS + "fastbreak";
	
	public static final String  COMBINED_IMPROBABLE_ACTIONS              = COMBINED_IMPROBABLE + "actions";
	
	private static final String COMBINED_INVULNERABLE                       = COMBINED + "invulnerable.";
	public static final String  COMBINED_INVULNERABLE_CHECK                 = COMBINED_INVULNERABLE + "active";
	private static final String COMBINED_INVULNERABLE_INITIALTICKS          = COMBINED_INVULNERABLE + "initialticks.";
    public static final String  COMBINED_INVULNERABLE_INITIALTICKS_JOIN     = COMBINED_INVULNERABLE_INITIALTICKS + "join";
    public static final String  COMBINED_INVULNERABLE_IGNORE                = COMBINED_INVULNERABLE + "ignore";
    public static final String  COMBINED_INVULNERABLE_MODIFIERS             = COMBINED_INVULNERABLE + "modifiers"; // no dot !
    private static final String COMBINED_INVULNERABLE_TRIGGERS              = COMBINED_INVULNERABLE + "triggers.";
    public static final String  COMBINED_INVULNERABLE_TRIGGERS_ALWAYS       = COMBINED_INVULNERABLE_TRIGGERS + "always";
    public static final String  COMBINED_INVULNERABLE_TRIGGERS_FALLDISTANCE = COMBINED_INVULNERABLE_TRIGGERS + "falldistance";

    public static final String COMBINED_MUNCHHAUSEN                         = COMBINED + "munchhausen.";
	public static final String COMBINED_MUNCHHAUSEN_CHECK				 	= COMBINED_MUNCHHAUSEN + "active";
	public static final String COMBINED_MUNCHHAUSEN_ACTIONS					= COMBINED_MUNCHHAUSEN + "actions";
	
	private static final String COMBINED_YAWRATE                         = COMBINED + "yawrate.";
	public static final String  COMBINED_YAWRATE_RATE                    = COMBINED_YAWRATE + "rate";
	public static final String  COMBINED_YAWRATE_IMPROBABLE              = COMBINED_YAWRATE + "improbable";
	private static final String COMBINED_YAWRATE_PENALTY                 = COMBINED_YAWRATE + "penalty.";
    public static final String  COMBINED_YAWRATE_PENALTY_FACTOR          = COMBINED_YAWRATE_PENALTY + "factor";
    public static final String  COMBINED_YAWRATE_PENALTY_MIN             = COMBINED_YAWRATE_PENALTY + "minimum";
    public static final String  COMBINED_YAWRATE_PENALTY_MAX             = COMBINED_YAWRATE_PENALTY + "maximum";
	
    public static final String  FIGHT                                    = CHECKS + "fight.";
    
	public static final String  FIGHT_CANCELDEAD                         = FIGHT + "canceldead";
	public static final  String FIGHT_TOOLCHANGEPENALTY		 			 = FIGHT + "toolchangepenalty";

    private static final String FIGHT_ANGLE                              = FIGHT + "angle.";
    public static final String  FIGHT_ANGLE_CHECK                        = FIGHT_ANGLE + "active";
    public static final String  FIGHT_ANGLE_THRESHOLD                    = FIGHT_ANGLE + "threshold";
    public static final String  FIGHT_ANGLE_ACTIONS                      = FIGHT_ANGLE + "actions";

    private static final String FIGHT_CRITICAL                           = FIGHT + "critical.";
    public static final String  FIGHT_CRITICAL_CHECK                     = FIGHT_CRITICAL + "active";
    public static final String  FIGHT_CRITICAL_FALLDISTANCE              = FIGHT_CRITICAL + "falldistance";
    public static final String  FIGHT_CRITICAL_VELOCITY                  = FIGHT_CRITICAL + "velocity";
    public static final String  FIGHT_CRITICAL_ACTIONS                   = FIGHT_CRITICAL + "actions";

    private static final String FIGHT_DIRECTION                          = FIGHT + "direction.";
    public static final String  FIGHT_DIRECTION_CHECK                    = FIGHT_DIRECTION + "active";
	public static final String  FIGHT_DIRECTION_STRICT					 = FIGHT_DIRECTION + "strict";
    public static final String  FIGHT_DIRECTION_PENALTY                  = FIGHT_DIRECTION + "penalty";
    public static final String  FIGHT_DIRECTION_ACTIONS                  = FIGHT_DIRECTION + "actions";
    
    private static final String FIGHT_FASTHEAL							 = FIGHT + "fastheal.";
    public static final String  FIGHT_FASTHEAL_CHECK					 = FIGHT_FASTHEAL + "active";
	public static final String  FIGHT_FASTHEAL_INTERVAL					 = FIGHT_FASTHEAL + "interval";
	public static final String  FIGHT_FASTHEAL_BUFFER					 = FIGHT_FASTHEAL + "buffer";
	public static final String  FIGHT_FASTHEAL_ACTIONS					 = FIGHT_FASTHEAL + "actions";

    private static final String FIGHT_GODMODE                            = FIGHT + "godmode.";
    public static final String  FIGHT_GODMODE_CHECK                      = FIGHT_GODMODE + "active";
	public static final String  FIGHT_GODMODE_LAGMINAGE					 = FIGHT_GODMODE + "minage";
	public static final String  FIGHT_GODMODE_LAGMAXAGE 				 = FIGHT_GODMODE + "maxage";
    public static final String  FIGHT_GODMODE_ACTIONS                    = FIGHT_GODMODE + "actions";
    
    private static final String FIGHT_KNOCKBACK                          = FIGHT + "knockback.";
    public static final String  FIGHT_KNOCKBACK_CHECK                    = FIGHT_KNOCKBACK + "active";
    public static final String  FIGHT_KNOCKBACK_INTERVAL                 = FIGHT_KNOCKBACK + "interval";
    public static final String  FIGHT_KNOCKBACK_ACTIONS                  = FIGHT_KNOCKBACK + "actions";

    private static final String FIGHT_NOSWING                            = FIGHT + "noswing.";
    public static final String  FIGHT_NOSWING_CHECK                      = FIGHT_NOSWING + "active";
    public static final String  FIGHT_NOSWING_ACTIONS                    = FIGHT_NOSWING + "actions";

    private static final String FIGHT_REACH                              = FIGHT + "reach.";
    public static final String  FIGHT_REACH_CHECK                        = FIGHT_REACH + "active";
	public static final String  FIGHT_REACH_SURVIVALDISTANCE			 = FIGHT_REACH + "survivaldistance";
    public static final String  FIGHT_REACH_PENALTY                      = FIGHT_REACH + "penalty";
	public static final String  FIGHT_REACH_PRECISION                    = FIGHT_REACH + "precision";
	public static final String  FIGHT_REACH_REDUCE                       = FIGHT_REACH + "reduce";
	public static final String  FIGHT_REACH_REDUCEDISTANCE				 = FIGHT_REACH + "reducedistance";
	public static final String  FIGHT_REACH_REDUCESTEP					 = FIGHT_REACH + "reducestep";
    public static final String  FIGHT_REACH_ACTIONS                      = FIGHT_REACH + "actions";
    
    public static final String FIGHT_SELFHIT                             = FIGHT + "selfhit.";
    public static final String FIGHT_SELFHIT_CHECK                       = FIGHT_SELFHIT + "active";
	public static final String FIGHT_SELFHIT_ACTIONS                     = FIGHT_SELFHIT + "actions";


    private static final String FIGHT_SPEED                              = FIGHT + "speed.";
    public static final String  FIGHT_SPEED_CHECK                        = FIGHT_SPEED + "active";
    public static final String  FIGHT_SPEED_LIMIT                        = FIGHT_SPEED + "limit";
    private static final String FIGHT_SPEED_BUCKETS                      = FIGHT_SPEED + "buckets.";
    @GlobalConfig
	public static final String  FIGHT_SPEED_BUCKETS_N                    = FIGHT_SPEED_BUCKETS + "number";
    @GlobalConfig
	public static final String  FIGHT_SPEED_BUCKETS_DUR                  = FIGHT_SPEED_BUCKETS + "duration";
	public static final String  FIGHT_SPEED_BUCKETS_FACTOR               = FIGHT_SPEED_BUCKETS + "factor";
	private static final String FIGHT_SPEED_SHORTTERM                    = FIGHT_SPEED + "shortterm.";
	public static final String  FIGHT_SPEED_SHORTTERM_LIMIT              = FIGHT_SPEED_SHORTTERM + "limit";
	public static final String  FIGHT_SPEED_SHORTTERM_TICKS              = FIGHT_SPEED_SHORTTERM + "ticks";
    public static final String  FIGHT_SPEED_ACTIONS                      = FIGHT_SPEED + "actions";
    
    private static final String FIGHT_YAWRATE                            = FIGHT + "yawrate.";
	public static final String  FIGHT_YAWRATE_CHECK                      = FIGHT_YAWRATE + "active";

    public static final String  INVENTORY                                = CHECKS + "inventory.";

    private static final String INVENTORY_DROP                           = INVENTORY + "drop.";
    public static final String  INVENTORY_DROP_CHECK                     = INVENTORY_DROP + "active";
    public static final String  INVENTORY_DROP_LIMIT                     = INVENTORY_DROP + "limit";
    public static final String  INVENTORY_DROP_TIMEFRAME                 = INVENTORY_DROP + "timeframe";
    public static final String  INVENTORY_DROP_ACTIONS                   = INVENTORY_DROP + "actions";

    private static final String INVENTORY_FASTCLICK                      = INVENTORY + "fastclick.";
    public static final String  INVENTORY_FASTCLICK_CHECK                = INVENTORY_FASTCLICK + "active";
    public static final String  INVENTORY_FASTCLICK_SPARECREATIVE        = INVENTORY_FASTCLICK + "sparecreative";
	public static final String  INVENTORY_FASTCLICK_TWEAKS1_5			 = INVENTORY_FASTCLICK + "tweaks1_5";
    private static final String INVENTORY_FASTCLICK_LIMIT				 = INVENTORY_FASTCLICK + "limit.";
	public static final String  INVENTORY_FASTCLICK_LIMIT_SHORTTERM		 = INVENTORY_FASTCLICK_LIMIT + "shortterm";
	public static final String  INVENTORY_FASTCLICK_LIMIT_NORMAL		 = INVENTORY_FASTCLICK_LIMIT + "normal";
    public static final String  INVENTORY_FASTCLICK_ACTIONS              = INVENTORY_FASTCLICK + "actions";
    
    private static final String INVENTORY_FASTCONSUME					 = INVENTORY + "fastconsume.";
	public static final String  INVENTORY_FASTCONSUME_CHECK				 = INVENTORY_FASTCONSUME + "active";
	public static final String  INVENTORY_FASTCONSUME_DURATION			 = INVENTORY_FASTCONSUME + "duration";
	public static final String  INVENTORY_FASTCONSUME_WHITELIST			 = INVENTORY_FASTCONSUME + "whitelist";
	public static final String  INVENTORY_FASTCONSUME_ITEMS				 = INVENTORY_FASTCONSUME + "items";
	public static final String  INVENTORY_FASTCONSUME_ACTIONS			 = INVENTORY_FASTCONSUME + "actions";

    private static final String INVENTORY_INSTANTBOW                     = INVENTORY + "instantbow.";
    public static final String  INVENTORY_INSTANTBOW_CHECK               = INVENTORY_INSTANTBOW + "active";
	public static final String  INVENTORY_INSTANTBOW_STRICT				 = INVENTORY_INSTANTBOW + "strict";
    public static final String  INVENTORY_INSTANTBOW_DELAY               = INVENTORY_INSTANTBOW + "delay";
    public static final String  INVENTORY_INSTANTBOW_ACTIONS             = INVENTORY_INSTANTBOW + "actions";

    private static final String INVENTORY_INSTANTEAT                     = INVENTORY + "instanteat.";
    public static final String  INVENTORY_INSTANTEAT_CHECK               = INVENTORY_INSTANTEAT + "active";
    public static final String  INVENTORY_INSTANTEAT_ACTIONS             = INVENTORY_INSTANTEAT + "actions";
    
    private static final String INVENTORY_ITEMS                          = INVENTORY + "items.";
    public static final String  INVENTORY_ITEMS_CHECK                    = INVENTORY_ITEMS + "active";
    
    private static final String INVENTORY_OPEN							 = INVENTORY + "open.";
    public static final  String INVENTORY_OPEN_CHECK					 = INVENTORY_OPEN + "active";
    // TODO: close and cancelother on open-section-level are temporary.
    public static final  String INVENTORY_OPEN_CLOSE					 = INVENTORY_OPEN + "close";
    public static final  String INVENTORY_OPEN_CANCELOTHER				 = INVENTORY_OPEN + "cancelother";
    
    public static final String  MOVING                                   = CHECKS + "moving.";

    private static final String MOVING_CREATIVEFLY                       = MOVING + "creativefly.";
    public static final String  MOVING_CREATIVEFLY_CHECK                 = MOVING_CREATIVEFLY + "active";
	public static final String  MOVING_CREATIVEFLY_IGNORECREATIVE        = MOVING_CREATIVEFLY + "ignorecreative";
	public static final String  MOVING_CREATIVEFLY_IGNOREALLOWFLIGHT     = MOVING_CREATIVEFLY + "ignoreallowflight";
    public static final String  MOVING_CREATIVEFLY_HORIZONTALSPEED       = MOVING_CREATIVEFLY + "horizontalspeed";
    public static final String  MOVING_CREATIVEFLY_MAXHEIGHT             = MOVING_CREATIVEFLY + "maxheight";
    public static final String  MOVING_CREATIVEFLY_VERTICALSPEED         = MOVING_CREATIVEFLY + "verticalspeed";
    public static final String  MOVING_CREATIVEFLY_ACTIONS               = MOVING_CREATIVEFLY + "actions";

    private static final String MOVING_MOREPACKETS                       = MOVING + "morepackets.";
    public static final String  MOVING_MOREPACKETS_CHECK                 = MOVING_MOREPACKETS + "active";
    public static final String  MOVING_MOREPACKETS_SECONDS				 = MOVING_MOREPACKETS + "seconds";
    public static final String  MOVING_MOREPACKETS_EPSIDEAL				 = MOVING_MOREPACKETS + "epsideal";
	public static final String  MOVING_MOREPACKETS_EPSMAX				 = MOVING_MOREPACKETS + "epsmax";
	private static final String MOVING_MOREPACKETS_BURST				 = MOVING_MOREPACKETS + "burst.";
	public static final String  MOVING_MOREPACKETS_BURST_PACKETS		 = MOVING_MOREPACKETS_BURST + "packets";
	public static final String  MOVING_MOREPACKETS_BURST_DIRECT			 = MOVING_MOREPACKETS_BURST + "directviolation";
	public static final String  MOVING_MOREPACKETS_BURST_EPM			 = MOVING_MOREPACKETS_BURST + "epmviolation";
    public static final String  MOVING_MOREPACKETS_ACTIONS               = MOVING_MOREPACKETS + "actions";

    private static final String MOVING_MOREPACKETSVEHICLE                = MOVING + "morepacketsvehicle.";
    public static final String  MOVING_MOREPACKETSVEHICLE_CHECK          = MOVING_MOREPACKETSVEHICLE + "active";
    public static final String  MOVING_MOREPACKETSVEHICLE_ACTIONS        = MOVING_MOREPACKETSVEHICLE + "actions";

    private static final String MOVING_NOFALL                            = MOVING + "nofall.";
    public static final String  MOVING_NOFALL_CHECK                      = MOVING_NOFALL + "active";
    public static final String  MOVING_NOFALL_DEALDAMAGE                 = MOVING_NOFALL + "dealdamage";
	public static final String  MOVING_NOFALL_RESETONVL  				 = MOVING_NOFALL + "resetonviolation";
	public static final String  MOVING_NOFALL_RESETONTP 				 = MOVING_NOFALL + "resetonteleport";
	public static final String  MOVING_NOFALL_RESETONVEHICLE			 = MOVING_NOFALL + "resetonvehicle";
	public static final String  MOVING_NOFALL_ANTICRITICALS				 = MOVING_NOFALL + "anticriticals";
    public static final String  MOVING_NOFALL_ACTIONS                    = MOVING_NOFALL + "actions";
    
    public static final String MOVING_PASSABLE                           = MOVING + "passable.";
	public static final String MOVING_PASSABLE_CHECK                     = MOVING_PASSABLE + "active";
	private static final String MOVING_PASSABLE_RAYTRACING				 = MOVING_PASSABLE + "raytracing.";
	public static final String MOVING_PASSABLE_RAYTRACING_CHECK 		 = MOVING_PASSABLE_RAYTRACING + "active";
	public static final String MOVING_PASSABLE_RAYTRACING_BLOCKCHANGEONLY= MOVING_PASSABLE_RAYTRACING + "blockchangeonly";
	public static final String MOVING_PASSABLE_ACTIONS                   = MOVING_PASSABLE + "actions";

	private static final String MOVING_SURVIVALFLY						= MOVING + "survivalfly.";
	public static final String MOVING_SURVIVALFLY_CHECK 				= MOVING_SURVIVALFLY + "active";
	public static final String MOVING_SURVIVALFLY_BLOCKINGSPEED			= MOVING_SURVIVALFLY + "blockingspeed";
	public static final String MOVING_SURVIVALFLY_SNEAKINGSPEED			= MOVING_SURVIVALFLY + "sneakingspeed";
	public static final String MOVING_SURVIVALFLY_SPEEDINGSPEED			= MOVING_SURVIVALFLY + "speedingspeed";
	public static final String MOVING_SURVIVALFLY_SPRINTINGSPEED		= MOVING_SURVIVALFLY + "sprintingspeed";
	public static final String MOVING_SURVIVALFLY_SWIMMINGSPEED			= MOVING_SURVIVALFLY + "swimmingspeed";
	public static final String MOVING_SURVIVALFLY_WALKINGSPEED			= MOVING_SURVIVALFLY + "walkingspeed";
	public static final String MOVING_SURVIVALFLY_COBWEBHACK			= MOVING_SURVIVALFLY + "cobwebhack";
	private static final String MOVING_SURVIVALFLY_EXTENDED				= MOVING_SURVIVALFLY + "extended.";
	public static final String MOVING_SURVIVALFLY_EXTENDED_HACC			= MOVING_SURVIVALFLY_EXTENDED + "horizontal-accounting";
	public static final String MOVING_SURVIVALFLY_EXTENDED_VACC			= MOVING_SURVIVALFLY_EXTENDED + "vertical-accounting";
	public static final String MOVING_SURVIVALFLY_FALLDAMAGE			= MOVING_SURVIVALFLY + "falldamage";
	public static final String MOVING_SURVIVALFLY_VLFREEZE				= MOVING_SURVIVALFLY + "vlfreeze";
	public static final String MOVING_SURVIVALFLY_ACTIONS				= MOVING_SURVIVALFLY + "actions";
	
	private static final String MOVING_SURVIVALFLY_HOVER			    = MOVING_SURVIVALFLY + "hover.";
	public static final String  MOVING_SURVIVALFLY_HOVER_CHECK			= MOVING_SURVIVALFLY_HOVER + "active";
	@GlobalConfig
	public static final String  MOVING_SURVIVALFLY_HOVER_STEP			= MOVING_SURVIVALFLY_HOVER + "step";
	public static final String  MOVING_SURVIVALFLY_HOVER_TICKS			= MOVING_SURVIVALFLY_HOVER + "ticks";
	public static final String  MOVING_SURVIVALFLY_HOVER_LOGINTICKS		= MOVING_SURVIVALFLY_HOVER + "loginticks";
	public static final String  MOVING_SURVIVALFLY_HOVER_FALLDAMAGE		= MOVING_SURVIVALFLY_HOVER + "falldamage";
	public static final String  MOVING_SURVIVALFLY_HOVER_SFVIOLATION	= MOVING_SURVIVALFLY_HOVER + "sfviolation";

    // Special (to be sorted in or factored out).
	private static final String MOVING_VELOCITY							= MOVING + "velocity.";
	public static final String  MOVING_VELOCITY_GRACETICKS				= MOVING_VELOCITY + "graceticks";
	public static final String  MOVING_VELOCITY_ACTIVATIONCOUNTER		= MOVING_VELOCITY + "activationcounter";
	public static final String  MOVING_VELOCITY_ACTIVATIONTICKS			= MOVING_VELOCITY + "activationticks";
	public static final String	MOVING_VELOCITY_STRICTINVALIDATION		= MOVING_VELOCITY + "strictinvalidation";

    public static final String  MOVING_NOFALL_YONGROUND                  = MOVING_NOFALL + "yonground";
    public static final String  MOVING_YONGROUND                         = MOVING + "yonground";
	public static final String  MOVING_SURVIVALFLY_YSTEP                 = MOVING_SURVIVALFLY + "ystep";
	
	// General.
	public static final String  MOVING_TEMPKICKILLEGAL					 = MOVING + "tempkickillegal";
	private static final String MOVING_LOADCHUNKS						 = MOVING + "loadchunks.";
	public static final String  MOVING_LOADCHUNKS_JOIN					 = MOVING_LOADCHUNKS + "join";
	public static final String  MOVING_SPRINTINGGRACE					 = MOVING + "sprintinggrace";
	public static final String  MOVING_ASSUMESPRINT						 = MOVING + "assumesprint";
	public static final String  MOVING_SPEEDGRACE					 	 = MOVING + "speedgrace";
	public static final String  MOVING_ENFORCELOCATION					 = MOVING + "enforcelocation";
	
	private static final String MOVING_VEHICLES							 = MOVING + "vehicles.";
	public static final String  MOVING_VEHICLES_ENFORCELOCATION			 = MOVING_VEHICLES + "enforcelocation";
	public static final String  MOVING_VEHICLES_PREVENTDESTROYOWN		 = MOVING_VEHICLES + "preventdestroyown";
	
	private static final String MOVING_TRACE							 = MOVING + "trace.";
	public  static final String MOVING_TRACE_SIZE						 = MOVING_TRACE + "size";
	public  static final String MOVING_TRACE_MERGEDIST					 = MOVING_TRACE + "mergedist";
	
	private static final String NET										 = CHECKS + "net.";
	
	private static final String NET_SOUNDDISTANCE						 = NET + "sounddistance.";
	public static final String  NET_SOUNDDISTANCE_ACTIVE				 = NET_SOUNDDISTANCE + "active";
	@GlobalConfig
	public static final String  NET_SOUNDDISTANCE_MAXDISTANCE			 = NET_SOUNDDISTANCE + "maxdistance";
	
	private static final String NET_FLYINGFREQUENCY						 = NET + "flyingfrequency.";
	public static final String  NET_FLYINGFREQUENCY_ACTIVE				 = NET_FLYINGFREQUENCY + "active";
	@GlobalConfig
	public static final String  NET_FLYINGFREQUENCY_SECONDS				 = NET_FLYINGFREQUENCY + "seconds";
	@GlobalConfig
	public static final String  NET_FLYINGFREQUENCY_MAXPACKETS			 = NET_FLYINGFREQUENCY + "maxpackets";
	
	
    public static final String  STRINGS                                  = "strings";

    // Compatibility section (possibly temporary).
    @GlobalConfig
    public static final String COMPATIBILITY                             = "compatibility.";
    public static final String COMPATIBILITY_MANAGELISTENERS			 = COMPATIBILITY + "managelisteners";
	public static final String COMPATIBILITY_BUKKITONLY                  = COMPATIBILITY + "bukkitapionly";
    public static final String COMPATIBILITY_BLOCKS                      = COMPATIBILITY + "blocks.";
    
    // Deprecated (don't use fields from above).
    @Moved(newPath = LOGGING_BACKEND_CONSOLE_ACTIVE)
    public static final String  LOGGING_CONSOLE                          = "logging.console";
    @Moved(newPath = LOGGING_BACKEND_FILE_ACTIVE)
    public static final String  LOGGING_FILE                             = "logging.file";
    @Moved(newPath = LOGGING_BACKEND_FILE_FILENAME)
    public static final String  LOGGING_FILENAME                         = "logging.filename";
    @Moved(newPath = LOGGING_BACKEND_INGAMECHAT_ACTIVE)
    public static final String  LOGGING_INGAMECHAT                       = "logging.ingamechat";
    @Moved(newPath = LOGGING_BACKEND_INGAMECHAT_SUBSCRIPTIONS)
    public static final String  LOGGING_USESUBSCRIPTIONS  		 		 = "logging.usesubscriptions";
	@Moved(newPath = PROTECT_PLUGINS_HIDE_ACTIVE)
	public static final String  MISCELLANEOUS_PROTECTPLUGINS			 = "miscellaneous.protectplugins";
	@Moved(newPath = PROTECT_CLIENTS_MOTD_ALLOWALL)
	public static final String  MISCELLANEOUS_ALLOWCLIENTMODS			 = "miscellaneous.allowclientmods";
	@Moved(newPath = PROTECT_PLUGINS_HIDE_NOCOMMAND_MSG)
	public static  final String PROTECT_PLUGINS_HIDE_MSG_NOCOMMAND		 = "protection.plugins.hide.messages.unknowncommand";
	@Moved(newPath = PROTECT_PLUGINS_HIDE_NOPERMISSION_MSG)
	public static  final String PROTECT_PLUGINS_HIDE_MSG_NOPERMISSION	 = "protection.plugins.hide.messages.nopermission";
	@Moved(newPath = PROTECT_COMMANDS_CONSOLEONLY_ACTIVE)
	public static final String  MISCELLANEOUS_OPINCONSOLEONLY			 = "miscellaneous.opinconsoleonly";
	@Moved(newPath = COMPATIBILITY_MANAGELISTENERS)
	public static final String  MISCELLANEOUS_MANAGELISTENERS			 = "miscellaneous.managelisteners";
	@Moved(newPath = INVENTORY_OPEN_CHECK)
	public static final String  INVENTORY_ENSURECLOSE					 = "checks.inventory.ensureclose";
	@Deprecated
	public static final String  MISCELLANEOUS_REPORTTOMETRICS			 = "miscellaneous.reporttometrics";
	@Deprecated
	public static final String  BLOCKBREAK_FASTBREAK_MOD_CREATIVE        = "checks.blockbreak.fastbreak.intervalcreative";
	@Deprecated
	public static final String MOVING_PASSABLE_RAYTRACING_VCLIPONLY      = "checks.moving.passable.raytracing.vcliponly";

	
}
