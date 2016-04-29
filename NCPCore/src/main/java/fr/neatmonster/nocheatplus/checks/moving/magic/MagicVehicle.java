package fr.neatmonster.nocheatplus.checks.moving.magic;

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
    public static final double maxDistanceHorizontal = 4.0;

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

}
