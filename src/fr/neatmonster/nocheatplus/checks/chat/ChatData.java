package fr.neatmonster.nocheatplus.checks.chat;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.checks.CheckData;
import fr.neatmonster.nocheatplus.checks.CheckDataFactory;

/*
 * MM'""""'YMM dP                  dP   M""""""'YMM            dP            
 * M' .mmm. `M 88                  88   M  mmmm. `M            88            
 *  * M  MMMMMooM 88d888b. .d8888b. d8888P M  MMMMM  M .d8888b. d8888P .d8888b. 
 * M  MMMMMMMM 88'  `88 88'  `88   88   M  MMMMM  M 88'  `88   88   88'  `88 
 * M. `MMM' .M 88    88 88.  .88   88   M  MMMM' .M 88.  .88   88   88.  .88 
 * MM.     .dM dP    dP `88888P8   dP   M       .MM `88888P8   dP   `88888P8 
 * MMMMMMMMMMM                          MMMMMMMMMMM                          
 */
/**
 * Player specific data for the chat checks.
 */
public class ChatData implements CheckData {

    /** The factory creating data. */
    public static final CheckDataFactory factory    = new CheckDataFactory() {
                                                        @Override
                                                        public final CheckData getData(final Player player) {
                                                            return ChatData.getData(player);
                                                        }
                                                    };

    /** The map containing the data per players. */
    private static Map<String, ChatData> playersMap = new HashMap<String, ChatData>();

    /**
     * Gets the data of a specified player.
     * 
     * @param player
     *            the player
     * @return the data
     */
    public synchronized static ChatData getData(final Player player) {
        if (!playersMap.containsKey(player.getName()))
            playersMap.put(player.getName(), new ChatData());
        return playersMap.get(player.getName());
    }

    // Violation levels.
    public double   colorVL;
    public double   noPwnageVL;

    // Data of the no pwnage check.
    public int      noPwnageCaptchTries;
    public String   noPwnageGeneratedCaptcha;
    public boolean  noPwnageHasFilledCaptcha;
    public boolean  noPwnageHasStartedCaptcha;
    public long     noPwnageJoinTime;
    public Location noPwnageLastLocation;
    public String   noPwnageLastMessage;
    public long     noPwnageLastMessageTime;
    public long     noPwnageLastMovedTime;
    public long     noPwnageLastWarningTime;
    public long     noPwnageLeaveTime;
    public int      noPwnageReloginWarnings;
    public long     noPwnageReloginWarningTime;

    /**
     * Clear the data of the no pwnage check.
     */
    public synchronized void clearNoPwnageData() {
        // TODO: re-think this sync [keep related to ChatData/NoPwnage/Color used lock.]
        noPwnageCaptchTries = noPwnageReloginWarnings = 0;
        noPwnageJoinTime = noPwnageLastMessageTime = noPwnageLastMovedTime = noPwnageLastWarningTime = noPwnageLeaveTime = noPwnageReloginWarningTime = 0L;
        noPwnageGeneratedCaptcha = noPwnageLastMessage = "";
        noPwnageLastLocation = null;
    }

}
