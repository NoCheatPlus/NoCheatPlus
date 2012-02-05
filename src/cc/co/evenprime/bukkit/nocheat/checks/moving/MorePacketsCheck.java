package cc.co.evenprime.bukkit.nocheat.checks.moving;

import java.util.Locale;
import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.NoCheatPlayer;
import cc.co.evenprime.bukkit.nocheat.actions.ParameterName;
import cc.co.evenprime.bukkit.nocheat.config.Permissions;
import cc.co.evenprime.bukkit.nocheat.data.PreciseLocation;

/**
 * The morePacketsCheck (previously called SpeedhackCheck) will try to identify
 * players that send more than the usual amount of move-packets to the server to
 * be able to move faster than normal, without getting caught by the other
 * checks (flying/running).
 * 
 * It monitors the number of packets sent to the server within 1 second and
 * compares it to the "legal" number of packets for that timeframe (22).
 * 
 */
public class MorePacketsCheck extends MovingCheck {

    private final static int packetsPerTimeframe = 22;

    public MorePacketsCheck(NoCheat plugin) {
        super(plugin, "moving.morepackets", Permissions.MOVING_MOREPACKETS);
    }

    /**
     * 1. Players get assigned a certain amount of "free" packets as a limit initially
     * 2. Every move packet reduces that limit by 1
     * 3. If more than 1 second of time passed, the limit gets increased
     *    by 22 * time in seconds, up to 50 and he gets a new "setback" location
     * 4. If the player reaches limit = 0 -> teleport him back to "setback"
     * 5. If there was a long pause (maybe lag), limit may be up to 100
     * 
     */
    public PreciseLocation check(NoCheatPlayer player, MovingData data, MovingConfig cc) {

        PreciseLocation newToLocation = null;

        if(!data.morePacketsSetbackPoint.isSet()) {
            data.morePacketsSetbackPoint.set(data.from);
        }

        long time = System.currentTimeMillis();

        // Take a packet from the buffer
        data.morePacketsBuffer--;

        // Player used up buffer, he fails the check
        if(data.morePacketsBuffer < 0) {

            data.morePacketsVL = -data.morePacketsBuffer;
            data.morePacketsTotalVL++;
            data.morePacketsFailed++;

            data.packets = -data.morePacketsBuffer;

            final boolean cancel = executeActions(player, cc.morePacketsActions.getActions(data.morePacketsVL));

            if(cancel)
                newToLocation = data.morePacketsSetbackPoint;
        }

        if(data.morePacketsLastTime + 1000 < time) {
            // More than 1 second elapsed, but how many?
            double seconds = ((double)(time - data.morePacketsLastTime)) / 1000D;

            // For each second, fill the buffer
            data.morePacketsBuffer += packetsPerTimeframe * seconds;

            // If there was a long pause (maybe server lag?)
            // Allow buffer to grow up to 100
            if(seconds > 2) {
                if(data.morePacketsBuffer > 100) {
                    data.morePacketsBuffer = 100;
                }
                // Else only allow growth up to 50
            } else {
                if(data.morePacketsBuffer > 50) {
                    data.morePacketsBuffer = 50;
                }
            }

            // Set the new "last" time
            data.morePacketsLastTime = time;
            
            // Set the new "setback" location
            if(newToLocation == null) {
                data.morePacketsSetbackPoint.set(data.from);
            }
        } else if(data.morePacketsLastTime > time) {
            // Security check, maybe system time changed
            data.morePacketsLastTime = time;
        }

        return newToLocation;
    }

    @Override
    public boolean isEnabled(MovingConfig moving) {
        return moving.morePacketsCheck;
    }

    @Override
    public String getParameter(ParameterName wildcard, NoCheatPlayer player) {

        if(wildcard == ParameterName.VIOLATIONS)
            return String.format(Locale.US, "%d", (int) getData(player.getDataStore()).morePacketsVL);
        else if(wildcard == ParameterName.PACKETS)
            return String.format(Locale.US, "%d", (int) getData(player.getDataStore()).packets);
        else
            return super.getParameter(wildcard, player);
    }
}
