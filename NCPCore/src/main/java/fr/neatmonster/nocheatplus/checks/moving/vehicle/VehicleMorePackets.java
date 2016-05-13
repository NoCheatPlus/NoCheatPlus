package fr.neatmonster.nocheatplus.checks.moving.vehicle;

import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.actions.ParameterName;
import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.ViolationData;
import fr.neatmonster.nocheatplus.checks.moving.MovingConfig;
import fr.neatmonster.nocheatplus.checks.moving.MovingData;
import fr.neatmonster.nocheatplus.checks.moving.location.setback.SetBackEntry;
import fr.neatmonster.nocheatplus.checks.moving.model.VehicleMoveData;

/**
 * This check does the exact same thing as the MorePacket check but this one works for players inside vehicles.
 */
public class VehicleMorePackets extends Check {

    /**
     * The usual number of packets per timeframe.
     * 
     * 20 would be for perfect internet connections, 22 is good enough.
     */
    private final static int packetsPerTimeframe = 22;

    /**
     * Instantiates a new more packet vehicle check.
     */
    public VehicleMorePackets() {
        super(CheckType.MOVING_VEHICLE_MOREPACKETS);
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
    public SetBackEntry check(final Player player, final VehicleMoveData thisMove, 
            final boolean allowSetSetBack, final MovingData data, final MovingConfig cc) {
        // Take time once, first:
        final long time = System.currentTimeMillis();

        SetBackEntry newTo = null;

        // Take a packet from the buffer.
        data.vehicleMorePacketsBuffer--;

        if (data.vehicleSetBackTaskId != -1){
            // Short version !
            // TODO: This is bad. Needs to check if still scheduled (a BukkitTask thing) and just skip.
            return data.vehicleSetBacks.getValidMidTermEntry();
        }

        // Player used up buffer, they fail the check.
        if (data.vehicleMorePacketsBuffer < 0) {

            // Increment violation level.
            data.vehicleMorePacketsVL = -data.vehicleMorePacketsBuffer;

            // Execute whatever actions are associated with this check and the violation level and find out if we should
            // cancel the event.
            final ViolationData vd = new ViolationData(this, player, data.vehicleMorePacketsVL, -data.vehicleMorePacketsBuffer, cc.vehicleMorePacketsActions);
            if (data.debug || vd.needsParameters()) {
                vd.setParameter(ParameterName.PACKETS, Integer.toString(-data.vehicleMorePacketsBuffer));
            }
            if (executeActions(vd).willCancel()){
                newTo = data.vehicleSetBacks.getValidMidTermEntry();
            }
        }

        if (data.vehicleMorePacketsLastTime + 1000 < time) {
            // More than 1 second elapsed, but how many?
            final double seconds = (time - data.vehicleMorePacketsLastTime) / 1000D;

            // For each second, fill the buffer.
            data.vehicleMorePacketsBuffer += packetsPerTimeframe * seconds;

            // If there was a long pause (maybe server lag?), allow buffer to grow up to 100.
            if (seconds > 2) {
                if (data.vehicleMorePacketsBuffer > 100) {
                    data.vehicleMorePacketsBuffer = 100;
                }
            } else if (data.vehicleMorePacketsBuffer > 50) {
                // Only allow growth up to 50.
                data.vehicleMorePacketsBuffer = 50;
            }

            // Set the new "last" time.
            data.vehicleMorePacketsLastTime = time;

            // Set the new set-back location.
            if (allowSetSetBack && newTo == null) {
                data.vehicleSetBacks.setMidTermEntry(thisMove.from);
                if (data.debug) {
                    debug(player, "Update vehicle morepackets set-back: " + thisMove.from);
                }
            }
        } else if (data.vehicleMorePacketsLastTime > time) {
            // Security check, maybe system time changed.
            data.vehicleMorePacketsLastTime = time;
        }

        return newTo;
    }

}
