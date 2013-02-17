package fr.neatmonster.nocheatplus.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

import org.bukkit.Material;

/*
 * M""""""'YMM          .8888b                   dP   dP   MM'""""'YMM                   .8888b oo          
 * M  mmmm. `M          88   "                   88   88   M' .mmm. `M                   88   "             
 * M  MMMMM  M .d8888b. 88aaa  .d8888b. dP    dP 88 d8888P M  MMMMMooM .d8888b. 88d888b. 88aaa  dP .d8888b. 
 * M  MMMMM  M 88ooood8 88     88'  `88 88    88 88   88   M  MMMMMMMM 88'  `88 88'  `88 88     88 88'  `88 
 * M  MMMM' .M 88.  ... 88     88.  .88 88.  .88 88   88   M. `MMM' .M 88.  .88 88    88 88     88 88.  .88 
 * M       .MM `88888P' dP     `88888P8 `88888P' dP   dP   MM.     .dM `88888P' dP    dP dP     dP `8888P88 
 * MMMMMMMMMMM                                             MMMMMMMMMMM                                  .88 
 *                                                                                                  d8888P  
 */
/**
 * These are the default settings for NoCheatPlus. They will be used in addition to/in replacement of configurations
 * given in the configFactory.yml file.
 */
public class DefaultConfig extends ConfigFile {

	/** 
	 * NCP build needed for this config.
	 * (Should only increment with changing or removing paths.) 
	 */
	public static final int buildNumber = 384;
	
	// TODO: auto input full version or null to an extra variable or several [fail safe for other syntax checking]?

