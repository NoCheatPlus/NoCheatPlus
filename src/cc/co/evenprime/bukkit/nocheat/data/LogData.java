package cc.co.evenprime.bukkit.nocheat.data;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

/**
 * Everything that could be relevant for logging or consolecommand actions
 * 
 * @author Evenprime
 *
 */
public class LogData {

    // The player never changes
    public final Player player;
    public String check;
    public int violationLevel;
    public Location toLocation;
    public int packets;
    public String text;
    public Material placedMaterial;
    public Block placed;
    public Block placedAgainst;
    public double reachdistance;
    public float falldistance;
    
    public LogData(Player player) {
        this.player = player;
    }
}
