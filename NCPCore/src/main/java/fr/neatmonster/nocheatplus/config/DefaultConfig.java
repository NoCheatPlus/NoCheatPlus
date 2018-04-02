/*
 * This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package fr.neatmonster.nocheatplus.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

import org.bukkit.Material;

import fr.neatmonster.nocheatplus.compat.Bridge1_9;
import fr.neatmonster.nocheatplus.compat.BridgeMisc;

/**
 * These are the default settings for NoCheatPlus. They will be used in addition to/in replacement of configurations
 * given in the configFactory.yml file.
 */
public class DefaultConfig extends ConfigFile {

    // TODO: Other version details ?

    private static final String unifiedBlockDirectionActions = "cancel vl>10 log:bdirection:0:3:if cancel";
    private static final String unifiedBlockReachActions = "cancel vl>5 log:breach:0:2:if cancel";

    /**
     * Instantiates a new default configuration.
     */
    public DefaultConfig() {
        super();


        // General.
        set(ConfPaths.SAVEBACKCONFIG, true, 785);

        // Config version.
        set(ConfPaths.CONFIGVERSION_NOTIFY, true, 785);
        set(ConfPaths.CONFIGVERSION_NOTIFYMAXPATHS, 5, 1085);
        //        not set(ConfPaths.CONFIGVERSION_CREATED, -1);
        //        not set(ConfPaths.CONFIGVERSION_SAVED, -1);
        set(ConfPaths.LOGGING_ACTIVE, true, 785);
        set(ConfPaths.LOGGING_MAXQUEUESIZE, 5000, 785);
        set(ConfPaths.LOGGING_EXTENDED_STATUS, false, 785);
        set(ConfPaths.LOGGING_EXTENDED_COMMANDS_ACTIONS, false, 1090);
        set(ConfPaths.LOGGING_EXTENDED_ALLVIOLATIONS_DEBUG, true, 785);
        set(ConfPaths.LOGGING_EXTENDED_ALLVIOLATIONS_DEBUGONLY, false, 785);
        set(ConfPaths.LOGGING_EXTENDED_ALLVIOLATIONS_BACKEND_TRACE, false, 785);
        set(ConfPaths.LOGGING_EXTENDED_ALLVIOLATIONS_BACKEND_NOTIFY, false, 785);
        set(ConfPaths.LOGGING_BACKEND_CONSOLE_ACTIVE, true, 785);
        set(ConfPaths.LOGGING_BACKEND_CONSOLE_ASYNCHRONOUS, true, 785);
        set(ConfPaths.LOGGING_BACKEND_FILE_ACTIVE, true, 785);
        set(ConfPaths.LOGGING_BACKEND_FILE_PREFIX, "", 785);
        set(ConfPaths.LOGGING_BACKEND_FILE_FILENAME, "nocheatplus.log", 785);
        set(ConfPaths.LOGGING_BACKEND_INGAMECHAT_ACTIVE, true, 785);
        set(ConfPaths.LOGGING_BACKEND_INGAMECHAT_PREFIX, "&cNCP: &f", 785);

        // Data settings.
        // Expired offline players data.
        set(ConfPaths.DATA_EXPIRATION_ACTIVE, false, 785);
        set(ConfPaths.DATA_EXPIRATION_DURATION, 60, 785);
        set(ConfPaths.DATA_EXPIRATION_HISTORY, false, 785);
        // Consistency checking.
        set(ConfPaths.DATA_CONSISTENCYCHECKS_CHECK, true, 785);
        set(ConfPaths.DATA_CONSISTENCYCHECKS_INTERVAL, 10, 785);
        set(ConfPaths.DATA_CONSISTENCYCHECKS_MAXTIME, 2, 785);
        set(ConfPaths.DATA_CONSISTENCYCHECKS_SUPPRESSWARNINGS, false, 785);

        // Permission settings.
        set(ConfPaths.PERMISSIONS_POLICY_DEFAULT, "ALWAYS", 1140);
        set(ConfPaths.PERMISSIONS_POLICY_RULES, Arrays.asList(
                "nocheatplus.notify :: INTERVAL:60, -world, -offline", // Not sure about this one.
                "nocheatplus.admin.debug :: INTERVAL:5",
                "nocheatplus.admin* :: ALWAYS",
                // TODO: Command permissions are always checked anyway :p. Will be changed...
                "nocheatplus.command* :: ALWAYS",
                "nocheatplus.bypass* :: ALWAYS",
                "regex:^nocheatplus\\.checks\\..*\\.silent$ :: FALSE",
                /*
                 * Relog, logins: Note: aims at login denial, would invalidate
                 * once offline/world change. +- not sure.
                 */
                "nocheatplus.checks.chat.relog :: INTERVAL:10",
                "nocheatplus.checks.chat.logins :: INTERVAL:10",
                "nocheatplus.checks.chat.* :: INTERVAL:2",
                "nocheatplus.checks.net.* :: INTERVAL:2",
                "nocheatplus.checks.moving.survivalfly.* :: INTERVAL:5" // (Excludes the sf base permission.)
                ), 1142);

        // Protection features.
        // Hide plugins.
        set(ConfPaths.PROTECT_PLUGINS_HIDE_ACTIVE, true, 785);
        set(ConfPaths.PROTECT_PLUGINS_HIDE_NOPERMISSION_MSG, "&cI'm sorry, but you do not have permission to perform this command. Please contact the server administrators if you believe that this is in error.", 785);
        set(ConfPaths.PROTECT_PLUGINS_HIDE_NOPERMISSION_CMDS, Arrays.asList("plugins", "version", "icanhasbukkit"), 785);
        set(ConfPaths.PROTECT_PLUGINS_HIDE_NOCOMMAND_MSG, "Unknown command. Type \"/help\" for help.", 785);
        set(ConfPaths.PROTECT_PLUGINS_HIDE_NOCOMMAND_CMDS, new LinkedList<String>(), 785);
        // Commands (other).
        set(ConfPaths.PROTECT_COMMANDS_CONSOLEONLY_ACTIVE, false, 785);
        set(ConfPaths.PROTECT_COMMANDS_CONSOLEONLY_MSG, "&cI'm sorry, but this command can't be executed in chat. Use the console instead!", 785);
        set(ConfPaths.PROTECT_COMMANDS_CONSOLEONLY_CMDS, Arrays.asList("op", "deop"), 785);
        // Client motd.
        set(ConfPaths.PROTECT_CLIENTS_MOTD_ACTIVE, true, 785);
        set(ConfPaths.PROTECT_CLIENTS_MOTD_ALLOWALL, false, 785);


        set(ConfPaths.CHECKS_ACTIVE, true, 1144);
        set(ConfPaths.CHECKS_LAG, true, 1144);
        set(ConfPaths.CHECKS_DEBUG, false, 1144);


        set(ConfPaths.BLOCKBREAK_ACTIVE, "default", 1144);

        set(ConfPaths.BLOCKBREAK_DIRECTION_CHECK, "default", 785);
        set(ConfPaths.BLOCKBREAK_DIRECTION_ACTIONS, unifiedBlockDirectionActions, 1097);

        set(ConfPaths.BLOCKBREAK_FASTBREAK_CHECK, "default", 785);
        set(ConfPaths.BLOCKBREAK_FASTBREAK_STRICT, true, 785);
        set(ConfPaths.BLOCKBREAK_FASTBREAK_DELAY, 100, 785);
        set(ConfPaths.BLOCKBREAK_FASTBREAK_MOD_SURVIVAL, 100, 785);
        set(ConfPaths.BLOCKBREAK_FASTBREAK_GRACE, 2000, 785);
        set(ConfPaths.BLOCKBREAK_FASTBREAK_ACTIONS, "cancel vl>0 log:fastbreak:3:5:cif cancel", 785);

        set(ConfPaths.BLOCKBREAK_FREQUENCY_CHECK, "default", 785);
        set(ConfPaths.BLOCKBREAK_FREQUENCY_MOD_CREATIVE, 95, 785);
        set(ConfPaths.BLOCKBREAK_FREQUENCY_MOD_SURVIVAL, 45, 785);
        set(ConfPaths.BLOCKBREAK_FREQUENCY_SHORTTERM_TICKS, 5, 785);
        set(ConfPaths.BLOCKBREAK_FREQUENCY_SHORTTERM_LIMIT, 7, 785);
        set(ConfPaths.BLOCKBREAK_FREQUENCY_ACTIONS, "cancel vl>5 log:bbfrequency:3:5:if cancel vl>60 log:bbfrequency:0:5:cif cancel cmd:kickfrequency", 785);

        set(ConfPaths.BLOCKBREAK_NOSWING_CHECK, "default", 785);
        set(ConfPaths.BLOCKBREAK_NOSWING_ACTIONS, "cancel vl>10 log:noswing:0:5:if cancel", 785);

        set(ConfPaths.BLOCKBREAK_REACH_CHECK, "default", 785);
        set(ConfPaths.BLOCKBREAK_REACH_ACTIONS, unifiedBlockReachActions, 785);

        set(ConfPaths.BLOCKBREAK_WRONGBLOCK_CHECK, "default", 785);
        set(ConfPaths.BLOCKBREAK_WRONGBLOCK_LEVEL, 10, 785);
        set(ConfPaths.BLOCKBREAK_WRONGBLOCK_ACTIONS, "cancel vl>10 log:bwrong:0:5:if cancel vl>30 log:bwrong:0:5:cif cancel cmd:kickwb", 785);


        set(ConfPaths.BLOCKINTERACT_ACTIVE, "default", 1144);

        set(ConfPaths.BLOCKINTERACT_DIRECTION_CHECK, "default", 785);
        set(ConfPaths.BLOCKINTERACT_DIRECTION_ACTIONS, unifiedBlockDirectionActions, 785);

        set(ConfPaths.BLOCKINTERACT_REACH_CHECK, "default", 785);
        set(ConfPaths.BLOCKINTERACT_REACH_ACTIONS, unifiedBlockReachActions, 785);

        set(ConfPaths.BLOCKINTERACT_SPEED_CHECK, "default", 785);
        set(ConfPaths.BLOCKINTERACT_SPEED_INTERVAL, 2000, 785);
        set(ConfPaths.BLOCKINTERACT_SPEED_LIMIT, 60, 785);
        set(ConfPaths.BLOCKINTERACT_SPEED_ACTIONS, "cancel vl>200 log:bspeed:0:2:if cancel vl>1000 cancel log:bspeed:0:2:icf cmd:kickbspeed", 785);

        set(ConfPaths.BLOCKINTERACT_VISIBLE_CHECK, "default", 785);
        set(ConfPaths.BLOCKINTERACT_VISIBLE_ACTIONS, "cancel vl>100 log:bvisible:0:10:if cancel", 785);


        // BLOCKPLACE
        set(ConfPaths.BLOCKPLACE_ACTIVE, "default", 1144);

        set(ConfPaths.BLOCKPLACE_AGAINST_CHECK, "default", 785);
        set(ConfPaths.BLOCKPLACE_AGAINST_ACTIONS, "cancel", 785);

        set(ConfPaths.BLOCKPLACE_AUTOSIGN_CHECK, "default", 785);
        set(ConfPaths.BLOCKPLACE_AUTOSIGN_SKIPEMPTY, false, 785);
        set(ConfPaths.BLOCKPLACE_AUTOSIGN_ACTIONS, "cancel vl>10 log:bautosign:0:3:if cancel", 785);

        set(ConfPaths.BLOCKPLACE_DIRECTION_CHECK, "default", 785);
        set(ConfPaths.BLOCKPLACE_DIRECTION_ACTIONS, unifiedBlockDirectionActions, 785);

        set(ConfPaths.BLOCKPLACE_FASTPLACE_CHECK, "default", 785);
        set(ConfPaths.BLOCKPLACE_FASTPLACE_LIMIT, 22, 785);
        set(ConfPaths.BLOCKPLACE_FASTPLACE_SHORTTERM_TICKS, 10, 785);
        set(ConfPaths.BLOCKPLACE_FASTPLACE_SHORTTERM_LIMIT, 6, 785);
        set(ConfPaths.BLOCKPLACE_FASTPLACE_ACTIONS, "cancel vl>100 log:fastplace:3:5:cif cancel", 785);

        set(ConfPaths.BLOCKPLACE_REACH_CHECK, "default", 785);
        set(ConfPaths.BLOCKPLACE_REACH_ACTIONS, unifiedBlockReachActions, 785);

        set(ConfPaths.BLOCKPLACE_NOSWING_CHECK, "default", 785);
        set(ConfPaths.BLOCKPLACE_NOSWING_EXCEPTIONS, Arrays.asList(Material.WATER_LILY.toString(), Material.FLINT_AND_STEEL.toString()), 785);
        set(ConfPaths.BLOCKPLACE_NOSWING_ACTIONS, "cancel vl>10 log:noswing:0:5:if cancel", 785);

        set(ConfPaths.BLOCKPLACE_SPEED_CHECK, "default", 785);
        set(ConfPaths.BLOCKPLACE_SPEED_INTERVAL, 45L, 785);
        set(ConfPaths.BLOCKPLACE_SPEED_ACTIONS,
                "cancel vl>150 log:bpspeed:3:5:if cancel vl>1000 log:bpspeed:3:5:cif cancel", 785);

        set(ConfPaths.BLOCKPLACE_PREVENTMISC_BOATSANYWHERE, true);


        set(ConfPaths.CHAT_ACTIVE, "default", 1144);

        // Captcha.
        set(ConfPaths.CHAT_CAPTCHA_CHECK, "default", 785);
        set(ConfPaths.CHAT_CAPTCHA_SKIP_COMMANDS, false, 785);
        set(ConfPaths.CHAT_CAPTCHA_CHARACTERS, "abcdefghjkmnpqrtuvwxyzABCDEFGHJKMNPQRTUVWXYZ2346789", 785);
        set(ConfPaths.CHAT_CAPTCHA_LENGTH, 6, 785);
        set(ConfPaths.CHAT_CAPTCHA_QUESTION, "&cPlease type '&6[captcha]&c' to continue sending messages/commands.", 785);
        set(ConfPaths.CHAT_CAPTCHA_SUCCESS, "&aOK, it sounds like you're not a spambot.", 785);
        set(ConfPaths.CHAT_CAPTCHA_TRIES, 3, 785);
        set(ConfPaths.CHAT_CAPTCHA_ACTIONS, "cancel cmd:kickcaptcha vl>4 log:captcha:2:5:cf cancel cmd:kickcaptcha", 785);

        set(ConfPaths.CHAT_COLOR_CHECK, "default", 785);
        set(ConfPaths.CHAT_COLOR_ACTIONS, "log:color:0:1:if cancel", 785);


        set(ConfPaths.CHAT_COMMANDS_CHECK, "default", 785);
        set(ConfPaths.CHAT_COMMANDS_EXCLUSIONS, new ArrayList<String>(), 785);
        set(ConfPaths.CHAT_COMMANDS_HANDLEASCHAT, Arrays.asList("me"), 785);
        set(ConfPaths.CHAT_COMMANDS_LEVEL, 10, 785);
        set(ConfPaths.CHAT_COMMANDS_SHORTTERM_TICKS, 18, 785);
        set(ConfPaths.CHAT_COMMANDS_SHORTTERM_LEVEL, 3, 785);
        set(ConfPaths.CHAT_COMMANDS_ACTIONS, "log:commands:0:5:cf cancel cmd:kickcommands vl>20 log:commands:0:5:cf cancel cmd:tempkick1", 785);

        // Text (ordering on purpose).
        set(ConfPaths.CHAT_TEXT_CHECK, "default", 785);
        set(ConfPaths.CHAT_TEXT_ALLOWVLRESET, true, 785);
        set(ConfPaths.CHAT_TEXT_FREQ_NORM_MIN, 0.0, 785);
        set(ConfPaths.CHAT_TEXT_FREQ_NORM_FACTOR, 0.9D, 785);
        set(ConfPaths.CHAT_TEXT_FREQ_NORM_WEIGHT, 6, 785);
        set(ConfPaths.CHAT_TEXT_FREQ_NORM_LEVEL, 160, 785);
        set(ConfPaths.CHAT_TEXT_FREQ_NORM_ACTIONS, "cancel cmd:tellchatnormal vl>7 log:chatnormal:0:5:f cancel cmd:tellchatnormal vl>20 log:chatnormal:0:5:cf cancel cmd:kickchatnormal vl>40 log:chatnormal:0:5:cf cancel cmd:kickchat5", 785);

        set(ConfPaths.CHAT_TEXT_FREQ_SHORTTERM_MIN, 2.0, 785);
        set(ConfPaths.CHAT_TEXT_FREQ_SHORTTERM_FACTOR, 0.7, 785);
        set(ConfPaths.CHAT_TEXT_FREQ_SHORTTERM_WEIGHT, 3.0, 785);
        set(ConfPaths.CHAT_TEXT_FREQ_SHORTTERM_LEVEL, 20.0, 785);
        set(ConfPaths.CHAT_TEXT_FREQ_SHORTTERM_ACTIONS, "log:chatfast:0:5:cf cancel cmd:kickchatfast vl>20 log:chatfast:0:5:cf cancel cmd:kickchat1 vl>40 log:chatfast:0:5:cf cancel cmd:kickchat5", 785);
        // Message
        set(ConfPaths.CHAT_TEXT_MSG_LETTERCOUNT, 1.0, 785);
        set(ConfPaths.CHAT_TEXT_MSG_PARTITION, 1.0, 785);
        set(ConfPaths.CHAT_TEXT_MSG_UPPERCASE, 1.0, 785);
        set(ConfPaths.CHAT_TEXT_MSG_AFTERJOIN, 1.5, 785);
        set(ConfPaths.CHAT_TEXT_MSG_NOMOVING, 1.5, 785);
        set(ConfPaths.CHAT_TEXT_MSG_REPEATCANCEL, 1.0, 785);
        set(ConfPaths.CHAT_TEXT_MSG_REPEATGLOBAL, 1.0, 785);
        set(ConfPaths.CHAT_TEXT_MSG_REPEATSELF, 1.5, 785);
        set(ConfPaths.CHAT_TEXT_MSG_WORDS_LENGTHAV, 1.0, 785);
        set(ConfPaths.CHAT_TEXT_MSG_WORDS_LENGTHMSG, 1.0, 785);
        set(ConfPaths.CHAT_TEXT_MSG_WORDS_NOLETTER, 0.0, 785);
        // Global
        set(ConfPaths.CHAT_TEXT_GL_CHECK, true, 785);
        set(ConfPaths.CHAT_TEXT_GL_WEIGHT, 0.5, 785);
        set(ConfPaths.CHAT_TEXT_GL_WORDS_CHECK, false, 785);
        set(ConfPaths.CHAT_TEXT_GL_WEIGHT, 1.0, 785);
        set(ConfPaths.CHAT_TEXT_GL_PREFIXES_CHECK , false, 785);
        set(ConfPaths.CHAT_TEXT_GL_SIMILARITY_CHECK , false, 785);
        // Player
        set(ConfPaths.CHAT_TEXT_PP_CHECK, true, 785);
        set(ConfPaths.CHAT_TEXT_PP_WORDS_CHECK, false, 785);
        set(ConfPaths.CHAT_TEXT_PP_PREFIXES_CHECK, false, 785);
        set(ConfPaths.CHAT_TEXT_PP_SIMILARITY_CHECK , false, 785);
        // Warning (commands + chat).
        set(ConfPaths.CHAT_WARNING_CHECK, true, 785);
        set(ConfPaths.CHAT_WARNING_LEVEL, 67, 785);
        set(ConfPaths.CHAT_WARNING_MESSAGE, "&e>>>\n&e>>> &cPlease &eslow down &cchat, &eyou might get kicked &cfor spam.\n&e>>>", 785);
        set(ConfPaths.CHAT_WARNING_TIMEOUT, 10, 785);
        // Relog
        set(ConfPaths.CHAT_RELOG_CHECK, "default", 785);
        set(ConfPaths.CHAT_RELOG_TIMEOUT, 5000L, 785);
        set(ConfPaths.CHAT_RELOG_WARNING_MESSAGE, "&cYou relogged really fast! If you keep doing that, you're going to be banned.", 785);
        set(ConfPaths.CHAT_RELOG_WARNING_NUMBER, 1, 785);
        set(ConfPaths.CHAT_RELOG_KICKMESSAGE, "Too fast re-login, try with a little delay.", 785);
        set(ConfPaths.CHAT_RELOG_WARNING_TIMEOUT, 60000L, 785);
        set(ConfPaths.CHAT_RELOG_ACTIONS, "log:relog:0:10:cf cancel vl>20 log:relog:0:10:cf cancel cmd:tempkick5", 785);
        // Logins
        set(ConfPaths.CHAT_LOGINS_CHECK, "default", 785);
        set(ConfPaths.CHAT_LOGINS_STARTUPDELAY, 600, 785);
        set(ConfPaths.CHAT_LOGINS_PERWORLDCOUNT, false, 785);
        set(ConfPaths.CHAT_LOGINS_SECONDS, 10, 785);
        set(ConfPaths.CHAT_LOGINS_LIMIT, 10, 785);
        set(ConfPaths.CHAT_LOGINS_KICKMESSAGE, "Too many people logging in, retry soon.", 785);

        /*
         * Combined !
         */

        set(ConfPaths.COMBINED_ACTIVE, "default", 1144);

        set(ConfPaths.COMBINED_BEDLEAVE_CHECK, "default", 785);
        set(ConfPaths.COMBINED_BEDLEAVE_ACTIONS, "cancel log:bedleave:0:5:if cmd:kickbedleave", 785);

        set(ConfPaths.COMBINED_ENDERPEARL_CHECK, "default", 785);
        set(ConfPaths.COMBINED_ENDERPEARL_PREVENTCLICKBLOCK, true, 785);

        set(ConfPaths.COMBINED_IMPROBABLE_CHECK , "default", 785);
        set(ConfPaths.COMBINED_IMPROBABLE_LEVEL, 300, 785);
        //        set(ConfPaths.COMBINED_IMPROBABLE_FASTBREAK_CHECK, false, 785);
        set(ConfPaths.COMBINED_IMPROBABLE_ACTIONS, "cancel log:improbable:2:8:if", 785);

        set(ConfPaths.COMBINED_INVULNERABLE_CHECK, true, 785); // Not a check type yet.
        set(ConfPaths.COMBINED_INVULNERABLE_TRIGGERS_ALWAYS, false, 785);
        set(ConfPaths.COMBINED_INVULNERABLE_TRIGGERS_FALLDISTANCE, true, 785);
        set(ConfPaths.COMBINED_INVULNERABLE_INITIALTICKS_JOIN, -1, 785);
        set(ConfPaths.COMBINED_INVULNERABLE_IGNORE, Arrays.asList("FALL"), 785);
        set(ConfPaths.COMBINED_INVULNERABLE_MODIFIERS + ".all", 0, 785);

        set(ConfPaths.COMBINED_MUNCHHAUSEN_CHECK, "default", 785);
        set(ConfPaths.COMBINED_MUNCHHAUSEN_ACTIONS, "cancel vl>100 cancel log:munchhausen:0:60:if", 785);

        // (No active flag set !?)
        set(ConfPaths.COMBINED_YAWRATE_RATE , 380, 785);
        set(ConfPaths.COMBINED_YAWRATE_PENALTY_FACTOR, 1.0, 785);
        set(ConfPaths.COMBINED_YAWRATE_PENALTY_MIN, 250, 785);
        set(ConfPaths.COMBINED_YAWRATE_PENALTY_MAX, 2000, 785);
        set(ConfPaths.COMBINED_YAWRATE_IMPROBABLE, true, 785);

        // FIGHT
        set(ConfPaths.FIGHT_ACTIVE, "default", 1144);

        set(ConfPaths.FIGHT_CANCELDEAD, true, 785);
        set(ConfPaths.FIGHT_TOOLCHANGEPENALTY, 500L, 785);
        set(ConfPaths.FIGHT_PVP_KNOCKBACKVELOCITY, "default", 785);

        set(ConfPaths.FIGHT_YAWRATE_CHECK, true, 785); // Not a check type.

        set(ConfPaths.FIGHT_ANGLE_CHECK, "default", 785);
        set(ConfPaths.FIGHT_ANGLE_THRESHOLD, 50, 785);
        set(ConfPaths.FIGHT_ANGLE_ACTIONS, "cancel vl>100 log:angle:3:5:f cancel vl>250 log:angle:0:5:cif cancel", 785);

        set(ConfPaths.FIGHT_CRITICAL_CHECK, "default", 785);
        set(ConfPaths.FIGHT_CRITICAL_CANCEL_CANCEL, 100, 785);
        set(ConfPaths.FIGHT_CRITICAL_CANCEL_DIVIDEDAMAGE, 1.5, 785);
        set(ConfPaths.FIGHT_CRITICAL_FALLDISTANCE, 0.06251, 785);
        set(ConfPaths.FIGHT_CRITICAL_ACTIONS, "cancel vl>50 log:critical:0:5:cif cancel", 785);

        set(ConfPaths.FIGHT_DIRECTION_CHECK, "default", 785);
        set(ConfPaths.FIGHT_DIRECTION_STRICT, false, 785);
        set(ConfPaths.FIGHT_DIRECTION_PENALTY, 500L, 785);
        set(ConfPaths.FIGHT_DIRECTION_ACTIONS,
                "cancel vl>5 log:fdirection:3:5:f cancel vl>20 log:fdirection:0:5:if cancel vl>50 log:fdirection:0:5:cif cancel", 785);

        set(ConfPaths.FIGHT_FASTHEAL_CHECK, "default", 785);
        set(ConfPaths.FIGHT_FASTHEAL_INTERVAL, 4000L, 785);
        set(ConfPaths.FIGHT_FASTHEAL_BUFFER, 1000L, 785);
        set(ConfPaths.FIGHT_FASTHEAL_ACTIONS, "cancel vl>10 cancel log:fastheal:0:10:i vl>30 cancel log:fastheal:0:10:if", 785);

        set(ConfPaths.FIGHT_GODMODE_CHECK, "default", 785);
        set(ConfPaths.FIGHT_GODMODE_LAGMINAGE, 1100, 785); // TODO: ndt/2 => 500-600.
        set(ConfPaths.FIGHT_GODMODE_LAGMAXAGE, 5000, 785);
        set(ConfPaths.FIGHT_GODMODE_ACTIONS, "log:godmode:2:5:if cancel vl>60 log:godmode:2:5:icf cancel", 785); // cmd:kickgod", 785);

        set(ConfPaths.FIGHT_NOSWING_CHECK, "default", 785);
        set(ConfPaths.FIGHT_NOSWING_ACTIONS, "cancel vl>10 log:noswing:0:5:if cancel", 785);

        set(ConfPaths.FIGHT_REACH_CHECK, "default", 785);
        set(ConfPaths.FIGHT_REACH_SURVIVALDISTANCE, 4.4, 785);
        set(ConfPaths.FIGHT_REACH_PENALTY, 500, 785);
        set(ConfPaths.FIGHT_REACH_REDUCE, true, 785);
        set(ConfPaths.FIGHT_REACH_REDUCEDISTANCE, 0.9, 785);
        set(ConfPaths.FIGHT_REACH_REDUCESTEP, 0.15, 785);
        set(ConfPaths.FIGHT_REACH_ACTIONS, "cancel vl>10 log:freach:2:5:if cancel", 785);

        set(ConfPaths.FIGHT_SELFHIT_CHECK, "default", 785);
        set(ConfPaths.FIGHT_SELFHIT_ACTIONS, "log:fselfhit:0:5:if cancel vl>10 log:fselfhit:0:5:icf cancel cmd:kickselfhit", 785);

        set(ConfPaths.FIGHT_SPEED_CHECK, "default", 785);
        set(ConfPaths.FIGHT_SPEED_LIMIT, 15, 785);
        set(ConfPaths.FIGHT_SPEED_ACTIONS, "cancel vl>50 log:fspeed:0:5:if cancel", 785);
        set(ConfPaths.FIGHT_SPEED_SHORTTERM_TICKS, 7, 785);
        set(ConfPaths.FIGHT_SPEED_SHORTTERM_LIMIT, 6, 785);

        set(ConfPaths.FIGHT_WRONGTURN_CHECK, "default", 1143);
        set(ConfPaths.FIGHT_WRONGTURN_ACTIONS, "cancel cmd:kick_wrongturn log:log_wrongturn:0:15:fci", 1143);


        set(ConfPaths.INVENTORY_ACTIVE, "default", 1144);

        set(ConfPaths.INVENTORY_DROP_CHECK, "default", 785);
        set(ConfPaths.INVENTORY_DROP_LIMIT, 100, 785);
        set(ConfPaths.INVENTORY_DROP_TIMEFRAME, 20L, 785);
        set(ConfPaths.INVENTORY_DROP_ACTIONS, "log:drop:0:1:cif cancel cmd:dropkick:0:1", 785);

        set(ConfPaths.INVENTORY_FASTCLICK_CHECK, "default", 785);
        set(ConfPaths.INVENTORY_FASTCLICK_SPARECREATIVE, true, 785);
        set(ConfPaths.INVENTORY_FASTCLICK_TWEAKS1_5, true, 785);
        set(ConfPaths.INVENTORY_FASTCLICK_LIMIT_SHORTTERM, 4, 785);
        set(ConfPaths.INVENTORY_FASTCLICK_LIMIT_NORMAL, 15, 785);
        set(ConfPaths.INVENTORY_FASTCLICK_ACTIONS, "cancel vl>50 log:fastclick:3:5:cif cancel", 785);

        set(ConfPaths.INVENTORY_INSTANTBOW_CHECK, "default", 785);
        set(ConfPaths.INVENTORY_INSTANTBOW_STRICT, true, 785);
        set(ConfPaths.INVENTORY_INSTANTBOW_DELAY, 130, 785);
        set(ConfPaths.INVENTORY_INSTANTBOW_IMPROBABLE_FEEDONLY, false, 1085);
        set(ConfPaths.INVENTORY_INSTANTBOW_IMPROBABLE_WEIGHT, 0.6, 1085);
        set(ConfPaths.INVENTORY_INSTANTBOW_ACTIONS, "cancel vl>15 log:instantbow:2:5:if cancel", 785);

        set(ConfPaths.INVENTORY_INSTANTEAT_CHECK, "default", 785);
        set(ConfPaths.INVENTORY_INSTANTEAT_ACTIONS, "log:instanteat:2:5:if cancel", 785);

        set(ConfPaths.INVENTORY_FASTCONSUME_CHECK, "default", 785);
        set(ConfPaths.INVENTORY_FASTCONSUME_DURATION, 0.7, 785);
        set(ConfPaths.INVENTORY_FASTCONSUME_WHITELIST, false, 785);
        set(ConfPaths.INVENTORY_FASTCONSUME_ITEMS, new LinkedList<String>(), 785);
        set(ConfPaths.INVENTORY_FASTCONSUME_ACTIONS, "log:fastconsume:2:5:if cancel", 785);

        set(ConfPaths.INVENTORY_GUTENBERG_CHECK, "default", 785);
        set(ConfPaths.INVENTORY_GUTENBERG_ACTIONS, "cancel log:gutenberg:0:10:icf cmd:kickinvaliddata", 785);

        set(ConfPaths.INVENTORY_ITEMS_CHECK, "default", 785);

        set(ConfPaths.INVENTORY_OPEN_CHECK, "default", 785);
        set(ConfPaths.INVENTORY_OPEN_CLOSE, true, 785);
        set(ConfPaths.INVENTORY_OPEN_CANCELOTHER, true, 785);

        set (ConfPaths.INVENTORY_HOTFIX_DUPE_FALLINGBLOCKENDPORTAL, true, 785);

        // MOVING
        set(ConfPaths.MOVING_ACTIVE, "default", 1144);

        set(ConfPaths.MOVING_CREATIVEFLY_CHECK, "default", 785);
        set(ConfPaths.MOVING_CREATIVEFLY_IGNOREALLOWFLIGHT, true, 785);
        set(ConfPaths.MOVING_CREATIVEFLY_IGNORECREATIVE, false, 785); // TODO: -> true ?
        set(ConfPaths.MOVING_CREATIVEFLY_MODEL + "creative." + ConfPaths.SUB_HORIZONTAL_SPEED, 100, 785);
        set(ConfPaths.MOVING_CREATIVEFLY_MODEL + "creative." + ConfPaths.SUB_VERTICAL_ASCEND_SPEED, 100, 785);
        set(ConfPaths.MOVING_CREATIVEFLY_MODEL + "creative." + ConfPaths.SUB_VERTICAL_MAXHEIGHT, 128, 785);
        if (BridgeMisc.GAME_MODE_SPECTATOR != null) {
            set(ConfPaths.MOVING_CREATIVEFLY_MODEL + "spectator." + ConfPaths.SUB_HORIZONTAL_SPEED, 450, 1102);
            set(ConfPaths.MOVING_CREATIVEFLY_MODEL + "spectator." + ConfPaths.SUB_VERTICAL_ASCEND_SPEED, 170, 1103);
            set(ConfPaths.MOVING_CREATIVEFLY_MODEL + "spectator." + ConfPaths.SUB_VERTICAL_MAXHEIGHT, 128, 785);
            set(ConfPaths.MOVING_CREATIVEFLY_MODEL + "spectator." + ConfPaths.SUB_GRAVITY, false, 785);
            set(ConfPaths.MOVING_CREATIVEFLY_MODEL + "spectator." + ConfPaths.SUB_GROUND, false, 785);
        }
        if (Bridge1_9.hasLevitation()) {
            set(ConfPaths.MOVING_CREATIVEFLY_MODEL + "levitation." + ConfPaths.SUB_HORIZONTAL_SPEED, 50, 785);
            set(ConfPaths.MOVING_CREATIVEFLY_MODEL + "levitation." + ConfPaths.SUB_VERTICAL_ASCEND_SPEED, 10, 785);
            set(ConfPaths.MOVING_CREATIVEFLY_MODEL + "levitation." + ConfPaths.SUB_VERTICAL_MAXHEIGHT, 128, 1104);
            set(ConfPaths.MOVING_CREATIVEFLY_MODEL + "levitation." + ConfPaths.SUB_VERTICAL_GRAVITY, false, 785);
            set(ConfPaths.MOVING_CREATIVEFLY_MODEL + "levitation." + ConfPaths.SUB_MODIFIERS, false, 785);
            set(ConfPaths.MOVING_CREATIVEFLY_MODEL + "levitation." + ConfPaths.SUB_GRAVITY, false, 785);
            set(ConfPaths.MOVING_CREATIVEFLY_MODEL + "levitation." + ConfPaths.SUB_GROUND, false, 785);
        }
        if (Bridge1_9.hasElytra()) {
            set(ConfPaths.MOVING_CREATIVEFLY_MODEL + "elytra." + ConfPaths.SUB_HORIZONTAL_SPEED, 520, 785);
            set(ConfPaths.MOVING_CREATIVEFLY_MODEL + "elytra." + ConfPaths.SUB_HORIZONTAL_MODSPRINT, 1.0, 785);
            set(ConfPaths.MOVING_CREATIVEFLY_MODEL + "elytra." + ConfPaths.SUB_VERTICAL_ASCEND_SPEED, 0, 785);
            set(ConfPaths.MOVING_CREATIVEFLY_MODEL + "elytra." + ConfPaths.SUB_VERTICAL_MAXHEIGHT, 128, 1104);
            set(ConfPaths.MOVING_CREATIVEFLY_MODEL + "elytra." + ConfPaths.SUB_MODIFIERS, false, 785);
        }
        set(ConfPaths.MOVING_CREATIVEFLY_ACTIONS,
                "log:flyfile:3:5:f cancel"
                        + " vl>100 log:flyshort:0:5:i log:flyfile:0:5:f cancel"
                        + " vl>400 log:flylong:0:5:i log:flyfile:0:5:cf cancel"
                        , 1080);

        set(ConfPaths.MOVING_MOREPACKETS_CHECK, "default", 785);
        set(ConfPaths.MOVING_MOREPACKETS_SECONDS, 6, 785);
        set(ConfPaths.MOVING_MOREPACKETS_EPSIDEAL, 20, 785);
        set(ConfPaths.MOVING_MOREPACKETS_EPSMAX, 22, 785);
        set(ConfPaths.MOVING_MOREPACKETS_BURST_PACKETS, 40, 785);
        set(ConfPaths.MOVING_MOREPACKETS_BURST_DIRECT, 60, 785);
        set(ConfPaths.MOVING_MOREPACKETS_BURST_EPM, 180, 785);
        set(ConfPaths.MOVING_MOREPACKETS_SETBACKAGE, 40, 1091);
        set(ConfPaths.MOVING_MOREPACKETS_ACTIONS, "cancel vl>10 log:morepackets:0:2:if cancel vl>100 log:morepackets:0:2:if cancel cmd:kickpackets", 785);

        set(ConfPaths.MOVING_NOFALL_CHECK, "default", 785);
        set(ConfPaths.MOVING_NOFALL_DEALDAMAGE, true, 785);
        set(ConfPaths.MOVING_NOFALL_SKIPALLOWFLIGHT, true, 785);
        set(ConfPaths.MOVING_NOFALL_RESETONVL, false, 785);
        set(ConfPaths.MOVING_NOFALL_RESETONTP, false, 785);
        set(ConfPaths.MOVING_NOFALL_RESETONVEHICLE, true, 785);
        set(ConfPaths.MOVING_NOFALL_ANTICRITICALS, true, 785);
        set(ConfPaths.MOVING_NOFALL_ACTIONS, "log:nofall:0:5:if cancel vl>30 log:nofall:0:5:icf cancel", 785);

        set(ConfPaths.MOVING_PASSABLE_CHECK, "default", 785);
        set(ConfPaths.MOVING_PASSABLE_ACTIONS, "cancel vl>10 log:passable:0:5:if cancel vl>50 log:passable:0:5:icf cancel", 785);
        set(ConfPaths.MOVING_PASSABLE_UNTRACKED_TELEPORT_ACTIVE, true, 785);
        set(ConfPaths.MOVING_PASSABLE_UNTRACKED_CMD_ACTIVE, true, 785);
        set(ConfPaths.MOVING_PASSABLE_UNTRACKED_CMD_TRYTELEPORT, true, 785);
        set(ConfPaths.MOVING_PASSABLE_UNTRACKED_CMD_PREFIXES, Arrays.asList("sethome", "home set", "setwarp", "warp set", "setback", "set back", "back set"), 785);

        set(ConfPaths.MOVING_SURVIVALFLY_CHECK, "default", 785);
        set(ConfPaths.MOVING_SURVIVALFLY_STEPHEIGHT, "default", 785);
        set(ConfPaths.MOVING_SURVIVALFLY_EXTENDED_VACC, true, 785);
        set(ConfPaths.MOVING_SURVIVALFLY_LENIENCY_HBUFMAX, 1.0, 1143);
        set(ConfPaths.MOVING_SURVIVALFLY_LENIENCY_FREEZECOUNT, 40, 1144);
        set(ConfPaths.MOVING_SURVIVALFLY_LENIENCY_FREEZEINAIR, true, 1143);
        set(ConfPaths.MOVING_SURVIVALFLY_SETBACKPOLICY_FALLDAMAGE, true, 785);
        set(ConfPaths.MOVING_SURVIVALFLY_SETBACKPOLICY_VOIDTOVOID, true, 785);
        set(ConfPaths.MOVING_SURVIVALFLY_ACTIONS, 
                "log:flyfile:3:10:f cancel"
                        + " vl>100 log:flyshort:0:10:i log:flyfile:0:10:f cancel"
                        + " vl>400 log:flylong:0:5:i log:flyfile:0:5:cf cancel"
                        + " vl>1500 log:flylong:0:5:i log:flyfile:0:5:cf cancel cmd:kickfly"
                        , 1080);

        // sf / hover check.
        set(ConfPaths.MOVING_SURVIVALFLY_HOVER_CHECK, true, 785); // Not a check type yet.
        set(ConfPaths.MOVING_SURVIVALFLY_HOVER_STEP, 5, 785);
        set(ConfPaths.MOVING_SURVIVALFLY_HOVER_TICKS, 85, 785);
        set(ConfPaths.MOVING_SURVIVALFLY_HOVER_LOGINTICKS, 60, 785);
        set(ConfPaths.MOVING_SURVIVALFLY_HOVER_FALLDAMAGE, true, 785);
        set(ConfPaths.MOVING_SURVIVALFLY_HOVER_SFVIOLATION, 500, 785);

        // Moving Trace
        //set(ConfPaths.MOVING_TRACE_MAXAGE, 200, 785); // Your grandma reads code.
        //set(ConfPaths.MOVING_TRACE_MAXSIZE, 200, 785);

        // Velocity.
        set(ConfPaths.MOVING_VELOCITY_ACTIVATIONCOUNTER, 80, 785);
        set(ConfPaths.MOVING_VELOCITY_ACTIVATIONTICKS, 140, 785);
        set(ConfPaths.MOVING_VELOCITY_STRICTINVALIDATION, true, 785);

        // General.
        set(ConfPaths.MOVING_SPLITMOVES, "default", 785);
        set(ConfPaths.MOVING_IGNORESTANCE, "default", 785);
        set(ConfPaths.MOVING_TEMPKICKILLEGAL, true, 785);
        set(ConfPaths.MOVING_LOADCHUNKS_JOIN, true, 785);
        set(ConfPaths.MOVING_LOADCHUNKS_MOVE, true, 785);
        set(ConfPaths.MOVING_LOADCHUNKS_TELEPORT, true, 785);
        set(ConfPaths.MOVING_LOADCHUNKS_WORLDCHANGE, true, 785);
        set(ConfPaths.MOVING_SPRINTINGGRACE, 2.0, 785);
        set(ConfPaths.MOVING_ASSUMESPRINT, true, 785);
        set(ConfPaths.MOVING_SPEEDGRACE, 4.0, 785);
        set(ConfPaths.MOVING_ENFORCELOCATION, "default", 785);
        set(ConfPaths.MOVING_SETBACK_METHOD, "default", 785);

        // Vehicles.
        set(ConfPaths.MOVING_VEHICLE_PREVENTDESTROYOWN, true, 785);
        set(ConfPaths.MOVING_VEHICLE_ENFORCELOCATION, "default", 785);
        set(ConfPaths.MOVING_VEHICLE_SCHEDULESETBACKS, "default", 785);

        set(ConfPaths.MOVING_VEHICLE_MOREPACKETS_CHECK, "default", 785);
        set(ConfPaths.MOVING_VEHICLE_MOREPACKETS_ACTIONS, "cancel vl>10 log:morepackets:0:2:if cancel", 785);

        set(ConfPaths.MOVING_VEHICLE_ENVELOPE_ACTIVE, "default", 785);
        set(ConfPaths.MOVING_VEHICLE_ENVELOPE_ACTIONS, "cancel vl>100 cancel log:vehicleenvelope:0:15:icf", 785);

        // Messages
        set(ConfPaths.MOVING_MESSAGE_ILLEGALPLAYERMOVE, "Illegal move.", 785);
        set(ConfPaths.MOVING_MESSAGE_ILLEGALVEHICLEMOVE, "Illegal vehicle move.", 785);


        // NET

        set(ConfPaths.NET_ACTIVE, "default", 1144);

        // AttackFrequency
        set(ConfPaths.NET_ATTACKFREQUENCY_ACTIVE, "default", 785);
        set(ConfPaths.NET_ATTACKFREQUENCY_SECONDS_HALF, 10, 785);
        set(ConfPaths.NET_ATTACKFREQUENCY_SECONDS_ONE, 15, 785);
        set(ConfPaths.NET_ATTACKFREQUENCY_SECONDS_TWO, 30, 785);
        set(ConfPaths.NET_ATTACKFREQUENCY_SECONDS_FOUR, 60, 785);
        set(ConfPaths.NET_ATTACKFREQUENCY_SECONDS_EIGHT, 100, 785);
        set(ConfPaths.NET_ATTACKFREQUENCY_ACTIONS, "cancel vl>30 cancel log:attackfrequency:0:5:if vl>160 cancel log:attackfrequency:0:0:cif cmd:kickattackfrequency", 785);

        // FlyingFrequency
        set(ConfPaths.NET_FLYINGFREQUENCY_ACTIVE, "default", 785);
        set(ConfPaths.NET_FLYINGFREQUENCY_SECONDS, 5, 785);
        set(ConfPaths.NET_FLYINGFREQUENCY_PACKETSPERSECOND, 60, 785);
        set(ConfPaths.NET_FLYINGFREQUENCY_ACTIONS, "cancel", 785); // TODO: Log actions.
        set(ConfPaths.NET_FLYINGFREQUENCY_REDUNDANT_ACTIVE, true, 785);
        set(ConfPaths.NET_FLYINGFREQUENCY_REDUNDANT_SECONDS, 3, 785);
        set(ConfPaths.NET_FLYINGFREQUENCY_REDUNDANT_ACTIONS, "cancel", 785); // TODO: Log actions.

        // KeepAliveFrequency
        set(ConfPaths.NET_KEEPALIVEFREQUENCY_ACTIVE, "default", 785);
        set(ConfPaths.NET_KEEPALIVEFREQUENCY_ACTIONS, "cancel vl>10 cancel log:keepalive:0:10:if vl>40 cancel log:keepalive:0:10:icf vl>100 cancel log:keepalive:0:10:icf cmd:kickalive", 785);

        // PacketFrequency (pre 1.9).
        set(ConfPaths.NET_PACKETFREQUENCY_ACTIVE, "default", 785);
        set(ConfPaths.NET_PACKETFREQUENCY_PPS, 200, 785);
        set(ConfPaths.NET_PACKETFREQUENCY_SECONDS, 4, 785);
        set(ConfPaths.NET_PACKETFREQUENCY_ACTIONS, "cancel cmd:kickpacketfrequency", 785);

        // SoundDistance
        set(ConfPaths.NET_SOUNDDISTANCE_ACTIVE, "default", 785);
        set(ConfPaths.NET_SOUNDDISTANCE_MAXDISTANCE, 320, 785);

        // Superseded
        set(ConfPaths.NET_SUPERSEDED_FLYING_CANCELWAITING, true, 1090);


        // TODO: An extra file might suit these.
        final String start = "[player] failed [check]: ";
        final String end = ". VL [violations].";
        final String tell = "ncp tell [player] ";
        set(ConfPaths.STRINGS + ".angle", start + "tried to hit multiple entities at the same time" + end, 785);
        set(ConfPaths.STRINGS + ".attackfrequency", start + "attacks with too high a frequency ([packets]/[limit], [tags])" + end, 785);
        set(ConfPaths.STRINGS + ".ban", "ban [player]", 785);
        set(ConfPaths.STRINGS + ".ban-ip", "ban-ip [ip]", 785);
        set(ConfPaths.STRINGS + ".bautosign", start + "failed autosign with [tags]" + end, 785);
        set(ConfPaths.STRINGS + ".bbfrequency", start + "tried to break too many blocks within time frame" + end, 785);
        set(ConfPaths.STRINGS + ".bdirection", start + "tried to interact with a block out of their line of sight" + end, 785);
        set(ConfPaths.STRINGS + ".bedleave", start + "sends bed leave packets (was not in bed)" + end, 785);
        set(ConfPaths.STRINGS + ".bpspeed", start + "tried to throw projectiles too quickly" + end, 785);
        set(ConfPaths.STRINGS + ".breach", start + "exceeds block-interact distance ([reachdistance])" + end, 785);
        set(ConfPaths.STRINGS + ".bspeed", start + "interacts too fast" + end, 785);
        set(ConfPaths.STRINGS + ".bvisible", start + "interacts with a block out of sight" + end, 785);
        set(ConfPaths.STRINGS + ".bwrong", start + "broke another block than clicked" + end, 785);
        set(ConfPaths.STRINGS + ".captcha", "[player] failed captcha repeatedly" + end, 785);
        set(ConfPaths.STRINGS + ".chatnormal", start + "potentially annoying chat" + end, 785);
        set(ConfPaths.STRINGS + ".color", start + "sent colored chat message" + end, 785);
        set(ConfPaths.STRINGS + ".commands", start + "issued too many commands" + end, 785);
        set(ConfPaths.STRINGS + ".combspeed", start + "performs different actions at very high speed" + end, 785);
        set(ConfPaths.STRINGS + ".critical", start + "tried to do a critical hit but wasn't technically jumping [tags]" + end, 785);
        set(ConfPaths.STRINGS + ".drop", start + "tried to drop more items than allowed" + end, 785);
        set(ConfPaths.STRINGS + ".dropkick", "ncp delay ncp kick [player] Dropping items too fast.", 785);
        set(ConfPaths.STRINGS + ".fastbreak", start + "tried to break blocks ([blocktype]) faster than possible" + end, 785);
        set(ConfPaths.STRINGS + ".fastclick", start + "tried to move items in their inventory too quickly" + end, 785);
        set(ConfPaths.STRINGS + ".fastconsume", start + "consumes [food] [tags] too fast" + end, 785);
        set(ConfPaths.STRINGS + ".fastheal", start + "regenerates health faster than usual (health [health])" + end, 785);
        set(ConfPaths.STRINGS + ".fastplace", start + "tried to place too many blocks" + end, 785);
        set(ConfPaths.STRINGS + ".fdirection", start + "tried to hit an entity out of line of sight" + end, 785);
        set(ConfPaths.STRINGS + ".flyshort", start + "tried to move unexpectedly" + end, 785);
        set(ConfPaths.STRINGS + ".flylong", start
                + "tried to move: [locationfrom] -> [locationto], d=[distance] ([tags])" + end, 1067);
        set(ConfPaths.STRINGS + ".flyfile", start 
                + "tried to move: [locationfrom] -> [locationto], d=[distance] ([tags])" + end, 785);
        set(ConfPaths.STRINGS + ".freach", start + "tried to attack entity out of reach" + end, 785);
        set(ConfPaths.STRINGS + ".fselfhit", start + "tried to self-hit" + end, 785);
        set(ConfPaths.STRINGS + ".fspeed", start + "tried to attack with too high a frequency" + end, 785);
        set(ConfPaths.STRINGS + ".gutenberg", start + "created a book with too many pages" + end, 785);
        set(ConfPaths.STRINGS + ".godmode", start + "avoided taking damage or lagging (health [health])" + end, 785);
        set(ConfPaths.STRINGS + ".improbable", start + "meets the improbable more than expected" + end, 785);
        set(ConfPaths.STRINGS + ".instantbow", start + "fires bow too fast" + end, 785);
        set(ConfPaths.STRINGS + ".instanteat", start + "eats food [food] too fast" + end, 785);
        set(ConfPaths.STRINGS + ".keepalive", start + "spams keep-alive packets (god/freecam?)" + end, 785);
        set(ConfPaths.STRINGS + ".kick", "kick [player]", 785);
        set(ConfPaths.STRINGS + ".kickalive", "ncp kick [player] Too many keep-alive packets.", 785);
        set(ConfPaths.STRINGS + ".kickattackfrequency", "ncp kick [player] Unlikely fast clicking.", 785);
        set(ConfPaths.STRINGS + ".kickbedleave", "ncp delay ncp kick [player] Go find a bed!", 785);
        set(ConfPaths.STRINGS + ".kickbspeed", "ncp kick [player] You interacted too fast!", 785);
        set(ConfPaths.STRINGS + ".kickcaptcha", "ncp kick [player] Enter the captcha!", 785);
        set(ConfPaths.STRINGS + ".kickchat1", "ncp tempkick [player] 1 You're still not allowed to spam!", 785);
        set(ConfPaths.STRINGS + ".kickchat5", "ncp tempkick [player] 5 You're not intended to spam!", 785);
        set(ConfPaths.STRINGS + ".kickchatfast", "ncp kick [player] You're not allowed to spam in chat!", 785);
        set(ConfPaths.STRINGS + ".kickchatnormal", "ncp kick [player] Too many chat messages, take a break.", 785);
        set(ConfPaths.STRINGS + ".kickcommands", "ncp tempkick [player] 1 You're not allowed to spam commands!", 785);
        set(ConfPaths.STRINGS + ".kickfly", "ncp delay ncp kick [player] Kicked for flying (or related)", 785);
        set(ConfPaths.STRINGS + ".kickfrequency", "ncp kick [player] You did something too fast!", 785);
        set(ConfPaths.STRINGS + ".kickgod", "ncp kick [player] God mode?", 785);
        set(ConfPaths.STRINGS + ".kickinvaliddata", "ncp kick [player] Invalid data.", 785);
        set(ConfPaths.STRINGS + ".kickpacketfrequency", "ncp kick [player] Too many packets.", 785); // TODO
        set(ConfPaths.STRINGS + ".kickpackets", "ncp delay ncp kick [player] Too many packets (extreme lag?)", 785);
        set(ConfPaths.STRINGS + ".kickselfhit", "ncp kick [player] You tried to hit yourself!", 785);
        set(ConfPaths.STRINGS + ".kickwb", "ncp kick [player] Block breaking out of sync!", 785);
        set(ConfPaths.STRINGS + ".kick_wrongturn", "ncp kick [player] Wrong turn!", 1143);
        set(ConfPaths.STRINGS + ".knockback", start + "tried to do a knockback but wasn't technically sprinting" + end, 785);
        set(ConfPaths.STRINGS + ".log_wrongturn", start + "looked wrongly" + end, 1143);
        set(ConfPaths.STRINGS + ".morepackets", start + "sent too many moves ([packets] [tags])" + end, 785);
        set(ConfPaths.STRINGS + ".msgtempdenylogin", "You are temporarily denied to join this server.", 785);
        set(ConfPaths.STRINGS + ".munchhausen", start + "almost made it off the pit" + end, 785);
        set(ConfPaths.STRINGS + ".nofall", start + "tried to alter fall damage ([tags])" + end, 1057);
        set(ConfPaths.STRINGS + ".chatfast", start + "acted like spamming (IP: [ip])" + end, 785);
        set(ConfPaths.STRINGS + ".noswing", start + "didn't swing arm" + end, 785);
        set(ConfPaths.STRINGS + ".passable", start + "moved into a block ([blocktype]) from [locationfrom] to [locationto] distance [distance] " + end, 785);
        set(ConfPaths.STRINGS + ".relog", start + "relogs too fast" + end, 785);
        set(ConfPaths.STRINGS + ".tellchatnormal", tell + "&cNCP: &eToo many messages, slow down...", 785);
        set(ConfPaths.STRINGS + ".tempkick1", "ncp tempkick [player] 1 Wait a minute!", 785);
        set(ConfPaths.STRINGS + ".tempkick5", "ncp tempkick [player] 5 You have five minutes to think about it!", 785);
        set(ConfPaths.STRINGS + ".vehicleenvelope", start + "moved a vehicle too fast ([tags])" + end, 785);

        // Compatibility settings.
        set(ConfPaths.COMPATIBILITY_EXEMPTIONS_WILDCARD_DEFAULT_METADATA_ACTIVE, true, 785);
        set(ConfPaths.COMPATIBILITY_EXEMPTIONS_WILDCARD_DEFAULT_METADATA_KEYS, Arrays.asList("nocheat.exempt"), 785);
        set(ConfPaths.COMPATIBILITY_EXEMPTIONS_WILDCARD_NPC_ACTIVE, true, 785);
        set(ConfPaths.COMPATIBILITY_EXEMPTIONS_WILDCARD_NPC_BUKKITINTERFACE, true, 785);
        set(ConfPaths.COMPATIBILITY_EXEMPTIONS_WILDCARD_NPC_METADATA_ACTIVE, true, 785);
        set(ConfPaths.COMPATIBILITY_EXEMPTIONS_WILDCARD_NPC_METADATA_KEYS, Arrays.asList("NPC"), 785);
        set(ConfPaths.COMPATIBILITY_EXEMPTIONS_REMOVE_JOIN, true, 785);
        set(ConfPaths.COMPATIBILITY_EXEMPTIONS_REMOVE_LEAVE, true, 785);
        set(ConfPaths.COMPATIBILITY_SERVER_CBDEDICATED_ENABLE, true, 785);
        set(ConfPaths.COMPATIBILITY_SERVER_CBREFLECT_ENABLE, true, 785);
        set(ConfPaths.COMPATIBILITY_BLOCKS + ConfPaths.SUB_BREAKINGTIME + ".IRON_BLOCK:PICKAXE:DIAMOND:12", 1139);
        set(ConfPaths.COMPATIBILITY_BLOCKS + ConfPaths.SUB_ALLOWINSTANTBREAK, new LinkedList<String>(), 785);
        set(ConfPaths.COMPATIBILITY_BLOCKS + ConfPaths.SUB_OVERRIDEFLAGS + "." + Material.SNOW.name().toLowerCase(), "default", 785);
        // Make blocks ign_passable+ground_height.
        for (final Material mat : Arrays.asList(
                Material.PISTON_MOVING_PIECE
                )) {
            set(ConfPaths.COMPATIBILITY_BLOCKS + ConfPaths.SUB_OVERRIDEFLAGS + "." + mat.name().toLowerCase(), "default+ign_passable+ground_height", 785);
        }
        set(ConfPaths.COMPATIBILITY_BLOCKS_CHANGETRACKER_ACTIVE, true, 1036); // With lastChangedBuildNumber.
        set(ConfPaths.COMPATIBILITY_BLOCKS_CHANGETRACKER_PISTONS, true, 785);
        set(ConfPaths.COMPATIBILITY_BLOCKS_CHANGETRACKER_MAXAGETICKS, 80, 785);
        set(ConfPaths.COMPATIBILITY_BLOCKS_CHANGETRACKER_PERWORLD_MAXENTRIES, 1000, 785);

        //        // Update internal factory based on all the new entries to the "actions" section.
        //        setActionFactory();
    }
}
