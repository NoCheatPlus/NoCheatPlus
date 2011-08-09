package cc.co.evenprime.bukkit.nocheat.data;

import org.bukkit.Location;

public class SpeedhackData {

    public int       lastCheckTicks        = 0;        // timestamp of last
                                                        // check for speedhacks
    public Location  setBackPoint          = null;
    public int       eventsSinceLastCheck  = 0;        // used to identify
                                                        // speedhacks
    public final int violationsInARow[]    = {0, 0, 0};
    public int       violationsInARowTotal = 0;

}
