package me.neatmonster.nocheatplus.config;

/**
 * Paths for the configuration options
 * Making everything final static prevents accidentially modifying any
 * of these
 * 
 */
public abstract class ConfPaths {

    private final static String CLIENT_MODS                               = "clientmods.";
    public final static String  CLIENT_MODS_ALLOW                         = CLIENT_MODS + "allow";

    private final static String LOGGING                                   = "logging.";
    public final static String  LOGGING_ACTIVE                            = LOGGING + "active";
    public final static String  LOGGING_PREFIX                            = LOGGING + "prefix";
    public final static String  LOGGING_FILENAME                          = LOGGING + "filename";
    public final static String  LOGGING_LOGTOFILE                         = LOGGING + "file";
    public final static String  LOGGING_LOGTOCONSOLE                      = LOGGING + "console";
    public final static String  LOGGING_LOGTOINGAMECHAT                   = LOGGING + "ingamechat";
    public final static String  LOGGING_SHOWACTIVECHECKS                  = LOGGING + "showactivechecks";
    public final static String  LOGGING_DEBUGMESSAGES                     = LOGGING + "debugmessages";

    private final static String CHECKS                                    = "checks.";

    private final static String INVENTORY                                 = CHECKS + "inventory.";

    private final static String INVENTORY_DROP                            = INVENTORY + "drop.";
    public final static String  INVENTORY_DROP_CHECK                      = INVENTORY_DROP + "active";
    public final static String  INVENTORY_DROP_TIMEFRAME                  = INVENTORY_DROP + "time";
    public final static String  INVENTORY_DROP_LIMIT                      = INVENTORY_DROP + "limit";
    public final static String  INVENTORY_DROP_ACTIONS                    = INVENTORY_DROP + "actions";

    private static final String INVENTORY_INSTANTBOW                      = INVENTORY + "instantbow.";
    public final static String  INVENTORY_INSTANTBOW_CHECK                = INVENTORY_INSTANTBOW + "active";
    public static final String  INVENTORY_INSTANTBOW_ACTIONS              = INVENTORY_INSTANTBOW + "actions";

    private static final String INVENTORY_INSTANTEAT                      = INVENTORY + "instanteat.";
    public final static String  INVENTORY_INSTANTEAT_CHECK                = INVENTORY_INSTANTEAT + "active";
    public static final String  INVENTORY_INSTANTEAT_ACTIONS              = INVENTORY_INSTANTEAT + "actions";

    private final static String MOVING                                    = CHECKS + "moving.";

    private final static String MOVING_RUNFLY                             = MOVING + "runfly.";
    public final static String  MOVING_RUNFLY_CHECK                       = MOVING_RUNFLY + "active";

    // These four are not automatically shown in the config
    public final static String  MOVING_RUNFLY_WALKSPEED                   = MOVING_RUNFLY + "walkspeed";
    public final static String  MOVING_RUNFLY_SNEAKSPEED                  = MOVING_RUNFLY + "sneakspeed";
    public final static String  MOVING_RUNFLY_SWIMSPEED                   = MOVING_RUNFLY + "swimspeed";
    public final static String  MOVING_RUNFLY_SPRINTSPEED                 = MOVING_RUNFLY + "sprintspeed";

    public final static String  MOVING_RUNFLY_ALLOWFASTSNEAKING           = MOVING_RUNFLY + "allowfastsneaking";
    public final static String  MOVING_RUNFLY_MAXCOOLDOWN                 = MOVING_RUNFLY + "maxcooldown";
    public final static String  MOVING_RUNFLY_ACTIONS                     = MOVING_RUNFLY + "actions";

    public final static String  MOVING_RUNFLY_CHECKNOFALL                 = MOVING_RUNFLY + "checknofall";
    public final static String  MOVING_RUNFLY_NOFALLAGGRESSIVE            = MOVING_RUNFLY + "nofallaggressivemode";
    public final static String  MOVING_RUNFLY_NOFALLACTIONS               = MOVING_RUNFLY + "nofallactions";

