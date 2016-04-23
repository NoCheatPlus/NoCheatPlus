package fr.neatmonster.nocheatplus.checks.moving.model;

import fr.neatmonster.nocheatplus.utilities.RichEntityLocation;
import fr.neatmonster.nocheatplus.utilities.TrigUtil;

public class VehicleMoveData {

    public double hDistance;
    public double vDistance;

    public VehicleMoveData set(final RichEntityLocation from, final RichEntityLocation to) {
        hDistance = TrigUtil.xzDistance(from, to);
        vDistance = to.getY() - from.getY();
        return this;
    }

}
