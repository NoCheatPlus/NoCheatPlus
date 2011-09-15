package cc.co.evenprime.bukkit.nocheat;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import cc.co.evenprime.bukkit.nocheat.config.tree.ActionListOption;
import cc.co.evenprime.bukkit.nocheat.config.tree.BooleanOption;
import cc.co.evenprime.bukkit.nocheat.config.tree.ConfigurationTree;
import cc.co.evenprime.bukkit.nocheat.config.tree.IntegerOption;
import cc.co.evenprime.bukkit.nocheat.config.tree.LogLevelOption;
import cc.co.evenprime.bukkit.nocheat.config.tree.MediumStringOption;
import cc.co.evenprime.bukkit.nocheat.config.tree.ParentOption;
import cc.co.evenprime.bukkit.nocheat.log.LogLevel;

/**
 * The place where the structure of the configuration tree is defined, the
 * default settings are defined, the default files are defined.
 * 
 * @author Evenprime
 * 
 */
public class DefaultConfiguration {

    /**
     * Create a full default options tree
     * 
     * @return
     */
    public static ConfigurationTree buildDefaultConfigurationTree() {

        ConfigurationTree root = new ConfigurationTree();

        /*** LOGGING section ***/
        {
            ParentOption loggingNode = new ParentOption("logging");
            root.add(loggingNode);

            loggingNode.add(new BooleanOption("active", true, true));
            loggingNode.add(new MediumStringOption("filename", "nocheat.log"));
            loggingNode.add(new LogLevelOption("filelevel", LogLevel.LOW));
            loggingNode.add(new LogLevelOption("consolelevel", LogLevel.HIGH));
            loggingNode.add(new LogLevelOption("chatlevel", LogLevel.MED));
        }
        
        /*** LOGGING section ***/
        {
            ParentOption debugNode = new ParentOption("debug");
            root.add(debugNode);

            debugNode.add(new BooleanOption("showactivechecks", false, false));
        }

        /*** MOVING ***/
        {
            ParentOption movingNode = new ParentOption("moving");
            root.add(movingNode);

            movingNode.add(new BooleanOption("check", true, true));

            /**** MOVING.FLYING ****/
            {
                ParentOption flyingNode = new ParentOption("flying");
                movingNode.add(flyingNode);

                flyingNode.add(new BooleanOption("check", true, true));
                flyingNode.add(new IntegerOption("speedlimitvertical", 100));
                flyingNode.add(new IntegerOption("speedlimithorizontal", 22));

                ActionListOption actions = new ActionListOption("actions");

                flyingNode.add(actions);

                actions.add(0, "moveLogLowShort moveCancel");
                actions.add(100, "moveLogMedShort moveCancel");
                actions.add(400, "moveLogHighShort moveCancel");
            }

            /**** MOVING.RUNNING ****/
            {
                ParentOption runningNode = new ParentOption("running");
                movingNode.add(runningNode);

                runningNode.add(new BooleanOption("check", true, true));
                runningNode.add(new IntegerOption("speedlimit", 22));

                /**** MOVING.RUNNING.SWIMMING ****/
                {
                    ParentOption swimmingNode = new ParentOption("swimming");

                    runningNode.add(swimmingNode);

                    swimmingNode.add(new BooleanOption("check", true, true));
                    swimmingNode.add(new IntegerOption("speedlimit", 18));
                }
                /**** MOVING.RUNNING.SNEAKING ****/
                {
                    ParentOption sneakingNode = new ParentOption("sneaking");

                    runningNode.add(sneakingNode);

                    sneakingNode.add(new BooleanOption("check", true, true));
                    sneakingNode.add(new IntegerOption("speedlimit", 14));
                }

                ActionListOption actions = new ActionListOption("actions");

                runningNode.add(actions);

                actions.add(0, "moveLogLowShort moveCancel");
                actions.add(100, "moveLogMedShort moveCancel");
                actions.add(400, "moveLogHighShort moveCancel");
            }

            /**** MOVING.MOREPACKETS ****/
            {
                ParentOption morePacketsNode = new ParentOption("morepackets");
                movingNode.add(morePacketsNode);

                morePacketsNode.add(new BooleanOption("check", true, true));

                ActionListOption actions = new ActionListOption("actions");

                morePacketsNode.add(actions);

                actions.add(0, "morepacketsLow moveCancel");
                actions.add(30, "morepacketsMed moveCancel");
                actions.add(60, "morepacketsHigh moveCancel");
            }

            /**** MOVING.NOCLIP ****/
            {
                ParentOption noclipNode = new ParentOption("noclip");
                movingNode.add(noclipNode);

                noclipNode.add(new BooleanOption("check", false, true));
                ActionListOption actions = new ActionListOption("actions");

                noclipNode.add(actions);

                actions.add(1, "noclipLog");
            }
        }

        /****** BLOCKBREAK ******/
        {
            ParentOption interactNode = new ParentOption("blockbreak");
            root.add(interactNode);

            interactNode.add(new BooleanOption("check", true, true));

            /**** BLOCKBREAK.REACH ****/
            {
                ParentOption reachNode = new ParentOption("reach");
                interactNode.add(reachNode);

                reachNode.add(new BooleanOption("check", true, true));
                reachNode.add(new IntegerOption("reachlimit", 485));

                ActionListOption actions = new ActionListOption("actions");

                reachNode.add(actions);

                actions.add(0, "reachLog blockbreakCancel");
            }

            /**** BLOCKBREAK.DIRECTION ****/
            {
                ParentOption directionNode = new ParentOption("direction");
                interactNode.add(directionNode);

                directionNode.add(new BooleanOption("check", true, true));

                ActionListOption actions = new ActionListOption("actions");

                directionNode.add(actions);

                actions.add(0, "directionLog blockbreakCancel");
            }
        }

        /****** BLOCKPLACE ******/
        {
            ParentOption blockPlaceNode = new ParentOption("blockplace");
            root.add(blockPlaceNode);

            blockPlaceNode.add(new BooleanOption("check", true, true));

            /**** BLOCKPLACE.REACH ****/
            {
                ParentOption reachNode = new ParentOption("reach");
                blockPlaceNode.add(reachNode);

                reachNode.add(new BooleanOption("check", true, true));
                reachNode.add(new IntegerOption("reachlimit", 485));

                ActionListOption actions = new ActionListOption("actions");

                reachNode.add(actions);

                actions.add(0, "reachLog blockplaceCancel");
            }

            /**** BLOCKPLACE.ONLIQUID ****/
            {
                ParentOption onliquidNode = new ParentOption("onliquid");
                blockPlaceNode.add(onliquidNode);

                onliquidNode.add(new BooleanOption("check", true, true));

                ActionListOption actions = new ActionListOption("actions");

                onliquidNode.add(actions);

                actions.add(0, "onliquidLog blockplaceCancel");
            }

        }

        /****** INTERACT ******/
        {
            ParentOption interactNode = new ParentOption("interact");
            root.add(interactNode);

            interactNode.add(new BooleanOption("check", true, true));

            /**** BLOCKBREAK.REACH ****/
            {
                ParentOption durabilityNode = new ParentOption("durability");
                interactNode.add(durabilityNode);

                durabilityNode.add(new BooleanOption("check", true, true));

                ActionListOption actions = new ActionListOption("actions");

                durabilityNode.add(actions);

                actions.add(0, "durabilityLog interactCancel");
            }
        }

        /****** CHAT ******/
        {
            ParentOption chatNode = new ParentOption("chat");
            root.add(chatNode);

            chatNode.add(new BooleanOption("check", true, true));

            /**** CHAT.SPAM ****/
            {
                ParentOption spamNode = new ParentOption("spam");
                chatNode.add(spamNode);

                spamNode.add(new BooleanOption("check", false, true));
                spamNode.add(new IntegerOption("timeframe", 5));
                spamNode.add(new IntegerOption("limit", 5));

                ActionListOption actions = new ActionListOption("actions");

                spamNode.add(actions);

                actions.add(0, "spamLog spamCancel");
            }
        }
        return root;
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

            w(w, "# This file contains the definitions of the default actions of NoCheat.");
            w(w, "# DO NOT EDIT THIS FILE DIRECTLY. If you want to change any of these, copy");
            w(w, "# them to your \"actions.txt\" file and modify them there. If an action with");
            w(w, "# the same name exists here and in your file, yours will be used.");
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
            w(w, "log moveLogLowShort 3 15 low NC: [player] failed [check]");
            w(w, "log moveLogMedShort 0 15 med NC: [player] failed [check]");
            w(w, "log moveLogHighShort 0 15 high NC: [player] failed [check]");
            w(w, "");
            w(w, "# Gives a log message of the violation, only containing name, violation type and total violation value, at most once every second, only if more than 5 violations happened within the last minute (low) and immediatly (med,high)");
            w(w, "log morepacketsLow 5 1 low NC: [player] failed [check]: Sent [packets] more packets than expected. Total violation level [violations].");
            w(w, "log morepacketsMed 0 1 med NC: [player] failed [check]: Sent [packets] more packets than expected. Total violation level [violations].");
            w(w, "log morepacketsHigh 0 1 high NC: [player] failed [check]: Sent [packets] more packets than expected. Total violation level [violations].");
            w(w, "");
            w(w, "# Gives a lengthy log message of the violation, containing name, location, violation type and total violation, at most once every 15 seconds, only if more than 3 violations happened within the last minute (low) and immediatly (med,high)");
            w(w, "log moveLogLowLong 3 15 low NC: [player] in [world] at [location] moving to [locationto] over distance [distance] failed check [check]. Total violation level so far [violations].");
            w(w, "log moveLogMedLong 0 15 med NC: [player] in [world] at [location] moving to [locationto] over distance [distance] failed check [check]. Total violation level so far [violations].");
            w(w, "log moveLogHighLong 0 15 high NC: [player] in [world] at [location] moving to [locationto] over distance [distance] failed check [check]. Total violation level so far [violations].");
            w(w, "");
            w(w, "# Some other log messages that are limited a bit by default, to avoid too extreme spam");
            w(w, "log noclipLog 0 1 high NC: [player] failed [check]: at [location] to [locationto].");
            w(w, "log reachLog 0 1 med NC: [player] failed [check]: tried to interact with a block over distance [distance].");
            w(w, "log directionLog 2 1 med NC: [player] failed [check]: tried to destroy a block out of line of sight.");
            w(w, "log durabilityLog 0 1 med NC: [player] failed [check]: tried to use infinity durability hack.");
            w(w, "log onliquidLog 2 1 med NC: [player] failed [check]: tried to place a block on liquids.");
            w(w, "log spamLog 0 4 med NC: [player] failed [check]: Last sent message \"[text]\".");
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
            w(w, "special interactCancel 0 0");
            w(w, "special spamCancel 0 0");
            w(w, "");
            w(w, "# CONSOLECOMMAND Actions: They will execute a command as if it were typed into the console.");
            w(w, "#   - They start with the word 'consolecommand'");
            w(w, "#   - Then comes their name. That name is used in the config file to identify them");
            w(w, "#   - Then comes the 'delay', that is how often has this action to be called before it really gets executed");
            w(w, "#   - Then comes the 'repeat', that is how many seconds have to be between two executions of the action");
            w(w, "#   - Then comes the command. You can use the same [ ] that you use for log actions. You'll most likely want to use [player] at some point.");
            w(w, "");
            w(w, "# E.g. Kick a player");
            w(w, "consolecommand kick 0 0 kick [player]");
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
