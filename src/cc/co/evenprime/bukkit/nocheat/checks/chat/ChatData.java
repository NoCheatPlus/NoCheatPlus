package cc.co.evenprime.bukkit.nocheat.checks.chat;

import java.util.LinkedList;
import cc.co.evenprime.bukkit.nocheat.DataItem;

/**
 * 
 */
public class ChatData implements DataItem {

    public int                spamVL;
    public int                colorVL;

    public int                messageCount   = 0;
    public int                commandCount   = 0;
    public long               spamLastTime   = 0;
    public String             message        = "";
    public boolean            botcheckpassed = true;
    public LinkedList<String> spamBotFailed  = new LinkedList<String>();
}
