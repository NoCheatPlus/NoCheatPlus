/*
 * This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package fr.neatmonster.nocheatplus.command.testing.stopwatch;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

import fr.neatmonster.nocheatplus.utilities.OnDemandTickListener;

/**
 * Registers stop-watches for players.
 * @author mc_dev
 *
 */
public class StopWatchRegistry {

    // TODO: Make its own plugin + no NCP dependency.

    /** Currently by player name. */
    private Map<String, StopWatch> clocks = new LinkedHashMap<String, StopWatch>(20);

    private final OnDemandTickListener tickListener = new OnDemandTickListener() {
        @Override
        public boolean delegateTick(int tick, long timeLast) {
            return tickClocks(); // true: stay registered
        }
    };

    protected void setup(Plugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(new Listener() {
            @EventHandler(priority = EventPriority.MONITOR)
            public void onPlayerQuit(PlayerQuitEvent event) {
                removeClocks(event.getPlayer());
            }
            @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
            public void onPlayerKick(PlayerKickEvent event) {
                removeClocks(event.getPlayer());
            }
            @EventHandler(priority = EventPriority.MONITOR)
            public void onWorldChange(PlayerChangedWorldEvent event) {
                checkWorldChange(event.getPlayer());
            }
        }, plugin);
    }

    public StopWatch getClock(Player player) {
        return clocks.get(player.getName());
    }

    protected void removeClocks(Player player) {
        clocks.remove(player.getName());
    }

    protected void checkWorldChange(Player player) {
        StopWatch sw = getClock(player);
        try {
            if (sw != null && !sw.isFinished() && sw.checkStop()) {
                sw.sendStatus();
            }
        } catch (RuntimeException e) {
            timeBackwards(player);
        }

    }

    protected void timeBackwards(Player player) {
        removeClocks(player);
        player.sendMessage(ChatColor.RED + "Stopwatch finished due to system time running backwards.");
    }

    /**
     * Check if having a tick listener registered is necessary.
     * @return If to stay registered.
     */
    protected boolean tickClocks() {
        if (clocks.isEmpty()) {
            return false;
        }
        boolean needsTick = false;
        Iterator<Entry<String, StopWatch>> it = clocks.entrySet().iterator();
        while (it.hasNext()) {
            final StopWatch sw = it.next().getValue();
            try {
                if (sw.needsTick()) {
                    if (sw.isFinished()) {
                        // Ignore.
                    } else if (sw.checkStop()) {
                        sw.sendStatus();
                        // Do not remove the clock.
                    } else {
                        needsTick = true;
                    }
                } // else: ignore.
            } catch (RuntimeException e) {
                it.remove();
                timeBackwards(sw.player);
            }
        }
        return needsTick;
    }

    public void setClock(Player player, StopWatch clock) {
        StopWatch oldClock = getClock(player);
        if (oldClock != null && !oldClock.isFinished()) {
            // TODO: Might add a more descriptive message.
            oldClock.stop();
            oldClock.sendStatus();
        }
        clocks.put(player.getName(), clock);
        if (clock.needsTick()) {
            tickListener.register();
        }
    }

    /**
     * 
     * @param player
     * @return If a clock was running (note that a stopped clock is not running).
     */
    public boolean stopClock(Player player) {
        StopWatch oldClock = getClock(player);
        boolean wasRunning = false;
        if (oldClock != null) {
            wasRunning = !oldClock.isFinished();
            // TODO: Might add a more descriptive message.
            oldClock.stop();
            oldClock.sendStatus();
        }
        return wasRunning;
    }

    public void tellClock(Player player) {
        StopWatch oldClock = getClock(player);
        if (oldClock != null) {
            // TODO: Might add a more descriptive message.
            oldClock.sendStatus();
        } else {
            // TODO: Might tell "no clock running".
        }
    }

}
