package fr.neatmonster.nocheatplus.checks.moving.model;

import fr.neatmonster.nocheatplus.utilities.PlayerLocation;
import fr.neatmonster.nocheatplus.utilities.TrigUtil;

/**
 * Carry data of a move, involving from- and to- location.
 * 
 * @author asofold
 *
 */
public class MoveData {

    // TODO: Invalidation flag?

    public double yDistance = Double.MAX_VALUE;
    public double hDistance = Double.MAX_VALUE;

    public void set(PlayerLocation from, PlayerLocation to) {
        yDistance = to.getY()- from.getY();
        hDistance = TrigUtil.xzDistance(from, to);
    }

    public void reset() {
        yDistance = Double.MAX_VALUE;
        hDistance = Double.MAX_VALUE;
    }

}
