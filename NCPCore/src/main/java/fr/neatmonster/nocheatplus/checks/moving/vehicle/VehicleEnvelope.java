package fr.neatmonster.nocheatplus.checks.moving.vehicle;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.moving.MovingConfig;
import fr.neatmonster.nocheatplus.checks.moving.MovingData;

public class VehicleEnvelope extends Check {

    public VehicleEnvelope() {
        super(CheckType.MOVING_VEHICLE_ENVELOPE);
    }

    public Location check(final Player player, final Location from, final Location to, final boolean isFake, final MovingData data, final MovingConfig cc) {
        // TODO: Implement.
        return null;
    }

}
