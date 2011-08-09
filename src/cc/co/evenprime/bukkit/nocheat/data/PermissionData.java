package cc.co.evenprime.bukkit.nocheat.data;

public class PermissionData {

    public final long            lastUpdate[]           = new long[11];
    public final boolean         cache[]                = new boolean[11];

    public static final String[] permissionNames        = new String[11];

    public static final int      PERMISSION_MOVING      = 0;
    public static final int      PERMISSION_FLYING      = 1;
    public static final int      PERMISSION_SPEEDHACK   = 2;
    public static final int      PERMISSION_AIRBUILD    = 3;
    public static final int      PERMISSION_BEDTELEPORT = 4;              // unused
    public static final int      PERMISSION_BOGUSITEMS  = 5;
    public static final int      PERMISSION_NOTIFY      = 6;
    public static final int      PERMISSION_ITEMDUPE    = 7;              // unused
    public static final int      PERMISSION_FAKESNEAK   = 8;
    public static final int      PERMISSION_FASTSWIM    = 9;
    public static final int      PERMISSION_NUKE        = 10;

    static {
        permissionNames[PERMISSION_AIRBUILD] = "nocheat.airbuild";
        permissionNames[PERMISSION_BEDTELEPORT] = "nocheat.bedteleport";
        permissionNames[PERMISSION_FLYING] = "nocheat.flying";
        permissionNames[PERMISSION_MOVING] = "nocheat.moving";
        permissionNames[PERMISSION_BOGUSITEMS] = "nocheat.bogusitems";
        permissionNames[PERMISSION_SPEEDHACK] = "nocheat.speedhack";
        permissionNames[PERMISSION_NOTIFY] = "nocheat.notify";
        permissionNames[PERMISSION_ITEMDUPE] = "nocheat.itemdupe";
        permissionNames[PERMISSION_FAKESNEAK] = "nocheat.fakesneak";
        permissionNames[PERMISSION_FASTSWIM] = "nocheat.fastswim";
        permissionNames[PERMISSION_NUKE] = "nocheat.nuke";
    }
}
