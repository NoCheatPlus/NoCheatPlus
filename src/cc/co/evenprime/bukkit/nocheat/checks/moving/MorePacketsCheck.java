package cc.co.evenprime.bukkit.nocheat.checks.moving;

import java.util.Locale;

import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.NoCheatPlayer;
import cc.co.evenprime.bukkit.nocheat.actions.types.WildCard;
import cc.co.evenprime.bukkit.nocheat.checks.MovingCheck;
import cc.co.evenprime.bukkit.nocheat.config.Permissions;
import cc.co.evenprime.bukkit.nocheat.config.cache.CCMoving;
import cc.co.evenprime.bukkit.nocheat.data.MovingData;
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
    private final static int bufferLimit         = 30;

    public MorePacketsCheck(NoCheat plugin) {
        super(plugin, "moving.morepackets", Permissions.MOVING_MOREPACKETS);
    }

    /**
     * 1. Collect packets processed within 20 server ticks = packetCounter
     * 2. Measure time taken for those 20 server ticks = elapsedTime
     * 3. elapsedTime >> 1 second -> ignore next check
     * 4. limit = 22 x elapsedTime
     * 5. difference = limit - packetCounter
     * 6. buffer = buffer + difference; if(buffer > 20) buffer = 20;
     * 7. if(buffer < 0) -> violation of size "buffer".
     * 8. reset packetCounter, wait for next 20 ticks to pass by.
     * 
     */
    public PreciseLocation check(NoCheatPlayer player, MovingData data, CCMoving cc) {

        PreciseLocation newToLocation = null;

        data.morePacketsCounter++;
        if(!data.morePacketsSetbackPoint.isSet()) {
            data.morePacketsSetbackPoint.set(data.from);
        }

        int ingameSeconds = plugin.getIngameSeconds();
        // Is at least a second gone by and has the server at least processed 20
        // ticks since last time
        if(ingameSeconds != data.lastElapsedIngameSeconds) {

            int limit = (int) ((packetsPerTimeframe * plugin.getIngameSecondDuration()) / 1000L);

            int difference = limit - data.morePacketsCounter;

            data.morePacketsBuffer += difference;
            if(data.morePacketsBuffer > bufferLimit)
                data.morePacketsBuffer = bufferLimit;
            // Are we over the 22 event limit for that time frame now? (limit
            // increases with time)

            int packetsAboveLimit = (int) -data.morePacketsBuffer;

            if(data.morePacketsBuffer < 0)
                data.morePacketsBuffer = 0;

            // Should we react? Only if the check doesn't get skipped and we
            // went over the limit
            if(!plugin.skipCheck() && packetsAboveLimit > 0) {
                data.morePacketsVL += packetsAboveLimit;
                data.morePacketsTotalVL += packetsAboveLimit;
                data.morePacketsFailed++;

                data.packets = packetsAboveLimit;

                final boolean cancel = executeActions(player, cc.morePacketsActions.getActions(data.morePacketsVL));

                if(cancel)
                    newToLocation = data.morePacketsSetbackPoint;
            }

            // No new setbackLocation was chosen
            if(newToLocation == null) {
                data.morePacketsSetbackPoint.set(data.from);
            }

            if(data.morePacketsVL > 0)
                // Shrink the "over limit" value by 20 % every second
                data.morePacketsVL *= 0.8;

            data.morePacketsCounter = 0; // Count from zero again
            data.lastElapsedIngameSeconds = ingameSeconds;
        }

        return newToLocation;
    }

    @Override
    public boolean isEnabled(CCMoving moving) {
        return moving.morePacketsCheck;
    }

    @Override
    public String getParameter(WildCard wildcard, NoCheatPlayer player) {

        switch (wildcard) {
        case VIOLATIONS:
            return String.format(Locale.US, "%d", (int) player.getData().moving.morePacketsVL);
        case PACKETS:
            return String.valueOf(player.getData().moving.packets);
        default:
            return super.getParameter(wildcard, player);
        }
    }
}
