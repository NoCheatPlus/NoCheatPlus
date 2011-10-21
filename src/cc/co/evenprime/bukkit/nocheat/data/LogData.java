package cc.co.evenprime.bukkit.nocheat.data;

import org.bukkit.block.Block;

/**
 * Everything that could be relevant for logging or consolecommand actions
 */
public class LogData extends Data {

    public String                check;
    public int                   violationLevel;
    public final PreciseLocation toLocation = new PreciseLocation();
    public int                   packets;
    public String                text;
    public Block                 placed;
    public Block                 placedAgainst;
    public double                reachdistance;
    public float                 falldistance;
    public String                playerName;

}
