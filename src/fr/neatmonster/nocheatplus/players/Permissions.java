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
    private static final String NOCHEATPLUS                  = "nocheatplus";

    /*
     *     e Y8b          888             ,e,         ,e,         d8                    d8   ,e,                   
     *    d8b Y8b     e88 888 888 888 8e   "  888 8e   "   dP"Y  d88   888,8,  ,"Y88b  d88    "   e88 88e  888 8e  
     *   d888b Y8b   d888 888 888 888 88b 888 888 88b 888 C88b  d88888 888 "  "8" 888 d88888 888 d888 888b 888 88b 
     *  d888888888b  Y888 888 888 888 888 888 888 888 888  Y88D  888   888    ,ee 888  888   888 Y888 888P 888 888 
     * d8888888b Y8b  "88 888 888 888 888 888 888 888 888 d,dP   888   888    "88 888  888   888  "88 88"  888 888 
     */
    private static final String ADMINISTRATION               = NOCHEATPLUS + ".admin";

    public static final String  ADMINISTRATION_BAN           = ADMINISTRATION + ".ban";
	public static final String  ADMINISTRATION_DELAY         = ADMINISTRATION + ".delay";
	
	public static final String  ADMINISTRATION_EXEMPT        = ADMINISTRATION + ".exempt";
	public static final String  ADMINISTRATION_UNEXEMPT      = ADMINISTRATION + ".unexempt";
	public static final String  ADMINISTRATION_EXEMPTIONS    = ADMINISTRATION + ".exemptions";
	
    public static final String  ADMINISTRATION_INFO          = ADMINISTRATION + ".info";
    public static final String  ADMINISTRATION_COMMANDS      = ADMINISTRATION + ".commands";
    public static final String  ADMINISTRATION_KICK          = ADMINISTRATION + ".kick";
	public static final String  ADMINISTRATION_KICKLIST      = ADMINISTRATION + ".kicklist";
    public static final String  ADMINISTRATION_NOTIFY        = ADMINISTRATION + ".notify";
    public static final String  ADMINISTRATION_PLUGINS       = ADMINISTRATION + ".plugins";
    public static final String  ADMINISTRATION_RELOAD        = ADMINISTRATION + ".reload";
	public static final String  ADMINISTRATION_REMOVEPLAYER  = ADMINISTRATION + ".removeplayer";
    public static final String  ADMINISTRATION_TELL          = ADMINISTRATION + ".tell";
    public static final String  ADMINISTRATION_TEMPKICK      = ADMINISTRATION + ".tempkick";
	public static final String  ADMINISTRATION_UNKICK        = ADMINISTRATION + ".unkick";
    // Debug permission, for player spam (not in plugin.yml, currently).
	public static final String  ADMINISTRATION_DEBUG          = ADMINISTRATION + ".debug";
    
	
    // Bypasses held extra from command permissions.
    private final static String BYPASS                       = NOCHEATPLUS + ".bypass";
    public static final  String BYPASS_DENY_LOGIN            = BYPASS + "denylogin";

 
    // Permissions for the individual checks.
    public static final String  CHECKS                       = NOCHEATPLUS + ".checks";

    /*
     * 888 88b, 888                    888    888 88b,                        888    
     * 888 88P' 888  e88 88e   e88'888 888 ee 888 88P' 888,8,  ,e e,   ,"Y88b 888 ee 
     * 888 8K   888 d888 888b d888  '8 888 P  888 8K   888 "  d88 88b "8" 888 888 P  
     * 888 88b, 888 Y888 888P Y888   , 888 b  888 88b, 888    888   , ,ee 888 888 b  
     * 888 88P' 888  "88 88"   "88,e8' 888 8b 888 88P' 888     "YeeP" "88 888 888 8b 
     */
    public static final String  BLOCKBREAK                   = CHECKS + ".blockbreak";
    public static final String  BLOCKBREAK_BREAK             = BLOCKBREAK + ".break";
    public static final String  BLOCKBREAK_BREAK_LIQUID      = BLOCKBREAK_BREAK + ".liquid";
    public static final String  BLOCKBREAK_DIRECTION         = BLOCKBREAK + ".direction";
    public static final String  BLOCKBREAK_FASTBREAK         = BLOCKBREAK + ".fastbreak";
	public static final String  BLOCKBREAK_FREQUENCY         = BLOCKBREAK + ".frequency";
    public static final String  BLOCKBREAK_NOSWING           = BLOCKBREAK + ".noswing";
    public static final String  BLOCKBREAK_REACH             = BLOCKBREAK + ".reach";
	public static final String  BLOCKBREAK_WRONGBLOCK        = BLOCKBREAK + ".wrongblock";

    /*
     * 888 88b, 888                    888    888           d8                                     d8   
     * 888 88P' 888  e88 88e   e88'888 888 ee 888 888 8e   d88    ,e e,  888,8,  ,"Y88b  e88'888  d88   
     * 888 8K   888 d888 888b d888  '8 888 P  888 888 88b d88888 d88 88b 888 "  "8" 888 d888  '8 d88888 
     * 888 88b, 888 Y888 888P Y888   , 888 b  888 888 888  888   888   , 888    ,ee 888 Y888   ,  888   
     * 888 88P' 888  "88 88"   "88,e8' 888 8b 888 888 888  888    "YeeP" 888    "88 888  "88,e8'  888   
     */
    public static final String  BLOCKINTERACT                = CHECKS + ".blockinteract";
    public static final String  BLOCKINTERACT_DIRECTION      = BLOCKINTERACT + ".direction";
    public static final String  BLOCKINTERACT_REACH          = BLOCKINTERACT + ".reach";

    /*
     * 888 88b, 888                    888    888 88e  888                          
     * 888 88P' 888  e88 88e   e88'888 888 ee 888 888D 888  ,"Y88b  e88'888  ,e e,  
     * 888 8K   888 d888 888b d888  '8 888 P  888 88"  888 "8" 888 d888  '8 d88 88b 
     * 888 88b, 888 Y888 888P Y888   , 888 b  888      888 ,ee 888 Y888   , 888   , 
     * 888 88P' 888  "88 88"   "88,e8' 888 8b 888      888 "88 888  "88,e8'  "YeeP" 
     */
    public static final String  BLOCKPLACE                   = CHECKS + ".blockplace";
    public static final String  BLOCKPLACE_AGAINST           = BLOCKPLACE + ".against";
    public static final String  BLOCKPLACE_AGAINST_AIR       = BLOCKPLACE_AGAINST + ".air";
	public static final String  BLOCKPLACE_AGAINST_LIQUIDS   = BLOCKPLACE_AGAINST + ".liquids";
    public static final String  BLOCKPLACE_DIRECTION         = BLOCKPLACE + ".direction";
    public static final String  BLOCKPLACE_FASTPLACE         = BLOCKPLACE + ".fastplace";
    public static final String  BLOCKPLACE_NOSWING           = BLOCKPLACE + ".noswing";
    public static final String  BLOCKPLACE_REACH             = BLOCKPLACE + ".reach";
    public static final String  BLOCKPLACE_SPEED             = BLOCKPLACE + ".speed";

    /*
     *   e88'Y88 888               d8   
     *  d888  'Y 888 ee   ,"Y88b  d88   
     * C8888     888 88b "8" 888 d88888 
     *  Y888  ,d 888 888 ,ee 888  888   
     *   "88,d88 888 888 "88 888  888   
     */
    public static final String  CHAT                         = CHECKS + ".chat";
    public static final String  CHAT_CAPTCHA                 = CHAT + ".captcha";
    public static final String  CHAT_COLOR                   = CHAT + ".color";
    public static final String  CHAT_COMMANDS                = CHAT + ".commands";
    public static final String  CHAT_TEXT                    = CHAT + ".text";
    public static final String  CHAT_LOGINS                  = CHAT + ".logins";
    public static final String  CHAT_RELOG                   = CHAT + ".relog";
    
    /*
     * Combined !
     */
    public static final String  COMBINED                     = CHECKS + ".combined";
	public static final String  COMBINED_IMPROBABLE          = COMBINED + ".improbable";

    /*
     * 888'Y88 ,e,          888       d8   
     * 888 ,'Y  "   e88 888 888 ee   d88   
     * 888C8   888 d888 888 888 88b d88888 
     * 888 "   888 Y888 888 888 888  888   
     * 888     888  "88 888 888 888  888   
     *               ,  88P                
     *              "8",P"                 
     */
    public static final String  FIGHT                        = CHECKS + ".fight";
    public static final String  FIGHT_ANGLE                  = FIGHT + ".angle";
    public static final String  FIGHT_CRITICAL               = FIGHT + ".critical";
    public static final String  FIGHT_DIRECTION              = FIGHT + ".direction";
    public static final String  FIGHT_GODMODE                = FIGHT + ".godmode";
    public static final String  FIGHT_KNOCKBACK              = FIGHT + ".knockback";
    public static final String  FIGHT_NOSWING                = FIGHT + ".noswing";
    public static final String  FIGHT_REACH                  = FIGHT + ".reach";
	public static final String  FIGHT_SELFHIT                = FIGHT + ".selfhit";
    public static final String  FIGHT_SPEED                  = FIGHT + ".speed";

    /*
     * 888                                     d8                              
     * 888 888 8e  Y8b Y888P  ,e e,  888 8e   d88    e88 88e  888,8, Y8b Y888P 
     * 888 888 88b  Y8b Y8P  d88 88b 888 88b d88888 d888 888b 888 "   Y8b Y8P  
     * 888 888 888   Y8b "   888   , 888 888  888   Y888 888P 888      Y8b Y   
     * 888 888 888    Y8P     "YeeP" 888 888  888    "88 88"  888       888    
     *                                                                  888    
     *                                                                  888    
     */
    public static final String  INVENTORY                    = CHECKS + ".inventory";
    public static final String  INVENTORY_DROP               = INVENTORY + ".drop";
    public static final String  INVENTORY_FASTCLICK          = INVENTORY + ".fastclick";
    public static final String  INVENTORY_INSTANTBOW         = INVENTORY + ".instantbow";
    public static final String  INVENTORY_INSTANTEAT         = INVENTORY + ".instanteat";
    public static final String  INVENTORY_ITEMS              = INVENTORY + ".items";

    /*
     *     e   e                         ,e,                  
     *    d8b d8b     e88 88e  Y8b Y888P  "  888 8e   e88 888 
     *   e Y8b Y8b   d888 888b  Y8b Y8P  888 888 88b d888 888 
     *  d8b Y8b Y8b  Y888 888P   Y8b "   888 888 888 Y888 888 
     * d888b Y8b Y8b  "88 88"     Y8P    888 888 888  "88 888 
     *                                                 ,  88P 
     *                                                "8",P"  
     */
    public static final String  MOVING                       = CHECKS + ".moving";
    public static final String  MOVING_BOATSANYWHERE         = MOVING + ".boatsanywhere";
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

    /*
     *     e   e                    888 ,e,  dP,e, ,e,                    d8   ,e,                         
     *    d8b d8b     e88 88e   e88 888  "   8b "   "   e88'888  ,"Y88b  d88    "   e88 88e  888 8e   dP"Y 
     *   e Y8b Y8b   d888 888b d888 888 888 888888 888 d888  '8 "8" 888 d88888 888 d888 888b 888 88b C88b  
     *  d8b Y8b Y8b  Y888 888P Y888 888 888  888   888 Y888   , ,ee 888  888   888 Y888 888P 888 888  Y88D 
     * d888b Y8b Y8b  "88 88"   "88 888 888  888   888  "88,e8' "88 888  888   888  "88 88"  888 888 d,dP  
     */
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

    private static final String SMARTMOVING                  = MODS + ".smartmoving";
    public static final String  SMARTMOVING_CLIMBING         = SMARTMOVING + ".climbing";
    public static final String  SMARTMOVING_CRAWLING         = SMARTMOVING + ".crawling";
    public static final String  SMARTMOVING_FLYING           = SMARTMOVING + ".flying";
    public static final String  SMARTMOVING_JUMPING          = SMARTMOVING + ".jumping";
    public static final String  SMARTMOVING_SLIDING          = SMARTMOVING + ".sliding";
    public static final String  SMARTMOVING_SWIMMING         = SMARTMOVING + ".swimming";

    private static final String ZOMBE                        = MODS + ".zombe";
    public static final String  ZOMBE_FLY                    = ZOMBE + ".fly";
    public static final String  ZOMBE_NOCLIP                 = ZOMBE + ".noclip";
    public static final String  ZOMBE_CHEAT                  = ZOMBE + ".cheat";
    
}
