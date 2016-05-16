package fr.neatmonster.nocheatplus.components.location;

import org.bukkit.entity.Entity;

/**
 * Retrieve last position and look for a Bukkit Entity instance.
 * 
 * @author asofold
 *
 */
public interface IEntityAccessLastPositionAndLook {

    /**
     * Fetch the last position with look from an entity.
     * 
     * @param entity
     * @param location
     *            This instance gets updated by last coordinates and looking
     *            direction.
     */
    public void getPositionAndLook(Entity entity, ISetPositionWithLook location);

    /**
     * Set the last position with look of an entity.
     * 
     * @param entity
     *            The entity for which to set last position and look.
     * @param location
     *            The reference data to set the last position and look of the
     *            entity to.
     */
    public void setPositionAndLook(Entity entity, IGetPositionWithLook location);

}
