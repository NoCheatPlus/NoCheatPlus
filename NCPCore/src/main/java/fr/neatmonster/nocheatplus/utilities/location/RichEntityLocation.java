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
package fr.neatmonster.nocheatplus.utilities.location;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import fr.neatmonster.nocheatplus.compat.MCAccess;
import fr.neatmonster.nocheatplus.components.registry.event.IHandle;
import fr.neatmonster.nocheatplus.utilities.map.BlockCache;
import fr.neatmonster.nocheatplus.utilities.map.BlockProperties;

// TODO: Auto-generated Javadoc
/**
 * A location with an entity with a lot of extra stuff.
 * 
 * @author asofold
 *
 */
public class RichEntityLocation extends RichBoundsLocation {

    /** The mc access. */
    // Final members //
    private final IHandle<MCAccess> mcAccess;


    // Simple members //

    /** Full bounding box width. */
    private double width; // TODO: This is the entity width, happens to usually be the bounding box width +-. Move to entity / replace.

    /** Some entity collision height. */
    private double height; // TODO: Move to entity / replace.

    /** Indicate that this is a living entity. */
    private boolean isLiving;

    /** Living entity eye height, otherwise same as height.*/
    private double eyeHeight;

    /**
     * Entity is on ground, due to standing on an entity. (Might not get
     * evaluated if the player is on ground anyway.)
     */
    private boolean standsOnEntity = false;


    // "Heavy" object members that need to be set to null on cleanup. //

    /** The entity. */
    private Entity entity = null;


    /**
     * Instantiates a new rich entity location.
     *
     * @param mcAccess
     *            the mc access
     * @param blockCache
     *            BlockCache instance, may be null.
     */
    public RichEntityLocation(final IHandle<MCAccess> mcAccess, final BlockCache blockCache) {
        super(blockCache);
        this.mcAccess = mcAccess;
    }

    /**
     * Gets the width.
     *
     * @return the width
     */
    public double getWidth() {
        return width;
    }

    /**
     * Gets the height.
     *
     * @return the height
     */
    public double getHeight() {
        return height;
    }

    /**
     * Gets the eye height.
     *
     * @return the eye height
     */
    public double getEyeHeight() {
        return eyeHeight;
    }

    /**
     * Gets the entity.
     *
     * @return the entity
     */
    public Entity getEntity() {
        return entity;
    }

    /**
     * Test if this is a LivingEntity instance.
     *
     * @return true, if is living
     */
    public boolean isLiving() {
        return isLiving;
    }

    /**
     * Retrieve the currently registered MCAccess instance.
     *
     * @return the MC access
     */
    public MCAccess getMCAccess() {
        return mcAccess.getHandle();
    }

    /**
     * Get the internally stored IHandle instance for retrieving the currently
     * registered instance of MCAccess.
     * 
     * @return
     */
    public IHandle<MCAccess> getMCAccessHandle() {
        return mcAccess;
    }

    /**
     * Simple check with custom margins (Boat, Minecart). Does not update the
     * internally stored standsOnEntity field.
     *
     * @param yOnGround
     *            Margin below the player.
     * @param xzMargin
     *            the xz margin
     * @param yMargin
     *            Extra margin added below and above.
     * @return true, if successful
     */
    public boolean standsOnEntity(final double yOnGround, final double xzMargin, final double yMargin) {
        return blockCache.standsOnEntity(entity, minX - xzMargin, minY - yOnGround - yMargin, minZ - xzMargin, maxX + xzMargin, minY + yMargin, maxZ + xzMargin);
    }

    /**
     * Checks if the entity is on ground, including entities such as Minecart, Boat.
     * 
     * @return true, if the player is on ground
     */
    public boolean isOnGround() {
        if (onGround != null) {
            return onGround;
        }
        boolean res = super.isOnGround();
        if (!res) {
            // Check if standing on an entity.
            // TODO: Get rid of needing an entity for checking this (!). Move to RichBoundsLocation.
            final double d1 = 0.25;
            if (blockCache.standsOnEntity(entity, minX - d1, minY - yOnGround - d1, minZ - d1, maxX + d1, minY + 0.25 + d1, maxZ + d1)) {
                res = onGround = standsOnEntity = true;
            }
        }
        return res;
    }

    /**
     * Test if the player is just on ground due to standing on an entity.
     * 
     * @return True, if the player is not standing on blocks, but on an entity.
     */
    public boolean isOnGroundDueToStandingOnAnEntity() {
        return isOnGround() && standsOnEntity; // Just ensure it is initialized.
    }

