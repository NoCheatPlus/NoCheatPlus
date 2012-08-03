package fr.neatmonster.nocheatplus.players;

/*
 * MM"""""""`YM                              oo                   oo                            
 * MM  mmmmm  M                                                                                 
 * M'        .M .d8888b. 88d888b. 88d8b.d8b. dP .d8888b. .d8888b. dP .d8888b. 88d888b. .d8888b. 
 * MM  MMMMMMMM 88ooood8 88'  `88 88'`88'`88 88 Y8ooooo. Y8ooooo. 88 88'  `88 88'  `88 Y8ooooo. 
 * MM  MMMMMMMM 88.  ... 88       88  88  88 88       88       88 88 88.  .88 88    88       88 
 * MM  MMMMMMMM `88888P' dP       dP  dP  dP dP `88888P' `88888P' dP `88888P' dP    dP `88888P' 
 * MMMMMMMMMMMM                                                                                 
 */
/**
 * The various permission nodes used by NoCheatPlus.
 */
public class Permissions {
    private static final String NOCHEATPLUS               = "nocheatplus";

    /*
     *     e Y8b          888             ,e,         ,e,         d8                    d8   ,e,                   
     *    d8b Y8b     e88 888 888 888 8e   "  888 8e   "   dP"Y  d88   888,8,  ,"Y88b  d88    "   e88 88e  888 8e  
     *   d888b Y8b   d888 888 888 888 88b 888 888 88b 888 C88b  d88888 888 "  "8" 888 d88888 888 d888 888b 888 88b 
     *  d888888888b  Y888 888 888 888 888 888 888 888 888  Y88D  888   888    ,ee 888  888   888 Y888 888P 888 888 
     * d8888888b Y8b  "88 888 888 888 888 888 888 888 888 d,dP   888   888    "88 888  888   888  "88 88"  888 888 
     */
    private static final String ADMINISTRATION            = NOCHEATPLUS + ".admin";
    public static final String  ADMINISTRATION_NOTIFY     = ADMINISTRATION + ".notify";
    public static final String  ADMINISTRATION_RELOAD     = ADMINISTRATION + ".reload";

    /*
     *     e   e                    888 ,e,  dP,e, ,e,                    d8   ,e,                         
     *    d8b d8b     e88 88e   e88 888  "   8b "   "   e88'888  ,"Y88b  d88    "   e88 88e  888 8e   dP"Y 
     *   e Y8b Y8b   d888 888b d888 888 888 888888 888 d888  '8 "8" 888 d88888 888 d888 888b 888 88b C88b  
     *  d8b Y8b Y8b  Y888 888P Y888 888 888  888   888 Y888   , ,ee 888  888   888 Y888 888P 888 888  Y88D 
     * d888b Y8b Y8b  "88 88"   "88 888 888  888   888  "88,e8' "88 888  888   888  "88 88"  888 888 d,dP  
     */
    private static final String MODS                      = NOCHEATPLUS + ".mods";

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

    private static final String CHECKS                    = NOCHEATPLUS + ".checks";

    /*
     * 888 88b, 888                    888    888 88b,                        888    
     * 888 88P' 888  e88 88e   e88'888 888 ee 888 88P' 888,8,  ,e e,   ,"Y88b 888 ee 
     * 888 8K   888 d888 888b d888  '8 888 P  888 8K   888 "  d88 88b "8" 888 888 P  
     * 888 88b, 888 Y888 888P Y888   , 888 b  888 88b, 888    888   , ,ee 888 888 b  
     * 888 88P' 888  "88 88"   "88,e8' 888 8b 888 88P' 888     "YeeP" "88 888 888 8b 
     */
    private static final String BLOCKBREAK                = CHECKS + ".blockbreak";
    public static final String  BLOCKBREAK_DIRECTION      = BLOCKBREAK + ".direction";
    public static final String  BLOCKBREAK_FASTBREAK      = BLOCKBREAK + ".fastbreak";
    public static final String  BLOCKBREAK_NOSWING        = BLOCKBREAK + ".noswing";
    public static final String  BLOCKBREAK_REACH          = BLOCKBREAK + ".reach";

    /*
     * 888 88b, 888                    888    888 88e  888                          
     * 888 88P' 888  e88 88e   e88'888 888 ee 888 888D 888  ,"Y88b  e88'888  ,e e,  
     * 888 8K   888 d888 888b d888  '8 888 P  888 88"  888 "8" 888 d888  '8 d88 88b 
     * 888 88b, 888 Y888 888P Y888   , 888 b  888      888 ,ee 888 Y888   , 888   , 
     * 888 88P' 888  "88 88"   "88,e8' 888 8b 888      888 "88 888  "88,e8'  "YeeP" 
     */
    private static final String BLOCKPLACE                = CHECKS + ".blockplace";
    public static final String  BLOCKPLACE_DIRECTION      = BLOCKPLACE + ".direction";
    public static final String  BLOCKPLACE_FASTPLACE      = BLOCKPLACE + "fastplace";
    public static final String  BLOCKPLACE_REACH          = BLOCKPLACE + ".reach";
    public static final String  BLOCKPLACE_SPEED          = BLOCKPLACE + ".speed";

    /*
     *   e88'Y88 888               d8   
     *  d888  'Y 888 ee   ,"Y88b  d88   
     * C8888     888 88b "8" 888 d88888 
     *  Y888  ,d 888 888 ,ee 888  888   
     *   "88,d88 888 888 "88 888  888   
     */
    private static final String CHAT                      = CHECKS + ".chat";
    public static final String  CHAT_ARRIVALS             = CHAT + ".arrivals";
    public static final String  CHAT_COLOR                = CHAT + ".color";
    public static final String  CHAT_NOPWNAGE             = CHAT + ".nopwnage";

    /*
     *     e   e                         ,e,                  
     *    d8b d8b     e88 88e  Y8b Y888P  "  888 8e   e88 888 
     *   e Y8b Y8b   d888 888b  Y8b Y8P  888 888 88b d888 888 
     *  d8b Y8b Y8b  Y888 888P   Y8b "   888 888 888 Y888 888 
     * d888b Y8b Y8b  "88 88"     Y8P    888 888 888  "88 888 
     *                                                 ,  88P 
     *                                                "8",P"  
     */
    private static final String MOVING                    = CHECKS + ".moving";
    public static final String  MOVING_BOATSANYWHERE      = MOVING + ".boatsanywhere";
    public static final String  MOVING_CREATIVEFLY        = MOVING + ".creativefly";
    public static final String  MOVING_MOREPACKETS        = MOVING + ".morepackets";
    public static final String  MOVING_MOREPACKETSVEHICLE = MOVING + ".morepacketsvehicle";
    public static final String  MOVING_NOFALL             = MOVING + ".nofall";
    public static final String  MOVING_SURVIVALFLY        = MOVING + ".survivalfly";
}
