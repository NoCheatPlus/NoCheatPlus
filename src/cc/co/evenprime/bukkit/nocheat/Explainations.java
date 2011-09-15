package cc.co.evenprime.bukkit.nocheat;

import java.util.HashMap;
import java.util.Map;

/**
 * Textual explainations of options, will be displayed in the gui tool and the
 * descriptions.txt file.
 * 
 * @author Evenprime
 * 
 */
public class Explainations {

    private static final Map<String, String> explainations = new HashMap<String, String>();

    static {

        set("logging.active", "Should NoCheat related messages get logged at all. Some messages may still appear, e.g. error\n messages, even if this option is deactivated");

        set("logging.filename", "Where logs that go to the logfile are stored. You can have different files for different worlds.");
        set("logging.filelevel", "What log-level need messages to have to get stored in the logfile. Values are:\n low: all messages\n med: med and high messages only\n high: high messages only\n off: no messages at all.");
        set("logging.consolelevel", "What log-level need messages to have to get displayed in your server console. Values are:\n low: all messages\n med: med and high messages only\n high: high messages only\n off: no messages at all.");
        set("logging.chatlevel", "What log-level need messages to have to get displayed in the ingame chat. Values are:\n low: all messages\n med: med and high messages only\n high: high messages only\n off: no messages at all.");

        set("debug.showactivechecks", "Print to the console an overview of all checks that are enabled when NoCheat gets loaded.");
        
        set("moving.check", "If true, do various checks on PlayerMove events.");

        set("moving.flying.check", "If true, check if a player is flying too fast.\nIf this and 'running.check' are true, running check will be done instead.");
        set("moving.flying.speedlimitvertical", "Set the speed limit for moving vertical while flying.\nUnit is 1/100 of a block, default is 100.");
        set("moving.flying.speedlimithorizontal", "Set the speed limit for moving horizontal while flying.\nUnit is 1/100 of a block, default is 22.");
        set("moving.flying.actions", "What should be done if a player flies faster than the speed limit(s). \nUnits are in 1/100 of a block above the limit.");

        set("moving.running.check", "If true, check if a player is running too fast/jumping too high.\nIf this is true, 'flying.check' is ignored.");
        set("moving.running.speedlimit", "Set the speed limit for moving horizontal under 'normal' conditions.\nUnit is 1/100 of a block, default is 22.");
        set("moving.running.swimming.check", "If 'running.check' and this are active, use a seperate speed limit for swimming players.");
        set("moving.running.swimming.speedlimit", "Set the speed limit for moving horizontal while in water.\nUnit is 1/100 of a block, default is 18");
        set("moving.running.sneaking.check", "If 'running.check' and this are active, use a seperate speed limit for sneaking players.");
        set("moving.running.sneaking.speedlimit", "Set the speed limit for moving horizontal while sneaking.\nUnit is 1/100 of a block, default is 14");
        set("moving.running.actions", "What should be done if a player moves faster than the speed limit(s) or jumps higher than allowed.\nUnits are in 1/100 of a block above the limit.");

        set("moving.morepackets.check", "If true, check if a player is sending too many 'move-packets' per second. In a normal game, the player won't send more than 22 packets per second.");
        set("moving.morepackets.actions", "What should be done if a player sends more 'move-packets' than normal.\nUnits are packets per second above the limit.");

        set("moving.noclip.check", "If true, check if a player is moving into a solid wall. EXPERIMENTAL! DOESN'T WORK RELIABLY! USE WITH CAUTION AND ONLY FOR NOTIFICATIONS!");
        set("moving.noclip.actions", "What should be done if a player moves into a wall.\nUnit is number of walls a player walks into/through.");

        set("blockbreak.check", "If true, do various checks on BlockBreak events.");

        set("blockbreak.reach.check", "If true, check if a player is breaking blocks that are too far away.");
        set("blockbreak.reach.reachlimit", "Set the distance limit for breaking blocks.\nUnit is 1/100 of a block, default is 485");
        set("blockbreak.reach.actions", "What should be done if a player is breaking blocks that are too far away.\nUnit is number of break(attempt)s beyond the limit.");

        set("blockbreak.direction.check", "If true, check if a player is looking at the block that he's breaking.");
        set("blockbreak.direction.actions", "What should be done if a player is breaking blocks that are not in his line of sight.\nUnit is number of break(attempt)s outside the line of sight.");
        
        set("blockplace.check", "If true, do various checks on BlockPlace events.");

        set("blockplace.reach.check", "If true, check if a player is placing blocks at locations too far away.");
        set("blockplace.reach.reachlimit", "Set the distance limit for placing blocks.\nUnit is 1/100 of a block, default is 485");
        set("blockplace.reach.actions", "What should be done if a player is placing blocks that are too far away.\nUnit is number of place(attempt)s beyond the limit.");

        set("blockplace.onliquid.check", "If true, check if a player is trying to place non-liquid blocks against liquid blocks\nIn a normal Minecraft game, it is impossible to place a block without it touching something that is considered solid (neither air nor a liquid).\nBut if people use a modified client, to can do that. This check tries to identify that trick.");
        set("blockplace.onliquid.actions", "What should be done if a player is is trying to place non-liquid blocks against liquid blocks.\nUnit is number of place(attempt)s.");
        
        set("interact.check", "If true, do various checks on PlayerInteract events.");
        
        set("interact.durability.check", "If true, check if a player is using a hack that provides infinite durability items.");
        set("interact.durability.actions", "What should be done if a player is trying to use the hack.\nUnit is number of uses or attempts to use the hack.");

        set("chat.check", "If true, do various checks on PlayerChat events.");

        set("chat.spam.check", "If true, check if a player is spamming the chat.");
        set("chat.spam.timeframe", "Over what timeframe (in seconds) should the messages be counted?\nWhen the time is over, counting starts at 0 again.");
        set("chat.spam.limit", "How many messages per timeframe may the player send without it counting as spamming?");
        set("chat.spam.actions", "What should be done if a player is trying to spam the chat.\nUnit is number of chat messages above the limit you declared above.");

    }

    private static void set(String id, String text) {
        explainations.put(id, text);
    }

    public static String get(String id) {
        String result = explainations.get(id);

        if(result == null) {
            result = "No description available";
        }

        return result;
    }
}
