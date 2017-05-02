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
package fr.neatmonster.nocheatplus.checks.moving.model;

import java.util.UUID;

import org.bukkit.entity.Entity;
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

    // Simple vehicle properties.
    /** Lazily set for minecarts. */
    public boolean fromOnRails, toOnRails;

    // Special properties added by checks.
    /** Special condition, the move coordinates may be wrong. */
    public boolean specialCondition = false;

    @Override
    protected void resetBase() {
        // Vehicle properties.
        vehicleId = null;
        vehicleType = null;
        fromOnRails = toOnRails = specialCondition = false;
        // Super class last, because it'll set valid to true in the end.
        super.resetBase();
    }

    public void setExtraVehicleProperties(final Entity vehicle) {
        vehicleId = vehicle.getUniqueId();
        vehicleType = vehicle.getType();
    }

    public void setExtraMinecartProperties(final VehicleMoveInfo moveInfo) {
        if (moveInfo.from.isOnRails()) {
            fromOnRails = true;
        }
        if (moveInfo.to.isOnRails()) {
            toOnRails = true;
        }
    }

}
