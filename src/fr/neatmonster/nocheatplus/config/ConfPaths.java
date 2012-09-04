package fr.neatmonster.nocheatplus.config;

/*
 * MM'""""'YMM                   .8888b MM"""""""`YM            dP   dP                
 * M' .mmm. `M                   88   " MM  mmmmm  M            88   88                
 * M  MMMMMooM .d8888b. 88d888b. 88aaa  M'        .M .d8888b. d8888P 88d888b. .d8888b. 
 * M  MMMMMMMM 88'  `88 88'  `88 88     MM  MMMMMMMM 88'  `88   88   88'  `88 Y8ooooo. 
 * M. `MMM' .M 88.  .88 88    88 88     MM  MMMMMMMM 88.  .88   88   88    88       88 
 * MM.     .dM `88888P' dP    dP dP     MM  MMMMMMMM `88888P8   dP   dP    dP `88888P' 
 * MMMMMMMMMMM                          MMMMMMMMMMMM                                   
 */
/**
 * Paths for the configuration options. Making everything final static prevents accidentally modifying any of these.
 */
public abstract class ConfPaths {

    /*
     * 888                                 ,e,                  
     * 888      e88 88e   e88 888  e88 888  "  888 8e   e88 888 
     * 888     d888 888b d888 888 d888 888 888 888 88b d888 888 
     * 888  ,d Y888 888P Y888 888 Y888 888 888 888 888 Y888 888 
     * 888,d88  "88 88"   "88 888  "88 888 888 888 888  "88 888 
     *                     ,  88P   ,  88P               ,  88P 
     *                    "8",P"   "8",P"               "8",P"  
     */
    private static final String LOGGING                                  = "logging.";
    public static final String  LOGGING_ACTIVE                           = LOGGING + "active";
    public static final String  LOGGING_CONSOLE                          = LOGGING + "console";
    public static final String  LOGGING_DEBUG                            = LOGGING + "debug";
    public static final String  LOGGING_FILE                             = LOGGING + "file";
    public static final String  LOGGING_FILENAME                         = LOGGING + "filename";
    public static final String  LOGGING_INGAMECHAT                       = LOGGING + "ingamechat";

    /*
     *     e   e     ,e,                        888 888                                                   
     *    d8b d8b     "   dP"Y  e88'888  ,e e,  888 888  ,"Y88b 888 8e   ,e e,   e88 88e  8888 8888  dP"Y 
     *   e Y8b Y8b   888 C88b  d888  '8 d88 88b 888 888 "8" 888 888 88b d88 88b d888 888b 8888 8888 C88b  
     *  d8b Y8b Y8b  888  Y88D Y888   , 888   , 888 888 ,ee 888 888 888 888   , Y888 888P Y888 888P  Y88D 
     * d888b Y8b Y8b 888 d,dP   "88,e8'  "YeeP" 888 888 "88 888 888 888  "YeeP"  "88 88"   "88 88"  d,dP  
     */
    private static final String MISCELLANEOUS                            = "miscellaneous.";
    public static final String  MISCELLANEOUS_ALLOWCLIENTMODS            = MISCELLANEOUS + "allowclientmods";
    public static final String  MISCELLANEOUS_OPINCONSOLEONLY            = MISCELLANEOUS + "opinconsoleonly";
    public static final String  MISCELLANEOUS_PROTECTPLUGINS             = MISCELLANEOUS + "protectplugins";
	public static final String  MISCELLANEOUS_CHECKFORUPDATES            = MISCELLANEOUS + "checkforupdates";
	public static final String  MISCELLANEOUS_READTIMEOUT                = MISCELLANEOUS + "readtimeout";
    public static final String  MISCELLANEOUS_REPORTTOMETRICS            = MISCELLANEOUS + "reporttometrics";

    private static final String MISCELLANEOUS_NOMOVEDTOOQUICKLY          = MISCELLANEOUS + "nomovedtooquickly.";
    public static final String  MISCELLANEOUS_NOMOVEDTOOQUICKLY_ENABLED  = MISCELLANEOUS_NOMOVEDTOOQUICKLY + "enabled";
    public static final String  MISCELLANEOUS_NOMOVEDTOOQUICKLY_USEPROXY = MISCELLANEOUS_NOMOVEDTOOQUICKLY + "useproxy";

    private static final String CHECKS                                   = "checks.";

