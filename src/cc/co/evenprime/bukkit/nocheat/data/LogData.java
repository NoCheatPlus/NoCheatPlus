package cc.co.evenprime.bukkit.nocheat.data;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

/**
 * Everything that could be relevant for logging or consolecommand actions
 */
public class LogData extends Data {

    public Player   player;
    public String   check;
    public int      violationLevel;
    public Location toLocation;
    public int      packets;
    public String   text;
    public Block    placed;
    public Block    placedAgainst;
    public double   reachdistance;
    public float    falldistance;

    public void initialize(Player player) {
        this.player = player;
        check = "";
        toLocation = player.getLocation();
    }
}
