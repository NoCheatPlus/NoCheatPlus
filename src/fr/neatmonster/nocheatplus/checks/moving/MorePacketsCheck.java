package fr.neatmonster.nocheatplus.checks.moving;

import org.bukkit.Bukkit;

import fr.neatmonster.nocheatplus.actions.ParameterName;
import fr.neatmonster.nocheatplus.actions.types.ActionList;
import fr.neatmonster.nocheatplus.players.NCPPlayer;
import fr.neatmonster.nocheatplus.players.informations.Statistics.Id;
import fr.neatmonster.nocheatplus.utilities.locations.PreciseLocation;

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

    public class MorePacketsCheckEvent extends MovingEvent {

        public MorePacketsCheckEvent(final MorePacketsCheck check, final NCPPlayer player, final ActionList actions,
                final double vL) {
            super(check, player, actions, vL);
        }
    }

    // 20 would be for perfect internet connections, 22 is good enough
    private final static int packetsPerTimeframe = 22;

    public MorePacketsCheck() {
        super("morepackets");
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
    public PreciseLocation check(final NCPPlayer player, final Object... args) {
        final MovingConfig cc = getConfig(player);
        final MovingData data = getData(player);

        PreciseLocation newToLocation = null;

        if (!data.morePacketsSetbackPoint.isSet())
            data.morePacketsSetbackPoint.set(data.from);

        final long time = System.currentTimeMillis();

        // Take a packet from the buffer
        data.morePacketsBuffer--;

        // Player used up buffer, he fails the check
        if (data.morePacketsBuffer < 0) {

            data.morePacketsVL = -data.morePacketsBuffer;
            incrementStatistics(player, Id.MOV_MOREPACKETS, 1);

            data.packets = -data.morePacketsBuffer;

            // Execute whatever actions are associated with this check and the
            // violation level and find out if we should cancel the event
            final boolean cancel = executeActions(player, cc.morePacketsActions, data.morePacketsVL);

            if (cancel)
                newToLocation = data.morePacketsSetbackPoint;
        }

        if (data.morePacketsLastTime + 1000 < time) {
            // More than 1 second elapsed, but how many?
            final double seconds = (time - data.morePacketsLastTime) / 1000D;

            // For each second, fill the buffer
            data.morePacketsBuffer += packetsPerTimeframe * seconds;

            // If there was a long pause (maybe server lag?)
            // Allow buffer to grow up to 100
            if (seconds > 2) {
                if (data.morePacketsBuffer > 100)
                    data.morePacketsBuffer = 100;
            } else if (data.morePacketsBuffer > 50)
                data.morePacketsBuffer = 50;

            // Set the new "last" time
            data.morePacketsLastTime = time;

            // Set the new "setback" location
            if (newToLocation == null)
                data.morePacketsSetbackPoint.set(data.from);
        } else if (data.morePacketsLastTime > time)
            // Security check, maybe system time changed
            data.morePacketsLastTime = time;

        return newToLocation;
    }

    @Override
    protected boolean executeActions(final NCPPlayer player, final ActionList actionList, final double violationLevel) {
        final MorePacketsCheckEvent event = new MorePacketsCheckEvent(this, player, actionList, violationLevel);
        Bukkit.getPluginManager().callEvent(event);
        if (!event.isCancelled())
            return super.executeActions(player, event.getActions(), event.getVL());
        return false;
    }

    @Override
    public String getParameter(final ParameterName wildcard, final NCPPlayer player) {

        if (wildcard == ParameterName.VIOLATIONS)
            return String.valueOf(Math.round(getData(player).morePacketsVL));
        else if (wildcard == ParameterName.PACKETS)
            return String.valueOf(Math.round(getData(player).packets));
        else
            return super.getParameter(wildcard, player);
    }
}