    /*
     * 888 88b, 888                    888    888 88b,                        888    
     * 888 88P' 888  e88 88e   e88'888 888 ee 888 88P' 888,8,  ,e e,   ,"Y88b 888 ee 
     * 888 8K   888 d888 888b d888  '8 888 P  888 8K   888 "  d88 88b "8" 888 888 P  
     * 888 88b, 888 Y888 888P Y888   , 888 b  888 88b, 888    888   , ,ee 888 888 b  
     * 888 88P' 888  "88 88"   "88,e8' 888 8b 888 88P' 888     "YeeP" "88 888 888 8b 
     */
    private static final String BLOCKBREAK                               = CHECKS + "blockbreak.";

    private static final String BLOCKBREAK_DIRECTION                     = BLOCKBREAK + "direction.";
    public static final String  BLOCKBREAK_DIRECTION_CHECK               = BLOCKBREAK_DIRECTION + "active";
    public static final String  BLOCKBREAK_DIRECTION_ACTIONS             = BLOCKBREAK_DIRECTION + "actions";

    private static final String BLOCKBREAK_FASTBREAK                     = BLOCKBREAK + "fastbreak.";
    public static final String  BLOCKBREAK_FASTBREAK_CHECK               = BLOCKBREAK_FASTBREAK + "active";
    public static final String  BLOCKBREAK_FASTBREAK_BUFFER              = BLOCKBREAK_FASTBREAK + "buffer";
    public static final String  BLOCKBREAK_FASTBREAK_INTERVAL            = BLOCKBREAK_FASTBREAK + "interval";
    public static final String  BLOCKBREAK_FASTBREAK_ACTIONS             = BLOCKBREAK_FASTBREAK + "actions";

    private static final String BLOCKBREAK_NOSWING                       = BLOCKBREAK + "noswing.";
    public static final String  BLOCKBREAK_NOSWING_CHECK                 = BLOCKBREAK_NOSWING + "active";
    public static final String  BLOCKBREAK_NOSWING_ACTIONS               = BLOCKBREAK_NOSWING + "actions";

    private static final String BLOCKBREAK_REACH                         = BLOCKBREAK + "reach.";
    public static final String  BLOCKBREAK_REACH_CHECK                   = BLOCKBREAK_REACH + "active";
    public static final String  BLOCKBREAK_REACH_ACTIONS                 = BLOCKBREAK_REACH + "actions";

    /*
     * 888 88b, 888                    888    888           d8                                     d8   
     * 888 88P' 888  e88 88e   e88'888 888 ee 888 888 8e   d88    ,e e,  888,8,  ,"Y88b  e88'888  d88   
     * 888 8K   888 d888 888b d888  '8 888 P  888 888 88b d88888 d88 88b 888 "  "8" 888 d888  '8 d88888 
     * 888 88b, 888 Y888 888P Y888   , 888 b  888 888 888  888   888   , 888    ,ee 888 Y888   ,  888   
     * 888 88P' 888  "88 88"   "88,e8' 888 8b 888 888 888  888    "YeeP" 888    "88 888  "88,e8'  888   
     */
    private static final String BLOCKINTERACT                            = CHECKS + "blockinteract.";

    private static final String BLOCKINTERACT_DIRECTION                  = BLOCKINTERACT + "direction.";
    public static final String  BLOCKINTERACT_DIRECTION_CHECK            = BLOCKINTERACT_DIRECTION + "active";
    public static final String  BLOCKINTERACT_DIRECTION_ACTIONS          = BLOCKINTERACT_DIRECTION + "actions";

    private static final String BLOCKINTERACT_REACH                      = BLOCKINTERACT + "reach.";
    public static final String  BLOCKINTERACT_REACH_CHECK                = BLOCKINTERACT_REACH + "active";
    public static final String  BLOCKINTERACT_REACH_ACTIONS              = BLOCKINTERACT_REACH + "actions";

    /*
     * 888 88b, 888                    888    888 88e  888                          
     * 888 88P' 888  e88 88e   e88'888 888 ee 888 888D 888  ,"Y88b  e88'888  ,e e,  
     * 888 8K   888 d888 888b d888  '8 888 P  888 88"  888 "8" 888 d888  '8 d88 88b 
     * 888 88b, 888 Y888 888P Y888   , 888 b  888      888 ,ee 888 Y888   , 888   , 
     * 888 88P' 888  "88 88"   "88,e8' 888 8b 888      888 "88 888  "88,e8'  "YeeP" 
     */
    private static final String BLOCKPLACE                               = CHECKS + "blockplace.";