    private final static String MOVING_RUNFLY_TRACKER                     = MOVING_RUNFLY + "tracker.";
    public final static String  MOVING_RUNFLY_TRACKER_CHECK               = MOVING_RUNFLY_TRACKER + "active";
    public final static String  MOVING_RUNFLY_TRACKER_ACTIONS             = MOVING_RUNFLY_TRACKER + "actions";

    private final static String MOVING_RUNFLY_FLYING                      = MOVING_RUNFLY + "flying.";
    public final static String  MOVING_RUNFLY_FLYING_ALLOWALWAYS          = MOVING_RUNFLY_FLYING + "allowflyingalways";
    public final static String  MOVING_RUNFLY_FLYING_ALLOWINCREATIVE      = MOVING_RUNFLY_FLYING
                                                                                  + "allowflyingincreative";
    public final static String  MOVING_RUNFLY_FLYING_SPEEDLIMITVERTICAL   = MOVING_RUNFLY_FLYING
                                                                                  + "flyingspeedlimitvertical";
    public final static String  MOVING_RUNFLY_FLYING_SPEEDLIMITHORIZONTAL = MOVING_RUNFLY_FLYING
                                                                                  + "flyingspeedlimithorizontal";
    public final static String  MOVING_RUNFLY_FLYING_HEIGHTLIMIT          = MOVING_RUNFLY_FLYING + "flyingheightlimit";
    public final static String  MOVING_RUNFLY_FLYING_ACTIONS              = MOVING_RUNFLY_FLYING + "actions";

    private final static String MOVING_MOREPACKETS                        = MOVING + "morepackets.";
    public final static String  MOVING_MOREPACKETS_CHECK                  = MOVING_MOREPACKETS + "active";
    public final static String  MOVING_MOREPACKETS_ACTIONS                = MOVING_MOREPACKETS + "actions";

    private final static String MOVING_MOREPACKETSVEHICLE                 = MOVING + "morepacketsvehicle.";
    public final static String  MOVING_MOREPACKETSVEHICLE_CHECK           = MOVING_MOREPACKETSVEHICLE + "active";
    public final static String  MOVING_MOREPACKETSVEHICLE_ACTIONS         = MOVING_MOREPACKETSVEHICLE + "actions";

    private final static String MOVING_WATERWALK                          = MOVING + "waterwalk.";
    public final static String  MOVING_WATERWALK_CHECK                    = MOVING_WATERWALK + "active";
    public final static String  MOVING_WATERWALK_ACTIONS                  = MOVING_WATERWALK + "actions";

    private final static String BLOCKBREAK                                = CHECKS + "blockbreak.";

    private final static String BLOCKBREAK_FASTBREAK                      = BLOCKBREAK + "fastbreak.";
    public final static String  BLOCKBREAK_FASTBREAK_CHECK                = BLOCKBREAK_FASTBREAK + "active";
    public final static String  BLOCKBREAK_FASTBREAK_INTERVALSURVIVAL     = BLOCKBREAK_FASTBREAK + "intervalsurvival";
    public final static String  BLOCKBREAK_FASTBREAK_INTERVALCREATIVE     = BLOCKBREAK_FASTBREAK + "intervalcreative";
    public final static String  BLOCKBREAK_FASTBREAK_ACTIONS              = BLOCKBREAK_FASTBREAK + "actions";

    private final static String BLOCKBREAK_REACH                          = BLOCKBREAK + "reach.";
    public final static String  BLOCKBREAK_REACH_CHECK                    = BLOCKBREAK_REACH + "active";
    public final static String  BLOCKBREAK_REACH_ACTIONS                  = BLOCKBREAK_REACH + "actions";

    private final static String BLOCKBREAK_DIRECTION                      = BLOCKBREAK + "direction.";
    public final static String  BLOCKBREAK_DIRECTION_CHECK                = BLOCKBREAK_DIRECTION + "active";
    public final static String  BLOCKBREAK_DIRECTION_PRECISION            = BLOCKBREAK_DIRECTION + "precision";
    public final static String  BLOCKBREAK_DIRECTION_PENALTYTIME          = BLOCKBREAK_DIRECTION + "penaltytime";
    public final static String  BLOCKBREAK_DIRECTION_ACTIONS              = BLOCKBREAK_DIRECTION + "actions";

