package fr.neatmonster.nocheatplus.checks.blockbreak;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.utilities.CheckUtils;

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
 * The Direction check will find out if a player tried to interact with something that's not in his field of view.
 */
public class Direction extends Check {

    /**
     * Instantiates a new direction check.
     */
    public Direction() {
        super(CheckType.BLOCKBREAK_DIRECTION);
    }

    /**
     * Checks a player.
     * 
     * @param player
     *            the player
     * @param location
     *            the location
     * @return true, if successful
     */
    public boolean check(final Player player, final Location location) {
        final BlockBreakData data = BlockBreakData.getData(player);

        boolean cancel = false;

        // How far "off" is the player with his aim. We calculate from the players eye location and view direction to
        // the center of the target block. If the line of sight is more too far off, "off" will be bigger than 0.
        final double off = CheckUtils.directionCheck(player, location.getX() + 0.5D, location.getY() + 0.5D,
                location.getZ() + 0.5D, 1D, 1D, 50);

        if (off > 0.1D) {
            // Player failed the check. Let's try to guess how far he was from looking directly to the block...
            final Vector direction = player.getEyeLocation().getDirection();
            final Vector blockEyes = location.add(0.5D, 0.5D, 0.5D).subtract(player.getEyeLocation()).toVector();
            final double distance = blockEyes.crossProduct(direction).length() / direction.length();

            // Add the overall violation level of the check.
            data.directionVL += distance;

            // Execute whatever actions are associated with this check and the violation level and find out if we should
            // cancel the event.
            cancel = executeActions(player, data.directionVL, BlockBreakConfig.getConfig(player).directionActions);
        } else
            // Player did likely nothing wrong, reduce violation counter to reward him.
            data.directionVL *= 0.9D;

        return cancel;
    }
}
