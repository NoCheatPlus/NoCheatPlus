package me.neatmonster.nocheatplus.config;

/**
 * Paths for the configuration options
 * Making everything final static prevents accidentially modifying any
 * of these
 * 
 */
public abstract class ConfPaths {

    private static final String LOGGING                                   = "logging.";
    public static final String  LOGGING_ACTIVE                            = LOGGING + "active";
    public static final String  LOGGING_PREFIX                            = LOGGING + "prefix";
    public static final String  LOGGING_FILENAME                          = LOGGING + "filename";
    public static final String  LOGGING_LOGTOFILE                         = LOGGING + "file";
    public static final String  LOGGING_LOGTOCONSOLE                      = LOGGING + "console";
    public static final String  LOGGING_LOGTOINGAMECHAT                   = LOGGING + "ingamechat";
    public static final String  LOGGING_SHOWACTIVECHECKS                  = LOGGING + "showactivechecks";
    public static final String  LOGGING_DEBUGMESSAGES                     = LOGGING + "debugmessages";

    private static final String MISCELLANEOUS                             = "miscellaneous.";
    public static final String  MISCELLANEOUS_ALLOWCLIENTMODS             = MISCELLANEOUS + "allowclientmods";
    public static final String  MISCELLANEOUS_OPBYCONSOLEONLY             = MISCELLANEOUS + "opbyconsoleonly";
    public static final String  MISCELLANEOUS_PROTECTPLUGINS              = MISCELLANEOUS + "protectplugins";

    private static final String CHECKS                                    = "checks.";

    private static final String INVENTORY                                 = CHECKS + "inventory.";

    private static final String INVENTORY_DROP                            = INVENTORY + "drop.";
    public static final String  INVENTORY_DROP_CHECK                      = INVENTORY_DROP + "active";
    public static final String  INVENTORY_DROP_TIMEFRAME                  = INVENTORY_DROP + "time";
    public static final String  INVENTORY_DROP_LIMIT                      = INVENTORY_DROP + "limit";
    public static final String  INVENTORY_DROP_ACTIONS                    = INVENTORY_DROP + "actions";

    private static final String INVENTORY_INSTANTBOW                      = INVENTORY + "instantbow.";
    public static final String  INVENTORY_INSTANTBOW_CHECK                = INVENTORY_INSTANTBOW + "active";
    public static final String  INVENTORY_INSTANTBOW_ACTIONS              = INVENTORY_INSTANTBOW + "actions";

    private static final String INVENTORY_INSTANTEAT                      = INVENTORY + "instanteat.";
    public static final String  INVENTORY_INSTANTEAT_CHECK                = INVENTORY_INSTANTEAT + "active";
    public static final String  INVENTORY_INSTANTEAT_ACTIONS              = INVENTORY_INSTANTEAT + "actions";

    private static final String MOVING                                    = CHECKS + "moving.";

    private static final String MOVING_RUNFLY                             = MOVING + "runfly.";
    public static final String  MOVING_RUNFLY_CHECK                       = MOVING_RUNFLY + "active";

    // These four are not automatically shown in the config
    public static final String  MOVING_RUNFLY_WALKSPEED                   = MOVING_RUNFLY + "walkspeed";
    public static final String  MOVING_RUNFLY_SNEAKSPEED                  = MOVING_RUNFLY + "sneakspeed";
    public static final String  MOVING_RUNFLY_SWIMSPEED                   = MOVING_RUNFLY + "swimspeed";
    public static final String  MOVING_RUNFLY_SPRINTSPEED                 = MOVING_RUNFLY + "sprintspeed";

    public static final String  MOVING_RUNFLY_ALLOWFASTSNEAKING           = MOVING_RUNFLY + "allowfastsneaking";
    public static final String  MOVING_RUNFLY_MAXCOOLDOWN                 = MOVING_RUNFLY + "maxcooldown";
    public static final String  MOVING_RUNFLY_ACTIONS                     = MOVING_RUNFLY + "actions";

    public static final String  MOVING_RUNFLY_CHECKNOFALL                 = MOVING_RUNFLY + "checknofall";
    public static final String  MOVING_RUNFLY_NOFALLAGGRESSIVE            = MOVING_RUNFLY + "nofallaggressivemode";
    public static final String  MOVING_RUNFLY_NOFALLACTIONS               = MOVING_RUNFLY + "nofallactions";

