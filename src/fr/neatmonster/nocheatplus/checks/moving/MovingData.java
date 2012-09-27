package fr.neatmonster.nocheatplus.checks.moving;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.checks.access.ACheckData;
import fr.neatmonster.nocheatplus.checks.access.CheckDataFactory;
import fr.neatmonster.nocheatplus.checks.access.ICheckData;
import fr.neatmonster.nocheatplus.utilities.PlayerLocation;

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
 * Player specific data for the moving checks.
 */
public class MovingData extends ACheckData {

	/** The factory creating data. */
	public static final CheckDataFactory factory = new CheckDataFactory() {
		@Override
		public final ICheckData getData(final Player player) {
			return MovingData.getData(player);
		}

		@Override
		public ICheckData removeData(final String playerName) {
			return MovingData.removeData(playerName);
		}
	};

    /** The map containing the data per players. */
    private static Map<String, MovingData> playersMap = new HashMap<String, MovingData>();

    /**
     * Gets the data of a specified player.
     * 
     * @param player
     *            the player
     * @return the data
     */
    public static MovingData getData(final Player player) {
        if (!playersMap.containsKey(player.getName()))
            playersMap.put(player.getName(), new MovingData());
        return playersMap.get(player.getName());
    }

    public static ICheckData removeData(final String playerName) {
		return playersMap.remove(playerName);
	}

	// Violation levels.
    public double         creativeFlyVL            = 0D;
    public double         morePacketsVL            = 0D;
    public double         morePacketsVehicleVL     = 0D;
    public double         noFallVL                 = 0D;
    public double         survivalFlyVL            = 0D;

    // Data shared between the fly checks.
//    public int            bunnyhopDelay;
    public double         horizontalBuffer;
    public double         horizontalFreedom;
    public double         horizontalVelocityCounter;
    public double         jumpAmplifier;
    public double         verticalFreedom;
    public double         verticalVelocity;
    public int            verticalVelocityCounter;

    // Data of the creative check.
    public boolean        creativeFlyPreviousRefused;

    // Data of the more packets check.
    public int            morePacketsBuffer        = 50;
    public long           morePacketsLastTime;
    public int            morePacketsPackets;
    public Location       morePacketsSetback;

    // Data of the more packets vehicle check.
    public int            morePacketsVehicleBuffer = 50;
    public long           morePacketsVehicleLastTime;
    public int            morePacketsVehiclePackets;
    public Location       morePacketsVehicleSetback;

    // Data of the no fall check.
    public double         noFallFallDistance;
    public boolean        noFallOnGround;
    public boolean        noFallWasOnGround;

    // Data of the survival fly check.
    public int            survivalFlyJumpPhase;
    public double         survivalFlyLastFromY;
    public int            survivalFlyOnIce;
    public boolean        survivalFlyWasInBed;

    // Locations shared between all checks.
    public final PlayerLocation from   = new PlayerLocation();
    public Location       ground;
    public Location       setBack;
    public Location       teleported;
    public final PlayerLocation to     = new PlayerLocation();

    /**
     * Clear the data of the fly checks.
     */
    public void clearFlyData() {
//        bunnyhopDelay = 0;
        noFallFallDistance = 0D;
        survivalFlyJumpPhase = 0;
        setBack = null;
        clearNoFallData();
    }

    /**
     * Clear the data of the more packets checks.
     */
    public void clearMorePacketsData() {
        morePacketsSetback = null;
        morePacketsVehicleSetback = null;
    }

    /**
     * Clear the data of the new fall check.
     */
    public void clearNoFallData() {
        noFallOnGround = noFallWasOnGround = true;
        noFallFallDistance = 0D;
    }
}