    private static final String BLOCKPLACE_DIRECTION                     = BLOCKPLACE + "direction.";
    public static final String  BLOCKPLACE_DIRECTION_CHECK               = BLOCKPLACE_DIRECTION + "active";
    public static final String  BLOCKPLACE_DIRECTION_ACTIONS             = BLOCKPLACE_DIRECTION + "actions";

    private static final String BLOCKPLACE_FASTPLACE                     = BLOCKPLACE + "fastplace.";
    public static final String  BLOCKPLACE_FASTPLACE_CHECK               = BLOCKPLACE_FASTPLACE + "active";
    public static final String  BLOCKPLACE_FASTPLACE_EXPERIMENTAL        = BLOCKPLACE_FASTPLACE + "experimental";
    public static final String  BLOCKPLACE_FASTPLACE_INTERVAL            = BLOCKPLACE_FASTPLACE + "interval";
    public static final String  BLOCKPLACE_FASTPLACE_ACTIONS             = BLOCKPLACE_FASTPLACE + "actions";

    private static final String BLOCKPLACE_NOSWING                       = BLOCKPLACE + "noswing.";
    public static final String  BLOCKPLACE_NOSWING_CHECK                 = BLOCKPLACE_NOSWING + "active";
    public static final String  BLOCKPLACE_NOSWING_ACTIONS               = BLOCKPLACE_NOSWING + "actions";

    private static final String BLOCKPLACE_REACH                         = BLOCKPLACE + "reach.";
    public static final String  BLOCKPLACE_REACH_CHECK                   = BLOCKPLACE_REACH + "active";
    public static final String  BLOCKPLACE_REACH_ACTIONS                 = BLOCKPLACE_REACH + "actions";

    private static final String BLOCKPLACE_SPEED                         = BLOCKPLACE + "speed.";
    public static final String  BLOCKPLACE_SPEED_CHECK                   = BLOCKPLACE_SPEED + "active";
    public static final String  BLOCKPLACE_SPEED_INTERVAL                = BLOCKPLACE_SPEED + "interval";
    public static final String  BLOCKPLACE_SPEED_ACTIONS                 = BLOCKPLACE_SPEED + "actions";

    /*
     *   e88'Y88 888               d8   
     *  d888  'Y 888 ee   ,"Y88b  d88   
     * C8888     888 88b "8" 888 d88888 
     *  Y888  ,d 888 888 ,ee 888  888   
     *   "88,d88 888 888 "88 888  888   
     */
    private static final String CHAT                                     = CHECKS + "chat.";

    private static final String CHAT_COLOR                               = CHAT + "color.";
    public static final String  CHAT_COLOR_CHECK                         = CHAT_COLOR + "active";
    public static final String  CHAT_COLOR_ACTIONS                       = CHAT_COLOR + "actions";
    
    private static final String CHAT_GLOBALCHAT                          = CHAT + "globalchat.";
    public static final String CHAT_GLOBALCHAT_CHECK                     = CHAT_GLOBALCHAT + "active";
    public static final String CHAT_GLOBALCHAT_ENGINE                    = CHAT_GLOBALCHAT + "engine.";
	public static final String CHAT_GLOBALCHAT_ENGINE_CHECK              = CHAT_GLOBALCHAT_ENGINE + "active";
	private static final String CHAT_GLOBALCHAT_ENGINE_GLWORDFREQ        = CHAT_GLOBALCHAT_ENGINE + "glwordfrequency.";
	public static final String CHAT_GLOBALCHAT_ENGINE_GLWORDFREQ_CHECK   = CHAT_GLOBALCHAT_ENGINE_GLWORDFREQ + "active";
	public static final String CHAT_GLOBALCHAT_ENGINE_GLCOMPRWORDS       = CHAT_GLOBALCHAT_ENGINE + "glcompressedwords.";
	public static final String CHAT_GLOBALCHAT_ENGINE_GLCOMPRWORDS_CHECK = CHAT_GLOBALCHAT_ENGINE_GLCOMPRWORDS + "active";
	public static final String CHAT_GLOBALCHAT_COMMANDS                  = CHAT_GLOBALCHAT + "commands";
    public static final String CHAT_GLOBALCHAT_FREQUENCY                 = CHAT_GLOBALCHAT + "frequency.";
	public static final String CHAT_GLOBALCHAT_FREQUENCY_WEIGHT          = CHAT_GLOBALCHAT_FREQUENCY + "weight";
	public static final String CHAT_GLOBALCHAT_FREQUENCY_FACTOR          = CHAT_GLOBALCHAT_FREQUENCY + "factor";
	public static final String CHAT_GLOBALCHAT_LEVEL                     = CHAT_GLOBALCHAT + "level";
    public static final String CHAT_GLOBALCHAT_ACTIONS                   = CHAT_GLOBALCHAT + "actions";

