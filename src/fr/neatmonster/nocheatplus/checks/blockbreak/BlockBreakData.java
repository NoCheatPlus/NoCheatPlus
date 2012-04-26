package fr.neatmonster.nocheatplus.checks.blockbreak;

import fr.neatmonster.nocheatplus.checks.CheckData;
import fr.neatmonster.nocheatplus.utilities.locations.SimpleLocation;

/**
 * Player specific data for the blockbreak checks
 * 
 */
public class BlockBreakData extends CheckData {

    // Keep track of violation levels for the three checks
    public double               fastBreakVL                = 0.0D;
    public double               reachVL                    = 0.0D;
    public double               directionVL                = 0.0D;
    public double               noswingVL                  = 0.0D;

    // Used to know when the player has broken his previous block
    public long                 lastBreakTime              = 0;

    // Used to know if the previous event was refused
    public boolean              previousRefused            = false;

    // Used for the penalty time feature of the direction check
    public long                 directionLastViolationTime = 0;

    // Have a nicer/simpler way to work with block locations instead of
    // Bukkits own "Location" class
    public final SimpleLocation instaBrokenBlockLocation   = new SimpleLocation();
    public final SimpleLocation brokenBlockLocation        = new SimpleLocation();
    public final SimpleLocation lastDamagedBlock           = new SimpleLocation();

    // indicate if the player swung his arm since he got checked last time
    public boolean              armswung                   = true;

    // For logging, remember the reachDistance that was calculated in the
    // reach check
    public double               reachDistance;

}