    private final static String BLOCKBREAK_NOSWING                        = BLOCKBREAK + "noswing.";
    public static final String  BLOCKBREAK_NOSWING_CHECK                  = BLOCKBREAK_NOSWING + "active";
    public static final String  BLOCKBREAK_NOSWING_ACTIONS                = BLOCKBREAK_NOSWING + "actions";

    private final static String BLOCKPLACE                                = CHECKS + "blockplace.";

    private final static String BLOCKPLACE_FASTPLACE                      = BLOCKPLACE + "fastplace.";
    public final static String  BLOCKPLACE_FASTPLACE_CHECK                = BLOCKPLACE_FASTPLACE + "active";
    public final static String  BLOCKPLACE_FASTPLACE_INTERVAL             = BLOCKPLACE_FASTPLACE + "interval";
    public final static String  BLOCKPLACE_FASTPLACE_ACTIONS              = BLOCKPLACE_FASTPLACE + "actions";

    private final static String BLOCKPLACE_REACH                          = BLOCKPLACE + "reach.";
    public final static String  BLOCKPLACE_REACH_CHECK                    = BLOCKPLACE_REACH + "active";
    public final static String  BLOCKPLACE_REACH_ACTIONS                  = BLOCKPLACE_REACH + "actions";

    private final static String BLOCKPLACE_DIRECTION                      = BLOCKPLACE + "direction.";
    public final static String  BLOCKPLACE_DIRECTION_CHECK                = BLOCKPLACE_DIRECTION + "active";
    public final static String  BLOCKPLACE_DIRECTION_PRECISION            = BLOCKPLACE_DIRECTION + "precision";
    public final static String  BLOCKPLACE_DIRECTION_PENALTYTIME          = BLOCKPLACE_DIRECTION + "penaltytime";
    public final static String  BLOCKPLACE_DIRECTION_ACTIONS              = BLOCKPLACE_DIRECTION + "actions";

    private final static String BLOCKPLACE_PROJECTILE                     = BLOCKPLACE + "projectile.";
    public final static String  BLOCKPLACE_PROJECTILE_CHECK               = BLOCKPLACE_PROJECTILE + "active";
    public final static String  BLOCKPLACE_PROJECTILE_INTERVAL            = BLOCKPLACE_PROJECTILE + "interval";
    public final static String  BLOCKPLACE_PROJECTILE_ACTIONS             = BLOCKPLACE_PROJECTILE + "actions";

    private final static String CHAT                                      = CHECKS + "chat.";

    public final static String  CHAT_HIDECOMMANDS                         = CHAT + "hidecommands";

    private final static String CHAT_NOPWNAGE                             = CHAT + "nopwnage.";
    public final static String  CHAT_NOPWNAGE_CHECK                       = CHAT_NOPWNAGE + "active";
    public final static String  CHAT_NOPWNAGE_WARNPLAYERS                 = CHAT_NOPWNAGE + "warnplayers";
    public final static String  CHAT_NOPWNAGE_WARNOTHERS                  = CHAT_NOPWNAGE + "warnothers";
    public final static String  CHAT_NOPWNAGE_WARNLEVEL                   = CHAT_NOPWNAGE + "warnlevel";
    public final static String  CHAT_NOPWNAGE_WARNTIMEOUT                 = CHAT_NOPWNAGE + "warntimeout";
    public final static String  CHAT_NOPWNAGE_BANLEVEL                    = CHAT_NOPWNAGE + "banlevel";
    public final static String  CHAT_NOPWNAGE_ACTIONS                     = CHAT_NOPWNAGE + "actions";

    private final static String CHAT_NOPWNAGE_MOVE                        = CHAT_NOPWNAGE + "move.";
    public final static String  CHAT_NOPWNAGE_MOVE_CHECK                  = CHAT_NOPWNAGE_MOVE + "active";
    public final static String  CHAT_NOPWNAGE_MOVE_WEIGHTBONUS            = CHAT_NOPWNAGE_MOVE + "weightbonus";
    public final static String  CHAT_NOPWNAGE_MOVE_WEIGHTMALUS            = CHAT_NOPWNAGE_MOVE + "weightmalus";
    public final static String  CHAT_NOPWNAGE_MOVE_TIMEOUT                = CHAT_NOPWNAGE_MOVE + "timeout";

