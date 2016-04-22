package fr.neatmonster.nocheatplus.utilities;

import org.bukkit.Location;
import org.bukkit.entity.Entity;

import fr.neatmonster.nocheatplus.compat.MCAccess;

/**
 * A location with an entity with a lot of extra stuff.
 * 
 * @author asofold
 *
 */
public class RichEntityLocation extends RichBoundsLocation {

    // Final members //
    private final MCAccess mcAccess;


    // Simple members //

    /** Full bounding box width. */
    private double width; // TODO: This is the entity width, happens to usually be the bounding box width +-. Move to entity / replace.

    /** Some entity collision height. */
    private double height; // TODO: Move to entity / replace.

    /**
     * Entity is on ground, due to standing on an entity. (Might not get
     * evaluated if the player is on ground anyway.)
     */
    private boolean standsOnEntity = false;


    // "Heavy" object members that need to be set to null on cleanup. //

    private Entity entity = null;


    public RichEntityLocation(final MCAccess mcAccess, final BlockCache blockCache) {
        super(blockCache);
        this.mcAccess = mcAccess;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    public Entity getEntity() {
        return entity;
    }

    /**
     * Retrieve the internally stored MCAccess instance.
     * @return
     */
    public MCAccess getMCAccess() {
        return mcAccess;
    }

    /**
     * Simple check with custom margins (Boat, Minecart). Does not update the
     * internally stored standsOnEntity field.
     * 
     * @param yOnGround
     *            Margin below the player.
     * @param xzMargin
     * @param yMargin
     *            Extra margin added below and above.
     * @return
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
            // TODO: Get rid of needing an entity for checking this (!). Move to bounds.
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
     * Convenience constructor for using mcAccess.getHeight for fullHeight.
     * @param location
     * @param entity
     * @param yOnGround
     */
    public void set(final Location location, final Entity entity, final double yOnGround) {
        set(location, entity, mcAccess.getHeight(entity), yOnGround);
    }

    /**
     * 
     * @param location
     * @param entity
     * @param fullHeight Allows to specify eyeHeight here.
     * @param yOnGround
     */
    public void set(final Location location, final Entity entity, final double fullHeight, final double yOnGround) {
        super.set(location, mcAccess.getWidth(entity), fullHeight, yOnGround);
        this.entity = entity;
        this.width = mcAccess.getWidth(entity);
        this.height = mcAccess.getHeight(entity);
        standsOnEntity = false;
    }

    /**
     * Not supported.
     */
    @Override
    public void set(Location location, double fullWidth, double fullHeight, double yOnGround) {
        throw new UnsupportedOperationException("Set must specify an instance of Entity.");
    }

    /**
     * Set cached info according to other.<br>
     * Minimal optimizations: take block flags directly, on-ground max/min bounds, only set stairs if not on ground and not reset-condition.
     * @param other
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
