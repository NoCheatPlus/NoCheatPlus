package cc.co.evenprime.bukkit.nocheat.config;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import cc.co.evenprime.bukkit.nocheat.config.util.ActionList;
import cc.co.evenprime.bukkit.nocheat.config.util.ActionMapper;
import cc.co.evenprime.bukkit.nocheat.log.LogLevel;

/**
 * The place where the structure of the configuration tree is defined, the
 * default settings are defined, the default files are defined.
 * 
 */
public class DefaultConfiguration extends Configuration {

    public DefaultConfiguration(ActionMapper action) {
        super(null, false);

        /*** LOGGING ***/
        {
            setValue(LOGGING_ACTIVE, true);
            setValue(LOGGING_PREFIX, "&4NC&f: ");
            setValue(LOGGING_FILENAME, "nocheat.log");
            setValue(LOGGING_FILELEVEL, LogLevel.LOW);
            setValue(LOGGING_CONSOLELEVEL, LogLevel.HIGH);
            setValue(LOGGING_CHATLEVEL, LogLevel.MED);
        }

        /*** DEBUG ***/
        {
            setValue(DEBUG_SHOWACTIVECHECKS, false);
        }

        /*** MOVING ***/
        {
            setValue(MOVING_CHECK, true);
            setValue(MOVING_IDENTIFYCREATIVEMODE, true);

            setValue(MOVING_RUNFLY_CHECK, true);

            setValue(MOVING_RUNFLY_WALKINGSPEEDLIMIT, 22);
            setValue(MOVING_RUNFLY_SPRINTINGSPEEDLIMIT, 40);
            setValue(MOVING_RUNFLY_JUMPHEIGHT, 135);

            setValue(MOVING_RUNFLY_CHECKSNEAKING, true);
            setValue(MOVING_RUNFLY_SNEAKINGSPEEDLIMIT, 14);

            setValue(MOVING_RUNFLY_CHECKSWIMMING, true);
            setValue(MOVING_RUNFLY_SWIMMINGSPEEDLIMIT, 18);

            ActionList movingActionList = new ActionList();
            movingActionList.setActions(0, action.getActions("moveLogLowShort moveCancel".split(" ")));
            movingActionList.setActions(100, action.getActions("moveLogMedShort moveCancel".split(" ")));
            movingActionList.setActions(400, action.getActions("moveLogHighShort moveCancel".split(" ")));
            setValue(MOVING_RUNFLY_ACTIONS, movingActionList);

            setValue(MOVING_RUNFLY_CHECKNOFALL, true);
            setValue(MOVING_RUNFLY_NOFALLMULTIPLIER, 200);

            ActionList nofallActionList = new ActionList();
            nofallActionList.setActions(0, action.getActions("nofallLog nofallDamage".split(" ")));
            setValue(MOVING_RUNFLY_NOFALLACTIONS, nofallActionList);

            setValue(MOVING_RUNFLY_ALLOWLIMITEDFLYING, false);
            setValue(MOVING_RUNFLY_FLYINGSPEEDLIMITVERTICAL, 100);
            setValue(MOVING_RUNFLY_FLYINGSPEEDLIMITHORIZONTAL, 60);

            ActionList flyingActionList = new ActionList();
            flyingActionList.setActions(0, action.getActions("moveLogLowShort moveCancel".split(" ")));
            flyingActionList.setActions(100, action.getActions("moveLogMedShort moveCancel".split(" ")));
            flyingActionList.setActions(400, action.getActions("moveLogHighShort moveCancel".split(" ")));
            setValue(MOVING_RUNFLY_FLYINGACTIONS, flyingActionList);

            setValue(MOVING_MOREPACKETS_CHECK, true);

            ActionList morepacketsActionList = new ActionList();
            morepacketsActionList.setActions(0, action.getActions("morepacketsLow moveCancel".split(" ")));
            morepacketsActionList.setActions(30, action.getActions("morepacketsMed moveCancel".split(" ")));
            morepacketsActionList.setActions(60, action.getActions("morepacketsHigh moveCancel".split(" ")));
            setValue(MOVING_MOREPACKETS_ACTIONS, morepacketsActionList);

        }

        /*** BLOCKBREAK ***/
        {
            setValue(BLOCKBREAK_CHECK, true);

            setValue(BLOCKBREAK_REACH_CHECK, true);
            setValue(BLOCKBREAK_REACH_LIMIT, 485);

            ActionList reachActionList = new ActionList();
            reachActionList.setActions(0, action.getActions("blockbreakCancel".split(" ")));
            reachActionList.setActions(5, action.getActions("reachLog blockbreakCancel".split(" ")));
            setValue(BLOCKBREAK_REACH_ACTIONS, reachActionList);

            setValue(BLOCKBREAK_DIRECTION_CHECK, true);

            setValue(BLOCKBREAK_DIRECTION_CHECKINSTABREAKBLOCKS, true);
            setValue(BLOCKBREAK_DIRECTION_PRECISION, 50);
            setValue(BLOCKBREAK_DIRECTION_PENALTYTIME, 300);

            ActionList directionActionList = new ActionList();
            directionActionList.setActions(0, action.getActions("blockbreakCancel".split(" ")));
            directionActionList.setActions(10, action.getActions("directionLog blockbreakCancel".split(" ")));
            setValue(BLOCKBREAK_DIRECTION_ACTIONS, directionActionList);

            setValue(BLOCKBREAK_NOSWING_CHECK, true);
            ActionList noswingActionList = new ActionList();
            noswingActionList.setActions(0, action.getActions("noswingLog blockbreakCancel".split(" ")));
            setValue(BLOCKBREAK_NOSWING_ACTIONS, noswingActionList);
        }

        /*** BLOCKPLACE ***/
        {
            setValue(BLOCKPLACE_CHECK, true);

            setValue(BLOCKPLACE_REACH_CHECK, true);
            setValue(BLOCKPLACE_REACH_LIMIT, 485);

            ActionList reachActionList = new ActionList();
            reachActionList.setActions(0, action.getActions("blockplaceCancel".split(" ")));
            reachActionList.setActions(5, action.getActions("reachLog blockplaceCancel".split(" ")));
            setValue(BLOCKPLACE_REACH_ACTIONS, reachActionList);

            setValue(BLOCKPLACE_ONLIQUID_CHECK, true);

            ActionList onliquidActionList = new ActionList();
            onliquidActionList.setActions(0, action.getActions("blockplaceCancel".split(" ")));
            onliquidActionList.setActions(3, action.getActions("onliquidLog blockplaceCancel".split(" ")));
            setValue(BLOCKPLACE_ONLIQUID_ACTIONS, onliquidActionList);

            setValue(BLOCKPLACE_NOSWING_CHECK, true);
            ActionList noswingActionList = new ActionList();
            noswingActionList.setActions(0, action.getActions("noswingLog blockplaceCancel".split(" ")));
            setValue(BLOCKPLACE_NOSWING_ACTIONS, noswingActionList);

        }

        /*** CHAT ***/
        {
            setValue(CHAT_CHECK, true);

            setValue(CHAT_SPAM_CHECK, true);
            setValue(CHAT_SPAM_WHITELIST, "");
            setValue(CHAT_SPAM_TIMEFRAME, 5);
            setValue(CHAT_SPAM_LIMIT, 5);

            ActionList spamActionList = new ActionList();
            spamActionList.setActions(0, action.getActions("spamLog spamCancel".split(" ")));
            spamActionList.setActions(50, action.getActions("spamLog spamCancel spamkick".split(" ")));
            setValue(CHAT_SPAM_ACTIONS, spamActionList);
        }

        /*** FIGHT ***/
        {
            setValue(FIGHT_CHECK, true);

            setValue(FIGHT_DIRECTION_CHECK, true);
            setValue(FIGHT_DIRECTION_PRECISION, 75);
            setValue(FIGHT_DIRECTION_PENALTYTIME, 500);

            ActionList directionActionList = new ActionList();
            directionActionList.setActions(0, action.getActions("fightCancel".split(" ")));
            directionActionList.setActions(5, action.getActions("fightDirectionLogLow fightCancel".split(" ")));
            directionActionList.setActions(20, action.getActions("fightDirectionLog fightCancel".split(" ")));
            directionActionList.setActions(50, action.getActions("fightDirectionLogHigh fightCancel".split(" ")));
            setValue(FIGHT_DIRECTION_ACTIONS, directionActionList);

            setValue(FIGHT_SELFHIT_CHECK, true);
            ActionList selfhitActionList = new ActionList();
            selfhitActionList.setActions(0, action.getActions("fightSelfhitLog fightCancel".split(" ")));
            setValue(FIGHT_SELFHIT_ACTIONS, selfhitActionList);

            setValue(FIGHT_NOSWING_CHECK, true);
            ActionList noswingActionList = new ActionList();
            noswingActionList.setActions(0, action.getActions("noswingLog fightCancel".split(" ")));
            setValue(FIGHT_NOSWING_ACTIONS, noswingActionList);
        }

        /*** TIMED ***/
        {
            setValue(TIMED_CHECK, true);

            setValue(TIMED_GODMODE_CHECK, true);
            setValue(TIMED_GODMODE_TICKSLIMIT, 50);

            ActionList directionActionList = new ActionList();
            directionActionList.setActions(0, action.getActions("godmodeCancel".split(" ")));
            directionActionList.setActions(100, action.getActions("godmodeLog godmodeCancel".split(" ")));
            setValue(TIMED_GODMODE_ACTIONS, directionActionList);
        }
    }

