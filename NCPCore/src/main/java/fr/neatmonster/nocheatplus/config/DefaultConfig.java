package fr.neatmonster.nocheatplus.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

import org.bukkit.Material;

import fr.neatmonster.nocheatplus.compat.BridgeMisc;

/**
 * These are the default settings for NoCheatPlus. They will be used in addition to/in replacement of configurations
 * given in the configFactory.yml file.
 */
public class DefaultConfig extends ConfigFile {

    /**
     * NCP build number, for which an existing entry has been changed. (Should
     * only increment, if the user is advised to change to that value instead of
     * the former default one.)
     */
    public static final int buildNumber = 785;

    // TODO: auto input full version or null to an extra variable or several [fail safe for other syntax checking]?

    /**
     * Instantiates a new default configuration.
     */
    public DefaultConfig() {
        super();


        // General.
        set(ConfPaths.SAVEBACKCONFIG, true);

        // Config version.
        set(ConfPaths.CONFIGVERSION_NOTIFY, true);
        //        not set(ConfPaths.CONFIGVERSION_CREATED, -1);
        //        not set(ConfPaths.CONFIGVERSION_SAVED, -1);
        set(ConfPaths.LOGGING_ACTIVE, true);
        set(ConfPaths.LOGGING_MAXQUEUESIZE, 5000);
        set(ConfPaths.LOGGING_EXTENDED_STATUS, false);
        set(ConfPaths.LOGGING_EXTENDED_ALLVIOLATIONS_DEBUGONLY, false);
        set(ConfPaths.LOGGING_EXTENDED_ALLVIOLATIONS_BACKEND_TRACE, false);
        set(ConfPaths.LOGGING_EXTENDED_ALLVIOLATIONS_BACKEND_NOTIFY, false);
        set(ConfPaths.LOGGING_BACKEND_CONSOLE_ACTIVE, true);
        set(ConfPaths.LOGGING_BACKEND_CONSOLE_PREFIX, "[NoCheatPlus] ");
        set(ConfPaths.LOGGING_BACKEND_CONSOLE_ASYNCHRONOUS, true);
        set(ConfPaths.LOGGING_BACKEND_FILE_ACTIVE, true);
        set(ConfPaths.LOGGING_BACKEND_FILE_PREFIX, "");
        set(ConfPaths.LOGGING_BACKEND_FILE_FILENAME, "nocheatplus.log");
        set(ConfPaths.LOGGING_BACKEND_INGAMECHAT_ACTIVE, true);
        set(ConfPaths.LOGGING_BACKEND_INGAMECHAT_PREFIX, "&cNCP: &f");
        set(ConfPaths.LOGGING_BACKEND_INGAMECHAT_SUBSCRIPTIONS, false);

        //        set(ConfPaths.MISCELLANEOUS_CHECKFORUPDATES, true);
        //        set(ConfPaths.MISCELLANEOUS_REPORTTOMETRICS, true);

        //        set(ConfPaths.MISCELLANEOUS_NOMOVEDTOOQUICKLY_ENABLED, false);
        //        set(ConfPaths.MISCELLANEOUS_NOMOVEDTOOQUICKLY_USEPROXY, false);

        // Data settings.
        // Expired offline players data.
        set(ConfPaths.DATA_EXPIRATION_ACTIVE, false);
        set(ConfPaths.DATA_EXPIRATION_DURATION, 60);
        set(ConfPaths.DATA_EXPIRATION_HISTORY, false);
        // Consistency checking.
        set(ConfPaths.DATA_CONSISTENCYCHECKS_CHECK, true);
        set(ConfPaths.DATA_CONSISTENCYCHECKS_INTERVAL, 10);
        set(ConfPaths.DATA_CONSISTENCYCHECKS_MAXTIME, 2);
        set(ConfPaths.DATA_CONSISTENCYCHECKS_SUPPRESSWARNINGS, false);

        // Protection features.
        // Hide plugins.
        set(ConfPaths.PROTECT_PLUGINS_HIDE_ACTIVE, true);
        set(ConfPaths.PROTECT_PLUGINS_HIDE_NOPERMISSION_MSG, "&cI'm sorry, but you do not have permission to perform this command. Please contact the server administrators if you believe that this is in error.");
        set(ConfPaths.PROTECT_PLUGINS_HIDE_NOPERMISSION_CMDS, Arrays.asList("plugins", "version", "icanhasbukkit"));
        set(ConfPaths.PROTECT_PLUGINS_HIDE_NOCOMMAND_MSG, "Unknown command. Type \"/help\" for help.");
        set(ConfPaths.PROTECT_PLUGINS_HIDE_NOCOMMAND_CMDS, new LinkedList<String>());
        // Commands (other).
        set(ConfPaths.PROTECT_COMMANDS_CONSOLEONLY_ACTIVE, false);
        set(ConfPaths.PROTECT_COMMANDS_CONSOLEONLY_MSG, "&cI'm sorry, but this command can't be executed in chat. Use the console instead!");
        set(ConfPaths.PROTECT_COMMANDS_CONSOLEONLY_CMDS, Arrays.asList("op", "deop"));
        // Client motd.
        set(ConfPaths.PROTECT_CLIENTS_MOTD_ACTIVE, true);
        set(ConfPaths.PROTECT_CLIENTS_MOTD_ALLOWALL, false);

        set(ConfPaths.BLOCKBREAK_DIRECTION_CHECK, true);
        set(ConfPaths.BLOCKBREAK_DIRECTION_ACTIONS, "cancel vl>10 log:bdirection:0:5:if cancel");

        set(ConfPaths.BLOCKBREAK_FASTBREAK_CHECK, true);
        set(ConfPaths.BLOCKBREAK_FASTBREAK_STRICT, true);
        set(ConfPaths.BLOCKBREAK_FASTBREAK_DELAY, 100);
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

        set(ConfPaths.BLOCKINTERACT_DIRECTION_CHECK, true);
        set(ConfPaths.BLOCKINTERACT_DIRECTION_ACTIONS, "cancel vl>10 log:bdirection:0:3:if cancel");

        set(ConfPaths.BLOCKINTERACT_REACH_CHECK, true);
        set(ConfPaths.BLOCKINTERACT_REACH_ACTIONS, "cancel vl>5 log:breach:0:2:if cancel");

        set(ConfPaths.BLOCKINTERACT_SPEED_CHECK, true);
        set(ConfPaths.BLOCKINTERACT_SPEED_INTERVAL, 2000);
        set(ConfPaths.BLOCKINTERACT_SPEED_LIMIT, 60);
        set(ConfPaths.BLOCKINTERACT_SPEED_ACTIONS, "cancel vl>200 log:bspeed:0:2:if cancel vl>1000 cancel log:bspeed:0:2:icf cmd:kickbspeed");

        set(ConfPaths.BLOCKINTERACT_VISIBLE_CHECK, true);
        set(ConfPaths.BLOCKINTERACT_VISIBLE_ACTIONS, "cancel vl>100 log:bvisible:0:10:if cancel");

        // BLOCKPLACE
        set(ConfPaths.BLOCKPLACE_AGAINST_CHECK, true);
        set(ConfPaths.BLOCKPLACE_AGAINST_ACTIONS, "cancel");

        set(ConfPaths.BLOCKPLACE_AUTOSIGN_CHECK, true);
        set(ConfPaths.BLOCKPLACE_AUTOSIGN_SKIPEMPTY, false);
        set(ConfPaths.BLOCKPLACE_AUTOSIGN_ACTIONS, "cancel vl>10 log:bautosign:0:3:if cancel");

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
        set(ConfPaths.BLOCKPLACE_NOSWING_EXCEPTIONS, Arrays.asList(Material.WATER_LILY.toString(), Material.FLINT_AND_STEEL.toString()));
        set(ConfPaths.BLOCKPLACE_NOSWING_ACTIONS, "cancel vl>10 log:noswing:0:5:if cancel");

        set(ConfPaths.BLOCKPLACE_SPEED_CHECK, true);
        set(ConfPaths.BLOCKPLACE_SPEED_INTERVAL, 45L);
        set(ConfPaths.BLOCKPLACE_SPEED_ACTIONS,
                "cancel vl>150 log:bpspeed:3:5:if cancel vl>1000 log:bpspeed:3:5:cif cancel");

        set(ConfPaths.CHAT_COLOR_CHECK, true);
        set(ConfPaths.CHAT_COLOR_ACTIONS, "log:color:0:1:if cancel");


        set(ConfPaths.CHAT_COMMANDS_CHECK, true);
        set(ConfPaths.CHAT_COMMANDS_EXCLUSIONS, new ArrayList<String>());
        set(ConfPaths.CHAT_COMMANDS_HANDLEASCHAT, Arrays.asList("me"));
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
        set(ConfPaths.CHAT_TEXT_ALLOWVLRESET, true);
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
        set(ConfPaths.CHAT_LOGINS_STARTUPDELAY, 600);
        set(ConfPaths.CHAT_LOGINS_PERWORLDCOUNT, false);
        set(ConfPaths.CHAT_LOGINS_SECONDS, 10);
        set(ConfPaths.CHAT_LOGINS_LIMIT, 10);
        set(ConfPaths.CHAT_LOGINS_KICKMESSAGE, "Too many people logging in, retry soon.");

        /*
         * Combined !
         */

        set(ConfPaths.COMBINED_BEDLEAVE_CHECK, true);
        set(ConfPaths.COMBINED_BEDLEAVE_ACTIONS, "cancel log:bedleave:0:5:if cmd:kickbedleave");

        set(ConfPaths.COMBINED_ENDERPEARL_CHECK, true);
        set(ConfPaths.COMBINED_ENDERPEARL_PREVENTCLICKBLOCK, true);

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

        // FIGHT
        set(ConfPaths.FIGHT_CANCELDEAD, true);
        set(ConfPaths.FIGHT_TOOLCHANGEPENALTY, 500L);
        set(ConfPaths.FIGHT_PVP_KNOCKBACKVELOCITY, "default");

        set(ConfPaths.FIGHT_YAWRATE_CHECK, true);

        set(ConfPaths.FIGHT_ANGLE_CHECK, true);
        set(ConfPaths.FIGHT_ANGLE_THRESHOLD, 50);
        set(ConfPaths.FIGHT_ANGLE_ACTIONS, "cancel vl>100 log:angle:3:5:f cancel vl>250 log:angle:0:5:cif cancel");

        set(ConfPaths.FIGHT_CRITICAL_CHECK, true);
        set(ConfPaths.FIGHT_CRITICAL_FALLDISTANCE, 0.06251D);
        set(ConfPaths.FIGHT_CRITICAL_ACTIONS, "cancel vl>50 log:critical:0:5:cif cancel");

        set(ConfPaths.FIGHT_DIRECTION_CHECK, true);
        set(ConfPaths.FIGHT_DIRECTION_STRICT, false);
        set(ConfPaths.FIGHT_DIRECTION_PENALTY, 500L);
        set(ConfPaths.FIGHT_DIRECTION_ACTIONS,
                "cancel vl>5 log:fdirection:3:5:f cancel vl>20 log:fdirection:0:5:if cancel vl>50 log:fdirection:0:5:cif cancel");

        set(ConfPaths.FIGHT_FASTHEAL_CHECK, true);
        set(ConfPaths.FIGHT_FASTHEAL_INTERVAL, 4000L);
        set(ConfPaths.FIGHT_FASTHEAL_BUFFER, 1000L);
        set(ConfPaths.FIGHT_FASTHEAL_ACTIONS, "cancel vl>10 cancel log:fastheal:0:10:i vl>30 cancel log:fastheal:0:10:if");

        set(ConfPaths.FIGHT_GODMODE_CHECK, true);
        set(ConfPaths.FIGHT_GODMODE_LAGMINAGE, 1100); // TODO: ndt/2 => 500-600.
        set(ConfPaths.FIGHT_GODMODE_LAGMAXAGE, 5000);
        set(ConfPaths.FIGHT_GODMODE_ACTIONS, "log:godmode:2:5:if cancel vl>60 log:godmode:2:5:icf cancel"); // cmd:kickgod");

        set(ConfPaths.FIGHT_NOSWING_CHECK, true);
        set(ConfPaths.FIGHT_NOSWING_ACTIONS, "cancel vl>10 log:noswing:0:5:if cancel");

        set(ConfPaths.FIGHT_REACH_CHECK, true);
        set(ConfPaths.FIGHT_REACH_SURVIVALDISTANCE, 4.4);
        set(ConfPaths.FIGHT_REACH_PENALTY, 500);
        set(ConfPaths.FIGHT_REACH_REDUCE, true);
        set(ConfPaths.FIGHT_REACH_REDUCEDISTANCE, 0.9);
        set(ConfPaths.FIGHT_REACH_REDUCESTEP, 0.15);
        set(ConfPaths.FIGHT_REACH_ACTIONS, "cancel vl>10 log:freach:2:5:if cancel");

        set(ConfPaths.FIGHT_SELFHIT_CHECK, true);
        set(ConfPaths.FIGHT_SELFHIT_ACTIONS, "log:fselfhit:0:5:if cancel vl>10 log:fselfhit:0:5:icf cancel cmd:kickselfhit");

        set(ConfPaths.FIGHT_SPEED_CHECK, true);
        set(ConfPaths.FIGHT_SPEED_LIMIT, 15);
        set(ConfPaths.FIGHT_SPEED_ACTIONS, "log:fspeed:0:5:if cancel");
        set(ConfPaths.FIGHT_SPEED_SHORTTERM_TICKS, 7);
        set(ConfPaths.FIGHT_SPEED_SHORTTERM_LIMIT, 6);

        set(ConfPaths.INVENTORY_DROP_CHECK, true);
        set(ConfPaths.INVENTORY_DROP_LIMIT, 100);
        set(ConfPaths.INVENTORY_DROP_TIMEFRAME, 20L);
        set(ConfPaths.INVENTORY_DROP_ACTIONS, "log:drop:0:1:cif cancel cmd:dropkick:0:1");

        set(ConfPaths.INVENTORY_FASTCLICK_CHECK, true);
        set(ConfPaths.INVENTORY_FASTCLICK_SPARECREATIVE, true);
        set(ConfPaths.INVENTORY_FASTCLICK_TWEAKS1_5, true);
        set(ConfPaths.INVENTORY_FASTCLICK_LIMIT_SHORTTERM, 4);
        set(ConfPaths.INVENTORY_FASTCLICK_LIMIT_NORMAL, 15);
        set(ConfPaths.INVENTORY_FASTCLICK_ACTIONS, "cancel vl>50 log:fastclick:3:5:cif cancel");

        set(ConfPaths.INVENTORY_INSTANTBOW_CHECK, true);
        set(ConfPaths.INVENTORY_INSTANTBOW_STRICT, true);
        set(ConfPaths.INVENTORY_INSTANTBOW_DELAY, 130);
        set(ConfPaths.INVENTORY_INSTANTBOW_ACTIONS, "cancel vl>15 log:instantbow:2:5:if cancel");

        set(ConfPaths.INVENTORY_INSTANTEAT_CHECK, true);
        set(ConfPaths.INVENTORY_INSTANTEAT_ACTIONS, "log:instanteat:2:5:if cancel");

        set(ConfPaths.INVENTORY_FASTCONSUME_CHECK, true);
        set(ConfPaths.INVENTORY_FASTCONSUME_DURATION, 0.7);
        set(ConfPaths.INVENTORY_FASTCONSUME_WHITELIST, false);
        set(ConfPaths.INVENTORY_FASTCONSUME_ITEMS, new LinkedList<String>());
        set(ConfPaths.INVENTORY_FASTCONSUME_ACTIONS, "log:fastconsume:2:5:if cancel");

        set(ConfPaths.INVENTORY_GUTENBERG_CHECK, true);
        set(ConfPaths.INVENTORY_GUTENBERG_ACTIONS, "cancel log:gutenberg:0:10:icf cmd:kickinvaliddata");

        set(ConfPaths.INVENTORY_ITEMS_CHECK, true);

        set(ConfPaths.INVENTORY_OPEN_CHECK, true);
        set(ConfPaths.INVENTORY_OPEN_CLOSE, true);
        set(ConfPaths.INVENTORY_OPEN_CANCELOTHER, true);

        // MOVING
        set(ConfPaths.MOVING_CREATIVEFLY_CHECK, true);
        set(ConfPaths.MOVING_CREATIVEFLY_IGNOREALLOWFLIGHT, false); // TODO: -> true ?
        set(ConfPaths.MOVING_CREATIVEFLY_IGNORECREATIVE, false); // TODO: -> true ?
        set(ConfPaths.MOVING_CREATIVEFLY_MODEL + "creative." + ConfPaths.SUB_HORIZONTALSPEED, 100);
        set(ConfPaths.MOVING_CREATIVEFLY_MODEL + "creative." + ConfPaths.SUB_VERTICALSPEED, 100);
        set(ConfPaths.MOVING_CREATIVEFLY_MODEL + "creative." + ConfPaths.SUB_MAXHEIGHT, 128);
        if (BridgeMisc.GAME_MODE_SPECTATOR != null) {
            set(ConfPaths.MOVING_CREATIVEFLY_MODEL + "spectator." + ConfPaths.SUB_HORIZONTALSPEED, 400);
            set(ConfPaths.MOVING_CREATIVEFLY_MODEL + "spectator." + ConfPaths.SUB_VERTICALSPEED, 100);
            set(ConfPaths.MOVING_CREATIVEFLY_MODEL + "spectator." + ConfPaths.SUB_MAXHEIGHT, 128);
        }
        set(ConfPaths.MOVING_CREATIVEFLY_ACTIONS,
                "log:flyshort:3:5:f cancel vl>100 log:flyshort:0:5:if cancel vl>400 log:flylong:0:5:cif cancel");

        set(ConfPaths.MOVING_MOREPACKETS_CHECK, true);
        set(ConfPaths.MOVING_MOREPACKETS_SECONDS, 6);
        set(ConfPaths.MOVING_MOREPACKETS_EPSIDEAL, 20);
        set(ConfPaths.MOVING_MOREPACKETS_EPSMAX, 22);
        set(ConfPaths.MOVING_MOREPACKETS_BURST_PACKETS, 40);
        set(ConfPaths.MOVING_MOREPACKETS_BURST_DIRECT, 60);
        set(ConfPaths.MOVING_MOREPACKETS_BURST_EPM, 180);
        set(ConfPaths.MOVING_MOREPACKETS_ACTIONS, "cancel vl>10 log:morepackets:0:2:if cancel vl>100 log:morepackets:0:2:if cancel cmd:kickpackets");

        set(ConfPaths.MOVING_MOREPACKETSVEHICLE_CHECK, true);
        set(ConfPaths.MOVING_MOREPACKETSVEHICLE_ACTIONS,
                "cancel vl>10 log:morepackets:0:2:if cancel");

        set(ConfPaths.MOVING_NOFALL_CHECK, true);
        set(ConfPaths.MOVING_NOFALL_DEALDAMAGE, true);
        set(ConfPaths.MOVING_NOFALL_RESETONVL, false);
        set(ConfPaths.MOVING_NOFALL_RESETONTP, false);
        set(ConfPaths.MOVING_NOFALL_RESETONVEHICLE, true);
        set(ConfPaths.MOVING_NOFALL_ANTICRITICALS, true);
        set(ConfPaths.MOVING_NOFALL_ACTIONS, "log:nofall:0:5:if cancel vl>30 log:nofall:0:5:icf cancel");

        set(ConfPaths.MOVING_PASSABLE_CHECK, true);
        set(ConfPaths.MOVING_PASSABLE_RAYTRACING_CHECK, true);
        set(ConfPaths.MOVING_PASSABLE_RAYTRACING_BLOCKCHANGEONLY, false);
        set(ConfPaths.MOVING_PASSABLE_ACTIONS, "cancel vl>10 log:passable:0:5:if cancel vl>50 log:passable:0:5:icf cancel");
        set(ConfPaths.MOVING_PASSABLE_UNTRACKED_TELEPORT_ACTIVE, true);
        set(ConfPaths.MOVING_PASSABLE_UNTRACKED_CMD_ACTIVE, true);
        set(ConfPaths.MOVING_PASSABLE_UNTRACKED_CMD_TRYTELEPORT, true);
        set(ConfPaths.MOVING_PASSABLE_UNTRACKED_CMD_PREFIXES, Arrays.asList("sethome", "home set", "setwarp", "warp set", "setback", "set back", "back set"));

        set(ConfPaths.MOVING_SURVIVALFLY_CHECK, true);
        //        set(ConfPaths.MOVING_SURVIVALFLY_EXTENDED_HACC, false);
        set(ConfPaths.MOVING_SURVIVALFLY_EXTENDED_VACC, true);
        set(ConfPaths.MOVING_SURVIVALFLY_FALLDAMAGE, true);
        set(ConfPaths.MOVING_SURVIVALFLY_BEDSTEP, "default");
        // The settings aren't enabled by default. Simply write them yourself in the configuration file.
        // set(ConfPaths.MOVING_SURVIVALFLY_BLOCKINGSPEED, 100);
        // set(ConfPaths.MOVING_SURVIVALFLY_SNEAKINGSPEED, 100);
        // set(ConfPaths.MOVING_SURVIVALFLY_SPEEDINGSPEED, 200);
        // set(ConfPaths.MOVING_SURVIVALFLY_SPRINTINGSPEED, 100);
        // set(ConfPaths.MOVING_SURVIVALFLY_SWIMMINGSPEED, 100);
        // set(ConfPaths.MOVING_SURVIVALFLY_WALKINGSPEED, 100);
        set(ConfPaths.MOVING_SURVIVALFLY_ACTIONS, "log:flyshort:3:10:f cancel vl>100 log:flyshort:0:10:if cancel vl>400 log:flylong:0:5:cif cancel vl>1500 log:flylong:0:5:cif cancel cmd:kickfly");

        // sf / hover check.
        set(ConfPaths.MOVING_SURVIVALFLY_HOVER_CHECK, true);
        set(ConfPaths.MOVING_SURVIVALFLY_HOVER_STEP, 5);
        set(ConfPaths.MOVING_SURVIVALFLY_HOVER_TICKS, 85);
        set(ConfPaths.MOVING_SURVIVALFLY_HOVER_LOGINTICKS, 0);
        set(ConfPaths.MOVING_SURVIVALFLY_HOVER_FALLDAMAGE, true);
        set(ConfPaths.MOVING_SURVIVALFLY_HOVER_SFVIOLATION, 500);

        // Moving Trace
        set(ConfPaths.MOVING_TRACE_SIZE, 60);
        set(ConfPaths.MOVING_TRACE_MERGEDIST, 0.9752); // Let all the hackers read code!

        // Vehicles.
        set(ConfPaths.MOVING_VEHICLES_PREVENTDESTROYOWN, true);
        set(ConfPaths.MOVING_VEHICLES_ENFORCELOCATION, "default");

        // Velocity.
        set(ConfPaths.MOVING_VELOCITY_GRACETICKS, 20);
        set(ConfPaths.MOVING_VELOCITY_ACTIVATIONCOUNTER, 80);
        set(ConfPaths.MOVING_VELOCITY_ACTIVATIONTICKS, 140);
        set(ConfPaths.MOVING_VELOCITY_STRICTINVALIDATION, true);

        // General.
        set(ConfPaths.MOVING_IGNORESTANCE, "default");
        set(ConfPaths.MOVING_TEMPKICKILLEGAL, true);
        set(ConfPaths.MOVING_LOADCHUNKS_JOIN, true);
        set(ConfPaths.MOVING_SPRINTINGGRACE, 2.0);
        set(ConfPaths.MOVING_ASSUMESPRINT, true);
        set(ConfPaths.MOVING_SPEEDGRACE, 4.0);
        set(ConfPaths.MOVING_ENFORCELOCATION, "default");

        // NET

        // FlyingFrequency
        set(ConfPaths.NET_FLYINGFREQUENCY_ACTIVE, true);
        set(ConfPaths.NET_FLYINGFREQUENCY_SECONDS, 5);
        set(ConfPaths.NET_FLYINGFREQUENCY_PACKETSPERSECOND, 60);
        set(ConfPaths.NET_FLYINGFREQUENCY_ACTIONS, "cancel"); // TODO: Log actions.
        set(ConfPaths.NET_FLYINGFREQUENCY_REDUNDANT_ACTIVE, true);
        set(ConfPaths.NET_FLYINGFREQUENCY_REDUNDANT_SECONDS, 3);
        set(ConfPaths.NET_FLYINGFREQUENCY_REDUNDANT_ACTIONS, "cancel"); // TODO: Log actions.

        // KeepAliveFrequency
        set(ConfPaths.NET_KEEPALIVEFREQUENCY_ACTIVE, true);
        set(ConfPaths.NET_KEEPALIVEFREQUENCY_ACTIONS, "cancel vl>10 cancel log:keepalive:0:10:if vl>40 cancel log:keepalive:0:10:icf vl>100 cancel log:keepalive:0:10:icf cmd:kickalive");

        // SoundDistance
        set(ConfPaths.NET_SOUNDDISTANCE_ACTIVE, true);
        set(ConfPaths.NET_SOUNDDISTANCE_MAXDISTANCE, 320);


        // TODO: An extra file might suit these.
        final String start = "[player] failed [check]: ";
        final String end = ". VL [violations].";
        final String tell = "ncp tell [player] ";
        set(ConfPaths.STRINGS + ".angle", start + "tried to hit multiple entities at the same time" + end);
        set(ConfPaths.STRINGS + ".ban", "ban [player]");
        set(ConfPaths.STRINGS + ".ban-ip", "ban-ip [ip]");
        set(ConfPaths.STRINGS + ".bautosign", start + "failed autosign with [tags]" + end);
        set(ConfPaths.STRINGS + ".bbfrequency", start + "tried to break too many blocks within time frame" + end);
        set(ConfPaths.STRINGS + ".bdirection", start + "tried to interact with a block out of their line of sight" + end);
        set(ConfPaths.STRINGS + ".bedleave", start + "sends bed leave packets (was not in bed)" + end);
        set(ConfPaths.STRINGS + ".bpspeed", start + "tried to throw projectiles too quickly" + end);
        set(ConfPaths.STRINGS + ".breach", start + "exceeds block-interact distance ([reachdistance])" + end);
        set(ConfPaths.STRINGS + ".bspeed", start + "interacts too fast" + end);
        set(ConfPaths.STRINGS + ".bvisible", start + "interacts with a block out of sight" + end);
        set(ConfPaths.STRINGS + ".bwrong", start + "broke another block than clicked" + end);
        set(ConfPaths.STRINGS + ".captcha", "[player] failed captcha repeatedly" + end);
        set(ConfPaths.STRINGS + ".chatnormal", start + "potentially annoying chat" + end);
        set(ConfPaths.STRINGS + ".color", start + "sent colored chat message" + end);
        set(ConfPaths.STRINGS + ".commands", start + "issued too many commands" + end);
        set(ConfPaths.STRINGS + ".combspeed", start + "performs different actions at very high speed" + end);
        set(ConfPaths.STRINGS + ".critical", start + "tried to do a critical hit but wasn't technically jumping [tags]" + end);
        set(ConfPaths.STRINGS + ".drop", start + "tried to drop more items than allowed" + end);
        set(ConfPaths.STRINGS + ".dropkick", "ncp delay ncp kick [player] Dropping items too fast.");
        set(ConfPaths.STRINGS + ".fastbreak", start + "tried to break blocks ([blockid]) faster than possible" + end);
        set(ConfPaths.STRINGS + ".fastclick", start + "tried to move items in their inventory too quickly" + end);
        set(ConfPaths.STRINGS + ".fastconsume", start + "consumes [food] [tags] too fast" + end);
        set(ConfPaths.STRINGS + ".fastheal", start + "regenerates health faster than usual" + end);
        set(ConfPaths.STRINGS + ".fastplace", start + "tried to place too many blocks" + end);
        set(ConfPaths.STRINGS + ".fdirection", start + "tried to hit an entity out of line of sight" + end);
        set(ConfPaths.STRINGS + ".flyshort", start + "tried to move unexpectedly" + end);
        set(ConfPaths.STRINGS + ".flylong", start
                + "tried to move from [locationfrom] to [locationto] over a distance of [distance] block(s)" + end);
        set(ConfPaths.STRINGS + ".freach", start + "tried to attack entity out of reach" + end);
        set(ConfPaths.STRINGS + ".fselfhit", start + "tried to self-hit" + end);
        set(ConfPaths.STRINGS + ".fspeed", start + "tried to attack with too high a frequency" + end);
        set(ConfPaths.STRINGS + ".gutenberg", start + "created a book with too many pages" + end);
        set(ConfPaths.STRINGS + ".godmode", start + "avoided taking damage or lagging" + end);
        set(ConfPaths.STRINGS + ".improbable", start + "meets the improbable more than expected" + end);
        set(ConfPaths.STRINGS + ".instantbow", start + "fires bow too fast" + end);
        set(ConfPaths.STRINGS + ".instanteat", start + "eats food [food] too fast" + end);
        set(ConfPaths.STRINGS + ".keepalive", start + "spams keep-alive packets (god/freecam?)" + end);
        set(ConfPaths.STRINGS + ".kick", "kick [player]");
        set(ConfPaths.STRINGS + ".kickalive", "ncp kick [player] Too many keep-alive packets.");
        set(ConfPaths.STRINGS + ".kickbedleave", "ncp delay ncp kick [player] Go find a bed!");
        set(ConfPaths.STRINGS + ".kickbspeed", "ncp kick [player] You interacted too fast!");
        set(ConfPaths.STRINGS + ".kickcaptcha", "ncp kick [player] Enter the captcha!");
        set(ConfPaths.STRINGS + ".kickchat1", "ncp tempkick [player] 1 You're still not allowed to spam!");
        set(ConfPaths.STRINGS + ".kickchat5", "ncp tempkick [player] 5 You're not intended to spam!");
        set(ConfPaths.STRINGS + ".kickchatfast", "ncp kick [player] You're not allowed to spam in chat!");
        set(ConfPaths.STRINGS + ".kickchatnormal", "ncp kick [player] Too many chat messages, take a break.");
        set(ConfPaths.STRINGS + ".kickcommands", "ncp tempkick [player] 1 You're not allowed to spam commands!");
        set(ConfPaths.STRINGS + ".kickfly", "ncp delay ncp kick [player] Kicked for flying (or related)");
        set(ConfPaths.STRINGS + ".kickfrequency", "ncp kick [player] You did something too fast!");
        set(ConfPaths.STRINGS + ".kickgod", "ncp kick [player] God mode?");
        set(ConfPaths.STRINGS + ".kickinvaliddata", "ncp kick [player] Invalid data.");
        set(ConfPaths.STRINGS + ".kickpackets", "ncp delay ncp kick [player] Too many packets (extreme lag?)");
        set(ConfPaths.STRINGS + ".kickselfhit", "ncp kick [player] You tried to hit yourself!");
        set(ConfPaths.STRINGS + ".kickwb", "ncp kick [player] Block breaking out of sync!");
        set(ConfPaths.STRINGS + ".knockback", start + "tried to do a knockback but wasn't technically sprinting" + end);
        set(ConfPaths.STRINGS + ".morepackets", start + "sent too many moves ([packets] [tags])" + end);
        set(ConfPaths.STRINGS + ".munchhausen", start + "almost made it off the pit" + end);
        set(ConfPaths.STRINGS + ".nofall", start + "tried to avoid fall damage" + end);
        set(ConfPaths.STRINGS + ".chatfast", start + "acted like spamming (IP: [ip])" + end);
        set(ConfPaths.STRINGS + ".noswing", start + "didn't swing arm" + end);
        set(ConfPaths.STRINGS + ".passable", start + "moved into a block ([blockid]) from [locationfrom] to [locationto] distance [distance] " + end);
        set(ConfPaths.STRINGS + ".relog", start + "relogs too fast" + end);
        set(ConfPaths.STRINGS + ".tellchatnormal", tell + "&cNCP: &eToo many messages, slow down...");
        set(ConfPaths.STRINGS + ".tempkick1", "ncp tempkick [player] 1 Wait a minute!");
        set(ConfPaths.STRINGS + ".tempkick5", "ncp tempkick [player] 5 You have five minutes to think about it!");

        // Compatibility settings.
        set(ConfPaths.COMPATIBILITY_MANAGELISTENERS, false);
        set(ConfPaths.COMPATIBILITY_EXEMPTIONS_REMOVE_JOIN, true);
        set(ConfPaths.COMPATIBILITY_EXEMPTIONS_REMOVE_LEAVE, true);
        set(ConfPaths.COMPATIBILITY_SERVER_CBDEDICATED_ENABLE, true);
        set(ConfPaths.COMPATIBILITY_SERVER_CBREFLECT_ENABLE, true);
        set(ConfPaths.COMPATIBILITY_BLOCKS + ConfPaths.SUB_IGNOREPASSABLE, Arrays.asList(
                Material.WOODEN_DOOR.name(), Material.IRON_DOOR_BLOCK.name(),
                Material.TRAP_DOOR.name(),
                Material.PISTON_EXTENSION.name(), 
                Material.PISTON_MOVING_PIECE.name() // TODO: ?
                ));
        set(ConfPaths.COMPATIBILITY_BLOCKS + ConfPaths.SUB_ALLOWINSTANTBREAK, new LinkedList<String>());
        set(ConfPaths.COMPATIBILITY_BLOCKS + ConfPaths.SUB_OVERRIDEFLAGS + ".snow", "default");

        //        // Update internal factory based on all the new entries to the "actions" section.
        //        setActionFactory();
    }
}
