package cc.co.evenprime.bukkit.nocheat.checks.timed;

import java.util.Locale;

import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.NoCheatPlayer;
import cc.co.evenprime.bukkit.nocheat.actions.types.ActionWithParameters.WildCard;
import cc.co.evenprime.bukkit.nocheat.checks.TimedCheck;
import cc.co.evenprime.bukkit.nocheat.config.Permissions;
import cc.co.evenprime.bukkit.nocheat.config.cache.CCTimed;
import cc.co.evenprime.bukkit.nocheat.data.TimedData;

public class GodmodeCheck extends TimedCheck {

    public GodmodeCheck(NoCheat plugin) {
        super(plugin, "timed.godmode", Permissions.TIMED_GODMODE);
    }

    @Override
    public void check(NoCheatPlayer player, TimedData data, CCTimed cc) {
        // server lag(ged), skip this, or player dead, therefore it's reasonable
        // for him to not move :)
        if(plugin.skipCheck() || player.getPlayer().isDead())
            return;

        final int ticksLived = player.getTicksLived();

        // Haven't been checking before
        if(data.ticksLived == 0) {
            // setup data for next time
            data.ticksLived = ticksLived;

            // And give up already
            return;
        }

        boolean cancel = false;

        // How far behind is the player with his ticks
        int behind = Math.min(10, (data.ticksLived + cc.tickTime) - ticksLived);        
        // difference should be >= tickTime for perfect synchronization
        if(behind <= 1) {
            // player as fast as expected, give him credit for that
            data.ticksBehind -= cc.tickTime / 2;
            // Reduce violation level over time
            data.godmodeVL -= cc.tickTime / 2.0;

        } else if(behind <= (cc.tickTime / 2)+1) {
            // close enough, let it pass
            data.ticksBehind -= cc.tickTime / 4;
            // Reduce violation level over time
            data.godmodeVL -= cc.tickTime / 4.0;
        } else {
            // That's a bit suspicious, why is the player more than half the
            // ticktime behind? Keep that in mind
            data.ticksBehind += behind;

            // Is he way too far behind, then correct that
            if(data.ticksBehind > cc.godmodeTicksLimit) {

                // Over the limit, start increasing VL for the player
                data.godmodeVL += behind;

                cancel = executeActions(player, cc.godmodeActions.getActions(data.godmodeVL));


                if(cancel) {
                    // Catch up for at least some of the ticks
                    try {
                        player.increaseAge(cc.tickTime);
                    }
                    catch(Exception e) {
                        e.printStackTrace();
                    }
                    
                    // Reduce the time the player is behind accordingly
                    data.ticksBehind -= cc.tickTime;
                }
            }
        }

        if(data.ticksBehind < 0) {
            data.ticksBehind = 0;
        }
        
        if(data.godmodeVL < 0) {
            data.godmodeVL = 0;
        }

        // setup data for next time
        data.ticksLived = player.getTicksLived();

        return;

    }

    @Override
    public boolean isEnabled(CCTimed cc) {
        return cc.godmodeCheck;
    }

    public String getParameter(WildCard wildcard, NoCheatPlayer player) {

        switch (wildcard) {

        case VIOLATIONS:
            return String.format(Locale.US, "%d", (int)player.getData().timed.godmodeVL);
        default:
            return super.getParameter(wildcard, player);
        }
    }
}
