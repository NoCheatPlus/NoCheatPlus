package fr.neatmonster.nocheatplus.permissions;

/**
 * The various permission nodes used by NoCheatPlus.
 */
public class Permissions {
    private static final String NOCHEATPLUS                  = "nocheatplus";

    // Access to all commands and debug info.
    private static final String ADMINISTRATION               = NOCHEATPLUS + ".admin";
    public static final String  ADMINISTRATION_DEBUG         = ADMINISTRATION + ".debug";

    // Bypasses held extra from command permissions.
    private final static String BYPASS                       = NOCHEATPLUS + ".bypass";
    public static final  String BYPASS_DENY_LOGIN            = BYPASS + "denylogin";

    // Bypass command filter.
    private static final String FILTER                       = NOCHEATPLUS + ".filter";
    public static final String  FILTER_COMMAND               = FILTER + ".command";
    public static final String  FILTER_COMMAND_NOCHEATPLUS   = FILTER_COMMAND + ".nocheatplus";

    // Notifications (in-game).
    public static final String  NOTIFY                       = NOCHEATPLUS + ".notify";

    // Command permissions.
    public static final String  COMMAND                      = NOCHEATPLUS + ".command";
    public static final String  COMMAND_COMMANDS             = COMMAND + ".commands";
    public static final String  COMMAND_EXEMPT               = COMMAND + ".exempt";
    public static final String  COMMAND_EXEMPTIONS           = COMMAND + ".exemptions";
    public static final String  COMMAND_INFO                 = COMMAND + ".info";
    public static final String  COMMAND_INSPECT              = COMMAND + ".inspect";
    public static final String  COMMAND_LAG                  = COMMAND + ".lag";
    public static final String  COMMAND_LOG                  = COMMAND + ".log";
    public static final String  COMMAND_NOTIFY               = COMMAND + ".notify";
    public static final String  COMMAND_RELOAD               = COMMAND + ".reload";
    public static final String  COMMAND_REMOVEPLAYER         = COMMAND + ".removeplayer";
    public static final String  COMMAND_RESET                = COMMAND + ".reset";
    public static final String  COMMAND_UNEXEMPT             = COMMAND + ".unexempt";
    public static final String  COMMAND_VERSION              = COMMAND + ".version";

    // Auxiliary Command permissions.
    public static final String  COMMAND_ALLOWLOGIN           = COMMAND + ".allowlogin";
    public static final String  COMMAND_BAN                  = COMMAND + ".ban";
    public static final String  COMMAND_DELAY                = COMMAND + ".delay";
    public static final String  COMMAND_DENYLOGIN            = COMMAND + ".denylogin";
    public static final String  COMMAND_KICK                 = COMMAND + ".kick";
    public static final String  COMMAND_KICKLIST             = COMMAND + ".kicklist";
    public static final String  COMMAND_TELL                 = COMMAND + ".tell";

    // Permissions for the individual checks.
    public static final String  CHECKS                       = NOCHEATPLUS + ".checks";

    public static final String  BLOCKBREAK                   = CHECKS + ".blockbreak";
    public static final String  BLOCKBREAK_BREAK             = BLOCKBREAK + ".break";
    public static final String  BLOCKBREAK_BREAK_LIQUID      = BLOCKBREAK_BREAK + ".liquid";
    public static final String  BLOCKBREAK_DIRECTION         = BLOCKBREAK + ".direction";
    public static final String  BLOCKBREAK_FASTBREAK         = BLOCKBREAK + ".fastbreak";
    public static final String  BLOCKBREAK_FREQUENCY         = BLOCKBREAK + ".frequency";
    public static final String  BLOCKBREAK_NOSWING           = BLOCKBREAK + ".noswing";
    public static final String  BLOCKBREAK_REACH             = BLOCKBREAK + ".reach";
    public static final String  BLOCKBREAK_WRONGBLOCK        = BLOCKBREAK + ".wrongblock";

