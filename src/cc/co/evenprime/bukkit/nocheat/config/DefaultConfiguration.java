package cc.co.evenprime.bukkit.nocheat.config;

import cc.co.evenprime.bukkit.nocheat.actions.types.ActionList;

/**
 * These are the default settings for NoCheat. They will be used
 * in addition to/in replacement of configurations given in the
 * config.yml file
 * 
 */
public class DefaultConfiguration extends NoCheatConfiguration {

    public DefaultConfiguration() {

        set(ConfPaths.STRINGS + ".drop", "[player] failed [check]: Tried to drop more items than allowed. VL [violations]");
        set(ConfPaths.STRINGS + ".moveshort", "[player] failed [check]. VL [violations]");
        set(ConfPaths.STRINGS + ".movelong", "[player] in [world] at [location] moving to [locationto] over distance [movedistance] failed check [check]. Total violation level so far [violations]");
        set(ConfPaths.STRINGS + ".nofall", "[player] failed [check]: tried to avoid fall damage for ~[falldistance] blocks. VL [violations]");
        set(ConfPaths.STRINGS + ".morepackets", "[player] failed [check]: Sent [packets] more packets than expected. Total violation level [violations]");
        set(ConfPaths.STRINGS + ".bbreach", "[player] failed [check]: tried to interact with a block over distance [reachdistance]. VL [violations]");
        set(ConfPaths.STRINGS + ".bbdirection", "[player] failed [check]: tried to interact with a block out of line of sight. VL [violations]");
        set(ConfPaths.STRINGS + ".bbnoswing", "[player] failed [check]: Didn't swing arm. VL [violations]");
        set(ConfPaths.STRINGS + ".bpreach", "[player] failed [check]: tried to interact with a block over distance [reachdistance]. VL [violations]");
        set(ConfPaths.STRINGS + ".bpdirection", "[player] failed [check]: tried to interact with a block out of line of sight. VL [violations]");
        set(ConfPaths.STRINGS + ".color", "[player] failed [check]: Sent colored chat message '[text]'. VL [violations]");
        set(ConfPaths.STRINGS + ".spam", "[player] failed [check]: Last sent message '[text]'. VL [violations]");
        set(ConfPaths.STRINGS + ".fdirection", "[player] failed [check]: tried to interact with a block out of line of sight. VL [violations]");
        set(ConfPaths.STRINGS + ".fnoswing", "[player] failed [check]: Didn't swing arm. VL [violations]");
        set(ConfPaths.STRINGS + ".kick", "kick [player]");

        // Update internal factory based on all the new entries to the "actions" section
        regenerateActionLists();

        /** LOGGING **/
        set(ConfPaths.LOGGING_ACTIVE, true);
        set(ConfPaths.LOGGING_SHOWACTIVECHECKS, false);
        set(ConfPaths.LOGGING_PREFIX, "&4NC&f: ");
        set(ConfPaths.LOGGING_FILENAME, "nocheat.log");
        set(ConfPaths.LOGGING_LOGTOFILE, true);
        set(ConfPaths.LOGGING_LOGTOCONSOLE, true);
        set(ConfPaths.LOGGING_LOGTOINGAMECHAT, true);

        /*** INVENTORY ***/
        set(ConfPaths.INVENTORY_DROP_CHECK, true);
        set(ConfPaths.INVENTORY_DROP_TIMEFRAME, 20);
        set(ConfPaths.INVENTORY_DROP_LIMIT, 100);

        ActionList dropActionList = new ActionList();
        dropActionList.setActions(0, createActions("log:drop:0:1:cif", "cmd:kick"));
        set(ConfPaths.INVENTORY_DROP_ACTIONS, dropActionList);

        /*** MOVING ***/
        set(ConfPaths.MOVING_RUNFLY_CHECK, true);
        set(ConfPaths.MOVING_RUNFLY_ALLOWFASTSNEAKING, false);

        ActionList movingActionList = new ActionList();
        movingActionList.setActions(0, createActions("log:moveshort:3:5:f", "cancel"));
        movingActionList.setActions(100, createActions("log:moveshort:0:5:if", "cancel"));
        movingActionList.setActions(400, createActions("log:movelong:0:5:cif", "cancel"));
        set(ConfPaths.MOVING_RUNFLY_ACTIONS, movingActionList);

        set(ConfPaths.MOVING_RUNFLY_CHECKNOFALL, true);

        ActionList nofallActionList = new ActionList();
        nofallActionList.setActions(0, createActions("log:nofall:0:5:cif", "cancel"));
        set(ConfPaths.MOVING_RUNFLY_NOFALLACTIONS, nofallActionList);

        set(ConfPaths.MOVING_RUNFLY_FLYING_ALLOWALWAYS, false);
        set(ConfPaths.MOVING_RUNFLY_FLYING_ALLOWINCREATIVE, true);
        set(ConfPaths.MOVING_RUNFLY_FLYING_SPEEDLIMITHORIZONTAL, 60);
        set(ConfPaths.MOVING_RUNFLY_FLYING_SPEEDLIMITVERTICAL, 100);
        set(ConfPaths.MOVING_RUNFLY_FLYING_HEIGHTLIMIT, 250);

        ActionList flyingActionList = new ActionList();
        flyingActionList.setActions(0, createActions("log:moveShort:3:5:f", "cancel"));
        flyingActionList.setActions(100, createActions("log:moveShort:0:5:if", "cancel"));
        flyingActionList.setActions(400, createActions("log:moveLong:0:5:cif", "cancel"));
        set(ConfPaths.MOVING_RUNFLY_FLYING_ACTIONS, flyingActionList);

        set(ConfPaths.MOVING_MOREPACKETS_CHECK, true);

        ActionList morepacketsActionList = new ActionList();
        morepacketsActionList.setActions(0, createActions("log:morepackets:3:2:f", "cancel"));
        morepacketsActionList.setActions(30, createActions("log:morepackets:0:2:if", "cancel"));
        morepacketsActionList.setActions(60, createActions("log:morepackets:0:2:cif", "cancel"));
        set(ConfPaths.MOVING_MOREPACKETS_ACTIONS, morepacketsActionList);

        /*** BLOCKBREAK ***/

        set(ConfPaths.BLOCKBREAK_REACH_CHECK, true);

        ActionList breakreachActionList = new ActionList();
        breakreachActionList.setActions(0, createActions("cancel"));
        breakreachActionList.setActions(5, createActions("log:bbreach:0:2:if", "cancel"));
        set(ConfPaths.BLOCKBREAK_REACH_ACTIONS, breakreachActionList);

        set(ConfPaths.BLOCKBREAK_DIRECTION_CHECK, true);
        set(ConfPaths.BLOCKBREAK_DIRECTION_PRECISION, 50);
        set(ConfPaths.BLOCKBREAK_DIRECTION_PENALTYTIME, 300);
        ActionList breakdirectionActionList = new ActionList();
        breakdirectionActionList.setActions(0, createActions("cancel"));
        breakdirectionActionList.setActions(10, createActions("log:bbdirection:0:5:cif", "cancel"));
        set(ConfPaths.BLOCKBREAK_DIRECTION_ACTIONS, breakdirectionActionList);

        set(ConfPaths.BLOCKBREAK_NOSWING_CHECK, true);
        ActionList breaknoswingActionList = new ActionList();
        breaknoswingActionList.setActions(0, createActions("log:bbnoswing:0:2:cif", "cancel"));
        set(ConfPaths.BLOCKBREAK_NOSWING_ACTIONS, breaknoswingActionList);

        /*** BLOCKPLACE ***/

        set(ConfPaths.BLOCKPLACE_REACH_CHECK, true);

        ActionList placereachActionList = new ActionList();
        placereachActionList.setActions(0, createActions("cancel"));
        placereachActionList.setActions(5, createActions("log:bpreach:0:2:cif", "cancel"));
        set(ConfPaths.BLOCKPLACE_REACH_ACTIONS, placereachActionList);

        set(ConfPaths.BLOCKPLACE_DIRECTION_CHECK, true);
        set(ConfPaths.BLOCKPLACE_DIRECTION_PRECISION, 75);
        set(ConfPaths.BLOCKPLACE_DIRECTION_PENALTYTIME, 100);
        ActionList placedirectionActionList = new ActionList();
        placedirectionActionList.setActions(0, createActions("cancel"));
        placedirectionActionList.setActions(10, createActions("log:bpdirection:0:3:cif", "cancel"));
        set(ConfPaths.BLOCKPLACE_DIRECTION_ACTIONS, placedirectionActionList);

        /*** CHAT ***/
        set(ConfPaths.CHAT_COLOR_CHECK, true);
        ActionList colorActionList = new ActionList();
        colorActionList.setActions(0, createActions("log:color:0:1:cif", "cancel"));
        set(ConfPaths.CHAT_COLOR_ACTIONS, colorActionList);

        set(ConfPaths.CHAT_SPAM_CHECK, true);
        set(ConfPaths.CHAT_SPAM_WHITELIST, "");
        set(ConfPaths.CHAT_SPAM_TIMEFRAME, 5);
        set(ConfPaths.CHAT_SPAM_LIMIT, 5);

        ActionList spamActionList = new ActionList();
        spamActionList.setActions(0, createActions("log:spam:0:5:cif", "cancel"));
        spamActionList.setActions(50, createActions("log:spam:0:5:cif", "cancel", "cmd:kick"));
        set(ConfPaths.CHAT_SPAM_ACTIONS, spamActionList);

        /*** FIGHT ***/

        set(ConfPaths.FIGHT_DIRECTION_CHECK, true);
        set(ConfPaths.FIGHT_DIRECTION_PRECISION, 75);
        set(ConfPaths.FIGHT_DIRECTION_PENALTYTIME, 500);

        ActionList directionActionList = new ActionList();
        directionActionList.setActions(0, createActions("cancel"));
        directionActionList.setActions(5, createActions("log:fdirection:3:5:f", "cancel"));
        directionActionList.setActions(20, createActions("log:fdirection:0:5:cf", "cancel"));
        directionActionList.setActions(50, createActions("log:fdirection:0:5:cif", "cancel"));
        set(ConfPaths.FIGHT_DIRECTION_ACTIONS, directionActionList);

        set(ConfPaths.FIGHT_NOSWING_CHECK, true);
        ActionList fightnoswingActionList = new ActionList();
        fightnoswingActionList.setActions(0, createActions("log:fnoswing:0:5:cif", "cancel"));
        set(ConfPaths.FIGHT_NOSWING_ACTIONS, fightnoswingActionList);

    }
}
