package fr.neatmonster.nocheatplus.utilities;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

import fr.neatmonster.nocheatplus.compat.MCAccess;

/**
 * A location with an entity that is living with a lot of extra stuff.
 * 
 * @author asofold
 *
 */
public class RichLivingEntityLocation extends RichEntityLocation {

    // Simple members // 

    /** Living entity eye height.*/
    private double eyeHeight;


    // "Heavy" object members that need to be set to null on cleanup. //

    private LivingEntity livingEntity = null;


    public RichLivingEntityLocation(final MCAccess mcAccess, final BlockCache blockCache) {
        super(mcAccess, blockCache);
    }

    public double getEyeHeight() {
        return eyeHeight;
    }

    public LivingEntity getLivingEntity() {
        return livingEntity;
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
     * Sets the player location object. Does not set or reset blockCache.
     * 
     * @param location
     *            the location
     * @param player
     *            the player
     * @throws NullPointerException, if Location.getWorld() returns null.
     */
    public void set(final Location location, final LivingEntity livingEntity, final double yOnGround)
    {
        super.set(location, livingEntity, eyeHeight, yOnGround);
        // Entity reference.
        this.livingEntity = livingEntity;
        this.eyeHeight = livingEntity.getEyeHeight();
    }

    // No need to override prepare.

    /**
     * Set some references to null.
     */
    public void cleanup() {
        super.cleanup();
        livingEntity = null; // Still reset, to be sure.
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder(128);
        builder.append("RichLivingEntityLocation(");
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