    public static final String  BLOCKINTERACT                = CHECKS + ".blockinteract";
    public static final String  BLOCKINTERACT_DIRECTION      = BLOCKINTERACT + ".direction";
    public static final String  BLOCKINTERACT_REACH          = BLOCKINTERACT + ".reach";
    public static final String  BLOCKINTERACT_SPEED          = BLOCKINTERACT + ".speed";
    public static final String  BLOCKINTERACT_VISIBLE        = BLOCKINTERACT + ".visible";

    public static final String  BLOCKPLACE                   = CHECKS + ".blockplace";
    public static final String  BLOCKPLACE_AGAINST           = BLOCKPLACE + ".against";
    public static final String  BLOCKPLACE_AGAINST_AIR       = BLOCKPLACE_AGAINST + ".air";
    public static final String  BLOCKPLACE_AGAINST_LIQUIDS   = BLOCKPLACE_AGAINST + ".liquids";
    public static final String  BLOCKPLACE_AUTOSIGN          = BLOCKPLACE + ".autosign";
    public static final String  BLOCKPLACE_BOATSANYWHERE     = BLOCKPLACE + ".boatsanywhere";
    public static final String  BLOCKPLACE_DIRECTION         = BLOCKPLACE + ".direction";
    public static final String  BLOCKPLACE_FASTPLACE         = BLOCKPLACE + ".fastplace";
    public static final String  BLOCKPLACE_NOSWING           = BLOCKPLACE + ".noswing";
    public static final String  BLOCKPLACE_REACH             = BLOCKPLACE + ".reach";
    public static final String  BLOCKPLACE_SPEED             = BLOCKPLACE + ".speed";

    public static final String  CHAT                         = CHECKS + ".chat";
    public static final String  CHAT_CAPTCHA                 = CHAT + ".captcha";
    public static final String  CHAT_COLOR                   = CHAT + ".color";
    public static final String  CHAT_COMMANDS                = CHAT + ".commands";
    public static final String  CHAT_LOGINS                  = CHAT + ".logins";
    public static final String  CHAT_RELOG                   = CHAT + ".relog";
    public static final String  CHAT_TEXT                    = CHAT + ".text";

    public static final String  COMBINED                     = CHECKS + ".combined";
    public static final String  COMBINED_BEDLEAVE            = COMBINED + ".bedleave";
    public static final String  COMBINED_IMPROBABLE          = COMBINED + ".improbable";
    public static final String  COMBINED_MUNCHHAUSEN         = COMBINED + ".munchhausen";

    public static final String  FIGHT                        = CHECKS + ".fight";
    public static final String  FIGHT_ANGLE                  = FIGHT + ".angle";
    public static final String  FIGHT_CRITICAL               = FIGHT + ".critical";
    public static final String  FIGHT_DIRECTION              = FIGHT + ".direction";
    public static final String  FIGHT_FASTHEAL               = FIGHT + ".fastheal";
    public static final String  FIGHT_GODMODE                = FIGHT + ".godmode";
    public static final String  FIGHT_KNOCKBACK              = FIGHT + ".knockback";
    public static final String  FIGHT_NOSWING                = FIGHT + ".noswing";
    public static final String  FIGHT_REACH                  = FIGHT + ".reach";
    public static final String  FIGHT_SELFHIT                = FIGHT + ".selfhit";
    public static final String  FIGHT_SPEED                  = FIGHT + ".speed";

    public static final String  INVENTORY                    = CHECKS + ".inventory";
    public static final String  INVENTORY_DROP               = INVENTORY + ".drop";
    public static final String  INVENTORY_FASTCLICK          = INVENTORY + ".fastclick";
    public static final String  INVENTORY_FASTCONSUME        = INVENTORY + ".fastconsume";
    public static final String  INVENTORY_INSTANTBOW         = INVENTORY + ".instantbow";
    public static final String  INVENTORY_INSTANTEAT         = INVENTORY + ".instanteat";
    public static final String  INVENTORY_ITEMS              = INVENTORY + ".items";
    public static final String  INVENTORY_OPEN               = INVENTORY + ".open";