    public static void writeActionFile(File file) {
        BufferedWriter w;

        try {
            if(!file.exists()) {
                try {
                    file.getParentFile().mkdirs();
                    file.createNewFile();
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }

            w = new BufferedWriter(new FileWriter(file));

            w(w, "# This file contains the definitions of your personal actions for NoCheat.");
            w(w, "# Look at the file default_actions.txt for inspiration on how it works.");
            w(w, "");
            w.flush();
            w.close();
        } catch(IOException e) {
            e.printStackTrace();
        }

    }

    public static void writeDefaultActionFile(File file) {

        BufferedWriter w;

        try {
            if(!file.exists()) {
                try {
                    file.getParentFile().mkdirs();
                    file.createNewFile();
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }

            w = new BufferedWriter(new FileWriter(file));

            w(w, "# This file contains the definitions of the default actions of NoCheat.");
            w(w, "# DO NOT EDIT THIS FILE DIRECTLY. If you want to change any of these, copy");
            w(w, "# them to your \"actions.txt\" file and modify them there. If an action with");
            w(w, "# the same name exists here and in your file, yours will be used.");
            w(w, "#");
            w(w, "# LOG Actions: They will print messages in your log file, console, chat, ...");
            w(w, "#   - They start with the word 'log'");
            w(w, "#   - Then comes their name. That name is used in the config file to identify them");
            w(w, "#   - Then comes the 'delay', that is how often has this action to be called before it really gets executed");
            w(w, "#   - Then comes the 'repeat', that is how many seconds have to be between two executions of the action");
            w(w, "#   - Then comes the 'loglevel', that is how the log message gets categorized (low, med, high)");
            w(w, "#   - Then comes the 'message', depending on where the action is used, different keywords in [ ] may be used");
            w(w, "");
            w(w, "# Gives a very short log message of the violation, only containing name, violation type and total violation value, at most once every 15 seconds, only if more than 3 violations happened within the last minute (low) and immediatly (med,high)");
            w(w, "log moveLogLowShort 3 15 low [player] failed [check]. VL [violations]");
            w(w, "log moveLogMedShort 0 15 med [player] failed [check]. VL [violations]");
            w(w, "log moveLogHighShort 0 15 high [player] failed [check]. VL [violations]");
            w(w, "");
            w(w, "# Gives a log message of the violation, only containing name, violation type and total violation value, at most once every second, only if more than 5 violations happened within the last minute (low) and immediatly (med,high)");
            w(w, "log morepacketsLow 5 1 low [player] failed [check]: Sent [packets] more packets than expected. Total violation level [violations].");
            w(w, "log morepacketsMed 0 1 med [player] failed [check]: Sent [packets] more packets than expected. Total violation level [violations].");
            w(w, "log morepacketsHigh 0 1 high [player] failed [check]: Sent [packets] more packets than expected. Total violation level [violations].");
            w(w, "");
            w(w, "# Gives a lengthy log message of the violation, containing name, location, violation type and total violation, at most once every 15 seconds, only if more than 3 violations happened within the last minute (low) and immediatly (med,high)");
            w(w, "log moveLogLowLong 3 15 low [player] in [world] at [location] moving to [locationto] over distance [movedistance] failed check [check]. Total violation level so far [violations].");
            w(w, "log moveLogMedLong 0 15 med [player] in [world] at [location] moving to [locationto] over distance [movedistance] failed check [check]. Total violation level so far [violations].");
            w(w, "log moveLogHighLong 0 15 high [player] in [world] at [location] moving to [locationto] over distance [movedistance] failed check [check]. Total violation level so far [violations].");
            w(w, "");
            w(w, "# Some other log messages that are limited a bit by default, to avoid too extreme spam");
            w(w, "log reachLog 0 5 med [player] failed [check]: tried to interact with a block over distance [reachdistance]. VL [violations]");
            w(w, "log directionLog 2 5 med [player] failed [check]: tried to destroy a block out of line of sight. VL [violations]");
            w(w, "log onliquidLog 2 5 med [player] failed [check]: tried to place a [blocktype] block at [placelocation] against block at [placeagainst]. VL [violations]");
            w(w, "log spamLog 0 5 med [player] failed [check]: Last sent message \"[text]\". VL [violations]");
            w(w, "log nofallLog 0 5 med [player] failed [check]: tried to avoid fall damage for ~[falldistance] blocks. VL [violations]");
            w(w, "log godmodeLog 0 5 med [player] failed [check]: lagging or using godmode. VL [violations]");
            w(w, "log noswingLog 2 5 med [player] failed [check]: Didn't swing arm. VL [violations]");
            w(w, "");
            w(w, "");
            w(w, "# Some log messages related to fighting, displaying the same text, but with different level (Info, Warning, Severe)");
            w(w, "log fightDirectionLogLow 0 5 low [player] failed [check]: tried to attack out of sight entity. Total violation level so far [violations].");
            w(w, "log fightDirectionLog 0 5 med [player] failed [check]: tried to attack out of sight entity. Total violation level so far [violations].");
            w(w, "log fightDirectionLogHigh 0 5 high [player] failed [check]: tried to attack out of sight entity. Total violation level so far [violations].");
            w(w, "");
            w(w, "log fightSelfhitlog 0 1 high [player] failed [check]: tried to attack himself. Total violation level so far [violations].");
            w(w, "");
            w(w, "# SPECIAL Actions: They will do something check dependant, usually cancel an event.");
            w(w, "#   - They start with the word 'special'");
            w(w, "#   - Then comes their name. That name is used in the config file to identify them");
            w(w, "#   - Then comes the 'delay', that is how often has this action to be called before it really gets executed");
            w(w, "#   - Then comes the 'repeat', that is how many seconds have to be between two executions of the action");
            w(w, "#   - Then come further instructions, if necessary");
            w(w, "");
            w(w, "# Cancels the event in case of an violation. Always. No delay. These are equivalent. The different names are just for better readability");
            w(w, "special moveCancel 0 0");
            w(w, "special blockbreakCancel 0 0");
            w(w, "special blockplaceCancel 0 0");
            w(w, "special spamCancel 0 0");
            w(w, "special nofallDamage 0 0");
            w(w, "special fightCancel 0 0");
            w(w, "special godmodeCancel 0 0");
            w(w, "");
            w(w, "# CONSOLECOMMAND Actions: They will execute a command as if it were typed into the console.");
            w(w, "#   - They start with the word 'consolecommand'");
            w(w, "#   - Then comes their name. That name is used in the config file to identify them");
            w(w, "#   - Then comes the 'delay', that is how often has this action to be called before it really gets executed");
            w(w, "#   - Then comes the 'repeat', that is how many seconds have to be between two executions of the action");
            w(w, "#   - Then comes the command. You can use the same [ ] that you use for log actions. You'll most likely want to use [player] at some point.");
            w(w, "");
            w(w, "# E.g. Kick a player");
            w(w, "consolecommand kick 0 1 kick [player]");
            w(w, "consolecommand spamkick 0 1 kick [player]");
            w.flush();
            w.close();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    private static void w(BufferedWriter writer, String text) throws IOException {
        writer.write(text);
        writer.newLine();
    }
}
