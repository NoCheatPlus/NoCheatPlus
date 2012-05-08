package fr.neatmonster.nocheatplus.players.informations;

/**
 * The various permission nodes used by NoCheatPlus
 * 
 */
public class Permissions {

    private static final String NOCHEATPLUS               = "nocheatplus";
    private static final String ADMIN                     = NOCHEATPLUS + ".admin";
    private static final String CHECKS                    = NOCHEATPLUS + ".checks";
    private static final String MODS                      = NOCHEATPLUS + ".mods";

    /** ADMIN **/
    public static final String  ADMIN_CHATLOG             = ADMIN + ".chatlog";
    public static final String  ADMIN_COMMANDS            = ADMIN + ".commands";
    public static final String  ADMIN_RELOAD              = ADMIN + ".reload";
    public static final String  ADMIN_PLUGINS             = ADMIN + ".plugins";

    /** CHECKS **/
    private static final String BLOCKBREAK                = CHECKS + ".blockbreak";
    public static final String  BLOCKBREAK_FASTBREAK      = BLOCKBREAK + ".fastbreak";
    public static final String  BLOCKBREAK_REACH          = BLOCKBREAK + ".reach";
    public static final String  BLOCKBREAK_DIRECTION      = BLOCKBREAK + ".direction";
    public static final String  BLOCKBREAK_NOSWING        = BLOCKBREAK + ".noswing";

    private static final String BLOCKPLACE                = CHECKS + ".blockplace";
    public static final String  BLOCKPLACE_FASTPLACE      = BLOCKPLACE + ".fastplace";
    public static final String  BLOCKPLACE_REACH          = BLOCKPLACE + ".reach";
    public static final String  BLOCKPLACE_DIRECTION      = BLOCKPLACE + ".direction";
    public static final String  BLOCKPLACE_PROJECTILE     = BLOCKPLACE + ".projectile";
    public static final String  BLOCKPLACE_AUTOSIGN       = BLOCKPLACE + ".autosign";

    private static final String CHAT                      = CHECKS + ".chat";
    public static final String  CHAT_NOPWNAGE             = CHAT + ".nopwnage";
    public static final String  CHAT_ARRIVALSLIMIT        = CHAT + ".arrivalslimit";
    public static final String  CHAT_COLOR                = CHAT + ".color";

    private static final String FIGHT                     = CHECKS + ".fight";
    public static final String  FIGHT_DIRECTION           = FIGHT + ".direction";
    public static final String  FIGHT_NOSWING             = FIGHT + ".noswing";
    public static final String  FIGHT_REACH               = FIGHT + ".reach";
    public static final String  FIGHT_SPEED               = FIGHT + ".speed";
    public static final String  FIGHT_GODMODE             = FIGHT + ".godmode";
    public static final String  FIGHT_INSTANTHEAL         = FIGHT + ".instantheal";
    public static final String  FIGHT_KNOCKBACK           = FIGHT + ".knockback";
    public static final String  FIGHT_CRITICAL            = FIGHT + ".critical";

    private static final String INVENTORY                 = CHECKS + ".inventory";
    public static final String  INVENTORY_DROP            = INVENTORY + ".drop";
    public static final String  INVENTORY_INSTANTBOW      = INVENTORY + ".instantbow";
    public static final String  INVENTORY_INSTANTEAT      = INVENTORY + ".instanteat";

    private static final String MOVING                    = CHECKS + ".moving";
    public static final String  MOVING_RUNFLY             = MOVING + ".runfly";
    public static final String  MOVING_SWIMMING           = MOVING + ".swimming";
    public static final String  MOVING_SNEAKING           = MOVING + ".sneaking";
    public static final String  MOVING_BLOCKING           = MOVING + ".blocking";
    public static final String  MOVING_FLYING             = MOVING + ".flying";
    public static final String  MOVING_COBWEB             = MOVING + ".cobweb";
    public static final String  MOVING_NOFALL             = MOVING + ".nofall";
    public static final String  MOVING_MOREPACKETS        = MOVING + ".morepackets";
    public static final String  MOVING_MOREPACKETSVEHICLE = MOVING + ".morepacketsvehicle";
    public static final String  MOVING_WATERWALK          = MOVING + ".waterwalk";
    public static final String  MOVING_RESPAWNTRICK       = MOVING + ".respawntrick";
    public static final String  MOVING_BOATONGROUND       = MOVING + ".boatonground";

    /** MODS **/
    private static final String CJB                       = MODS + ".cjb";
    public static final String  CJB_FLY                   = CJB + ".fly";
    public static final String  CJB_XRAY                  = CJB + ".xray";
    public static final String  CJB_RADAR                 = CJB + ".radar";

    private static final String MINECRAFTAUTOMAP          = MODS + ".minecraftautomap";
    public static final String  MINECRAFTAUTOMAP_ORES     = MINECRAFTAUTOMAP + ".ores";
    public static final String  MINECRAFTAUTOMAP_CAVE     = MINECRAFTAUTOMAP + ".cave";
    public static final String  MINECRAFTAUTOMAP_RADAR    = MINECRAFTAUTOMAP + ".radar";

    private static final String REI                       = MODS + ".rei";
    public static final String  REI_CAVE                  = REI + ".cave";
    public static final String  REI_RADAR                 = REI + ".radar";

    private static final String SMARTMOVING               = MODS + ".smartmoving";
    public static final String  SMARTMOVING_CLIMBING      = SMARTMOVING + ".climbing";
    public static final String  SMARTMOVING_SWIMMING      = SMARTMOVING + ".swimming";
    public static final String  SMARTMOVING_CRAWLING      = SMARTMOVING + ".crawling";
    public static final String  SMARTMOVING_SLIDING       = SMARTMOVING + ".sliding";
    public static final String  SMARTMOVING_JUMPING       = SMARTMOVING + ".jumping";
    public static final String  SMARTMOVING_FLYING        = SMARTMOVING + ".flying";

    private static final String ZOMBE                     = MODS + ".zombe";
    public static final String  ZOMBE_FLY                 = ZOMBE + ".fly";
    public static final String  ZOMBE_NOCLIP              = ZOMBE + ".noclip";
    public static final String  ZOMBE_CHEAT               = ZOMBE + ".cheat";
}
