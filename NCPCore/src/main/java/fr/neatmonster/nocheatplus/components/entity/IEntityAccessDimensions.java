package fr.neatmonster.nocheatplus.components.entity;

import org.bukkit.entity.Entity;

/**
 * Somehow access entity dimensions. (MCAccess extends this for now.)
 * 
 * @author asofold
 *
 */
public interface IEntityAccessDimensions {

    /**
     * Return some width (rather the full bounding box width).
     * 
     * @param entity
     * @return
     */
    public double getWidth(Entity entity);

    /**
     * Get height of an entity (attack relevant, the maximal "thing" found).
     * 
     * @param entity
     * @return
     */
    public double getHeight(Entity entity);

}
