package fr.neatmonster.nocheatplus.checks.moving.model;

import org.bukkit.Location;
import org.bukkit.entity.Entity;

import fr.neatmonster.nocheatplus.compat.MCAccess;
import fr.neatmonster.nocheatplus.utilities.RichEntityLocation;

public class VehicleMoveInfo extends MoveInfo<RichEntityLocation, Entity> {

    public VehicleMoveInfo(final MCAccess mcAccess){
        super(mcAccess, new RichEntityLocation(mcAccess, null), new RichEntityLocation(mcAccess, null));
    }

    @Override
    protected void set(RichEntityLocation rLoc, Location loc, Entity entity, double yOnGround) {
        rLoc.set(loc, entity, yOnGround);
    }

}
