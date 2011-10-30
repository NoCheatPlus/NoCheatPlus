package cc.co.evenprime.bukkit.nocheat.checks.timed;

import net.minecraft.server.EntityPlayer;

import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;

import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.config.Permissions;
import cc.co.evenprime.bukkit.nocheat.config.cache.ConfigurationCache;
import cc.co.evenprime.bukkit.nocheat.data.BaseData;

public class TimedCheck {

    private final NoCheat plugin;

    public TimedCheck(NoCheat plugin) {

        this.plugin = plugin;
    }

    public void check(Player player, int tickTime, ConfigurationCache cc) {

        // server lag(ged), skip this, or player dead, therefore it's reasonable for him to not move :)
        if(plugin.skipCheck() || player.isDead())
            return;

        if(cc.timed.godmodeCheck && !player.hasPermission(Permissions.TIMED_GODMODE)) {


            BaseData data = plugin.getData(player.getName());
            
            EntityPlayer p = ((CraftPlayer) player).getHandle();
            // Haven't been checking before
            if(data.timed.ticksLived == 0) {
                // setup data for next time
                data.timed.ticksLived = p.ticksLived;
                
                // And give up already
                return;
            }
            
            boolean cancel = false;

            // Compare ingame record of players ticks to our last observed value
            int difference = p.ticksLived - data.timed.ticksLived;

            // difference should be >= tickTime for perfect synchronization
            if(difference > tickTime) {
                // player was faster than expected, give him credit for the
                // difference
                data.timed.ticksBehind -= (difference - tickTime);
                // Reduce violation level over time
                data.timed.godmodeVL *= 0.90D;

            } else if(difference >= tickTime / 2) {
                // close enough, let it pass

                // Reduce violation level over time
                data.timed.godmodeVL *= 0.95D;
            } else {
                // That's a bit suspicious, why is the player more than half the
                // ticktime behind? Keep that in mind
                data.timed.ticksBehind += tickTime - difference;

                // Is he way too far behind, then correct that
                if(data.timed.ticksBehind > cc.timed.godmodeTicksLimit) {

                    data.timed.godmodeVL += tickTime - difference;

                    // Enough is enough
                    data.log.check = "timed.godmode";
                    data.log.godmodeTicksBehind = data.timed.ticksBehind;

                    cancel = plugin.execute(player, cc.timed.godmodeActions, (int) data.timed.godmodeVL, data.timed.history, cc);

                    // Reduce the time the player is behind accordingly
                    data.timed.ticksBehind -= tickTime;
                }
            }

            if(data.timed.ticksBehind < 0) {
                data.timed.ticksBehind = 0;
            }

            if(cancel) {
                // Catch up for at least some of the ticks
                for(int i = 0; i < tickTime; i++) {
                    p.b(true); // Catch up with the server, one tick at a time
                }
            }

            // setup data for next time
            data.timed.ticksLived = p.ticksLived;

        }
    }
}