    private static final String CHAT_NOPWNAGE                            = CHAT + "nopwnage.";
    public static final String  CHAT_NOPWNAGE_CHECK                      = CHAT_NOPWNAGE + "active";
    public static final String  CHAT_NOPWNAGE_EXCLUSIONS                 = CHAT_NOPWNAGE + "exclusions";
    public static final String  CHAT_NOPWNAGE_LEVEL                      = CHAT_NOPWNAGE + "level";

    private static final String CHAT_NOPWNAGE_BANNED                     = CHAT_NOPWNAGE + "banned.";
    public static final String  CHAT_NOPWNAGE_BANNED_CHECK               = CHAT_NOPWNAGE_BANNED + "active";
    public static final String  CHAT_NOPWNAGE_BANNED_TIMEOUT             = CHAT_NOPWNAGE_BANNED + "timeout";
    public static final String  CHAT_NOPWNAGE_BANNED_WEIGHT              = CHAT_NOPWNAGE_BANNED + "weight";

    private static final String CHAT_NOPWNAGE_CAPTCHA                    = CHAT_NOPWNAGE + "captcha.";
    public static final String  CHAT_NOPWNAGE_CAPTCHA_CHECK              = CHAT_NOPWNAGE_CAPTCHA + "active";
    public static final String  CHAT_NOPWNAGE_CAPTCHA_CHARACTERS         = CHAT_NOPWNAGE_CAPTCHA + "characters";
    public static final String  CHAT_NOPWNAGE_CAPTCHA_LENGTH             = CHAT_NOPWNAGE_CAPTCHA + "length";
    public static final String  CHAT_NOPWNAGE_CAPTCHA_QUESTION           = CHAT_NOPWNAGE_CAPTCHA + "question";
    public static final String  CHAT_NOPWNAGE_CAPTCHA_SUCCESS            = CHAT_NOPWNAGE_CAPTCHA + "success";
    public static final String  CHAT_NOPWNAGE_CAPTCHA_TRIES              = CHAT_NOPWNAGE_CAPTCHA + "tries";
    public static final String  CHAT_NOPWNAGE_CAPTCHA_ACTIONS            = CHAT_NOPWNAGE_CAPTCHA + "actions";

    private static final String CHAT_NOPWNAGE_FIRST                      = CHAT_NOPWNAGE + "first.";
    public static final String  CHAT_NOPWNAGE_FIRST_CHECK                = CHAT_NOPWNAGE_FIRST + "active";
    public static final String  CHAT_NOPWNAGE_FIRST_TIMEOUT              = CHAT_NOPWNAGE_FIRST + "timeout";
    public static final String  CHAT_NOPWNAGE_FIRST_WEIGHT               = CHAT_NOPWNAGE_FIRST + "weight";

    private static final String CHAT_NOPWNAGE_GLOBAL                     = CHAT_NOPWNAGE + "global.";
    public static final String  CHAT_NOPWNAGE_GLOBAL_CHECK               = CHAT_NOPWNAGE_GLOBAL + "active";
    public static final String  CHAT_NOPWNAGE_GLOBAL_TIMEOUT             = CHAT_NOPWNAGE_GLOBAL + "timeout";
    public static final String  CHAT_NOPWNAGE_GLOBAL_WEIGHT              = CHAT_NOPWNAGE_GLOBAL + "weight";

    private static final String CHAT_NOPWNAGE_MOVE                       = CHAT_NOPWNAGE + "move.";
    public static final String  CHAT_NOPWNAGE_MOVE_CHECK                 = CHAT_NOPWNAGE_MOVE + "active";
    public static final String  CHAT_NOPWNAGE_MOVE_TIMEOUT               = CHAT_NOPWNAGE_MOVE + "timeout";
    public static final String  CHAT_NOPWNAGE_MOVE_WEIGHT                = CHAT_NOPWNAGE_MOVE + "weight";