    private final static String CHAT_NOPWNAGE_REPEAT                      = CHAT_NOPWNAGE + "repeat.";
    public final static String  CHAT_NOPWNAGE_REPEAT_CHECK                = CHAT_NOPWNAGE_REPEAT + "active";
    public final static String  CHAT_NOPWNAGE_REPEAT_WEIGHT               = CHAT_NOPWNAGE_REPEAT + "weight";
    public final static String  CHAT_NOPWNAGE_REPEAT_TIMEOUT              = CHAT_NOPWNAGE_REPEAT + "timeout";

    private final static String CHAT_NOPWNAGE_SPEED                       = CHAT_NOPWNAGE + "speed.";
    public final static String  CHAT_NOPWNAGE_SPEED_CHECK                 = CHAT_NOPWNAGE_SPEED + "active";
    public final static String  CHAT_NOPWNAGE_SPEED_WEIGHT                = CHAT_NOPWNAGE_SPEED + "weight";
    public final static String  CHAT_NOPWNAGE_SPEED_TIMEOUT               = CHAT_NOPWNAGE_SPEED + "timeout";

    private final static String CHAT_NOPWNAGE_FIRST                       = CHAT_NOPWNAGE + "first.";
    public final static String  CHAT_NOPWNAGE_FIRST_CHECK                 = CHAT_NOPWNAGE_FIRST + "active";
    public final static String  CHAT_NOPWNAGE_FIRST_WEIGHT                = CHAT_NOPWNAGE_FIRST + "weight";
    public final static String  CHAT_NOPWNAGE_FIRST_TIMEOUT               = CHAT_NOPWNAGE_FIRST + "timeout";

    private final static String CHAT_NOPWNAGE_GLOBAL                      = CHAT_NOPWNAGE + "global.";
    public final static String  CHAT_NOPWNAGE_GLOBAL_CHECK                = CHAT_NOPWNAGE_GLOBAL + "active";
    public final static String  CHAT_NOPWNAGE_GLOBAL_WEIGHT               = CHAT_NOPWNAGE_GLOBAL + "weight";
    public final static String  CHAT_NOPWNAGE_GLOBAL_TIMEOUT              = CHAT_NOPWNAGE_GLOBAL + "timeout";

    private final static String CHAT_NOPWNAGE_BANNED                      = CHAT_NOPWNAGE + "banned.";
    public final static String  CHAT_NOPWNAGE_BANNED_CHECK                = CHAT_NOPWNAGE_BANNED + "active";
    public final static String  CHAT_NOPWNAGE_BANNED_WEIGHT               = CHAT_NOPWNAGE_BANNED + "weight";
    public final static String  CHAT_NOPWNAGE_BANNED_TIMEOUT              = CHAT_NOPWNAGE_BANNED + "timeout";

    private final static String CHAT_NOPWNAGE_RELOG                       = CHAT_NOPWNAGE + "relog.";
    public final static String  CHAT_NOPWNAGE_RELOG_CHECK                 = CHAT_NOPWNAGE_RELOG + "active";
    public final static String  CHAT_NOPWNAGE_RELOG_TIME                  = CHAT_NOPWNAGE_RELOG + "time";
    public final static String  CHAT_NOPWNAGE_RELOG_WARNINGS              = CHAT_NOPWNAGE_RELOG + "warnings";
    public final static String  CHAT_NOPWNAGE_RELOG_TIMEOUT               = CHAT_NOPWNAGE_RELOG + "timeout";

    private final static String CHAT_NOPWNAGE_CAPTCHA                     = CHAT_NOPWNAGE + "captcha.";
    public final static String  CHAT_NOPWNAGE_CAPTCHA_CHECK               = CHAT_NOPWNAGE_CAPTCHA + "active";
    public final static String  CHAT_NOPWNAGE_CAPTCHA_TRIES               = CHAT_NOPWNAGE_CAPTCHA + "tries";
    public final static String  CHAT_NOPWNAGE_CAPTCHA_LENGTH              = CHAT_NOPWNAGE_CAPTCHA + "length";
    public final static String  CHAT_NOPWNAGE_CAPTCHA_CHARACTERS          = CHAT_NOPWNAGE_CAPTCHA + "characters";

