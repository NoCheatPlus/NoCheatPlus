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
package fr.neatmonster.nocheatplus.checks.moving.magic;

import fr.neatmonster.nocheatplus.checks.moving.MovingData;
import fr.neatmonster.nocheatplus.checks.moving.model.VehicleMoveData;
import fr.neatmonster.nocheatplus.checks.workaround.WRPT;

/**
 * Magic for vehicles.
 * 
 * @author asofold
 *
 */
public class MagicVehicle {

    // Vertical distances //

    /** Maximum descend distance. */
    public static final double maxDescend = 5.0;
    /** Maximum ascend distance (overall, no special effects counted in). */
    // TODO: Likely needs adjustments, many thinkable edge cases, also pistons (!).
    // TODO: Does trigger on vehicle enter somehow some time.
    public static final double maxAscend = 0.27;

    public static final double boatGravityMin = Magic.GRAVITY_MIN / 2.0;
    public static final double boatGravityMax = (Magic.GRAVITY_MAX + Magic.GRAVITY_SPAN) / 2.0;
    /** The speed up to which gravity mechanics roughly work. */
    public static final double boatVerticalFallTarget = 3.7;
    public static final double boatMaxBackToSurfaceAscend = 3.25;

    /**
     * 
     * @param thisMove
     * @param minDescend Case-sensitive.
     * @param maxDescend Case-sensitive.
     * @param data
     * @return
     */
    public static boolean oddInAir(final VehicleMoveData thisMove, final double minDescend, final double maxDescend, final MovingData data) {
        // TODO: Guard by past move tracking, instead of minDescend and maxDescend.
        // (Try individual if this time, let JIT do the rest.)
        if (thisMove.yDistance < 0 && oddInAirDescend(thisMove, minDescend, maxDescend, data)) {
            return true;
        }
        return false;
    }

    /**
     * 
     * @param thisMove
     * @param minDescend Case-sensitive.
     * @param maxDescend Case-sensitive.
     * @param data
     * @return
     */
    private static boolean oddInAirDescend(final VehicleMoveData thisMove, final double minDescend, final double maxDescend, final MovingData data) {
        // TODO: Guard by past move tracking, instead of minDescend and maxDescend.
        // (Try individual if this time, let JIT do the rest.)
        if (data.sfJumpPhase > 54 && thisMove.yDistance < 2.0 * minDescend && thisMove.yDistance > 2.0 * maxDescend
                // TODO: Past move tracking.
                // TODO: Fall distances?
                && data.ws.use(WRPT.W_M_V_ENV_INAIR_SKIP)
                ) {
            // (In-air count usually > 60.)
            return true;
        }
        return false;
    }

    /**
     * In-water (water-water) cases.
     * @param thisMove
     * @return
     */
    public static boolean oddInWater(final VehicleMoveData thisMove, final MovingData data) {
        if (thisMove.yDistance > 0.0) {
            if (oddInWaterAscend(thisMove, data)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Ascending in-water (water-water).
     * @param thisMove
     * @return
     */
    private static boolean oddInWaterAscend(final VehicleMoveData thisMove, final MovingData data) {
        // (Try individual if this time, let JIT do the rest.)
        if (thisMove.yDistance > MagicVehicle.maxAscend && thisMove.yDistance < MagicVehicle.boatMaxBackToSurfaceAscend
                && data.ws.use(WRPT.W_M_V_ENV_INWATER_BTS)) {
            // (Assume players can't control sinking boats for now.)
            // TODO: Limit by more side conditions (e.g. to is on the surface and in-medium count is about 1, past moves).
            // TODO: Checking for surface can be complicated. Might check blocks at location and above and accept if any is not liquid.
            // (Always smaller than previous descending move, roughly to below 0.5 above water.)
            return true;
        }
        return false;
    }

}
