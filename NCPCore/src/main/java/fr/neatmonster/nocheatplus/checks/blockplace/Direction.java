package fr.neatmonster.nocheatplus.checks.blockplace;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.utilities.TrigUtil;

/*
 * M""""""'YMM oo                              dP   oo                   
 * M  mmmm. `M                                 88                        
 * M  MMMMM  M dP 88d888b. .d8888b. .d8888b. d8888P dP .d8888b. 88d888b. 
 * M  MMMMM  M 88 88'  `88 88ooood8 88'  `""   88   88 88'  `88 88'  `88 
 * M  MMMM' .M 88 88       88.  ... 88.  ...   88   88 88.  .88 88    88 
 * M       .MM dP dP       `88888P' `88888P'   dP   dP `88888P' dP    dP 
 * MMMMMMMMMMM                                                           
 */
/**
 * The Direction check will find out if a player tried to interact with something that's not in their field of view.
 */
public class Direction extends Check {

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
     * @param location
     *            the location
     * @return true, if successful
     */
    public boolean check(final Player player, final Block placed, final Block against, final BlockPlaceData data) {

        boolean cancel = false;

        // How far "off" is the player with their aim. We calculate from the players eye location and view direction to
        // the center of the target block. If the line of sight is more too far off, "off" will be bigger than 0.
        final Location loc = player.getLocation();
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
            cancel = executeActions(player, data.directionVL, distance,
                    BlockPlaceConfig.getConfig(player).directionActions);
        } else
            // Player did likely nothing wrong, reduce violation counter to reward them.
            data.directionVL *= 0.9D;

        return cancel;
    }
}