    private static final String MOVING_RUNFLY_FLYING                      = MOVING_RUNFLY + "flying.";
    public static final String  MOVING_RUNFLY_FLYING_ALLOWALWAYS          = MOVING_RUNFLY_FLYING + "allowflyingalways";
    public static final String  MOVING_RUNFLY_FLYING_ALLOWINCREATIVE      = MOVING_RUNFLY_FLYING
                                                                                  + "allowflyingincreative";
    public static final String  MOVING_RUNFLY_FLYING_SPEEDLIMITVERTICAL   = MOVING_RUNFLY_FLYING
                                                                                  + "flyingspeedlimitvertical";
    public static final String  MOVING_RUNFLY_FLYING_SPEEDLIMITHORIZONTAL = MOVING_RUNFLY_FLYING
                                                                                  + "flyingspeedlimithorizontal";
    public static final String  MOVING_RUNFLY_FLYING_HEIGHTLIMIT          = MOVING_RUNFLY_FLYING + "flyingheightlimit";
    public static final String  MOVING_RUNFLY_FLYING_ACTIONS              = MOVING_RUNFLY_FLYING + "actions";

    private static final String MOVING_MOREPACKETS                        = MOVING + "morepackets.";
    public static final String  MOVING_MOREPACKETS_CHECK                  = MOVING_MOREPACKETS + "active";
    public static final String  MOVING_MOREPACKETS_ACTIONS                = MOVING_MOREPACKETS + "actions";

    private static final String MOVING_MOREPACKETSVEHICLE                 = MOVING + "morepacketsvehicle.";
    public static final String  MOVING_MOREPACKETSVEHICLE_CHECK           = MOVING_MOREPACKETSVEHICLE + "active";
    public static final String  MOVING_MOREPACKETSVEHICLE_ACTIONS         = MOVING_MOREPACKETSVEHICLE + "actions";

    private static final String MOVING_WATERWALK                          = MOVING + "waterwalk.";
    public static final String  MOVING_WATERWALK_CHECK                    = MOVING_WATERWALK + "active";
    public static final String  MOVING_WATERWALK_ACTIONS                  = MOVING_WATERWALK + "actions";

    private static final String MOVING_UNPUSHABLE                         = MOVING + "unpushable.";
    public static final String  MOVING_UNPUSHABLE_CHECK                   = MOVING_UNPUSHABLE + "active";

    private static final String BLOCKBREAK                                = CHECKS + "blockbreak.";

    private static final String BLOCKBREAK_FASTBREAK                      = BLOCKBREAK + "fastbreak.";
    public static final String  BLOCKBREAK_FASTBREAK_CHECK                = BLOCKBREAK_FASTBREAK + "active";
    public static final String  BLOCKBREAK_FASTBREAK_INTERVALSURVIVAL     = BLOCKBREAK_FASTBREAK + "intervalsurvival";
    public static final String  BLOCKBREAK_FASTBREAK_INTERVALCREATIVE     = BLOCKBREAK_FASTBREAK + "intervalcreative";
    public static final String  BLOCKBREAK_FASTBREAK_ACTIONS              = BLOCKBREAK_FASTBREAK + "actions";

    private static final String BLOCKBREAK_REACH                          = BLOCKBREAK + "reach.";
    public static final String  BLOCKBREAK_REACH_CHECK                    = BLOCKBREAK_REACH + "active";
    public static final String  BLOCKBREAK_REACH_ACTIONS                  = BLOCKBREAK_REACH + "actions";

    private static final String BLOCKBREAK_DIRECTION                      = BLOCKBREAK + "direction.";
    public static final String  BLOCKBREAK_DIRECTION_CHECK                = BLOCKBREAK_DIRECTION + "active";
    public static final String  BLOCKBREAK_DIRECTION_PRECISION            = BLOCKBREAK_DIRECTION + "precision";
    public static final String  BLOCKBREAK_DIRECTION_PENALTYTIME          = BLOCKBREAK_DIRECTION + "penaltytime";
    public static final String  BLOCKBREAK_DIRECTION_ACTIONS              = BLOCKBREAK_DIRECTION + "actions";