    private static final String CHAT_NOPWNAGE_RELOGIN                    = CHAT_NOPWNAGE + "relogin.";
    public static final String  CHAT_NOPWNAGE_RELOGIN_CHECK              = CHAT_NOPWNAGE_RELOGIN + "active";
    public static final String  CHAT_NOPWNAGE_RELOGIN_KICKMESSAGE        = CHAT_NOPWNAGE_RELOGIN + "kickmessage";
    public static final String  CHAT_NOPWNAGE_RELOGIN_TIMEOUT            = CHAT_NOPWNAGE_RELOGIN + "timeout";

    private static final String CHAT_NOPWNAGE_RELOGIN_WARNING            = CHAT_NOPWNAGE_RELOGIN + "warning.";
    public static final String  CHAT_NOPWNAGE_RELOGIN_WARNING_MESSAGE    = CHAT_NOPWNAGE_RELOGIN_WARNING + "message";
    public static final String  CHAT_NOPWNAGE_RELOGIN_WARNING_NUMBER     = CHAT_NOPWNAGE_RELOGIN_WARNING + "number";
    public static final String  CHAT_NOPWNAGE_RELOGIN_WARNING_TIMEOUT    = CHAT_NOPWNAGE_RELOGIN_WARNING + "timeout";

    private static final String CHAT_NOPWNAGE_REPEAT                     = CHAT_NOPWNAGE + "repeat.";
    public static final String  CHAT_NOPWNAGE_REPEAT_CHECK               = CHAT_NOPWNAGE_REPEAT + "active";
    public static final String  CHAT_NOPWNAGE_REPEAT_TIMEOUT             = CHAT_NOPWNAGE_REPEAT + "timeout";
    public static final String  CHAT_NOPWNAGE_REPEAT_WEIGHT              = CHAT_NOPWNAGE_REPEAT + "weight";

    private static final String CHAT_NOPWNAGE_SPEED                      = CHAT_NOPWNAGE + "speed.";
    public static final String  CHAT_NOPWNAGE_SPEED_CHECK                = CHAT_NOPWNAGE_SPEED + "active";
//    public static final String  CHAT_NOPWNAGE_SPEED_TIMEOUT              = CHAT_NOPWNAGE_SPEED + "timeout";
    public static final String  CHAT_NOPWNAGE_SPEED_WEIGHT               = CHAT_NOPWNAGE_SPEED + "weight";
    
    public static final String CHAT_NOPWNAGE_VL                          = CHAT_NOPWNAGE + "vl.";
	public static final String CHAT_NOPWNAGE_VL_FACTOR                   = CHAT_NOPWNAGE_VL + "factor";

    private static final String CHAT_NOPWNAGE_WARN                       = CHAT_NOPWNAGE + "warn.";
    public static final String  CHAT_NOPWNAGE_WARN_LEVEL                 = CHAT_NOPWNAGE_WARN + "level";
    public static final String  CHAT_NOPWNAGE_WARN_TIMEOUT               = CHAT_NOPWNAGE_WARN + "timeout";

    private static final String CHAT_NOPWNAGE_WARN_OTHERS                = CHAT_NOPWNAGE_WARN + "others.";
    public static final String  CHAT_NOPWNAGE_WARN_OTHERS_CHECK          = CHAT_NOPWNAGE_WARN_OTHERS + "active";
    public static final String  CHAT_NOPWNAGE_WARN_OTHERS_MESSAGE        = CHAT_NOPWNAGE_WARN_OTHERS + "message";

    private static final String CHAT_NOPWNAGE_WARN_PLAYER                = CHAT_NOPWNAGE_WARN + "player.";
    public static final String  CHAT_NOPWNAGE_WARN_PLAYER_CHECK          = CHAT_NOPWNAGE_WARN_PLAYER + "active";
    public static final String  CHAT_NOPWNAGE_WARN_PLAYER_MESSAGE        = CHAT_NOPWNAGE_WARN_PLAYER + "message";

    public static final String  CHAT_NOPWNAGE_ACTIONS                    = CHAT_NOPWNAGE + "actions";

    /*
     * 888'Y88 ,e,          888       d8   
     * 888 ,'Y  "   e88 888 888 ee   d88   
     * 888C8   888 d888 888 888 88b d88888 
     * 888 "   888 Y888 888 888 888  888   
     * 888     888  "88 888 888 888  888   
     *               ,  88P                
     *              "8",P"                 
     */
    private static final String FIGHT                                    = CHECKS + "fight.";

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
    public static final String  FIGHT_DIRECTION_PENALTY                  = FIGHT_DIRECTION + "penalty";
    public static final String  FIGHT_DIRECTION_ACTIONS                  = FIGHT_DIRECTION + "actions";

