package fr.neatmonster.nocheatplus.checks.moving;

import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.actions.ParameterName;
import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.ViolationData;

/*
 * M"""""`'"""`YM                            MM"""""""`YM                   dP                  dP            
 * M  mm.  mm.  M                            MM  mmmmm  M                   88                  88            
 * M  MMM  MMM  M .d8888b. 88d888b. .d8888b. M'        .M .d8888b. .d8888b. 88  .dP  .d8888b. d8888P .d8888b. 
 * M  MMM  MMM  M 88'  `88 88'  `88 88ooood8 MM  MMMMMMMM 88'  `88 88'  `"" 88888"   88ooood8   88   Y8ooooo. 
 * M  MMM  MMM  M 88.  .88 88       88.  ... MM  MMMMMMMM 88.  .88 88.  ... 88  `8b. 88.  ...   88         88 
 * M  MMM  MMM  M `88888P' dP       `88888P' MM  MMMMMMMM `88888P8 `88888P' dP   `YP `88888P'   dP   `88888P' 
 * MMMMMMMMMMMMMM                            MMMMMMMMMMMM                                                     
 * 
 * M""MMMMM""M          dP       oo                   dP          
 * M  MMMMM  M          88                            88          
 * M  MMMMP  M .d8888b. 88d888b. dP .d8888b. dP    dP 88 .d8888b. 
 * M  MMMM' .M 88ooood8 88'  `88 88 88'  `"" 88    88 88 88ooood8 
 * M  MMP' .MM 88.  ... 88    88 88 88.  ... 88.  .88 88 88.  ... 
 * M     .dMMM `88888P' dP    dP dP `88888P' `88888P' dP `88888P' 
 * MMMMMMMMMMM                                                    
 */
/**
 * This check does the exact same thing as the MorePacket check but this one works for players inside vehicles.
 */
public class MorePacketsVehicle extends Check {

    /**
     * The usual number of packets per timeframe.
     * 
     * 20 would be for perfect internet connections, 22 is good enough.
     */
    private final static int packetsPerTimeframe = 22;

    /**
     * Instantiates a new more packet vehicle check.
     */
    public MorePacketsVehicle() {
        super(CheckType.MOVING_MOREPACKETSVEHICLE);
    }

    /**
     * Checks a player.
     * 
     * (More information on the MorePacket class.)
     * 
     * @param player
     *            the player
     * @param from
     *            the from
     * @param to
     *            the to
     * @param cc 
     * @param data 
     * @return the location
     */
    public Location check(final Player player, final Location from, final Location to, final MovingData data, final MovingConfig cc) {
    	// Take time once, first:
    	final long time = System.currentTimeMillis();

        Location newTo = null;

        if (!data.hasMorePacketsVehicleSetBack()){
        	// TODO: Check if other set-back is appropriate or if to set on other events.
        	data.setMorePacketsVehicleSetBack(from);
        	if (data.morePacketsVehicleTaskId != -1) {
        		// TODO: Set back outdated or not?
        		Bukkit.getScheduler().cancelTask(data.morePacketsVehicleTaskId);
        	}
        }

        // Take a packet from the buffer.
        data.morePacketsVehicleBuffer--;
        
        if (data.morePacketsVehicleTaskId != -1){
        	// Short version !
        	return data.getMorePacketsVehicleSetBack();
        }

        // Player used up buffer, he fails the check.
        if (data.morePacketsVehicleBuffer < 0) {
            data.morePacketsVehiclePackets = -data.morePacketsVehicleBuffer;

            // Increment violation level.
            data.morePacketsVehicleVL = -data.morePacketsVehicleBuffer;

            // Execute whatever actions are associated with this check and the violation level and find out if we should
            // cancel the event.
            if (executeActions(player, data.morePacketsVehicleVL, -data.morePacketsVehicleBuffer, cc.morePacketsVehicleActions)){
            	newTo = data.getMorePacketsVehicleSetBack();
            }
        }

        if (data.morePacketsVehicleLastTime + 1000 < time) {
            // More than 1 second elapsed, but how many?
            final double seconds = (time - data.morePacketsVehicleLastTime) / 1000D;

            // For each second, fill the buffer.
            data.morePacketsVehicleBuffer += packetsPerTimeframe * seconds;

            // If there was a long pause (maybe server lag?), allow buffer to grow up to 100.
            if (seconds > 2) {
                if (data.morePacketsVehicleBuffer > 100)
                    data.morePacketsVehicleBuffer = 100;
            } else if (data.morePacketsVehicleBuffer > 50)
                // Only allow growth up to 50.
                data.morePacketsVehicleBuffer = 50;

            // Set the new "last" time.
            data.morePacketsVehicleLastTime = time;

            // Set the new "setback" location.
            if (newTo == null)
                data.setMorePacketsVehicleSetBack(from);
        } else if (data.morePacketsVehicleLastTime > time)
            // Security check, maybe system time changed.
            data.morePacketsVehicleLastTime = time;

        if (newTo == null)
            return null;

        // Compose a new location based on coordinates of "newTo" and viewing direction of "event.getTo()" to allow the
        // player to look somewhere else despite getting pulled back by NoCheatPlus.
        return new Location(player.getWorld(), newTo.getX(), newTo.getY(), newTo.getZ(), to.getYaw(), to.getPitch());
    }
    
	@Override
	protected Map<ParameterName, String> getParameterMap(final ViolationData violationData) {
		final Map<ParameterName, String> parameters = super.getParameterMap(violationData);
		parameters.put(ParameterName.PACKETS, String.valueOf(MovingData.getData(violationData.player).morePacketsVehiclePackets));
		return parameters;
	}
}