    private static final String BLOCKBREAK_NOSWING                        = BLOCKBREAK + "noswing.";
    public static final String  BLOCKBREAK_NOSWING_CHECK                  = BLOCKBREAK_NOSWING + "active";
    public static final String  BLOCKBREAK_NOSWING_ACTIONS                = BLOCKBREAK_NOSWING + "actions";

    private static final String BLOCKPLACE                                = CHECKS + "blockplace.";

    private static final String BLOCKPLACE_FASTPLACE                      = BLOCKPLACE + "fastplace.";
    public static final String  BLOCKPLACE_FASTPLACE_CHECK                = BLOCKPLACE_FASTPLACE + "active";
    public static final String  BLOCKPLACE_FASTPLACE_INTERVAL             = BLOCKPLACE_FASTPLACE + "interval";
    public static final String  BLOCKPLACE_FASTPLACE_ACTIONS              = BLOCKPLACE_FASTPLACE + "actions";

    private static final String BLOCKPLACE_REACH                          = BLOCKPLACE + "reach.";
    public static final String  BLOCKPLACE_REACH_CHECK                    = BLOCKPLACE_REACH + "active";
    public static final String  BLOCKPLACE_REACH_ACTIONS                  = BLOCKPLACE_REACH + "actions";

    private static final String BLOCKPLACE_DIRECTION                      = BLOCKPLACE + "direction.";
    public static final String  BLOCKPLACE_DIRECTION_CHECK                = BLOCKPLACE_DIRECTION + "active";
    public static final String  BLOCKPLACE_DIRECTION_PRECISION            = BLOCKPLACE_DIRECTION + "precision";
    public static final String  BLOCKPLACE_DIRECTION_PENALTYTIME          = BLOCKPLACE_DIRECTION + "penaltytime";
    public static final String  BLOCKPLACE_DIRECTION_ACTIONS              = BLOCKPLACE_DIRECTION + "actions";

    private static final String BLOCKPLACE_PROJECTILE                     = BLOCKPLACE + "projectile.";
    public static final String  BLOCKPLACE_PROJECTILE_CHECK               = BLOCKPLACE_PROJECTILE + "active";
    public static final String  BLOCKPLACE_PROJECTILE_INTERVAL            = BLOCKPLACE_PROJECTILE + "interval";
    public static final String  BLOCKPLACE_PROJECTILE_ACTIONS             = BLOCKPLACE_PROJECTILE + "actions";

    private static final String CHAT                                      = CHECKS + "chat.";

    private static final String CHAT_NOPWNAGE                             = CHAT + "nopwnage.";
    public static final String  CHAT_NOPWNAGE_CHECK                       = CHAT_NOPWNAGE + "active";
    public static final String  CHAT_NOPWNAGE_WARNPLAYERS                 = CHAT_NOPWNAGE + "warnplayers";
    public static final String  CHAT_NOPWNAGE_WARNOTHERS                  = CHAT_NOPWNAGE + "warnothers";
    public static final String  CHAT_NOPWNAGE_WARNLEVEL                   = CHAT_NOPWNAGE + "warnlevel";
    public static final String  CHAT_NOPWNAGE_WARNTIMEOUT                 = CHAT_NOPWNAGE + "warntimeout";
    public static final String  CHAT_NOPWNAGE_BANLEVEL                    = CHAT_NOPWNAGE + "banlevel";
    public static final String  CHAT_NOPWNAGE_ACTIONS                     = CHAT_NOPWNAGE + "actions";

    private static final String CHAT_NOPWNAGE_MOVE                        = CHAT_NOPWNAGE + "move.";
    public static final String  CHAT_NOPWNAGE_MOVE_CHECK                  = CHAT_NOPWNAGE_MOVE + "active";
    public static final String  CHAT_NOPWNAGE_MOVE_WEIGHTBONUS            = CHAT_NOPWNAGE_MOVE + "weightbonus";
    public static final String  CHAT_NOPWNAGE_MOVE_WEIGHTMALUS            = CHAT_NOPWNAGE_MOVE + "weightmalus";
    public static final String  CHAT_NOPWNAGE_MOVE_TIMEOUT                = CHAT_NOPWNAGE_MOVE + "timeout";