    public static final String  MOVING                       = CHECKS + ".moving";
    public static final String  MOVING_CREATIVEFLY           = MOVING + ".creativefly";
    public static final String  MOVING_MOREPACKETS           = MOVING + ".morepackets";
    public static final String  MOVING_MOREPACKETSVEHICLE    = MOVING + ".morepacketsvehicle";
    public static final String  MOVING_NOFALL                = MOVING + ".nofall";
    public static final String  MOVING_PASSABLE              = MOVING + ".passable";
    public static final String  MOVING_SURVIVALFLY           = MOVING + ".survivalfly";
    public static final String  MOVING_SURVIVALFLY_BLOCKING  = MOVING_SURVIVALFLY + ".blocking";
    public static final String  MOVING_SURVIVALFLY_SNEAKING  = MOVING_SURVIVALFLY + ".sneaking";
    public static final String  MOVING_SURVIVALFLY_SPEEDING  = MOVING_SURVIVALFLY + ".speeding";
    public static final String  MOVING_SURVIVALFLY_SPRINTING = MOVING_SURVIVALFLY + ".sprinting";
    public static final String  MOVING_SURVIVALFLY_STEP      = MOVING_SURVIVALFLY + ".step";

    // Permissions for the individual client mods.
    private static final String MODS                         = NOCHEATPLUS + ".mods";

    private static final String CJB                          = MODS + ".cjb";
    public static final String  CJB_FLY                      = CJB + ".fly";
    public static final String  CJB_RADAR                    = CJB + ".radar";
    public static final String  CJB_XRAY                     = CJB + ".xray";

    private static final String MINECRAFTAUTOMAP             = MODS + ".minecraftautomap";
    public static final String  MINECRAFTAUTOMAP_CAVE        = MINECRAFTAUTOMAP + ".cave";
    public static final String  MINECRAFTAUTOMAP_ORES        = MINECRAFTAUTOMAP + ".ores";
    public static final String  MINECRAFTAUTOMAP_RADAR       = MINECRAFTAUTOMAP + ".radar";

    private static final String REI                          = MODS + ".rei";
    public static final String  REI_CAVE                     = REI + ".cave";
    public static final String  REI_RADAR                    = REI + ".radar";
    public static final String  REI_RADAR_ANIMAL             = REI_RADAR + ".animal";
    public static final String  REI_RADAR_PLAYER             = REI_RADAR + ".player";
    public static final String  REI_RADAR_MOB                = REI_RADAR + ".mob";
    public static final String  REI_RADAR_OTHER              = REI_RADAR + ".other";
    public static final String  REI_RADAR_SLIME              = REI_RADAR + ".slime";
    public static final String  REI_RADAR_SQUID              = REI_RADAR + ".squid";

    private static final String SMARTMOVING                  = MODS + ".smartmoving";
    public static final String  SMARTMOVING_CLIMBING         = SMARTMOVING + ".climbing";
    public static final String  SMARTMOVING_CRAWLING         = SMARTMOVING + ".crawling";
    public static final String  SMARTMOVING_FLYING           = SMARTMOVING + ".flying";
    public static final String  SMARTMOVING_JUMPING          = SMARTMOVING + ".jumping";
    public static final String  SMARTMOVING_SLIDING          = SMARTMOVING + ".sliding";
    public static final String  SMARTMOVING_SWIMMING         = SMARTMOVING + ".swimming";

    private static final String ZOMBE                        = MODS + ".zombe";
    public static final String  ZOMBE_CHEAT                  = ZOMBE + ".cheat";
    public static final String  ZOMBE_FLY                    = ZOMBE + ".fly";
    public static final String  ZOMBE_NOCLIP                 = ZOMBE + ".noclip";

}
