package cc.co.evenprime.bukkit.nocheat;

/**
 * The various permission nodes used by NoCheat
 * 
 * @author Evenprime
 * 
 */
public class Permissions {

    private final static String _NOCHEAT             = "nocheat";
    private final static String _CHECKS              = _NOCHEAT + ".checks";
    private final static String _MOVE                = _CHECKS + ".moving";
    private final static String _BLOCKBREAK          = _CHECKS + ".blockbreak";
    public static final String  _BLOCKPLACE          = _CHECKS + ".blockplace";
    public final static String  _INTERACT            = _CHECKS + ".interact";

    public final static String  MOVE                 = _CHECKS + ".moving.*";
    public final static String  MOVE_FLY             = _MOVE + ".flying";
    public final static String  MOVE_RUN             = _MOVE + ".running";
    public final static String  MOVE_SNEAK           = _MOVE + ".sneaking";
    public final static String  MOVE_SWIM            = _MOVE + ".swimming";
    public final static String  MOVE_NOCLIP          = _MOVE + ".noclip";
    public final static String  MOVE_MOREPACKETS     = _MOVE + ".morepackets";

    public final static String  BLOCKBREAK           = _CHECKS + ".blockbreak.*";
    public final static String  BLOCKBREAK_REACH     = _BLOCKBREAK + ".reach";
    public final static String  BLOCKBREAK_DIRECTION = _BLOCKBREAK + ".direction";

    public final static String  INTERACT             = _CHECKS + ".interact.*";
    public final static String  INTERACT_DURABILITY  = _INTERACT + ".durability";

    public final static String  BLOCKPLACE           = _CHECKS + ".blockplace.*";
    public final static String  BLOCKPLACE_ONLIQUID  = _BLOCKPLACE + ".onliquid";
    public static final String  BLOCKPLACE_REACH     = _BLOCKPLACE + ".reach";

    private final static String _ADMIN               = _NOCHEAT + ".admin";

    public final static String  ADMIN_CHATLOG        = _ADMIN + ".chatlog";

    private Permissions() {}
}