    private static final String FIGHT_GODMODE                            = FIGHT + "godmode.";
    public static final String  FIGHT_GODMODE_CHECK                      = FIGHT_GODMODE + "active";
    public static final String  FIGHT_GODMODE_ACTIONS                    = FIGHT_GODMODE + "actions";

    private static final String FIGHT_INSTANTHEAL                        = FIGHT + "instantheal.";
    public static final String  FIGHT_INSTANTHEAL_CHECK                  = FIGHT_INSTANTHEAL + "active";
    public static final String  FIGHT_INSTANTHEAL_ACTIONS                = FIGHT_INSTANTHEAL + "actions";

    private static final String FIGHT_KNOCKBACK                          = FIGHT + "knockback.";
    public static final String  FIGHT_KNOCKBACK_CHECK                    = FIGHT_KNOCKBACK + "active";
    public static final String  FIGHT_KNOCKBACK_INTERVAL                 = FIGHT_KNOCKBACK + "interval";
    public static final String  FIGHT_KNOCKBACK_ACTIONS                  = FIGHT_KNOCKBACK + "actions";

    private static final String FIGHT_NOSWING                            = FIGHT + "noswing.";
    public static final String  FIGHT_NOSWING_CHECK                      = FIGHT_NOSWING + "active";
    public static final String  FIGHT_NOSWING_ACTIONS                    = FIGHT_NOSWING + "actions";

    private static final String FIGHT_REACH                              = FIGHT + "reach.";
    public static final String  FIGHT_REACH_CHECK                        = FIGHT_REACH + "active";
    public static final String  FIGHT_REACH_PENALTY                      = FIGHT_REACH + "penalty";
    public static final String  FIGHT_REACH_ACTIONS                      = FIGHT_REACH + "actions";

    private static final String FIGHT_SPEED                              = FIGHT + "speed.";
    public static final String  FIGHT_SPEED_CHECK                        = FIGHT_SPEED + "active";
    public static final String  FIGHT_SPEED_LIMIT                        = FIGHT_SPEED + "limit";
    public static final String  FIGHT_SPEED_ACTIONS                      = FIGHT_SPEED + "actions";

    /*
     * 888                                     d8                              
     * 888 888 8e  Y8b Y888P  ,e e,  888 8e   d88    e88 88e  888,8, Y8b Y888P 
     * 888 888 88b  Y8b Y8P  d88 88b 888 88b d88888 d888 888b 888 "   Y8b Y8P  
     * 888 888 888   Y8b "   888   , 888 888  888   Y888 888P 888      Y8b Y   
     * 888 888 888    Y8P     "YeeP" 888 888  888    "88 88"  888       888    
     *                                                                  888    
     *                                                                  888    
     */
    private static final String INVENTORY                                = CHECKS + "inventory.";

    private static final String INVENTORY_DROP                           = INVENTORY + "drop.";
    public static final String  INVENTORY_DROP_CHECK                     = INVENTORY_DROP + "active";
    public static final String  INVENTORY_DROP_LIMIT                     = INVENTORY_DROP + "limit";
    public static final String  INVENTORY_DROP_TIMEFRAME                 = INVENTORY_DROP + "timeframe";
    public static final String  INVENTORY_DROP_ACTIONS                   = INVENTORY_DROP + "actions";

    private static final String INVENTORY_FASTCLICK                      = INVENTORY + "fastclick.";
    public static final String  INVENTORY_FASTCLICK_CHECK                = INVENTORY_FASTCLICK + "active";
    public static final String  INVENTORY_FASTCLICK_ACTIONS              = INVENTORY_FASTCLICK + "actions";

    private static final String INVENTORY_INSTANTBOW                     = INVENTORY + "instantbow.";
    public static final String  INVENTORY_INSTANTBOW_CHECK               = INVENTORY_INSTANTBOW + "active";
    public static final String  INVENTORY_INSTANTBOW_ACTIONS             = INVENTORY_INSTANTBOW + "actions";

    private static final String INVENTORY_INSTANTEAT                     = INVENTORY + "instanteat.";
    public static final String  INVENTORY_INSTANTEAT_CHECK               = INVENTORY_INSTANTEAT + "active";
    public static final String  INVENTORY_INSTANTEAT_ACTIONS             = INVENTORY_INSTANTEAT + "actions";

