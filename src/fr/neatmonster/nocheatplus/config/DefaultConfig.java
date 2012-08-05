package fr.neatmonster.nocheatplus.config;

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
 * given in the config.yml file.
 */
public class DefaultConfig extends ConfigFile {

    /**
     * Instantiates a new default configuration.
     */
    public DefaultConfig() {
        super();

        options().header("Main configuration file for NoCheatPlus. Read \"Instructions.txt\".");

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
        set(ConfPaths.LOGGING_DEBUGMESSAGES, false);
        set(ConfPaths.LOGGING_LOGTOFILE, true);
        set(ConfPaths.LOGGING_LOGTOCONSOLE, true);
        set(ConfPaths.LOGGING_LOGTOINGAMECHAT, true);

        /*
         *     e   e     ,e,                        888 888                                                   
         *    d8b d8b     "   dP"Y  e88'888  ,e e,  888 888  ,"Y88b 888 8e   ,e e,   e88 88e  8888 8888  dP"Y 
         *   e Y8b Y8b   888 C88b  d888  '8 d88 88b 888 888 "8" 888 888 88b d88 88b d888 888b 8888 8888 C88b  
         *  d8b Y8b Y8b  888  Y88D Y888   , 888   , 888 888 ,ee 888 888 888 888   , Y888 888P Y888 888P  Y88D 
         * d888b Y8b Y8b 888 d,dP   "88,e8'  "YeeP" 888 888 "88 888 888 888  "YeeP"  "88 88"   "88 88"  d,dP  
         */
        set(ConfPaths.MISCELLANEOUS_ALLOWCLIENTMODS, false);
        set(ConfPaths.MISCELLANEOUS_PROTECTPLUGINS, true);

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
        set(ConfPaths.BLOCKBREAK_FASTBREAK_EXPERIMENTAL, true);
        set(ConfPaths.BLOCKBREAK_FASTBREAK_INTERVAL, 100);
        set(ConfPaths.BLOCKBREAK_FASTBREAK_ACTIONS, "cancel vl>100 log:fastbreak:3:5:cif cancel");

        set(ConfPaths.BLOCKBREAK_NOSWING_CHECK, true);
        set(ConfPaths.BLOCKBREAK_NOSWING_ACTIONS, "log:noswing:3:2:if cancel");

        set(ConfPaths.BLOCKBREAK_REACH_CHECK, true);
        set(ConfPaths.BLOCKBREAK_REACH_ACTIONS, "cancel vl>5 log:breach:0:2:if cancel");

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
        set(ConfPaths.BLOCKPLACE_FASTPLACE_EXPERIMENTAL, true);
        set(ConfPaths.BLOCKPLACE_FASTPLACE_INTERVAL, 95L);
        set(ConfPaths.BLOCKPLACE_FASTPLACE_ACTIONS, "cancel vl>100 log:fastplace:3:5:cif cancel");

        set(ConfPaths.BLOCKPLACE_REACH_CHECK, true);
        set(ConfPaths.BLOCKPLACE_REACH_ACTIONS, "cancel vl>5 log:breach:0:2:if cancel");

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
        set(ConfPaths.CHAT_ARRIVALS_CHECK, false);
        set(ConfPaths.CHAT_ARRIVALS_JOINSLIMIT, 3);
        set(ConfPaths.CHAT_ARRIVALS_MESSAGE, "Please try again later!");
        set(ConfPaths.CHAT_ARRIVALS_TIMELIMIT, 5000L);
        set(ConfPaths.CHAT_ARRIVALS_ACTIONS, "cancel");

        set(ConfPaths.CHAT_COLOR_CHECK, true);
        set(ConfPaths.CHAT_COLOR_ACTIONS, "log:color:0:1:if cancel");

        set(ConfPaths.CHAT_NOPWNAGE_CHECK, true);
        set(ConfPaths.CHAT_NOPWNAGE_LEVEL, 800);
        set(ConfPaths.CHAT_NOPWNAGE_KICKMESSAGE, "You're not allowed to spam this server!");

        set(ConfPaths.CHAT_NOPWNAGE_BANNED_CHECK, true);
        set(ConfPaths.CHAT_NOPWNAGE_BANNED_TIMEOUT, 5000L);
        set(ConfPaths.CHAT_NOPWNAGE_BANNED_WEIGHT, 100);

        set(ConfPaths.CHAT_NOPWNAGE_CAPTCHA_CHECK, true);
        set(ConfPaths.CHAT_NOPWNAGE_CAPTCHA_CHARACTERS,
                "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890");
        set(ConfPaths.CHAT_NOPWNAGE_CAPTCHA_LENGTH, 4);
        set(ConfPaths.CHAT_NOPWNAGE_CAPTCHA_QUESTION,
                "&cPlease type '&6[captcha]&c' to continue sending messages/commands.");
        set(ConfPaths.CHAT_NOPWNAGE_CAPTCHA_SUCCESS, "&aOK, it sounds like you're not a spambot.");
        set(ConfPaths.CHAT_NOPWNAGE_CAPTCHA_TRIES, 20);

        set(ConfPaths.CHAT_NOPWNAGE_FIRST_CHECK, true);
        set(ConfPaths.CHAT_NOPWNAGE_FIRST_TIMEOUT, 3000L);
        set(ConfPaths.CHAT_NOPWNAGE_FIRST_WEIGHT, 200);

        set(ConfPaths.CHAT_NOPWNAGE_GLOBAL_CHECK, true);
        set(ConfPaths.CHAT_NOPWNAGE_GLOBAL_TIMEOUT, 5000L);
        set(ConfPaths.CHAT_NOPWNAGE_GLOBAL_WEIGHT, 100);

        set(ConfPaths.CHAT_NOPWNAGE_MOVE_CHECK, true);
        set(ConfPaths.CHAT_NOPWNAGE_MOVE_TIMEOUT, 30000L);
        set(ConfPaths.CHAT_NOPWNAGE_MOVE_WEIGHT_BONUS, 200);
        set(ConfPaths.CHAT_NOPWNAGE_MOVE_WEIGHT_MALUS, 200);

        set(ConfPaths.CHAT_NOPWNAGE_RELOGIN_CHECK, true);
        set(ConfPaths.CHAT_NOPWNAGE_RELOGIN_TIMEOUT, 1500L);

        set(ConfPaths.CHAT_NOPWNAGE_RELOGIN_WARNING_MESSAGE,
                "&cYou relogged really fast! If you keep doing that, you're going to be banned.");
        set(ConfPaths.CHAT_NOPWNAGE_RELOGIN_WARNING_NUMBER, 1);
        set(ConfPaths.CHAT_NOPWNAGE_RELOGIN_KICKMESSAGE, "Please try again later!");
        set(ConfPaths.CHAT_NOPWNAGE_RELOGIN_WARNING_TIMEOUT, 60000L);

        set(ConfPaths.CHAT_NOPWNAGE_REPEAT_CHECK, true);
        set(ConfPaths.CHAT_NOPWNAGE_REPEAT_TIMEOUT, 5000L);
        set(ConfPaths.CHAT_NOPWNAGE_REPEAT_WEIGHT, 150);

        set(ConfPaths.CHAT_NOPWNAGE_SPEED_CHECK, true);
        set(ConfPaths.CHAT_NOPWNAGE_SPEED_TIMEOUT, 500L);
        set(ConfPaths.CHAT_NOPWNAGE_SPEED_WEIGHT, 200);

        set(ConfPaths.CHAT_NOPWNAGE_WARN_LEVEL, 400);
        set(ConfPaths.CHAT_NOPWNAGE_WARN_TIMEOUT, 30000L);

        set(ConfPaths.CHAT_NOPWNAGE_WARN_OTHERS_CHECK, false);
        set(ConfPaths.CHAT_NOPWNAGE_WARN_OTHERS_MESSAGE, "&cPlease do not say anything similar to what [player] said!");

        set(ConfPaths.CHAT_NOPWNAGE_WARN_PLAYER_CHECK, false);
        set(ConfPaths.CHAT_NOPWNAGE_WARN_PLAYER_MESSAGE,
                "&cOur system has detected unusual bot activities coming from you. Please be careful with what you say. DON'T repeat what you just said either, unless you want to be banned.");

        set(ConfPaths.CHAT_NOPWNAGE_ACTIONS, "cancel log:nopwnage:2:5:cf cmd:ban cmd:ban-ip");

        /*
         * 888'Y88 ,e,          888       d8   
         * 888 ,'Y  "   e88 888 888 ee   d88   
         * 888C8   888 d888 888 888 88b d88888 
         * 888 "   888 Y888 888 888 888  888   
         * 888     888  "88 888 888 888  888   
         *               ,  88P                
         *              "8",P"                 
         */
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
        set(ConfPaths.FIGHT_GODMODE_ACTIONS, "log:godmode:2:5:if cancel");

        set(ConfPaths.FIGHT_INSTANTHEAL_CHECK, true);
        set(ConfPaths.FIGHT_INSTANTHEAL_ACTIONS, "log:instantheal:1:1:if cancel");

        set(ConfPaths.FIGHT_KNOCKBACK_CHECK, true);
        set(ConfPaths.FIGHT_KNOCKBACK_INTERVAL, 50L);
        set(ConfPaths.FIGHT_KNOCKBACK_ACTIONS, "cancel vl>50 log:knockback:0:5:cif cancel");

        set(ConfPaths.FIGHT_NOSWING_CHECK, true);
        set(ConfPaths.FIGHT_NOSWING_ACTIONS, "log:noswing:0:5:cif cancel");

        set(ConfPaths.FIGHT_REACH_CHECK, true);
        set(ConfPaths.FIGHT_REACH_PENALTY, 500);
        set(ConfPaths.FIGHT_REACH_ACTIONS, "cancel vl>10 log:freach:2:5:if cancel");

        set(ConfPaths.FIGHT_SPEED_CHECK, true);
        set(ConfPaths.FIGHT_SPEED_LIMIT, 15);
        set(ConfPaths.FIGHT_SPEED_ACTIONS, "log:fspeed:0:5:if cancel");

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
        set(ConfPaths.MOVING_CREATIVEFLY_HORIZONTALSPEED, 100);
        set(ConfPaths.MOVING_CREATIVEFLY_MAXHEIGHT, 128);
        set(ConfPaths.MOVING_CREATIVEFLY_VERTICALSPEED, 100);
        set(ConfPaths.MOVING_CREATIVEFLY_ACTIONS,
                "log:flyshort:3:5:f cancel vl>100 log:flyshort:0:5:if cancel vl>400 log:flylong:0:5:cif cancel");

        set(ConfPaths.MOVING_MOREPACKETS_CHECK, true);
        set(ConfPaths.MOVING_MOREPACKETS_ACTIONS, "log:morepackets:3:2:if cancel vl>20 log:morepackets:0:2:if cancel");

        set(ConfPaths.MOVING_MOREPACKETSVEHICLE_CHECK, true);
        set(ConfPaths.MOVING_MOREPACKETSVEHICLE_ACTIONS,
                "log:morepackets:3:2:if cancel vl>20 log:morepackets:0:2:if cancel");

        set(ConfPaths.MOVING_NOFALL_CHECK, true);
        set(ConfPaths.MOVING_NOFALL_AGGRESSIVE, true);
        set(ConfPaths.MOVING_NOFALL_ACTIONS, "log:nofall:0:5:cif cancel");

        set(ConfPaths.MOVING_SURVIVALFLY_CHECK, true);
        set(ConfPaths.MOVING_SURVIVALFLY_ALLOWFASTBLOCKING, false);
        set(ConfPaths.MOVING_SURVIVALFLY_ALLOWFASTSNEAKING, false);
        // The settings aren't enabled by default. Simply write them yourself in the configuration file.
        // set(ConfPaths.MOVING_SURVIVALFLY_BLOCKINGSPEED, 100);
        // set(ConfPaths.MOVING_SURVIVALFLY_COBWEBSPEED, 100);
        // set(ConfPaths.MOVING_SURVIVALFLY_LADDERSPEED, 100);
        // set(ConfPaths.MOVING_SURVIVALFLY_LAVASPEED, 100);
        // set(ConfPaths.MOVING_SURVIVALFLY_MOVESPEED, 100);
        // set(ConfPaths.MOVING_SURVIVALFLY_SNEAKINGSPEED, 100);
        // set(ConfPaths.MOVING_SURVIVALFLY_SOULSANDSPEED, 100);
        // set(ConfPaths.MOVING_SURVIVALFLY_SPRINTINGSPEED, 100);
        // set(ConfPaths.MOVING_SURVIVALFLY_WATERSPEED, 100);
        set(ConfPaths.MOVING_SURVIVALFLY_ACTIONS,
                "log:flyshort:3:5:f cancel vl>100 log:flyshort:0:5:if cancel vl>400 log:flylong:0:5:cif cancel");

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
        set(ConfPaths.STRINGS + ".angle", start + "tried to hit multiple entities at the same time" + end);
        set(ConfPaths.STRINGS + ".ban", "ban [player]");
        set(ConfPaths.STRINGS + ".ban-ip", "ban-ip [ip]");
        set(ConfPaths.STRINGS + ".bdirection", start + "tried to interact with a block out of his line of sight" + end);
        set(ConfPaths.STRINGS + ".bpspeed", start + "tried to throw projectiles too quickly" + end);
        set(ConfPaths.STRINGS + ".breach", start
                + "tried to interact with a block over distance [reachdistance] block(s)" + end);
        set(ConfPaths.STRINGS + ".critical", start + "tried to do a critical hit but wasn't technically jumping" + end);
        set(ConfPaths.STRINGS + ".fastbreak", start + "tried to break too much blocks" + end);
        set(ConfPaths.STRINGS + ".fastplace", start + "tried to place too much blocks" + end);
        set(ConfPaths.STRINGS + ".fdirection", start + "tried to hit an entity out of line of sight" + end);
        set(ConfPaths.STRINGS + ".flyshort", start + "tried to move unexpectedly" + end);
        set(ConfPaths.STRINGS + ".flylong", start
                + "tried to move from [locationfrom] to [locationto] over a distance of [distance] block(s)" + end);
        set(ConfPaths.STRINGS + ".freach", start + "tried to attack entity out of reach" + end);
        set(ConfPaths.STRINGS + ".fspeed", start + "tried to attack more than [attackslimit] times per second" + end);
        set(ConfPaths.STRINGS + ".godmode", start + "avoided taking damage or lagging" + end);
        set(ConfPaths.STRINGS + ".instantheal", start + "tried to regenerate health faster than normal" + end);
        set(ConfPaths.STRINGS + ".kick", "kick [player]");
        set(ConfPaths.STRINGS + ".knockback", start + "tried to do a knockback but wasn't technically sprinting" + end);
        set(ConfPaths.STRINGS + ".morepackets", start + "sent [packets] more packet(s) than expected" + end);
        set(ConfPaths.STRINGS + ".nofall", start + "tried to avoid fall damage for ~[falldistance] block(s)" + end);
        set(ConfPaths.STRINGS + ".nopwnage", start + "acted like a spambot (IP: [ip])" + end);
        set(ConfPaths.STRINGS + ".noswing", start + "didn't swing arm" + end);

        // Update internal factory based on all the new entries to the "actions" section.
        regenerateActionLists();
    }
}
