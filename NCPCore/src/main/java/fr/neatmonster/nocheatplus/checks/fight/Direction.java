package fr.neatmonster.nocheatplus.checks.fight;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.utilities.TrigUtil;

/**
 * The Direction check will find out if a player tried to interact with something that's not in their field of view.
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
    public boolean check(final Player player, final Location loc, final Entity damaged, final Location dLoc) {
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
        
        // TODO: allow any hit on the y axis (might just adapt interface to use foot position + height)!
        
        // How far "off" is the player with their aim. We calculate from the players eye location and view direction to
        // the center of the target entity. If the line of sight is more too far off, "off" will be bigger than 0.
        final Vector direction = loc.getDirection();
        
        final double off;
        if (cc.directionStrict){
        	off = TrigUtil.combinedDirectionCheck(loc, player.getEyeHeight(), direction, dLoc.getX(), dLoc.getY() + height / 2D, dLoc.getZ(), width, height, TrigUtil.DIRECTION_PRECISION, 80.0);
        }
        else{
        	// Also take into account the angle.
        	off = TrigUtil.directionCheck(loc, player.getEyeHeight(), direction, dLoc.getX(), dLoc.getY() + height / 2D, dLoc.getZ(), width, height, TrigUtil.DIRECTION_PRECISION);
        }

        if (off > 0.1) {
            // Player failed the check. Let's try to guess how far they were from looking directly to the entity...
            final Vector blockEyes = new Vector(dLoc.getX() - loc.getX(),  dLoc.getY() + height / 2D - loc.getY() - player.getEyeHeight(), dLoc.getZ() - loc.getZ());
            final double distance = blockEyes.crossProduct(direction).length() / direction.length();

            // Add the overall violation level of the check.
            data.directionVL += distance;

            // Execute whatever actions are associated with this check and the violation level and find out if we should
            // cancel the event.
            cancel = executeActions(player, data.directionVL, distance, cc.directionActions);

            if (cancel) {
            	// Deal an attack penalty time.
            	data.attackPenalty.applyPenalty(cc.directionPenalty);
            }
        } else
            // Reward the player by lowering their violation level.
            data.directionVL *= 0.8D;

        return cancel;
    }
}
