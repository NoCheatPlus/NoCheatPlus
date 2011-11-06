package cc.co.evenprime.bukkit.nocheat.checks.timed;

import java.util.Locale;

import net.minecraft.server.EntityPlayer;

import org.bukkit.craftbukkit.entity.CraftPlayer;

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
    public boolean check(NoCheatPlayer player, TimedData data, CCTimed cc) {
        // server lag(ged), skip this, or player dead, therefore it's reasonable
        // for him to not move :)
        if(plugin.skipCheck() || player.getPlayer().isDead())
            return false;

        EntityPlayer p = ((CraftPlayer) player).getHandle();
        // Haven't been checking before
        if(data.ticksLived == 0) {
            // setup data for next time
            data.ticksLived = p.ticksLived;

            // And give up already
            return false;
        }

        boolean cancel = false;

        // Compare ingame record of players ticks to our last observed value
        int difference = p.ticksLived - data.ticksLived;

        // difference should be >= tickTime for perfect synchronization
        if(difference > cc.tickTime) {
            // player was faster than expected, give him credit for the
            // difference
            data.ticksBehind -= (difference - cc.tickTime);
            // Reduce violation level over time
            data.godmodeVL *= 0.9D;

        } else if(difference >= cc.tickTime / 2) {
            // close enough, let it pass
            data.ticksBehind -= cc.tickTime / 2;
            // Reduce violation level over time
            data.godmodeVL *= 0.9D;
        } else {
            // That's a bit suspicious, why is the player more than half the
            // ticktime behind? Keep that in mind
            data.ticksBehind += cc.tickTime - difference;

            // Is he way too far behind, then correct that
            if(data.ticksBehind > cc.godmodeTicksLimit) {

                data.godmodeVL += cc.tickTime - difference;

                cancel = executeActions(player, cc.godmodeActions.getActions(data.godmodeVL));

                // Reduce the time the player is behind accordingly
                data.ticksBehind -= cc.tickTime;
            }
        }

        if(data.ticksBehind < 0) {
            data.ticksBehind = 0;
        }

        if(cancel) {
            // Catch up for at least some of the ticks
            for(int i = 0; i < cc.tickTime; i++) {
                p.b(true); // Catch up with the server, one tick at a time
            }
        }

        // setup data for next time
        data.ticksLived = p.ticksLived;

        return cancel;

    }

    @Override
    public boolean isEnabled(CCTimed cc) {
        return cc.godmodeCheck;
    }

    public String getParameter(WildCard wildcard, NoCheatPlayer player) {

        switch (wildcard) {

        case VIOLATIONS:
            return String.format(Locale.US, "%d", player.getData().timed.godmodeVL);
        default:
            return super.getParameter(wildcard, player);
        }
    }
}