    /**
     * Instantiates a new default configuration.
     */
    public DefaultConfig() {
        super();
        
        
        // General:
        set(ConfPaths.SAVEBACKCONFIG, true);

        /*
         * 888                                 ,e,                  
         * 888      e88 88e   e88 888  e88 888  "  888 8e   e88 888 
         * 888     d888 888b d888 888 d888 888 888 888 88b d888 888 
         * 888  ,d Y888 888P Y888 888 Y888 888 888 888 888 Y888 888 
         * 888,d88  "88 88"   "88 888  "88 888 888 888 888  "88 888 
         *                     ,  88P   ,  88P               ,  88P 
         *                    "8",P"   "8",P"               "8",P"  
         */
        set(ConfPaths.LOGGING_ACTIVE, true);
        set(ConfPaths.LOGGING_DEBUG, false);
        set(ConfPaths.LOGGING_CONSOLE, true);
        set(ConfPaths.LOGGING_FILE, true);
        set(ConfPaths.LOGGING_FILENAME, "nocheatplus.log");
        set(ConfPaths.LOGGING_INGAMECHAT, true);

        /*
         *     e   e     ,e,                        888 888                                                   
         *    d8b d8b     "   dP"Y  e88'888  ,e e,  888 888  ,"Y88b 888 8e   ,e e,   e88 88e  8888 8888  dP"Y 
         *   e Y8b Y8b   888 C88b  d888  '8 d88 88b 888 888 "8" 888 888 88b d88 88b d888 888b 8888 8888 C88b  
         *  d8b Y8b Y8b  888  Y88D Y888   , 888   , 888 888 ,ee 888 888 888 888   , Y888 888P Y888 888P  Y88D 
         * d888b Y8b Y8b 888 d,dP   "88,e8'  "YeeP" 888 888 "88 888 888 888  "YeeP"  "88 88"   "88 88"  d,dP  
         */
        set(ConfPaths.MISCELLANEOUS_ALLOWCLIENTMODS, false);
        set(ConfPaths.MISCELLANEOUS_OPINCONSOLEONLY, false);
        set(ConfPaths.MISCELLANEOUS_PROTECTPLUGINS, true);
        set(ConfPaths.MISCELLANEOUS_MANAGELISTENERS, false);
//        set(ConfPaths.MISCELLANEOUS_CHECKFORUPDATES, true);
        set(ConfPaths.MISCELLANEOUS_REPORTTOMETRICS, true);

//        set(ConfPaths.MISCELLANEOUS_NOMOVEDTOOQUICKLY_ENABLED, false);
//        set(ConfPaths.MISCELLANEOUS_NOMOVEDTOOQUICKLY_USEPROXY, false);
        
        
        set(ConfPaths.DATA_EXPIRATION_DURATION, 0);
        set(ConfPaths.DATA_EXPIRATION_HISTORY, false);

        /*
         * 888 88b, 888                    888    888 88b,                        888    
         * 888 88P' 888  e88 88e   e88'888 888 ee 888 88P' 888,8,  ,e e,   ,"Y88b 888 ee 
         * 888 8K   888 d888 888b d888  '8 888 P  888 8K   888 "  d88 88b "8" 888 888 P  
         * 888 88b, 888 Y888 888P Y888   , 888 b  888 88b, 888    888   , ,ee 888 888 b  
         * 888 88P' 888  "88 88"   "88,e8' 888 8b 888 88P' 888     "YeeP" "88 888 888 8b 
         */
        set(ConfPaths.BLOCKBREAK_DIRECTION_CHECK, true);
        set(ConfPaths.BLOCKBREAK_DIRECTION_ACTIONS, "cancel vl>10 log:bdirection:0:5:if cancel");

        set(ConfPaths.BLOCKBREAK_FASTBREAK_CHECK, true);
        set(ConfPaths.BLOCKBREAK_FASTBREAK_STRICT, true);
        set(ConfPaths.BLOCKBREAK_FASTBREAK_DELAY, 90);
        set(ConfPaths.BLOCKBREAK_FASTBREAK_MOD_SURVIVAL, 100);
        set(ConfPaths.BLOCKBREAK_FASTBREAK_GRACE, 2000);
        set(ConfPaths.BLOCKBREAK_FASTBREAK_ACTIONS, "cancel vl>0 log:fastbreak:3:5:cif cancel");
        
        set(ConfPaths.BLOCKBREAK_FREQUENCY_CHECK, true);
        set(ConfPaths.BLOCKBREAK_FREQUENCY_MOD_CREATIVE, 95);
        set(ConfPaths.BLOCKBREAK_FREQUENCY_MOD_SURVIVAL, 45);
        set(ConfPaths.BLOCKBREAK_FREQUENCY_SHORTTERM_TICKS, 5);
        set(ConfPaths.BLOCKBREAK_FREQUENCY_SHORTTERM_LIMIT, 7);
        set(ConfPaths.BLOCKBREAK_FREQUENCY_ACTIONS, "cancel vl>5 log:bbfrequency:3:5:if cancel vl>60 log:bbfrequency:0:5:cif cancel cmd:kickfrequency");

        set(ConfPaths.BLOCKBREAK_NOSWING_CHECK, true);
        set(ConfPaths.BLOCKBREAK_NOSWING_ACTIONS, "cancel vl>10 log:noswing:0:5:if cancel");

        set(ConfPaths.BLOCKBREAK_REACH_CHECK, true);
        set(ConfPaths.BLOCKBREAK_REACH_ACTIONS, "cancel vl>5 log:breach:0:2:if cancel");
        
        set(ConfPaths.BLOCKBREAK_WRONGBLOCK_CHECK, true);
        set(ConfPaths.BLOCKBREAK_WRONGBLOCK_LEVEL, 10);
        set(ConfPaths.BLOCKBREAK_WRONGBLOCK_ACTIONS, "cancel vl>10 log:bwrong:0:5:if cancel vl>30 log:bwrong:0:5:cif cancel cmd:kickwb");
        
        /*
         * 888 88b, 888                    888    888           d8                                     d8   
         * 888 88P' 888  e88 88e   e88'888 888 ee 888 888 8e   d88    ,e e,  888,8,  ,"Y88b  e88'888  d88   
         * 888 8K   888 d888 888b d888  '8 888 P  888 888 88b d88888 d88 88b 888 "  "8" 888 d888  '8 d88888 
         * 888 88b, 888 Y888 888P Y888   , 888 b  888 888 888  888   888   , 888    ,ee 888 Y888   ,  888   
         * 888 88P' 888  "88 88"   "88,e8' 888 8b 888 888 888  888    "YeeP" 888    "88 888  "88,e8'  888   
         */
        set(ConfPaths.BLOCKINTERACT_DIRECTION_CHECK, true);
        set(ConfPaths.BLOCKINTERACT_DIRECTION_ACTIONS, "cancel vl>10 log:bdirection:0:3:if cancel");

        set(ConfPaths.BLOCKINTERACT_REACH_CHECK, true);
        set(ConfPaths.BLOCKINTERACT_REACH_ACTIONS, "cancel vl>5 log:breach:0:2:if cancel");
        
        set(ConfPaths.BLOCKINTERACT_SPEED_CHECK, true);
        set(ConfPaths.BLOCKINTERACT_SPEED_INTERVAL, 2000);
        set(ConfPaths.BLOCKINTERACT_SPEED_LIMIT, 82);
        set(ConfPaths.BLOCKINTERACT_SPEED_ACTIONS, "cancel vl>200 log:bspeed:0:2:if cancel vl>1000 cancel log:bspeed:0:2:icf cmd:kickbspeed");
        
        set(ConfPaths.BLOCKINTERACT_VISIBLE_CHECK, true);
        set(ConfPaths.BLOCKINTERACT_VISIBLE_ACTIONS, "cancel vl>5 log:bvisible:0:2:if cancel");
        
        /*
         * 888 88b, 888                    888    888 88e  888                          
         * 888 88P' 888  e88 88e   e88'888 888 ee 888 888D 888  ,"Y88b  e88'888  ,e e,  
         * 888 8K   888 d888 888b d888  '8 888 P  888 88"  888 "8" 888 d888  '8 d88 88b 
         * 888 88b, 888 Y888 888P Y888   , 888 b  888      888 ,ee 888 Y888   , 888   , 
         * 888 88P' 888  "88 88"   "88,e8' 888 8b 888      888 "88 888  "88,e8'  "YeeP" 
         */
        set(ConfPaths.BLOCKPLACE_DIRECTION_CHECK, true);
        set(ConfPaths.BLOCKPLACE_DIRECTION_ACTIONS, "cancel vl>10 log:bdirection:0:3:if cancel");

        set(ConfPaths.BLOCKPLACE_FASTPLACE_CHECK, true);
        set(ConfPaths.BLOCKPLACE_FASTPLACE_LIMIT, 22);
        set(ConfPaths.BLOCKPLACE_FASTPLACE_SHORTTERM_TICKS, 10);
        set(ConfPaths.BLOCKPLACE_FASTPLACE_SHORTTERM_LIMIT, 6);
        set(ConfPaths.BLOCKPLACE_FASTPLACE_ACTIONS, "cancel vl>100 log:fastplace:3:5:cif cancel");

        set(ConfPaths.BLOCKPLACE_REACH_CHECK, true);
        set(ConfPaths.BLOCKPLACE_REACH_ACTIONS, "cancel vl>5 log:breach:0:2:if cancel");

        set(ConfPaths.BLOCKPLACE_NOSWING_CHECK, true);
        set(ConfPaths.BLOCKPLACE_NOSWING_ACTIONS, "cancel vl>10 log:noswing:0:5:if cancel");

        set(ConfPaths.BLOCKPLACE_SPEED_CHECK, true);
        set(ConfPaths.BLOCKPLACE_SPEED_INTERVAL, 45L);
        set(ConfPaths.BLOCKPLACE_SPEED_ACTIONS,
                "cancel vl>150 log:bpspeed:3:5:if cancel vl>1000 log:bpspeed:3:5:cif cancel");

        /*
         *   e88'Y88 888               d8   
         *  d888  'Y 888 ee   ,"Y88b  d88   
         * C8888     888 88b "8" 888 d88888 
         *  Y888  ,d 888 888 ,ee 888  888   
         *   "88,d88 888 888 "88 888  888   
         */
        
        set(ConfPaths.CHAT_COLOR_CHECK, true);
        set(ConfPaths.CHAT_COLOR_ACTIONS, "log:color:0:1:if cancel");
        
        
        set(ConfPaths.CHAT_COMMANDS_CHECK, true);
        set(ConfPaths.CHAT_COMMANDS_EXCLUSIONS, new ArrayList<String>());
        set(ConfPaths.CHAT_COMMANDS_HANDLEASCHAT, Arrays.asList("/me"));
        set(ConfPaths.CHAT_COMMANDS_LEVEL, 10);
        set(ConfPaths.CHAT_COMMANDS_SHORTTERM_TICKS, 18);
        set(ConfPaths.CHAT_COMMANDS_SHORTTERM_LEVEL, 3);
        set(ConfPaths.CHAT_COMMANDS_ACTIONS, "log:commands:0:5:cf cancel cmd:kickcommands vl>20 log:commands:0:5:cf cancel cmd:tempkick1");
        
        // Captcha.
        set(ConfPaths.CHAT_CAPTCHA_CHECK, false);
        set(ConfPaths.CHAT_CAPTCHA_CHARACTERS, "abcdefghjkmnpqrtuvwxyzABCDEFGHJKMNPQRTUVWXYZ2346789");
        set(ConfPaths.CHAT_CAPTCHA_LENGTH, 6);
        set(ConfPaths.CHAT_CAPTCHA_QUESTION, "&cPlease type '&6[captcha]&c' to continue sending messages/commands.");
        set(ConfPaths.CHAT_CAPTCHA_SUCCESS, "&aOK, it sounds like you're not a spambot.");
        set(ConfPaths.CHAT_CAPTCHA_TRIES, 3);
        set(ConfPaths.CHAT_CAPTCHA_ACTIONS, "cancel cmd:kickcaptcha vl>4 log:captcha:2:5:cf cancel cmd:kickcaptcha");
        
        // Text (ordering on purpose).
        set(ConfPaths.CHAT_TEXT_CHECK, true);
        set(ConfPaths.CHAT_TEXT_FREQ_NORM_MIN, 0.0);
        set(ConfPaths.CHAT_TEXT_FREQ_NORM_FACTOR, 0.9D);
        set(ConfPaths.CHAT_TEXT_FREQ_NORM_WEIGHT, 6);
        set(ConfPaths.CHAT_TEXT_FREQ_NORM_LEVEL, 160);
        set(ConfPaths.CHAT_TEXT_FREQ_NORM_ACTIONS, "cancel cmd:tellchatnormal vl>7 log:chatnormal:0:5:f cancel cmd:tellchatnormal vl>20 log:chatnormal:0:5:cf cancel cmd:kickchatnormal vl>40 log:chatnormal:0:5:cf cancel cmd:kickchat5");

        set(ConfPaths.CHAT_TEXT_FREQ_SHORTTERM_MIN, 2.0);
        set(ConfPaths.CHAT_TEXT_FREQ_SHORTTERM_FACTOR, 0.7);
        set(ConfPaths.CHAT_TEXT_FREQ_SHORTTERM_WEIGHT, 3.0);
        set(ConfPaths.CHAT_TEXT_FREQ_SHORTTERM_LEVEL, 20.0);
        set(ConfPaths.CHAT_TEXT_FREQ_SHORTTERM_ACTIONS, "log:chatfast:0:5:cf cancel cmd:kickchatfast vl>20 log:chatfast:0:5:cf cancel cmd:kickchat1 vl>40 log:chatfast:0:5:cf cancel cmd:kickchat5");
        // Message
        set(ConfPaths.CHAT_TEXT_MSG_LETTERCOUNT, 1.0);
        set(ConfPaths.CHAT_TEXT_MSG_PARTITION, 1.0);
        set(ConfPaths.CHAT_TEXT_MSG_UPPERCASE, 1.0);
        set(ConfPaths.CHAT_TEXT_MSG_AFTERJOIN, 1.5);
        set(ConfPaths.CHAT_TEXT_MSG_NOMOVING, 1.5);
        set(ConfPaths.CHAT_TEXT_MSG_REPEATCANCEL, 1.0);
        set(ConfPaths.CHAT_TEXT_MSG_REPEATGLOBAL, 1.0);
        set(ConfPaths.CHAT_TEXT_MSG_REPEATSELF, 1.5);
        set(ConfPaths.CHAT_TEXT_MSG_WORDS_LENGTHAV, 1.0);
        set(ConfPaths.CHAT_TEXT_MSG_WORDS_LENGTHMSG, 1.0);
        set(ConfPaths.CHAT_TEXT_MSG_WORDS_NOLETTER, 0.0);
        // Global
        set(ConfPaths.CHAT_TEXT_GL_CHECK, true);
        set(ConfPaths.CHAT_TEXT_GL_WEIGHT, 0.5);
        set(ConfPaths.CHAT_TEXT_GL_WORDS_CHECK, false);
        set(ConfPaths.CHAT_TEXT_GL_WEIGHT, 1.0);
        set(ConfPaths.CHAT_TEXT_GL_PREFIXES_CHECK , false);
        set(ConfPaths.CHAT_TEXT_GL_SIMILARITY_CHECK , false);
        // Player
        set(ConfPaths.CHAT_TEXT_PP_CHECK, true);
        set(ConfPaths.CHAT_TEXT_PP_WORDS_CHECK, false);
        set(ConfPaths.CHAT_TEXT_PP_PREFIXES_CHECK, false);
        set(ConfPaths.CHAT_TEXT_PP_SIMILARITY_CHECK , false);
        // Warning (commands + chat).
        set(ConfPaths.CHAT_WARNING_CHECK, true);
        set(ConfPaths.CHAT_WARNING_LEVEL, 67);
        set(ConfPaths.CHAT_WARNING_MESSAGE, "&e>>>\n&e>>> &cPlease &eslow down &cchat, &eyou might get kicked &cfor spam.\n&e>>>");
        set(ConfPaths.CHAT_WARNING_TIMEOUT, 10);
        // Relog
        set(ConfPaths.CHAT_RELOG_CHECK, true);
        set(ConfPaths.CHAT_RELOG_TIMEOUT, 5000L);
        set(ConfPaths.CHAT_RELOG_WARNING_MESSAGE, "&cYou relogged really fast! If you keep doing that, you're going to be banned.");
        set(ConfPaths.CHAT_RELOG_WARNING_NUMBER, 1);
        set(ConfPaths.CHAT_RELOG_KICKMESSAGE, "Too fast re-login, try with a little delay.");
        set(ConfPaths.CHAT_RELOG_WARNING_TIMEOUT, 60000L);
        set(ConfPaths.CHAT_RELOG_ACTIONS, "log:relog:0:10:cf cancel vl>20 log:relog:0:10:cf cancel cmd:tempkick5");
        // Logins
        set(ConfPaths.CHAT_LOGINS_CHECK, true);
        set(ConfPaths.CHAT_LOGINS_STARTUPDELAY, 300);
        set(ConfPaths.CHAT_LOGINS_PERWORLDCOUNT, false);
        set(ConfPaths.CHAT_LOGINS_SECONDS, 10);
        set(ConfPaths.CHAT_LOGINS_LIMIT, 6);
        set(ConfPaths.CHAT_LOGINS_KICKMESSAGE, "Too many people logging in, retry soon.");
        
        /*
         * Combined !
         */
        
        set(ConfPaths.COMBINED_BEDLEAVE_CHECK, true);
        set(ConfPaths.COMBINED_BEDLEAVE_ACTIONS, "cancel log:bedleave:0:5:if cmd:kickbedleave");
        
        set(ConfPaths.COMBINED_IMPROBABLE_CHECK , true);
        set(ConfPaths.COMBINED_IMPROBABLE_LEVEL, 300);
//        set(ConfPaths.COMBINED_IMPROBABLE_FASTBREAK_CHECK, false);
        set(ConfPaths.COMBINED_IMPROBABLE_ACTIONS, "cancel log:improbable:2:8:if");
        
        set(ConfPaths.COMBINED_INVULNERABLE_CHECK, true);
        set(ConfPaths.COMBINED_INVULNERABLE_TRIGGERS_ALWAYS, false);
        set(ConfPaths.COMBINED_INVULNERABLE_TRIGGERS_FALLDISTANCE, true);
        set(ConfPaths.COMBINED_INVULNERABLE_INITIALTICKS_JOIN, -1);
        set(ConfPaths.COMBINED_INVULNERABLE_IGNORE, Arrays.asList("FALL"));
        set(ConfPaths.COMBINED_INVULNERABLE_MODIFIERS + ".all", 0);
        
        set(ConfPaths.COMBINED_MUNCHHAUSEN_CHECK, false);
        set(ConfPaths.COMBINED_MUNCHHAUSEN_ACTIONS, "cancel vl>100 cancel log:munchhausen:0:60:if");
        
        set(ConfPaths.COMBINED_YAWRATE_RATE , 380);
        set(ConfPaths.COMBINED_YAWRATE_PENALTY_FACTOR, 1.0);
        set(ConfPaths.COMBINED_YAWRATE_PENALTY_MIN, 250);
        set(ConfPaths.COMBINED_YAWRATE_PENALTY_MAX, 2000);
        set(ConfPaths.COMBINED_YAWRATE_IMPROBABLE, true);
        
        /*
         * 888'Y88 ,e,          888       d8   
         * 888 ,'Y  "   e88 888 888 ee   d88   
         * 888C8   888 d888 888 888 88b d88888 
         * 888 "   888 Y888 888 888 888  888   
         * 888     888  "88 888 888 888  888   
         *               ,  88P                
         *              "8",P"                 
         */
        set(ConfPaths.FIGHT_CANCELDEAD, true);
        set(ConfPaths.FIGHT_YAWRATE_CHECK, true);
        
        set(ConfPaths.FIGHT_ANGLE_CHECK, true);
        set(ConfPaths.FIGHT_ANGLE_THRESHOLD, 50);
        set(ConfPaths.FIGHT_ANGLE_ACTIONS, "cancel vl>100 log:angle:3:5:f cancel vl>250 log:angle:0:5:cif cancel");

        set(ConfPaths.FIGHT_CRITICAL_CHECK, true);
        set(ConfPaths.FIGHT_CRITICAL_FALLDISTANCE, 0.01D);
        set(ConfPaths.FIGHT_CRITICAL_VELOCITY, 0.1D);
        set(ConfPaths.FIGHT_CRITICAL_ACTIONS, "cancel vl>50 log:critical:0:5:cif cancel");

        set(ConfPaths.FIGHT_DIRECTION_CHECK, true);
        set(ConfPaths.FIGHT_DIRECTION_PENALTY, 500L);
        set(ConfPaths.FIGHT_DIRECTION_ACTIONS,
                "cancel vl>5 log:fdirection:3:5:f cancel vl>20 log:fdirection:0:5:if cancel vl>50 log:fdirection:0:5:cif cancel");

        set(ConfPaths.FIGHT_GODMODE_CHECK, true);
        set(ConfPaths.FIGHT_GODMODE_LAGMINAGE, 1100); // TODO: ndt/2 => 500-600.
        set(ConfPaths.FIGHT_GODMODE_LAGMAXAGE, 5000);
        set(ConfPaths.FIGHT_GODMODE_ACTIONS, "log:godmode:2:5:if cancel vl>60 log:godmode:2:5:icf cancel"); // cmd:kickgod");

        set(ConfPaths.FIGHT_KNOCKBACK_CHECK, true);
        set(ConfPaths.FIGHT_KNOCKBACK_INTERVAL, 50L);
        set(ConfPaths.FIGHT_KNOCKBACK_ACTIONS, "cancel vl>50 log:knockback:0:5:cif cancel");

        set(ConfPaths.FIGHT_NOSWING_CHECK, true);
        set(ConfPaths.FIGHT_NOSWING_ACTIONS, "cancel vl>10 log:noswing:0:5:if cancel");

        set(ConfPaths.FIGHT_REACH_CHECK, true);
        set(ConfPaths.FIGHT_REACH_PENALTY, 500);
        set(ConfPaths.FIGHT_REACH_REDUCE, true);
        set(ConfPaths.FIGHT_REACH_ACTIONS, "cancel vl>10 log:freach:2:5:if cancel");
        
        set(ConfPaths.FIGHT_SELFHIT_CHECK, true);
        set(ConfPaths.FIGHT_SELFHIT_ACTIONS, "log:fselfhit:0:5:if cancel vl>10 log:fselfhit:0:5:icf cancel cmd:kickselfhit");

        set(ConfPaths.FIGHT_SPEED_CHECK, true);
        set(ConfPaths.FIGHT_SPEED_LIMIT, 15);
        set(ConfPaths.FIGHT_SPEED_ACTIONS, "log:fspeed:0:5:if cancel");
        set(ConfPaths.FIGHT_SPEED_SHORTTERM_TICKS, 7);
        set(ConfPaths.FIGHT_SPEED_SHORTTERM_LIMIT, 6);

        /*
         * 888                                     d8                              
         * 888 888 8e  Y8b Y888P  ,e e,  888 8e   d88    e88 88e  888,8, Y8b Y888P 
         * 888 888 88b  Y8b Y8P  d88 88b 888 88b d88888 d888 888b 888 "   Y8b Y8P  
         * 888 888 888   Y8b "   888   , 888 888  888   Y888 888P 888      Y8b Y   
         * 888 888 888    Y8P     "YeeP" 888 888  888    "88 88"  888       888    
         *                                                                  888    
         *                                                                  888    
         */
        set(ConfPaths.INVENTORY_DROP_CHECK, true);
        set(ConfPaths.INVENTORY_DROP_LIMIT, 100);
        set(ConfPaths.INVENTORY_DROP_TIMEFRAME, 20L);
        set(ConfPaths.INVENTORY_DROP_ACTIONS, "log:drop:0:1:cif cancel cmd:dropkick:0:1");

        set(ConfPaths.INVENTORY_FASTCLICK_CHECK, true);
        set(ConfPaths.INVENTORY_FASTCLICK_SPARECREATIVE, true);
        set(ConfPaths.INVENTORY_FASTCLICK_ACTIONS, "cancel vl>50 log:fastclick:3:5:cif cancel");

        set(ConfPaths.INVENTORY_INSTANTBOW_CHECK, true);
        set(ConfPaths.INVENTORY_INSTANTBOW_STRICT, true);
        set(ConfPaths.INVENTORY_INSTANTBOW_DELAY, 130);
        set(ConfPaths.INVENTORY_INSTANTBOW_ACTIONS, "cancel vl>15 log:instantbow:2:5:if cancel");

        set(ConfPaths.INVENTORY_INSTANTEAT_CHECK, true);
        set(ConfPaths.INVENTORY_INSTANTEAT_ACTIONS, "log:instanteat:2:5:if cancel");
        
        set(ConfPaths.INVENTORY_ITEMS_CHECK, true);

        /*
         *     e   e                         ,e,                  
         *    d8b d8b     e88 88e  Y8b Y888P  "  888 8e   e88 888 
         *   e Y8b Y8b   d888 888b  Y8b Y8P  888 888 88b d888 888 
         *  d8b Y8b Y8b  Y888 888P   Y8b "   888 888 888 Y888 888 
         * d888b Y8b Y8b  "88 88"     Y8P    888 888 888  "88 888 
         *                                                 ,  88P 
         *                                                "8",P"  
         */
        set(ConfPaths.MOVING_CREATIVEFLY_CHECK, true);
        set(ConfPaths.MOVING_CREATIVEFLY_IGNOREALLOWFLIGHT, false);
        set(ConfPaths.MOVING_CREATIVEFLY_IGNORECREATIVE, false);
        set(ConfPaths.MOVING_CREATIVEFLY_HORIZONTALSPEED, 100);
        set(ConfPaths.MOVING_CREATIVEFLY_MAXHEIGHT, 128);
        set(ConfPaths.MOVING_CREATIVEFLY_VERTICALSPEED, 100);
        set(ConfPaths.MOVING_CREATIVEFLY_ACTIONS,
            "log:flyshort:3:5:f cancel vl>100 log:flyshort:0:5:if cancel vl>400 log:flylong:0:5:cif cancel");

        set(ConfPaths.MOVING_MOREPACKETS_CHECK, true);
        set(ConfPaths.MOVING_MOREPACKETS_ACTIONS, "cancel vl>10 log:morepackets:0:2:if cancel vl>100 log:morepackets:0:2:if cancel cmd:kickpackets");

        set(ConfPaths.MOVING_MOREPACKETSVEHICLE_CHECK, true);
        set(ConfPaths.MOVING_MOREPACKETSVEHICLE_ACTIONS,
                "cancel vl>10 log:morepackets:0:2:if cancel");

        set(ConfPaths.MOVING_NOFALL_CHECK, true);
        set(ConfPaths.MOVING_NOFALL_DEALDAMAGE, true);
        set(ConfPaths.MOVING_NOFALL_RESETONVL, false);
        set(ConfPaths.MOVING_NOFALL_RESETONTP, false);
        set(ConfPaths.MOVING_NOFALL_ACTIONS, "log:nofall:0:5:if cancel vl>30 log:nofall:0:5:icf cancel");
        
        set(ConfPaths.MOVING_PASSABLE_CHECK, true);
        set(ConfPaths.MOVING_PASSABLE_RAYTRACING_CHECK, true);
        set(ConfPaths.MOVING_PASSABLE_RAYTRACING_BLOCKCHANGEONLY, true);
        set(ConfPaths.MOVING_PASSABLE_RAYTRACING_VCLIPONLY, true);
        set(ConfPaths.MOVING_PASSABLE_ACTIONS, "cancel vl>10 log:passable:0:5:if cancel vl>50 log:passable:0:5:icf cancel");

        set(ConfPaths.MOVING_SURVIVALFLY_CHECK, true);
//        set(ConfPaths.MOVING_SURVIVALFLY_EXTENDED_HACC, false);
        set(ConfPaths.MOVING_SURVIVALFLY_EXTENDED_VACC, true);
        set(ConfPaths.MOVING_SURVIVALFLY_FALLDAMAGE, true);
        // The settings aren't enabled by default. Simply write them yourself in the configuration file.
        // set(ConfPaths.MOVING_SURVIVALFLY_BLOCKINGSPEED, 100);
        // set(ConfPaths.MOVING_SURVIVALFLY_SNEAKINGSPEED, 100);
        // set(ConfPaths.MOVING_SURVIVALFLY_SPEEDINGSPEED, 200);
        // set(ConfPaths.MOVING_SURVIVALFLY_SPRINTINGSPEED, 100);
        // set(ConfPaths.MOVING_SURVIVALFLY_SWIMMINGSPEED, 100);
        // set(ConfPaths.MOVING_SURVIVALFLY_WALKINGSPEED, 100);
        set(ConfPaths.MOVING_SURVIVALFLY_ACTIONS,
                "log:flyshort:3:5:f cancel vl>100 log:flyshort:0:5:if cancel vl>400 log:flylong:0:5:cif cancel vl>1000 log:flylong:0:5:cif cancel cmd:kickfly");

        // sf / hover check.
        set(ConfPaths.MOVING_SURVIVALFLY_HOVER_CHECK, true);
        set(ConfPaths.MOVING_SURVIVALFLY_HOVER_TICKS, 80);
        set(ConfPaths.MOVING_SURVIVALFLY_HOVER_FALLDAMAGE, true);
        set(ConfPaths.MOVING_SURVIVALFLY_HOVER_SFVIOLATION, 500);
        
        // Special.
        set(ConfPaths.MOVING_VELOCITY_GRACETICKS, 20);
        
        /*
         *  dP"8   d8          ,e,                        
         * C8b Y  d88   888,8,  "  888 8e   e88 888  dP"Y 
         *  Y8b  d88888 888 "  888 888 88b d888 888 C88b  
         * b Y8D  888   888    888 888 888 Y888 888  Y88D 
         * 8edP   888   888    888 888 888  "88 888 d,dP  
         *                                   ,  88P       
         *                                  "8",P"        
         */
        final String start = "[player] failed [check]: ";
        final String end = ". VL [violations].";
        final String tell = "ncp tell [player] ";
        set(ConfPaths.STRINGS + ".angle", start + "tried to hit multiple entities at the same time" + end);
        set(ConfPaths.STRINGS + ".ban", "ban [player]");
        set(ConfPaths.STRINGS + ".ban-ip", "ban-ip [ip]");
        set(ConfPaths.STRINGS + ".bbfrequency", start + "tried to break too many blocks within time frame" + end);
        set(ConfPaths.STRINGS + ".bdirection", start + "tried to interact with a block out of his line of sight" + end);
        set(ConfPaths.STRINGS + ".bedleave", start + "sends bed leave packets (was not in bed)" + end);
        set(ConfPaths.STRINGS + ".bpspeed", start + "tried to throw projectiles too quickly" + end);
        set(ConfPaths.STRINGS + ".breach", start + "exceeds block-interact distance ([reachdistance])" + end);
        set(ConfPaths.STRINGS + ".bspeed", start + "interacts too fast" + end);
        set(ConfPaths.STRINGS + ".bvisible", start + "interacts with a block out of sight" + end);
        set(ConfPaths.STRINGS + ".bwrong", start + "broke another block than clicked" + end);
        set(ConfPaths.STRINGS + ".captcha", "[player] failed captcha repeatedly" + end);
        set(ConfPaths.STRINGS + ".color", start + "sent colored chat message" + end);
        set(ConfPaths.STRINGS + ".commands", start + "issued too many commands" + end);
        set(ConfPaths.STRINGS + ".combspeed", start + "performs different actions at very high speed" + end);
        set(ConfPaths.STRINGS + ".critical", start + "tried to do a critical hit but wasn't technically jumping" + end);
        set(ConfPaths.STRINGS + ".drop", start + "tried to drop more items than allowed" + end);
        set(ConfPaths.STRINGS + ".dropkick", "ncp delay ncp kick [player] Dropping items too fast.");
        set(ConfPaths.STRINGS + ".fastbreak", start + "tried to break blocks ([blockid]) faster than possible" + end);
        set(ConfPaths.STRINGS + ".fastclick", start + "tried to move items in his inventory too quickly" + end);
        set(ConfPaths.STRINGS + ".fastplace", start + "tried to place too many blocks" + end);
        set(ConfPaths.STRINGS + ".fdirection", start + "tried to hit an entity out of line of sight" + end);
        set(ConfPaths.STRINGS + ".flyshort", start + "tried to move unexpectedly" + end);
        set(ConfPaths.STRINGS + ".flylong", start
                + "tried to move from [locationfrom] to [locationto] over a distance of [distance] block(s)" + end);
        set(ConfPaths.STRINGS + ".freach", start + "tried to attack entity out of reach" + end);
        set(ConfPaths.STRINGS + ".fselfhit", start + "tried to self-hit" + end);
        set(ConfPaths.STRINGS + ".fspeed", start + "tried to attack with too high a frequency" + end);
        set(ConfPaths.STRINGS + ".chatnormal", start + "potentially annoying chat" + end);
        set(ConfPaths.STRINGS + ".godmode", start + "avoided taking damage or lagging" + end);
        set(ConfPaths.STRINGS + ".improbable", start + "meets the improbable more than expected" + end);
        set(ConfPaths.STRINGS + ".instantbow", start + "fires bow too fast" + end);
        set(ConfPaths.STRINGS + ".instanteat", start + "eats food [food] too fast" + end);
        set(ConfPaths.STRINGS + ".kick", "kick [player]");
        set(ConfPaths.STRINGS + ".kickbedleave", "ncp delay ncp kick [player] Go find a bed!");
        set(ConfPaths.STRINGS + ".kickbspeed", "ncp kick [player] Too fast interaction!");
        set(ConfPaths.STRINGS + ".kickcaptcha", "ncp kick [player] Enter the captcha!");
        set(ConfPaths.STRINGS + ".kickchat1", "ncp tempkick [player] 1 You're still not allowed to spam!");
        set(ConfPaths.STRINGS + ".kickchat5", "ncp tempkick [player] 5 You're not intended to spam!");
        set(ConfPaths.STRINGS + ".kickchatfast", "ncp kick [player] You're not allowed to spam in chat!");
        set(ConfPaths.STRINGS + ".kickchatnormal", "ncp kick [player] Too many chat messages, take a break.");
        set(ConfPaths.STRINGS + ".kickfly", "ncp delay ncp kick [player] Kicked for flying (or related).");
        set(ConfPaths.STRINGS + ".kickcommands", "ncp tempkick [player] 1 You're not allowed to spam commands!");
        set(ConfPaths.STRINGS + ".kickfrequency", "ncp kick [player] How about doing that less often?");
        set(ConfPaths.STRINGS + ".kickgod", "ncp kick [player] God mode?");
        set(ConfPaths.STRINGS + ".kickpackets", "ncp delay ncp kick [player] Too many packets (extreme lag?).");
        set(ConfPaths.STRINGS + ".kickselfhit", "ncp kick [player] That must be exhausting!");
        set(ConfPaths.STRINGS + ".kickwb", "ncp kick [player] Block breaking out of sync!");
        set(ConfPaths.STRINGS + ".knockback", start + "tried to do a knockback but wasn't technically sprinting" + end);
        set(ConfPaths.STRINGS + ".morepackets", start + "sent [packets] more packet(s) than expected" + end);
        set(ConfPaths.STRINGS + ".munchhausen", start + "almost made it off the pit" + end);
        set(ConfPaths.STRINGS + ".nofall", start + "tried to avoid fall damage" + end);
        set(ConfPaths.STRINGS + ".chatfast", start + "acted like spamming (IP: [ip])" + end);
        set(ConfPaths.STRINGS + ".noswing", start + "didn't swing arm" + end);
        set(ConfPaths.STRINGS + ".passable", start + "moved into a block ([blockid])" + end);
        set(ConfPaths.STRINGS + ".relog", start + "relogs too fast" + end);
        set(ConfPaths.STRINGS + ".tellchatnormal", tell + "&cNCP: &eToo many messages, slow down...");
        set(ConfPaths.STRINGS + ".tempkick1", "ncp tempkick [player] 1 Wait a minute!");
        set(ConfPaths.STRINGS + ".tempkick5", "ncp tempkick [player] 5 You have five minutes to think about it!");

        set(ConfPaths.COMPATIBILITY_BUKKITONLY, false);
        set(ConfPaths.COMPATIBILITY_BLOCKS + ConfPaths.SUB_IGNOREPASSABLE, Arrays.asList(
                Material.WOODEN_DOOR.name(), Material.IRON_DOOR_BLOCK.name(),
                Material.TRAP_DOOR.name(),
                Material.PISTON_EXTENSION.name(), 
                Material.PISTON_MOVING_PIECE.name() // TODO: ?
        ));
        set(ConfPaths.COMPATIBILITY_BLOCKS + ConfPaths.SUB_ALLOWINSTANTBREAK, new LinkedList<String>());
        // Update internal factory based on all the new entries to the "actions" section.
        regenerateActionLists();
    }
}
