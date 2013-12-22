package fr.neatmonster.nocheatplus.checks.fight;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
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
 * The Direction check will find out if a player tried to interact with something that's not in his field of view.
 */
public class Direction extends Check {

    /**
     * Instantiates a new direction check.
     */
    public Direction() {
        super(CheckType.FIGHT_DIRECTION);
    }

    /**
     * Checks a player.
     * 
     * @param player
     *            the player
     * @param damaged
     *            the damaged
     * @return true, if successful
     */
    public boolean check(final Player player, final Entity damaged) {
        final FightConfig cc = FightConfig.getConfig(player);
        final FightData data = FightData.getData(player);

        boolean cancel = false;

        // Safeguard, if entity is complex, this check will fail due to giant and hard to define hitboxes.
//        if (damaged instanceof EntityComplex || damaged instanceof EntityComplexPart)
        if (mcAccess.isComplexPart(damaged))
            return false;

        // Find out how wide the entity is.
        final double width = mcAccess.getWidth(damaged);

        // entity.height is broken and will always be 0, therefore. Calculate height instead based on boundingBox.
        final double height = mcAccess.getHeight(damaged);
        
        final Location dLoc = damaged.getLocation();
        
        // TODO: allow any hit on the y axis (might just adapt interface to use foot position + height)!
        
        // How far "off" is the player with his aim. We calculate from the players eye location and view direction to
        // the center of the target entity. If the line of sight is more too far off, "off" will be bigger than 0.
        final Location loc = player.getLocation();
        final Vector direction = player.getEyeLocation().getDirection();
        
        final double off;
        if (cc.directionStrict){
        	off = TrigUtil.combinedDirectionCheck(loc, player.getEyeHeight(), direction, dLoc.getX(), dLoc.getY() + height / 2D, dLoc.getZ(), width, height, TrigUtil.DIRECTION_PRECISION, 80.0);
        }
        else{
        	// Also take into account the angle.
        	off = TrigUtil.directionCheck(loc, player.getEyeHeight(), direction, dLoc.getX(), dLoc.getY() + height / 2D, dLoc.getZ(), width, height, TrigUtil.DIRECTION_PRECISION);
        }

        if (off > 0.1) {
            // Player failed the check. Let's try to guess how far he was from looking directly to the entity...
            final Vector blockEyes = new Vector(dLoc.getX() - loc.getX(),  dLoc.getY() + height / 2D - loc.getY() - player.getEyeHeight(), dLoc.getZ() - loc.getZ());
            final double distance = blockEyes.crossProduct(direction).length() / direction.length();

            // Add the overall violation level of the check.
            data.directionVL += distance;

            // Execute whatever actions are associated with this check and the violation level and find out if we should
            // cancel the event.
            cancel = executeActions(player, data.directionVL, distance, cc.directionActions);

            if (cancel)
                // If we should cancel, remember the current time too.
                data.directionLastViolationTime = System.currentTimeMillis();
        } else
            // Reward the player by lowering his violation level.
            data.directionVL *= 0.8D;

        // If the player is still in penalty time, cancel the event anyway.
        if (data.directionLastViolationTime + cc.directionPenalty > System.currentTimeMillis()) {
            // A safeguard to avoid people getting stuck in penalty time indefinitely in case the system time of the
            // server gets changed.
        	// TODO: Change this to a general attack penalty time.
            if (data.directionLastViolationTime > System.currentTimeMillis())
                data.directionLastViolationTime = 0;

            // He is in penalty time, therefore request cancelling of the event.
            return true;
        }

        return cancel;
    }
}
