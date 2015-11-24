package fr.neatmonster.nocheatplus.checks.chat;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.checks.access.AsyncCheckData;
import fr.neatmonster.nocheatplus.checks.access.CheckDataFactory;
import fr.neatmonster.nocheatplus.checks.access.ICheckData;
import fr.neatmonster.nocheatplus.utilities.ds.count.ActionFrequency;

/**
 * Player specific data for the chat checks.
 */
public class ChatData extends AsyncCheckData {

    /** The factory creating data. */
    public static final CheckDataFactory factory = new CheckDataFactory() {
        @Override
        public final ICheckData getData(final Player player) {
            return ChatData.getData(player);
        }

        @Override
        public ICheckData removeData(final String playerName) {
            return ChatData.removeData(playerName);
        }

        @Override
        public void removeAllData() {
            clear();
        }
    };

    /** The map containing the data per players. */
    private static final Map<String, ChatData> playersMap = new HashMap<String, ChatData>();

    /**
     * Gets the data of a specified player.
     * 
     * @param player
     *            the player
     * @return the data
     */
    public static synchronized ChatData getData(final Player player) {
        if (!playersMap.containsKey(player.getName()))
            playersMap.put(player.getName(), new ChatData(ChatConfig.getConfig(player)));
        return playersMap.get(player.getName());
    }

    public static synchronized ICheckData removeData(final String playerName) {
        return playersMap.remove(playerName);
    }

    public static synchronized void clear(){
        playersMap.clear();
    }

    // Violation levels.
    public double  captchaVL;
    public double  colorVL;
    public double  commandsVL;
    public double  textVL;
    public double  relogVL;

    // Captcha data.
    public int     captchTries;
    public String  captchaGenerated;
    public boolean captchaStarted;

    /// Commands data.
    public final ActionFrequency commandsWeights = new ActionFrequency(5, 1000);
    public long commandsShortTermTick;
    public double commandsShortTermWeight;

    // Data of the text check.
    public final ActionFrequency chatFrequency = new ActionFrequency(10, 3000);
    public final ActionFrequency chatShortTermFrequency = new ActionFrequency(6, 500);


    // Data of the no pwnage check.     
    public String  chatLastMessage;
    public long    chatLastTime;
    public long    chatWarningTime;



    public int     relogWarnings;
    public long    relogWarningTime;

    public ChatData(final ChatConfig config) {
        super(config);
    }

    /**
     * Clear the data of the no pwnage check.
     */
    public synchronized void reset() {
        captchTries = relogWarnings = 0;
        captchaVL = 0D;
        // colorVL <- is spared to avoid problems with spam + captcha success.
        textVL = 0;
        final long now = System.currentTimeMillis();
        chatFrequency.clear(now);
        chatShortTermFrequency.clear(now);
        chatLastTime = relogWarningTime = 0L;
        captchaGenerated = chatLastMessage = "";
        chatLastTime = 0;
        chatWarningTime = 0;
        commandsShortTermTick = 0;
        commandsWeights.clear(now);
    }

}
