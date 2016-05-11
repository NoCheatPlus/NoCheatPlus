package fr.neatmonster.nocheatplus.checks.moving.model;

import java.util.UUID;

import org.bukkit.entity.EntityType;

/**
 * Include vehicle move data for a move.
 * 
 * @author asofold
 *
 */
public class VehicleMoveData extends PlayerMoveData {

    // Vehicle properties.
    /**
     * Unique identifier for the vehicle. Set at the start of some check (-ing).
     */
    public UUID vehicleId = null;
    /** Type of the vehicle. Set at the start of some check (-ing). */
    public EntityType vehicleType = null;

    @Override
    protected void resetBase() {
        // Vehicle properties.
        vehicleId = null;
        vehicleType = null;
        // Super class last, because it'll set valid to true in the end.
        super.resetBase();
    }



}