    private static final String CHAT_NOPWNAGE_REPEAT                      = CHAT_NOPWNAGE + "repeat.";
    public static final String  CHAT_NOPWNAGE_REPEAT_CHECK                = CHAT_NOPWNAGE_REPEAT + "active";
    public static final String  CHAT_NOPWNAGE_REPEAT_WEIGHT               = CHAT_NOPWNAGE_REPEAT + "weight";
    public static final String  CHAT_NOPWNAGE_REPEAT_TIMEOUT              = CHAT_NOPWNAGE_REPEAT + "timeout";

    private static final String CHAT_NOPWNAGE_SPEED                       = CHAT_NOPWNAGE + "speed.";
    public static final String  CHAT_NOPWNAGE_SPEED_CHECK                 = CHAT_NOPWNAGE_SPEED + "active";
    public static final String  CHAT_NOPWNAGE_SPEED_WEIGHT                = CHAT_NOPWNAGE_SPEED + "weight";
    public static final String  CHAT_NOPWNAGE_SPEED_TIMEOUT               = CHAT_NOPWNAGE_SPEED + "timeout";

    private static final String CHAT_NOPWNAGE_FIRST                       = CHAT_NOPWNAGE + "first.";
    public static final String  CHAT_NOPWNAGE_FIRST_CHECK                 = CHAT_NOPWNAGE_FIRST + "active";
    public static final String  CHAT_NOPWNAGE_FIRST_WEIGHT                = CHAT_NOPWNAGE_FIRST + "weight";
    public static final String  CHAT_NOPWNAGE_FIRST_TIMEOUT               = CHAT_NOPWNAGE_FIRST + "timeout";

    private static final String CHAT_NOPWNAGE_GLOBAL                      = CHAT_NOPWNAGE + "global.";
    public static final String  CHAT_NOPWNAGE_GLOBAL_CHECK                = CHAT_NOPWNAGE_GLOBAL + "active";
    public static final String  CHAT_NOPWNAGE_GLOBAL_WEIGHT               = CHAT_NOPWNAGE_GLOBAL + "weight";
    public static final String  CHAT_NOPWNAGE_GLOBAL_TIMEOUT              = CHAT_NOPWNAGE_GLOBAL + "timeout";

    private static final String CHAT_NOPWNAGE_BANNED                      = CHAT_NOPWNAGE + "banned.";
    public static final String  CHAT_NOPWNAGE_BANNED_CHECK                = CHAT_NOPWNAGE_BANNED + "active";
    public static final String  CHAT_NOPWNAGE_BANNED_WEIGHT               = CHAT_NOPWNAGE_BANNED + "weight";
    public static final String  CHAT_NOPWNAGE_BANNED_TIMEOUT              = CHAT_NOPWNAGE_BANNED + "timeout";

    private static final String CHAT_NOPWNAGE_RELOG                       = CHAT_NOPWNAGE + "relog.";
    public static final String  CHAT_NOPWNAGE_RELOG_CHECK                 = CHAT_NOPWNAGE_RELOG + "active";
    public static final String  CHAT_NOPWNAGE_RELOG_TIME                  = CHAT_NOPWNAGE_RELOG + "time";
    public static final String  CHAT_NOPWNAGE_RELOG_WARNINGS              = CHAT_NOPWNAGE_RELOG + "warnings";
    public static final String  CHAT_NOPWNAGE_RELOG_TIMEOUT               = CHAT_NOPWNAGE_RELOG + "timeout";

    private static final String CHAT_NOPWNAGE_CAPTCHA                     = CHAT_NOPWNAGE + "captcha.";
    public static final String  CHAT_NOPWNAGE_CAPTCHA_CHECK               = CHAT_NOPWNAGE_CAPTCHA + "active";
    public static final String  CHAT_NOPWNAGE_CAPTCHA_TRIES               = CHAT_NOPWNAGE_CAPTCHA + "tries";
    public static final String  CHAT_NOPWNAGE_CAPTCHA_LENGTH              = CHAT_NOPWNAGE_CAPTCHA + "length";
    public static final String  CHAT_NOPWNAGE_CAPTCHA_CHARACTERS          = CHAT_NOPWNAGE_CAPTCHA + "characters";

