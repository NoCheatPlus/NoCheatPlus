package cc.co.evenprime.bukkit.nocheat.config;

/**
 * The various permission nodes used by NoCheat
 * 
 */
public class Permissions {

    private final static String NOCHEAT              = "nocheat";
    private final static String ADMIN                = NOCHEAT + ".admin";
    private final static String CHECKS               = NOCHEAT + ".checks";

    public final static String  MOVE                 = CHECKS + ".moving";
    public final static String  MOVE_RUNFLY          = MOVE + ".runfly";
    public final static String  MOVE_SNEAK           = MOVE + ".sneaking";
    public final static String  MOVE_SWIM            = MOVE + ".swimming";
    public final static String  MOVE_FLY             = MOVE + ".flying";
    public final static String  MOVE_NOFALL          = MOVE + ".nofall";
    public final static String  MOVE_MOREPACKETS     = MOVE + ".morepackets";

    public final static String  BLOCKBREAK           = CHECKS + ".blockbreak";
    public final static String  BLOCKBREAK_REACH     = BLOCKBREAK + ".reach";
    public final static String  BLOCKBREAK_DIRECTION = BLOCKBREAK + ".direction";
    public static final String  BLOCKBREAK_NOSWING   = BLOCKBREAK + ".noswing";

    public final static String  BLOCKPLACE           = CHECKS + ".blockplace";
    public final static String  BLOCKPLACE_ONLIQUID  = BLOCKPLACE + ".onliquid";
    public final static String  BLOCKPLACE_REACH     = BLOCKPLACE + ".reach";
    public static final String  BLOCKPLACE_NOSWING   = BLOCKPLACE + ".noswing";

    public final static String  CHAT                 = CHECKS + ".chat";
    public final static String  CHAT_SPAM            = CHAT + ".spam";

    public static final String  FIGHT                = CHECKS + ".fight";
    public static final String  FIGHT_DIRECTION      = FIGHT + ".direction";
    public static final String  FIGHT_SELFHIT        = FIGHT + ".selfhit";
    public static final String  FIGHT_NOSWING        = FIGHT + ".noswing";

    public static final String  TIMED                = CHECKS + ".timed";
    public static final String  TIMED_GODMODE        = TIMED + ".godmode";

    public final static String  ADMIN_CHATLOG        = ADMIN + ".chatlog";
    public static final String  ADMIN_PERMLIST       = ADMIN + ".permlist";
    public static final String  ADMIN_RELOAD         = ADMIN + ".reload";
    public static final String  ADMIN_PERFORMANCE    = ADMIN + ".performance";

    private Permissions() {}
}
