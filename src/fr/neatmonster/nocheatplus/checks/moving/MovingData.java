package fr.neatmonster.nocheatplus.checks.moving;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.checks.CheckData;
import fr.neatmonster.nocheatplus.checks.CheckDataFactory;

/*
 * M"""""`'"""`YM                   oo                   M""""""'YMM            dP            
 * M  mm.  mm.  M                                        M  mmmm. `M            88            
 * M  MMM  MMM  M .d8888b. dP   .dP dP 88d888b. .d8888b. M  MMMMM  M .d8888b. d8888P .d8888b. 
 * M  MMM  MMM  M 88'  `88 88   d8' 88 88'  `88 88'  `88 M  MMMMM  M 88'  `88   88   88'  `88 
 * M  MMM  MMM  M 88.  .88 88 .88'  88 88    88 88.  .88 M  MMMM' .M 88.  .88   88   88.  .88 
 * M  MMM  MMM  M `88888P' 8888P'   dP dP    dP `8888P88 M       .MM `88888P8   dP   `88888P8 
 * MMMMMMMMMMMMMM                                    .88 MMMMMMMMMMM                          
 *                                               d8888P                                       
 */
/**
 * Player specific dataFactory for the moving checks.
 */
public class MovingData implements CheckData {

    /** The factory creating data. */
    public static final CheckDataFactory   factory    = new CheckDataFactory() {
                                                          @Override
                                                          public final CheckData getData(final Player player) {
                                                              return MovingData.getData(player);
                                                          }
                                                      };

    /** The map containing the dataFactory per players. */
    private static Map<String, MovingData> playersMap = new HashMap<String, MovingData>();

    /**
     * Gets the dataFactory of a specified player.
     * 
     * @param player
     *            the player
     * @return the dataFactory
     */
    public static MovingData getData(final Player player) {
        if (!playersMap.containsKey(player.getName()))
            playersMap.put(player.getName(), new MovingData());
        return playersMap.get(player.getName());
    }

    // Violation levels.
    public double     creativeFlyVL            = 0D;
    public double     morePacketsVL            = 0D;
    public double     morePacketsVehicleVL     = 0D;
    public double     noFallVL                 = 0D;
    public double     survivalFlyVL            = 0D;

    // Data shared between the fly checks.
    public int        bunnyhopDelay;
    public double     horizontalBuffer;
    public double     horizontalFreedom;
    public double     horizontalVelocityCounter;
    public double     jumpAmplifier;
    public double     verticalFreedom;
    public double     verticalVelocity;
    public int        verticalVelocityCounter;

    public Location[] lastSafeLocations        = new Location[] {null, null};

    // Data of the creative check.
    public boolean    creativeFlyPreviousRefused;

    // Data of the more packets check.
    public int        morePacketsBuffer        = 50;
    public long       morePacketsLastTime;
    public int        morePacketsPackets;
    public Location   morePacketsSetback;

    // Data of the more packets vehicle check.
    public int        morePacketsVehicleBuffer = 50;
    public long       morePacketsVehicleLastTime;
    public int        morePacketsVehiclePackets;
    public Location   morePacketsVehicleSetback;

    // Data of the no fall check.
    public double     noFallFallDistance;
    public boolean    noFallOnGroundClient;
    public boolean    noFallOnGroundServer;
    public boolean    noFallWasOnGroundClient;
    public boolean    noFallWasOnGroundServer;

    // Data of the survival fly check.
    public int        survivalFlyJumpPhase;
    public double[]   survivalFlyLastDistances = new double[] {0D, 0D};
    public int        survivalFlyOnIce;
    public boolean    survivalFlyWasInBed;

    // Locations shared between all checks.
    public Location   from;
    public Location   to;
    public Location   setBack;
    public Location   teleported;

    /**
     * Clear the dataFactory of the fly checks.
     */
    public void clearFlyData() {
        bunnyhopDelay = 0;
        noFallFallDistance = 0D;
        noFallOnGroundClient = noFallOnGroundServer = noFallWasOnGroundClient = noFallWasOnGroundServer = true;
        survivalFlyJumpPhase = 0;
        setBack = null;
    }

    /**
     * Clear the dataFactory of the more packets checks.
     */
    public void clearMorePacketsData() {
        morePacketsSetback = null;
        morePacketsVehicleSetback = null;
    }
}
