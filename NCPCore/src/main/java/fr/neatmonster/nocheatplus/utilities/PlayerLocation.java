package fr.neatmonster.nocheatplus.utilities;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.compat.MCAccess;

/**
 * Lots of content for a location a player is supposed to be at (simplified
 * view).
 */
public class PlayerLocation extends RichEntityLocation {

    // TODO: Rather RichLivingEntityLocation, the rest: compile/JIT.

    // Simple members // 

    /** Living entity eye height.*/
    private double eyeHeight;


    // Light object members //

    /** Is the player on ice? */
    Boolean onIce = null;


    // "Heavy" object members that need to be set to null on cleanup. //

    Player entity = null;


    public PlayerLocation(final MCAccess mcAccess, final BlockCache blockCache) {
        super(mcAccess, blockCache);
    }

    public Player getPlayer() {
        return entity;
    }

    public double getEyeHeight() {
        return eyeHeight;
    }

    /**
     * Checks if the player is on ice.
     * 
     * @return true, if the player is on ice
     */
    public boolean isOnIce() {
        // TODO: Move to entity or bounds, other way of telling sneaking? Might have another on-ice method for boats, though.
        if (onIce == null) {
            // TODO: Use a box here too ?
            // TODO: check if player is really sneaking (refactor from survivalfly to static access in Combined ?)!
            if (blockFlags != null && (blockFlags.longValue() & BlockProperties.F_ICE) == 0) {
                // TODO: check onGroundMinY !?
                onIce = false;
            } else {
                final int id;
                if (entity.isSneaking() || entity.isBlocking()) {
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
     * Check if a player may climb upwards (isOnClimbable returned true, player does not move from/to ground).<br>
     * Having checked the other stuff is prerequisite for calling this (!).
     * @param jumpHeigth Height the player is allowed to have jumped.
     * @return
     */
    public boolean canClimbUp(double jumpHeigth) {
        // TODO: distinguish vines.
        if (BlockProperties.isAttachedClimbable(getTypeId())) {
            // Check if vine is attached to something solid
            if (BlockProperties.canClimbUp(blockCache, blockX, blockY, blockZ)) {
                return true;
            }
            // Check the block at head height.
            final int headY = Location.locToBlock(maxY);
            if (headY > blockY) {
                for (int cy = blockY + 1; cy <= headY; cy ++) {
                    if (BlockProperties.canClimbUp(blockCache, blockX, cy, blockZ)) {
                        return true;
                    }
                }
            }
            // Finally check possible jump height.
            // TODO: This too is inaccurate.
            if (isOnGround(jumpHeigth)) {
                // Here ladders are ok.
                return true;
            }
            return false;
        }
        return true;
    }

    /**
     * Test if something solid/ground-like collides within the given margin
     * above the eye height of the player.
     * 
     * @param marginAboveEyeHeight
     * @return
     */
    public boolean isHeadObstructed(double marginAboveEyeHeight) {
        return isHeadObstructed(marginAboveEyeHeight, true); // TODO: This is changed behavior, need to check calls.
    }

    /**
     * Test if something solid/ground-like collides within the given margin
     * above the eye height of the player.
     * 
     * @param marginAboveEyeHeight
     *            Must be greater than or equal zero.
     * @param stepCorrection
     *            If set to true, a correction method is used for leniency.
     * @return
     * @throws IllegalArgumentException
     *             If marginAboveEyeHeight is smaller than 0.
     */
    public boolean isHeadObstructed(double marginAboveEyeHeight, boolean stepCorrection) {
        if (marginAboveEyeHeight < 0.0) {
            throw new IllegalArgumentException("marginAboveEyeHeight must be greater than 0.");
        }
        // TODO: Add test for this bit of code.
        if (stepCorrection) {
            double ref = maxY + marginAboveEyeHeight;
            ref = ref - (double) Location.locToBlock(ref) + 0.35;
            for (double bound = 1.0; bound > 0.0; bound -= 0.25) {
                if (ref >= bound) {
                    // Use this level for correction.
                    marginAboveEyeHeight += bound + 0.35 - ref;
                    break;
                }
            }
        }
        return BlockProperties.collides(blockCache, minX , maxY, minZ, maxX, maxY + marginAboveEyeHeight, maxZ, BlockProperties.F_GROUND | BlockProperties.F_SOLID);
    }

    /**
     * Test if something solid/ground-like collides within a default
     * margin/estimation above the eye height of the player.
     * 
     * @return
     */
    public boolean isHeadObstructed() {
        return isHeadObstructed(0.0, true);
    }

    /**
     * Set cached info according to other.<br>
     * Minimal optimizations: take block flags directly, on-ground max/min bounds, only set stairs if not on ground and not reset-condition.
     * @param other
     */
    public void prepare(final PlayerLocation other) {
        super.prepare(other);
        this.onIce = other.isOnIce();
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
        super.set(location, player, eyeHeight, yOnGround);
        // Entity reference.
        this.entity = player;
        this.eyeHeight = player.getEyeHeight();
        this.onIce = null;
    }

    /**
     * Set some references to null.
     */
    public void cleanup() {
        super.cleanup();
        entity = null; // Still reset, to be sure.
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
        return mcAccess.isIllegalBounds(entity).decide(); // MAYBE = NO
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
