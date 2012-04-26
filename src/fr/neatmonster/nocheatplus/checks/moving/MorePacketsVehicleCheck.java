package fr.neatmonster.nocheatplus.checks.moving;

import java.util.Locale;

import fr.neatmonster.nocheatplus.actions.ParameterName;
import fr.neatmonster.nocheatplus.players.NCPPlayer;
import fr.neatmonster.nocheatplus.players.informations.Statistics.Id;

/**
 * The morePacketsVehiculeCheck will try to identify players that send more than
 * the usual amount of vehicule-move-packets to the server to be able to move
 * faster than normal, without getting caught by the other checks (flying/running).
 * 
 * It monitors the number of packets sent to the server within 1 second and
 * compares it to the "legal" number of packets for that timeframe (22).
 * 
 */
public class MorePacketsVehicleCheck extends MovingCheck {

    // 20 would be for perfect internet connections, 22 is good enough
    private final static int packetsPerTimeframe = 22;

    public MorePacketsVehicleCheck() {
        super("morepacketsvehicle");
    }

    /**
     * 1. Players get assigned a certain amount of "free" packets as a limit initially
     * 2. Every move packet reduces that limit by 1
     * 3. If more than 1 second of time passed, the limit gets increased
     * by 22 * time in seconds, up to 50 and he gets a new "setback" location
     * 4. If the player reaches limit = 0 -> teleport him back to "setback"
     * 5. If there was a long pause (maybe lag), limit may be up to 100
     * 
     */
    public boolean check(final NCPPlayer player, final Object... args) {
        final MovingConfig cc = getConfig(player);
        final MovingData data = getData(player);

        boolean cancel = false;

        final long time = System.currentTimeMillis();

        // Take a packet from the buffer
        data.morePacketsVehicleBuffer--;

        // Player used up buffer, he fails the check
        if (data.morePacketsVehicleBuffer < 0) {

            data.morePacketsVehicleVL = -data.morePacketsVehicleBuffer;
            incrementStatistics(player, Id.MOV_MOREPACKETSVEHICLE, 1);

            data.packetsVehicle = -data.morePacketsVehicleBuffer;

            // Execute whatever actions are associated with this check and the
            // violation level and find out if we should cancel the event
            cancel = executeActions(player, cc.morePacketsVehicleActions, data.morePacketsVehicleVL);
        }

        if (data.morePacketsVehicleLastTime + 1000 < time) {
            // More than 1 second elapsed, but how many?
            final double seconds = (time - data.morePacketsVehicleLastTime) / 1000D;

            // For each second, fill the buffer
            data.morePacketsVehicleBuffer += packetsPerTimeframe * seconds;

            // If there was a long pause (maybe server lag?)
            // Allow buffer to grow up to 100
            if (seconds > 2) {
                if (data.morePacketsVehicleBuffer > 100)
                    data.morePacketsVehicleBuffer = 100;
            } else if (data.morePacketsVehicleBuffer > 50)
                data.morePacketsVehicleBuffer = 50;

            // Set the new "last" time
            data.morePacketsVehicleLastTime = time;
        } else if (data.morePacketsVehicleLastTime > time)
            // Security check, maybe system time changed
            data.morePacketsVehicleLastTime = time;

        return cancel;
    }

    @Override
    public String getParameter(final ParameterName wildcard, final NCPPlayer player) {

        if (wildcard == ParameterName.VIOLATIONS)
            return String.format(Locale.US, "%d", (int) getData(player).morePacketsVehicleVL);
        else if (wildcard == ParameterName.PACKETS)
            return String.format(Locale.US, "%d", getData(player).packetsVehicle);
        else
            return super.getParameter(wildcard, player);
    }
}
