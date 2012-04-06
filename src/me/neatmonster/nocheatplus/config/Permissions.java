package me.neatmonster.nocheatplus.config;

/**
 * The various permission nodes used by NoCheatPlus
 * 
 */
public class Permissions {

    private static final String NOCHEATPLUS          = "nocheatplus";
    private static final String ADMIN                = NOCHEATPLUS + ".admin";
    private static final String CHECKS               = NOCHEATPLUS + ".checks";
    private static final String MODS                 = NOCHEATPLUS + ".mods";

    /** ADMIN **/
    public static final String  ADMIN_CHATLOG        = ADMIN + ".chatlog";
    public static final String  ADMIN_COMMANDS       = ADMIN + ".commands";
    public static final String  ADMIN_RELOAD         = ADMIN + ".reload";

    /** CHECKS **/
    private static final String BLOCKBREAK           = CHECKS + ".blockbreak";
    public static final String  BLOCKBREAK_FASTBREAK = BLOCKBREAK + ".fastbreak";
    public static final String  BLOCKBREAK_REACH     = BLOCKBREAK + ".reach";
    public static final String  BLOCKBREAK_DIRECTION = BLOCKBREAK + ".direction";
    public static final String  BLOCKBREAK_NOSWING   = BLOCKBREAK + ".noswing";

    private static final String BLOCKPLACE           = CHECKS + ".blockplace";
    public static final String  BLOCKPLACE_FASTPLACE = BLOCKPLACE + ".fastplace";
    public static final String  BLOCKPLACE_REACH     = BLOCKPLACE + ".reach";
    public static final String  BLOCKPLACE_DIRECTION = BLOCKPLACE + ".direction";

    private static final String CHAT                 = CHECKS + ".chat";
    public static final String  CHAT_SPAM            = CHAT + ".spam";
    public static final String  CHAT_COLOR           = CHAT + ".color";

    private static final String FIGHT                = CHECKS + ".fight";
    public static final String  FIGHT_DIRECTION      = FIGHT + ".direction";
    public static final String  FIGHT_NOSWING        = FIGHT + ".noswing";
    public static final String  FIGHT_REACH          = FIGHT + ".reach";
    public static final String  FIGHT_SPEED          = FIGHT + ".speed";
    public static final String  FIGHT_GODMODE        = FIGHT + ".godmode";
    public static final String  FIGHT_INSTANTHEAL    = FIGHT + ".instantheal";

    private static final String INVENTORY            = CHECKS + ".inventory";
    public static final String  INVENTORY_DROP       = INVENTORY + ".drop";
    public static final String  INVENTORY_INSTANTBOW = INVENTORY + ".instantbow";
    public static final String  INVENTORY_INSTANTEAT = INVENTORY + ".instanteat";

    private static final String MOVING               = CHECKS + ".moving";
    public static final String  MOVING_RUNFLY        = MOVING + ".runfly";
    public static final String  MOVING_SWIMMING      = MOVING + ".swimming";
    public static final String  MOVING_SNEAKING      = MOVING + ".sneaking";
    public static final String  MOVING_FLYING        = MOVING + ".flying";
    public static final String  MOVING_NOFALL        = MOVING + ".nofall";
    public static final String  MOVING_MOREPACKETS   = MOVING + ".morepackets";
    public static final String  MOVING_WATERWALK     = MOVING + ".waterwalk";

    /** MODS **/
    private static final String CJB                  = MODS + ".cjb";
    public static final String  CJB_FLY              = CJB + ".fly";
    public static final String  CJB_XRAY             = CJB + ".xray";
    public static final String  CJB_MINIMAP          = CJB + ".minimap";

    public static final String  MC_AUTOMAP           = MODS + ".mcautomap";

    private static final String REI                  = MODS + ".rei";
    public static final String  REI_CAVE             = REI + ".cave";
    public static final String  REI_RADAR            = REI + ".radar";

    private static final String ZOMBE                = MODS + ".zombe";
    public static final String  ZOMBE_FLY            = ZOMBE + ".fly";
    public static final String  ZOMBE_NOCLIP         = ZOMBE + ".noclip";
    public static final String  ZOMBE_CHEATS         = ZOMBE + ".cheats";

    private Permissions() {}
}
