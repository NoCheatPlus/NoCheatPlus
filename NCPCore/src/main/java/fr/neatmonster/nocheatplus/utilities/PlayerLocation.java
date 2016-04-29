package fr.neatmonster.nocheatplus.utilities;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.compat.MCAccess;

/**
 * Lots of content for a location a player is supposed to be at. Constructors
 * for convenient use.
 */
public class PlayerLocation extends RichLivingEntityLocation {

    // TODO: Rather RichLivingEntityLocation, the rest: compile/JIT.

    // "Heavy" object members that need to be set to null on cleanup. //

    private Player player = null;


    public PlayerLocation(final MCAccess mcAccess, final BlockCache blockCache) {
        super(mcAccess, blockCache);
    }

    public Player getPlayer() {
        return player;
    }

    /**
     * Checks if the player is on ice.
     * 
     * @return true, if the player is on ice
     */
    public boolean isOnIce() {
        // TODO: Move fully to entity or bounds, other way of telling sneaking? Might have another on-ice method for boats, though.
        if (onIce == null) {
            // TODO: Use a box here too ?
            // TODO: check if player is really sneaking (refactor from survivalfly to static access in Combined ?)!
            if (blockFlags != null && (blockFlags.longValue() & BlockProperties.F_ICE) == 0) {
                // TODO: check onGroundMinY !?
                onIce = false;
            } else {
                final int id;
                if (player.isSneaking() || player.isBlocking()) {
                    id = getTypeId(blockX, Location.locToBlock(minY - 0.1D), blockZ);
                }
                else {
                    id = getTypeIdBelow().intValue();
                }
                onIce = BlockProperties.isIce(id);
            }
        }
        return onIce;
    }

    /**
     * Sets the player location object.
     * 
     * @param location
     *            the location
     * @param player
     *            the player
     * @throws NullPointerException, if player.getLocation.getWorld() returns null.
     */
    public void set(final Location location, final Player player) {
        set(location, player, 0.001);
    }

    /**
     * Sets the player location object. Does not set or reset blockCache.
     * 
     * @param location
     *            the location
     * @param player
     *            the player
     * @throws NullPointerException, if Location.getWorld() returns null.
     */
    public void set(final Location location, final Player player, final double yOnGround)
    {
        super.set(location, player, yOnGround);
        // Entity reference.
        this.player = player;
    }

    /**
     * Not supported.
     */
    @Override
    public void set(Location location, LivingEntity livingEntity, double yOnGround) {
        throw new UnsupportedOperationException("Set must specify an instance of Player.");
    }

    /**
     * Set cached info according to other.<br>
     * Minimal optimizations: take block flags directly, on-ground max/min bounds, only set stairs if not on ground and not reset-condition.
     * @param other
     */
    public void prepare(final PlayerLocation other) {
        super.prepare(other);
    }

    /**
     * Set some references to null.
     */
    public void cleanup() {
        super.cleanup();
        player = null; // Still reset, to be sure.
    }

    /**
     * Check absolute coordinates and stance for (typical) exploits.
     * 
     * @return
     * @deprecated Not used anymore (hasIllegalCoords and hasIllegalStance are used individually instead).
     */
    public boolean isIllegal() {
        if (hasIllegalCoords()) {
            return true;
        } else {
            return hasIllegalStance();
        }
    }

    /**
     * Check for bounding box properties that might crash the server (if available, not the absolute coordinates).
     * @return
     */
    public boolean hasIllegalStance() {
        // TODO: This doesn't check this location, but the player.
        return getMCAccess().isIllegalBounds(player).decide(); // MAYBE = NO
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder(128);
        builder.append("PlayerLocation(");
        builder.append(world == null ? "null" : world.getName());
        builder.append('/');
        builder.append(Double.toString(x));
        builder.append(", ");
        builder.append(Double.toString(y));
        builder.append(", ");
        builder.append(Double.toString(z));
        builder.append(')');
        return builder.toString();
    }

}
