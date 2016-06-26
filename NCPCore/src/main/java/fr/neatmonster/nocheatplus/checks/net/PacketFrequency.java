package fr.neatmonster.nocheatplus.checks.net;

import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.utilities.TickTask;

/**
 * Fall-back check for pre 1.9: Limit the overall packet frequency, aiming at
 * crash exploits only. Thus the default actions should be kicking, as this
 * won't distinguish type of packets with the global packet count.
 * 
 * @author asofold
 *
 */
public class PacketFrequency extends Check {

    public PacketFrequency() {
        super(null); //CheckType.NET_PACKETFREQUENCY);
    }

    /**
     * Actual state.
     * 
     * @param player
     * @param data
     *           
     * @param cc
     *            
     * @return If to cancel a packet event.
     */
    public boolean check(final Player player, final NetData data, final NetConfig cc) {
        data.packetFrequency.add(System.currentTimeMillis(), 1f);
        final long fDur = data.packetFrequency.bucketDuration() * data.packetFrequency.numberOfBuckets();
        double amount = data.packetFrequency.score(1f) * 1000f / (float) fDur;
        //        if (data.debug) {
        //            debug(player, "Basic amount: " + amount);
        //        }
        if (amount > cc.packetFrequencyPacketsPerSecond) {
            amount /= TickTask.getLag(fDur);
            if (amount > cc.packetFrequencyPacketsPerSecond) {
                if (executeActions(player, amount - cc.packetFrequencyPacketsPerSecond, 1.0, cc.packetFrequencyActions).willCancel()) {
                    return true;
                }
            }
        }
        return false; // Cancel state.
    }

    /**
     * Allow to relax the count by 1, e.g. with outgoing teleport.
     * 
     * @param player
     * @param data
     * 
     * @param cc
     * 
     * @return
     */
    public void relax(final Player player, final NetData data, final NetConfig cc) {
        // TODO: Concept (not more locking, instead a counter (optimistic)).
    }

}