    /*
     *     e   e                         ,e,                  
     *    d8b d8b     e88 88e  Y8b Y888P  "  888 8e   e88 888 
     *   e Y8b Y8b   d888 888b  Y8b Y8P  888 888 88b d888 888 
     *  d8b Y8b Y8b  Y888 888P   Y8b "   888 888 888 Y888 888 
     * d888b Y8b Y8b  "88 88"     Y8P    888 888 888  "88 888 
     *                                                 ,  88P 
     *                                                "8",P"  
     */
    private static final String MOVING                                   = CHECKS + "moving.";

    private static final String MOVING_CREATIVEFLY                       = MOVING + "creativefly.";
    public static final String  MOVING_CREATIVEFLY_CHECK                 = MOVING_CREATIVEFLY + "active";
    public static final String  MOVING_CREATIVEFLY_HORIZONTALSPEED       = MOVING_CREATIVEFLY + "horizontalspeed";
    public static final String  MOVING_CREATIVEFLY_MAXHEIGHT             = MOVING_CREATIVEFLY + "maxheight";
    public static final String  MOVING_CREATIVEFLY_VERTICALSPEED         = MOVING_CREATIVEFLY + "verticalspeed";
    public static final String  MOVING_CREATIVEFLY_ACTIONS               = MOVING_CREATIVEFLY + "actions";

    private static final String MOVING_MOREPACKETS                       = MOVING + "morepackets.";
    public static final String  MOVING_MOREPACKETS_CHECK                 = MOVING_MOREPACKETS + "active";
    public static final String  MOVING_MOREPACKETS_ACTIONS               = MOVING_MOREPACKETS + "actions";

    private static final String MOVING_MOREPACKETSVEHICLE                = MOVING + "morepacketsvehicle.";
    public static final String  MOVING_MOREPACKETSVEHICLE_CHECK          = MOVING_MOREPACKETSVEHICLE + "active";
    public static final String  MOVING_MOREPACKETSVEHICLE_ACTIONS        = MOVING_MOREPACKETSVEHICLE + "actions";

    private static final String MOVING_NOFALL                            = MOVING + "nofall.";
    public static final String  MOVING_NOFALL_CHECK                      = MOVING_NOFALL + "active";
    public static final String  MOVING_NOFALL_ACTIONS                    = MOVING_NOFALL + "actions";

    private static final String MOVING_SURVIVALFLY                       = MOVING + "survivalfly.";
    public static final String  MOVING_SURVIVALFLY_CHECK                 = MOVING_SURVIVALFLY + "active";
    public static final String  MOVING_SURVIVALFLY_BLOCKINGSPEED         = MOVING_SURVIVALFLY + "blockingspeed";
    public static final String  MOVING_SURVIVALFLY_SNEAKINGSPEED         = MOVING_SURVIVALFLY + "sneakingspeed";
	public static final String  MOVING_SURVIVALFLY_SPEEDINGSPEED         = MOVING_SURVIVALFLY + "speedingspeed";
    public static final String  MOVING_SURVIVALFLY_SPRINTINGSPEED        = MOVING_SURVIVALFLY + "sprintingspeed";
    public static final String  MOVING_SURVIVALFLY_SWIMMINGSPEED         = MOVING_SURVIVALFLY + "swimmingspeed";
    public static final String  MOVING_SURVIVALFLY_WALKINGSPEED          = MOVING_SURVIVALFLY + "walkingspeed";
    public static final String  MOVING_SURVIVALFLY_ACTIONS               = MOVING_SURVIVALFLY + "actions";
    
    // Special (to be sorted in or factored out).
    public static final String  MOVING_NOFALL_YONGROUND                  = MOVING_NOFALL + "yonground";
    public static final String  MOVING_YONGROUND                         = MOVING + "yonground";

    /*
     *  dP"8   d8          ,e,                        
     * C8b Y  d88   888,8,  "  888 8e   e88 888  dP"Y 
     *  Y8b  d88888 888 "  888 888 88b d888 888 C88b  
     * b Y8D  888   888    888 888 888 Y888 888  Y88D 
     * 8edP   888   888    888 888 888  "88 888 d,dP  
     *                                   ,  88P       
     *                                  "8",P"        
     */
    public static final String  STRINGS                                  = "strings";
    
}
