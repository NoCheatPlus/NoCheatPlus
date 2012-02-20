package cc.co.evenprime.bukkit.nocheat.config;

/**
 * The various permission nodes used by NoCheat
 * 
 */
public class Permissions {

    private final static String NOCHEAT              = "nocheat";
    private final static String ADMIN                = NOCHEAT + ".admin";
    private final static String CHECKS               = NOCHEAT + ".checks";

    public final static String  MOVING               = CHECKS + ".moving";
    public final static String  MOVING_RUNFLY        = MOVING + ".runfly";
    public static final String  MOVING_SWIMMING      = MOVING + ".swimming";
    public final static String  MOVING_SNEAKING      = MOVING + ".sneaking";
    public final static String  MOVING_FLYING        = MOVING + ".flying";
    public final static String  MOVING_NOFALL        = MOVING + ".nofall";
    public final static String  MOVING_MOREPACKETS   = MOVING + ".morepackets";

    public final static String  BLOCKBREAK           = CHECKS + ".blockbreak";
    public final static String  BLOCKBREAK_REACH     = BLOCKBREAK + ".reach";
    public final static String  BLOCKBREAK_DIRECTION = BLOCKBREAK + ".direction";
    public static final String  BLOCKBREAK_NOSWING   = BLOCKBREAK + ".noswing";

    public final static String  BLOCKPLACE           = CHECKS + ".blockplace";
    public final static String  BLOCKPLACE_REACH     = BLOCKPLACE + ".reach";
    public static final String  BLOCKPLACE_DIRECTION = BLOCKPLACE + ".direction";

    public final static String  CHAT                 = CHECKS + ".chat";
    public final static String  CHAT_SPAM            = CHAT + ".spam";
    public static final String  CHAT_COLOR           = CHAT + ".color";

    public static final String  FIGHT                = CHECKS + ".fight";
    public static final String  FIGHT_DIRECTION      = FIGHT + ".direction";
    public static final String  FIGHT_NOSWING        = FIGHT + ".noswing";
    public static final String  FIGHT_REACH          = FIGHT + ".reach";
    public static final String  FIGHT_SPEED          = FIGHT + ".speed";
    public static final String  FIGHT_GODMODE        = FIGHT + ".godmode";

    public final static String  ADMIN_CHATLOG        = ADMIN + ".chatlog";
    public static final String  ADMIN_COMMANDS       = ADMIN + ".commands";
    public static final String  ADMIN_RELOAD         = ADMIN + ".reload";

    public static final String  INVENTORY            = CHECKS + ".inventory";
    public static final String  INVENTORY_DROP       = INVENTORY + ".drop";
    public static final String  INVENTORY_INSTANTBOW = INVENTORY + ".instantbow";
    public static final String  INVENTORY_INSTANTEAT = INVENTORY + ".instanteat";

    private Permissions() {}
}
