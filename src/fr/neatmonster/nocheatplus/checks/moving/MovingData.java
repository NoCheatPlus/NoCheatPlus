package fr.neatmonster.nocheatplus.checks.moving;

import org.bukkit.Location;

import fr.neatmonster.nocheatplus.checks.CheckData;
import fr.neatmonster.nocheatplus.players.informations.Statistics.Id;
import fr.neatmonster.nocheatplus.utilities.locations.PreciseLocation;

/**
 * Player specific data for the moving check group
 */
public class MovingData extends CheckData {

    // Keep track of the violation levels of the checks
    public double                runflyVL;
    public double                nofallVL;
    public double                morePacketsVL;
    public double                morePacketsVehicleVL;
    public double                waterWalkVL;
    public double                bedFlyVL;

    // Count how long a player is in the air
    public int                   jumpPhase;

    // Remember how big the players last JumpAmplifier (potion effect) was
    public double                lastJumpAmplifier;

    // Remember for a short time that the player was on ice and therefore
    // should be allowed to move a bit faster
    public int                   onIce;

    // Where should a player be teleported back to when failing the check
    public final PreciseLocation runflySetBackPoint       = new PreciseLocation();

    // Some values for estimating movement freedom
    public double                vertFreedom;
    public double                vertVelocity;
    public int                   vertVelocityCounter;
    public double                horizFreedom;
    public int                   horizVelocityCounter;
    public double                horizontalBuffer;
    public int                   bunnyhopdelay;

    // Keep track of estimated fall distance to compare to real fall distance
    public float                 fallDistance;
    public float                 lastAddedFallDistance;

    // Keep in mind the player's last safe position
    public Location[]            lastSafeLocations        = new Location[] {null, null};

    // Keep track of when "morePackets" last time checked and how much packets
    // a player sent and may send before failing the check
    public long                  morePacketsLastTime;
    public int                   packets;
    public int                   morePacketsBuffer        = 50;

    // Where to teleport the player that fails the "morepackets" check
    public final PreciseLocation morePacketsSetbackPoint  = new PreciseLocation();

    // Keep track of when "morePacketsVehicle" last time checked an how much
    // packets a vehicle sent and may send before failing the check
    public long                  morePacketsVehicleLastTime;
    public int                   packetsVehicle;
    public int                   morePacketsVehicleBuffer = 50;

    // When NoCheatPlus does teleport the player, remember the target location to
    // be able to distinguish "our" teleports from teleports of others
    public final PreciseLocation teleportTo               = new PreciseLocation();

    // For logging and convenience, make copies of the events locations
    public final PreciseLocation from                     = new PreciseLocation();
    public final PreciseLocation fromVehicle              = new PreciseLocation();
    public final PreciseLocation to                       = new PreciseLocation();
    public final PreciseLocation toVehicle                = new PreciseLocation();

    // For convenience, remember if the locations are considered "on ground"
    // by NoCheatPlus
    public boolean               fromOnOrInGround;
    public boolean               toOnOrInGround;

    // Remember if the player was previously sleeping
    public boolean               wasSleeping              = false;

    public Id                    statisticCategory        = Id.MOV_RUNNING;

    public void clearMorePacketsData() {
        morePacketsSetbackPoint.reset();
    }

    public void clearRunFlyData() {
        runflySetBackPoint.reset();
        jumpPhase = 0;
        fallDistance = 0;
        lastAddedFallDistance = 0;
        bunnyhopdelay = 0;
    }
}
