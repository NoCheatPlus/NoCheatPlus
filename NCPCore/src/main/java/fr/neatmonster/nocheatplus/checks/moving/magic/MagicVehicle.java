package fr.neatmonster.nocheatplus.checks.moving.magic;

import fr.neatmonster.nocheatplus.checks.moving.MovingData;
import fr.neatmonster.nocheatplus.checks.moving.model.VehicleMoveData;
import fr.neatmonster.nocheatplus.checks.workaround.WRPT;
import fr.neatmonster.nocheatplus.utilities.RichEntityLocation;

/**
 * Magic for vehicles.
 * 
 * @author asofold
 *
 */
public class MagicVehicle {

    // Maximum distances per tick are wild guessing or from the wiki. Meant for extreme move checking, disregard velocity and effects. //
    // TODO: Configuration, once certain.

    /** Extreme value. */
    public static final double maxDistanceHorizontal = 4.0; // TODO: 2.5 / 3.4?

    // Entity.
    public static final double minecartMaxDistanceHorizontal = maxDistanceHorizontal; // 13.0 / 20.0; // Include turn.
    public static final double boatMaxDistanceHorizontal = maxDistanceHorizontal; // 18.0 / 20.0; // Including some downstream, rough testing.
    // TODO: Boat on ice: 42 / 20. 
    public static final double entityMaxDistanceHorizontal = maxDistanceHorizontal; // (No idea, cannon balls?)

    // LivingEntity.
    public static final double horseMaxDistanceHorizontal = maxDistanceHorizontal; // 15.0 / 20.0; // TODO: Doesn't fire anything.
    public static final double donkeyMaxDistanceHorizontal = maxDistanceHorizontal; // 7.7 / 20.0; // TODO: 
    public static final double pigMaxDistanceHorizontal = maxDistanceHorizontal; // 8.2 / 20.0; // TODO: Doesn't fire anything.
    public static final double livingEntityMaxDistanceHorizontal = maxDistanceHorizontal; // (No idea, dragons?)


    // Vertical distances //

    /** Maximum descend distance. */
    public static final double maxDescend = 5.0;
    /** Maximum ascend distance (overall, no special effects counted in). */
    // TODO: Likely needs adjustments, many thinkable edge cases, also pistons (!).
    // TODO: Does trigger on vehicle enter somehow some time.
    public static final double maxAscend = 0.27;

    public static final double boatGravityMin = Magic.GRAVITY_MIN / 2.0;
    public static final double boatGravityMax = Magic.GRAVITY_MAX + Magic.GRAVITY_SPAN;
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
        if (thisMove.vDistance < 0 && oddInAirDescend(thisMove, minDescend, maxDescend, data)) {
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
        if (thisMove.vDistance < 2.0 * minDescend && thisMove.vDistance > 2.0 * maxDescend
                // TODO: Past move tracking.
                // TODO: Fall distances?
                && data.ws.use(WRPT.W_M_V_ENV_INAIR_SKIP)
                ) {
            return true;
        }
        return false;
    }

    /**
     * In-water (water-water) cases.
     * @param thisMove
     * @return
     */
    public static boolean oddInWater(final RichEntityLocation from, final RichEntityLocation to, 
            final VehicleMoveData thisMove, final MovingData data) {
        if (thisMove.vDistance > 0.0) {
            if (oddInWaterAscend(from, to, thisMove, data)) {
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
    private static boolean oddInWaterAscend(final RichEntityLocation from, final RichEntityLocation to, 
            final VehicleMoveData thisMove, final MovingData data) {
        // (Try individual if this time, let JIT do the rest.)
        if (thisMove.vDistance > MagicVehicle.maxAscend && thisMove.vDistance < MagicVehicle.boatMaxBackToSurfaceAscend
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
