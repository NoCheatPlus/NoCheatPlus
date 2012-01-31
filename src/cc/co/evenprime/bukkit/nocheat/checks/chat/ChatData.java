package cc.co.evenprime.bukkit.nocheat.checks.chat;

import java.util.Map;
import cc.co.evenprime.bukkit.nocheat.DataItem;

/**
 * 
 */
public class ChatData implements DataItem {

    public int    spamVL;
    public int    spamTotalVL;
    public int    spamFailed;
    public int    colorVL;
    public int    colorTotalVL;
    public int    colorFailed;

    public int    messageCount = 0;
    public int    commandCount = 0;
    public long   spamLastTime = 0;
    public String message      = "";

    @Override
    public void collectData(Map<String, Object> map) {
        map.put("chat.spam.vl", (int) spamTotalVL);
        map.put("chat.color.vl", (int) colorTotalVL);
        map.put("chat.spam.failed", spamFailed);
        map.put("chat.color.failed", colorFailed);
    }

    @Override
    public void clearCriticalData() {

    }
}
