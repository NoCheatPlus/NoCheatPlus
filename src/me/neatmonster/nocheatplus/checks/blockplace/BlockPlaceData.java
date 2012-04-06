package me.neatmonster.nocheatplus.checks.blockplace;

import me.neatmonster.nocheatplus.DataItem;
import me.neatmonster.nocheatplus.data.SimpleLocation;

/**
 * Player specific data for the blockbreak checks
 * 
 */
public class BlockPlaceData implements DataItem {

    // Keep track of violation levels for the two checks
    public double               fastPlaceVL                = 0.0D;
    public double               reachVL                    = 0.0D;
    public double               directionVL                = 0.0D;

    // Used the know when the player has placed his previous block
    public long                 lastPlaceTime              = 0;

    // Used for the penalty time feature of the direction check
    public long                 directionLastViolationTime = 0;

    // Have a nicer/simpler way to work with block locations instead of
    // Bukkits own "Location" class
    public final SimpleLocation blockPlacedAgainst         = new SimpleLocation();
    public final SimpleLocation blockPlaced                = new SimpleLocation();

    // For logging, remember the reachDistance that was calculated in the
    // reach check
    public double               reachdistance;
}
