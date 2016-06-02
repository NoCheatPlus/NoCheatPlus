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
package fr.neatmonster.nocheatplus.checks.blockplace;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.utilities.TrigUtil;

/**
 * The Direction check will find out if a player tried to interact with something that's not in their field of view.
 */
public class Direction extends Check {

    /** For temporary use: LocUtil.clone before passing deeply, call setWorld(null) after use. */
    private final Location useLoc = new Location(null, 0, 0, 0);

    /**
     * Instantiates a new direction check.
     */
    public Direction() {
        super(CheckType.BLOCKPLACE_DIRECTION);
    }

    /**
     * Checks a player.
     * 
     * @param player
     *            the player
     * @param data 
     * @param cc 
     * @param location
     *            the location
     * @return true, if successful
     */
    public boolean check(final Player player, final Block placed, final Block against, final BlockPlaceData data, final BlockPlaceConfig cc) {

        boolean cancel = false;

        // How far "off" is the player with their aim. We calculate from the players eye location and view direction to
        // the center of the target block. If the line of sight is more too far off, "off" will be bigger than 0.
        final Location loc = player.getLocation(useLoc);
        final Vector direction = loc.getDirection();
        double off = TrigUtil.directionCheck(loc, player.getEyeHeight(), direction, against, TrigUtil.DIRECTION_PRECISION);

        // Now check if the player is looking at the block from the correct side.
        double off2 = 0.0D;

        // Find out against which face the player tried to build, and if they
        // stood on the correct side of it
        if (placed.getX() > against.getX())
            off2 = against.getX() + 0.5D - loc.getX();
        else if (placed.getX() < against.getX())
            off2 = -(against.getX() + 0.5D - loc.getX());
        else if (placed.getY() > against.getY())
            off2 = against.getY() + 0.5D - loc.getY() - player.getEyeHeight();
        else if (placed.getY() < against.getY())
            off2 = -(against.getY() + 0.5D - loc.getY() - player.getEyeHeight());
        else if (placed.getZ() > against.getZ())
            off2 = against.getZ() + 0.5D - loc.getZ();
        else if (placed.getZ() < against.getZ())
            off2 = -(against.getZ() + 0.5D - loc.getZ());

        // If they weren't on the correct side, add that to the "off" value
        if (off2 > 0.0D)
            off += off2;

        if (off > 0.1D) {
            // Player failed the check. Let's try to guess how far they were from looking directly to the block...
            final Vector blockEyes = new Vector(0.5 + placed.getX() - loc.getX(), 0.5 + placed.getY() - loc.getY() - player.getEyeHeight(), 0.5 + placed.getZ() - loc.getZ());
            final double distance = blockEyes.crossProduct(direction).length() / direction.length();

            // Add the overall violation level of the check.
            data.directionVL += distance;

            // Execute whatever actions are associated with this check and the violation level and find out if we should
            // cancel the event.
            cancel = executeActions(player, data.directionVL, distance, cc.directionActions).willCancel();
        } else {
            // Player did likely nothing wrong, reduce violation counter to reward them.
            data.directionVL *= 0.9D;
        }
        useLoc.setWorld(null);
        return cancel;
    }
}