    /**
     * Check if a player may climb upwards (isOnClimbable returned true, player
     * does not move from/to ground).<br>
     * Having checked the other stuff is prerequisite for calling this (!).
     *
     * @param jumpHeigth
     *            Height the player is allowed to have jumped.
     * @return true, if successful
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
     *            the margin above eye height
     * @return true, if is head obstructed
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
     * @return true, if is head obstructed
     * @throws IllegalArgumentException
     *             If marginAboveEyeHeight is smaller than 0.
     */
    public boolean isHeadObstructed(double marginAboveEyeHeight, boolean stepCorrection) {
        // TODO: Add an isObstructed method with extra height parameter to RichBoundsLocation?
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
     * @return true, if is head obstructed
     */
    public boolean isHeadObstructed() {
        return isHeadObstructed(0.0, true);
    }

    /**
     * Convenience constructor for using mcAccess.getHeight for fullHeight.
     *
     * @param location
     *            the location
     * @param entity
     *            the entity
     * @param yOnGround
     *            the y on ground
     */
    public void set(final Location location, final Entity entity, final double yOnGround) {
        final MCAccess mcAccess = this.mcAccess.getHandle();
        doSet(location, entity, mcAccess.getWidth(entity), mcAccess.getHeight(entity), yOnGround);
    }

    /**
     * Sets the.
     *
     * @param location
     *            the location
     * @param entity
     *            the entity
     * @param fullHeight
     *            Allows to specify eyeHeight here. Currently might be
     *            overridden by eyeHeight, if that is greater.
     * @param yOnGround
     *            the y on ground
     */
    public void set(final Location location, final Entity entity, double fullHeight, final double yOnGround) {
        final MCAccess mcAccess = this.mcAccess.getHandle();
        doSet(location, entity, mcAccess.getWidth(entity), fullHeight, yOnGround);
    }

    /**
     * Sets the.
     *
     * @param location
     *            the location
     * @param entity
     *            the entity
     * @param fullWidth
     *            Override the bounding box width (full width).
     * @param fullHeight
     *            Allows to specify eyeHeight here. Currently might be
     *            overridden by eyeHeight, if that is greater.
     * @param yOnGround
     *            the y on ground
     */
    public void set(final Location location, final Entity entity, final double fullWidth, double fullHeight, final double yOnGround) {
        doSet(location, entity, fullWidth, fullHeight, yOnGround);
    }

    /**
     * Do set.<br>
     * For the bounding box height, the maximum of given fullHeight, eyeHeight
     * with sneaking ignored and entity height is used.
     *
     * @param location
     *            the location
     * @param entity
     *            the entity
     * @param fullWidth
     *            the full width
     * @param fullHeight
     *            the full height
     * @param yOnGround
     *            the y on ground
     */
    protected void doSet(final Location location, final Entity entity, final double fullWidth, double fullHeight, final double yOnGround) {
        if (entity instanceof LivingEntity) {
            isLiving = true;
            final LivingEntity living = (LivingEntity) entity;
            eyeHeight = living.getEyeHeight();
            fullHeight = Math.max(Math.max(fullHeight, eyeHeight), living.getEyeHeight(true));
        }
        else {
            isLiving = false;
            eyeHeight = fullHeight;
        }
        this.entity = entity;
        final MCAccess mcAccess = this.mcAccess.getHandle();
        this.width = mcAccess.getWidth(entity);
        this.height = mcAccess.getHeight(entity);
        standsOnEntity = false;
        super.set(location, fullWidth, fullHeight, yOnGround);
    }

    /**
     * Not supported.
     *
     * @param location
     *            the location
     * @param fullWidth
     *            the full width
     * @param fullHeight
     *            the full height
     * @param yOnGround
     *            the y on ground
     */
    @Override
    public void set(Location location, double fullWidth, double fullHeight, double yOnGround) {
        throw new UnsupportedOperationException("Set must specify an instance of Entity.");
    }

    /**
     * Set cached info according to other.<br>
     * Minimal optimizations: take block flags directly, on-ground max/min
     * bounds, only set stairs if not on ground and not reset-condition.
     *
     * @param other
     *            the other
     */
    public void prepare(final RichEntityLocation other) {
        super.prepare(other);
        this.standsOnEntity = other.standsOnEntity;
    }

    /**
     * Set some references to null.
     */
    public void cleanup() {
        super.cleanup();
        entity = null;
    }

    /* (non-Javadoc)
     * @see fr.neatmonster.nocheatplus.utilities.RichBoundsLocation#toString()
     */
    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder(128);
        builder.append("RichEntityLocation(");
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
