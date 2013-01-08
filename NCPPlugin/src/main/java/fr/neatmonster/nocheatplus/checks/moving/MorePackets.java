package fr.neatmonster.nocheatplus.checks.moving;

import java.util.Map;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.actions.ParameterName;
import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.ViolationData;
import fr.neatmonster.nocheatplus.utilities.PlayerLocation;

/*
 * M"""""`'"""`YM                            MM"""""""`YM                   dP                  dP            
 * M  mm.  mm.  M                            MM  mmmmm  M                   88                  88            
 * M  MMM  MMM  M .d8888b. 88d888b. .d8888b. M'        .M .d8888b. .d8888b. 88  .dP  .d8888b. d8888P .d8888b. 
 * M  MMM  MMM  M 88'  `88 88'  `88 88ooood8 MM  MMMMMMMM 88'  `88 88'  `"" 88888"   88ooood8   88   Y8ooooo. 
 * M  MMM  MMM  M 88.  .88 88       88.  ... MM  MMMMMMMM 88.  .88 88.  ... 88  `8b. 88.  ...   88         88 
 * M  MMM  MMM  M `88888P' dP       `88888P' MM  MMMMMMMM `88888P8 `88888P' dP   `YP `88888P'   dP   `88888P' 
 * MMMMMMMMMMMMMM                            MMMMMMMMMMMM                                                     
 */
/**
 * The MorePackets check (previously called Speedhack check) will try to identify players that send more than the usual
 * amount of move-packets to the server to be able to move faster than normal, without getting caught by the other
 * checks (flying/running).
 * 
 * It monitors the number of packets sent to the server within 1 second and compares it to the "legal" number of packets
 * for that timeframe (22).
 */
public class MorePackets extends Check {

    /**
     * The usual number of packets per timeframe.
     * 
     * 20 would be for perfect internet connections, 22 is good enough.
     */
    private final static int packetsPerTimeframe = 22;

    /**
     * Instantiates a new more packets check.
     */
    public MorePackets() {
        super(CheckType.MOVING_MOREPACKETS);
    }

    /**
     * Checks a player.
     * 
     * Players get assigned a certain amount of "free" packets as a limit initially. Every move packet reduces that
     * limit by 1. If more than 1 second of time passed, the limit gets increased by 22 * time in seconds, up to 50 and
     * he gets a new "setback" location. If the player reaches limit = 0 -> teleport him back to "setback". If there was
     * a long pause (maybe lag), limit may be up to 100.
     * 
     * @param player
     *            the player
     * @param from
     *            the from
     * @param to
     *            the to
     * @return the location
     */
    public Location check(final Player player, final PlayerLocation from, final PlayerLocation to, final MovingData data, final MovingConfig cc) {
    	// Take time once, first:
    	final long time = System.currentTimeMillis();
    	

        Location newTo = null;

        if (data.morePacketsSetback == null){
        	// TODO: Check if other set-back is appropriate or if to set on other events.
        	if (data.setBack != null) data.morePacketsSetback = data.getSetBack(to);
        	else data.morePacketsSetback = from.getLocation();
        }

        // Take a packet from the buffer.
        data.morePacketsBuffer--;

        // Player used up buffer, he fails the check.
        if (data.morePacketsBuffer < 0) {
            data.morePacketsPackets = -data.morePacketsBuffer;

            // Increment violation level.
            data.morePacketsVL = -data.morePacketsBuffer;

            // Execute whatever actions are associated with this check and the violation level and find out if we should
            // cancel the event.
            if (executeActions(player, data.morePacketsVL, -data.morePacketsBuffer,
                    MovingConfig.getConfig(player).morePacketsActions))
                newTo = data.teleported = data.morePacketsSetback;
        }

        if (data.morePacketsLastTime + 1000 < time) {
            // More than 1 second elapsed, but how many?
            final double seconds = (time - data.morePacketsLastTime) / 1000D;

            // For each second, fill the buffer.
            data.morePacketsBuffer += packetsPerTimeframe * seconds;

            // If there was a long pause (maybe server lag?), allow buffer to grow up to 100.
            if (seconds > 2) {
                if (data.morePacketsBuffer > 100)
                    data.morePacketsBuffer = 100;
            } else if (data.morePacketsBuffer > 50)
                // Only allow growth up to 50.
                data.morePacketsBuffer = 50;

            // Set the new "last" time.
            data.morePacketsLastTime = time;

            // Set the new "setback" location.
            if (newTo == null)
                data.morePacketsSetback = from.getLocation();
        } else if (data.morePacketsLastTime > time)
            // Security check, maybe system time changed.
            data.morePacketsLastTime = time;

        if (newTo == null)
            return null;

        // Compose a new location based on coordinates of "newTo" and viewing direction of "event.getTo()" to allow the
        // player to look somewhere else despite getting pulled back by NoCheatPlus.
        return new Location(player.getWorld(), newTo.getX(), newTo.getY(), newTo.getZ(), to.getYaw(), to.getPitch());
    }
    
	@Override
	protected Map<ParameterName, String> getParameterMap(final ViolationData violationData) {
		final Map<ParameterName, String> parameters = super.getParameterMap(violationData);
		parameters.put(ParameterName.PACKETS, String.valueOf(MovingData.getData(violationData.player).morePacketsPackets));
		return parameters;
	}
}
