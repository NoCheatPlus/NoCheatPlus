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

import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * Just base on the players location at initialization.
 * @author mc_dev
 *
 */
public abstract class LocationBasedStopWatchData extends StopWatch{

    /** For efficient location getting, such as player.getLocation(useLoc), always setWorld(null). */
    protected static final Location useLoc = new Location(null, 0, 0, 0);

    public final String worldName;
    public final double x;
    public final double y;
    public final double z;

    /**
     * Makes use of useLoc, only call from the primary thread.
     * @param player
     */
    public LocationBasedStopWatchData(Player player) {
        super(player);
        final Location loc = player.getLocation(useLoc);
        worldName = loc.getWorld().getName();
        x = loc.getX();
        y = loc.getY();
        z = loc.getZ();
        useLoc.setWorld(null);
    }

}