    private final static String CHAT_ARRIVALSLIMIT                        = CHAT + "arrivalslimit.";
    public final static String  CHAT_ARRIVALSLIMIT_CHECK                  = CHAT_ARRIVALSLIMIT + "active";
    public final static String  CHAT_ARRIVALSLIMIT_PLAYERSLIMIT           = CHAT_ARRIVALSLIMIT + "playerslimit";
    public final static String  CHAT_ARRIVALSLIMIT_TIMEFRAME              = CHAT_ARRIVALSLIMIT + "timeframe";
    public final static String  CHAT_ARRIVALSLIMIT_COOLDOWNDELAY          = CHAT_ARRIVALSLIMIT + "cooldowndelay";
    public final static String  CHAT_ARRIVALSLIMIT_KICKMESSAGE            = CHAT_ARRIVALSLIMIT + "kickmessage";
    public final static String  CHAT_ARRIVALSLIMIT_NEWTIME                = CHAT_ARRIVALSLIMIT + "newtime";
    public final static String  CHAT_ARRIVALSLIMIT_ACTIONS                = CHAT_ARRIVALSLIMIT + "actions";

    private final static String CHAT_COLOR                                = CHAT + "color.";
    public final static String  CHAT_COLOR_CHECK                          = CHAT_COLOR + "active";
    public final static String  CHAT_COLOR_ACTIONS                        = CHAT_COLOR + "actions";

    private final static String FIGHT                                     = CHECKS + "fight.";

    private final static String FIGHT_DIRECTION                           = FIGHT + "direction.";
    public final static String  FIGHT_DIRECTION_CHECK                     = FIGHT_DIRECTION + "active";
    public final static String  FIGHT_DIRECTION_PRECISION                 = FIGHT_DIRECTION + "precision";
    public final static String  FIGHT_DIRECTION_PENALTYTIME               = FIGHT_DIRECTION + "penaltytime";
    public final static String  FIGHT_DIRECTION_ACTIONS                   = FIGHT_DIRECTION + "actions";

    private final static String FIGHT_NOSWING                             = FIGHT + "noswing.";
    public final static String  FIGHT_NOSWING_CHECK                       = FIGHT_NOSWING + "active";
    public final static String  FIGHT_NOSWING_ACTIONS                     = FIGHT_NOSWING + "actions";

    private final static String FIGHT_REACH                               = FIGHT + "reach.";
    public static final String  FIGHT_REACH_CHECK                         = FIGHT_REACH + "active";
    public static final String  FIGHT_REACH_LIMIT                         = FIGHT_REACH + "distance";
    public static final String  FIGHT_REACH_PENALTYTIME                   = FIGHT_REACH + "penaltytime";
    public static final String  FIGHT_REACH_ACTIONS                       = FIGHT_REACH + "actions";

    private final static String FIGHT_SPEED                               = FIGHT + "speed.";
    public final static String  FIGHT_SPEED_CHECK                         = FIGHT_SPEED + "active";
    public final static String  FIGHT_SPEED_ATTACKLIMIT                   = FIGHT_SPEED + "attacklimit";
    public final static String  FIGHT_SPEED_ACTIONS                       = FIGHT_SPEED + "actions";

    private final static String FIGHT_GODMODE                             = FIGHT + "godmode.";
    public static final String  FIGHT_GODMODE_CHECK                       = FIGHT_GODMODE + "active";
    public final static String  FIGHT_GODMODE_ACTIONS                     = FIGHT_GODMODE + "actions";

    private final static String FIGHT_INSTANTHEAL                         = FIGHT + "instantheal.";
    public static final String  FIGHT_INSTANTHEAL_CHECK                   = FIGHT_INSTANTHEAL + "active";
    public final static String  FIGHT_INSTANTHEAL_ACTIONS                 = FIGHT_INSTANTHEAL + "actions";

    public final static String  STRINGS                                   = "strings";

}