    private static final String CHAT_ARRIVALSLIMIT                        = CHAT + "arrivalslimit.";
    public static final String  CHAT_ARRIVALSLIMIT_CHECK                  = CHAT_ARRIVALSLIMIT + "active";
    public static final String  CHAT_ARRIVALSLIMIT_PLAYERSLIMIT           = CHAT_ARRIVALSLIMIT + "playerslimit";
    public static final String  CHAT_ARRIVALSLIMIT_TIMEFRAME              = CHAT_ARRIVALSLIMIT + "timeframe";
    public static final String  CHAT_ARRIVALSLIMIT_COOLDOWNDELAY          = CHAT_ARRIVALSLIMIT + "cooldowndelay";
    public static final String  CHAT_ARRIVALSLIMIT_KICKMESSAGE            = CHAT_ARRIVALSLIMIT + "kickmessage";
    public static final String  CHAT_ARRIVALSLIMIT_NEWTIME                = CHAT_ARRIVALSLIMIT + "newtime";
    public static final String  CHAT_ARRIVALSLIMIT_ACTIONS                = CHAT_ARRIVALSLIMIT + "actions";

    private static final String CHAT_COLOR                                = CHAT + "color.";
    public static final String  CHAT_COLOR_CHECK                          = CHAT_COLOR + "active";
    public static final String  CHAT_COLOR_ACTIONS                        = CHAT_COLOR + "actions";

    private static final String FIGHT                                     = CHECKS + "fight.";

    private static final String FIGHT_DIRECTION                           = FIGHT + "direction.";
    public static final String  FIGHT_DIRECTION_CHECK                     = FIGHT_DIRECTION + "active";
    public static final String  FIGHT_DIRECTION_PRECISION                 = FIGHT_DIRECTION + "precision";
    public static final String  FIGHT_DIRECTION_PENALTYTIME               = FIGHT_DIRECTION + "penaltytime";
    public static final String  FIGHT_DIRECTION_ACTIONS                   = FIGHT_DIRECTION + "actions";

    private static final String FIGHT_NOSWING                             = FIGHT + "noswing.";
    public static final String  FIGHT_NOSWING_CHECK                       = FIGHT_NOSWING + "active";
    public static final String  FIGHT_NOSWING_ACTIONS                     = FIGHT_NOSWING + "actions";

    private static final String FIGHT_REACH                               = FIGHT + "reach.";
    public static final String  FIGHT_REACH_CHECK                         = FIGHT_REACH + "active";
    public static final String  FIGHT_REACH_LIMIT                         = FIGHT_REACH + "distance";
    public static final String  FIGHT_REACH_PENALTYTIME                   = FIGHT_REACH + "penaltytime";
    public static final String  FIGHT_REACH_ACTIONS                       = FIGHT_REACH + "actions";

    private static final String FIGHT_SPEED                               = FIGHT + "speed.";
    public static final String  FIGHT_SPEED_CHECK                         = FIGHT_SPEED + "active";
    public static final String  FIGHT_SPEED_ATTACKLIMIT                   = FIGHT_SPEED + "attacklimit";
    public static final String  FIGHT_SPEED_ACTIONS                       = FIGHT_SPEED + "actions";

    private static final String FIGHT_GODMODE                             = FIGHT + "godmode.";
    public static final String  FIGHT_GODMODE_CHECK                       = FIGHT_GODMODE + "active";
    public static final String  FIGHT_GODMODE_ACTIONS                     = FIGHT_GODMODE + "actions";

    private static final String FIGHT_INSTANTHEAL                         = FIGHT + "instantheal.";
    public static final String  FIGHT_INSTANTHEAL_CHECK                   = FIGHT_INSTANTHEAL + "active";
    public static final String  FIGHT_INSTANTHEAL_ACTIONS                 = FIGHT_INSTANTHEAL + "actions";

    private static final String FIGHT_KNOCKBACK                           = FIGHT + "knockback.";
    public static final String  FIGHT_KNOCKBACK_CHECK                     = FIGHT_KNOCKBACK + "active";
    public static final String  FIGHT_KNOCKBACK_INTERVAL                  = FIGHT_KNOCKBACK + "interval";
    public static final String  FIGHT_KNOCKBACK_ACTIONS                   = FIGHT_KNOCKBACK + "actions";

    public static final String  STRINGS                                   = "strings";

}
