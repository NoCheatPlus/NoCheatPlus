package fr.neatmonster.nocheatplus.checks.chat;

import fr.neatmonster.nocheatplus.checks.CheckData;
import fr.neatmonster.nocheatplus.utilities.locations.SimpleLocation;

/**
 * Player specific data for the chat checks
 * 
 */
public class ChatData extends CheckData {

    // Keep track of the violation levels for the check
    public int                  colorVL;

    // Remember the player's location
    public final SimpleLocation location            = new SimpleLocation();

    // Remember the last chat message or command for logging purposes
    public String               message             = "";
    public String               lastMessage         = "";
    public long                 lastMessageTime;

    // Remember if the message is a command or not
    public boolean              isCommand           = false;

    // Remember some other time informations about the player
    public long                 joinTime;
    public long                 leaveTime;
    public long                 lastWarningTime;
    public long                 lastRelogWarningTime;
    public long                 lastMovedTime;

    // Remember how many time the player has repeated the same message
    public int                  messageRepeated;

    // Remember some warning levels
    public int                  relogWarnings;
    public int                  speedRepeated;

    // Remember some data about captcha
    public String               captchaAnswer       = "";
    public String               captchaQuestion     = "";
    public boolean              captchaDone         = false;
    public boolean              captchaStarted      = false;
    public int                  captchaTries;

    // Remember if commands have been run by the player
    public boolean              commandsHaveBeenRun = false;

    // Remember reason and player's IP
    public String               reason              = "";
    public String               ip                  = "";

    public boolean compareLocation(final SimpleLocation l) {
        return location != null && location.x == l.x && location.y == l.y && location.z == l.z;
    }

    public void setLocation(final SimpleLocation l) {
        location.x = l.x;
        location.y = l.y;
        location.z = l.z;
    }
}
