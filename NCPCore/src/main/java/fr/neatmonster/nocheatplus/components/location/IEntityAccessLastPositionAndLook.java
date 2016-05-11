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
     * 
     * @param entity
     * @param location
     *            This instance gets updated by last coordinates and looking
     *            direction.
     */
    public void setPositionAndLook(Entity entity, ISetPositionWithLook location);

}
