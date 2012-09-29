package fr.neatmonster.nocheatplus.checks.chat;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.checks.access.AsyncCheckData;
import fr.neatmonster.nocheatplus.checks.access.CheckDataFactory;
import fr.neatmonster.nocheatplus.checks.access.ICheckData;
import fr.neatmonster.nocheatplus.utilities.ActionFrequency;

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
            playersMap.put(player.getName(), new ChatData());
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
    public double  globalChatVL;
    public double noPwnageVL;
    
    // Data of the globalchat check.
    public final ActionFrequency globalChatFrequency = new ActionFrequency(10, 3000);

    // Data of the no pwnage check.
    public int     noPwnageCaptchTries;
    public String  noPwnageGeneratedCaptcha;
    public boolean noPwnageHasStartedCaptcha;
    public long    noPwnageJoinTime;
    public String  noPwnageLastMessage;
    public long    noPwnageLastMessageTime;
    public long    noPwnageLastWarningTime;
    public long    noPwnageLeaveTime;
    public int     noPwnageReloginWarnings;
    public long    noPwnageReloginWarningTime;
    public final ActionFrequency noPwnageSpeed = new ActionFrequency(5, 1000);

    /**
     * Clear the data of the no pwnage check.
     */
    public synchronized void clearNoPwnageData() {
        noPwnageCaptchTries = noPwnageReloginWarnings = 0;
        captchaVL = 0D;
        // colorVL <- is spared to avoid problems with spam + captcha success.
        noPwnageVL = 0;
        noPwnageSpeed.clear(System.currentTimeMillis());
        noPwnageJoinTime = noPwnageLastMessageTime = noPwnageLastWarningTime = noPwnageLeaveTime = noPwnageReloginWarningTime = 0L;
        noPwnageGeneratedCaptcha = noPwnageLastMessage = "";
    }

}
