package fr.neatmonster.nocheatplus.checks.moving.model;

import org.bukkit.Location;
import org.bukkit.entity.Entity;

import fr.neatmonster.nocheatplus.compat.MCAccess;
import fr.neatmonster.nocheatplus.utilities.BlockCache;
import fr.neatmonster.nocheatplus.utilities.RichEntityLocation;

/**
 * Represent a move with start and end point. Short-term use of
 * RichEntityLocation instances (from and to), and a Location for temporary use.
 * 
 * @author asofold
 *
 * @param <REL>
 *            Location type to use.
 */
public abstract class MoveInfo <REL extends RichEntityLocation, E extends Entity> {

    /**
     * Would need cloning for passing to external API. This is not initialized
     * in set. World is set to null on cleanup!
     */
    public final Location useLoc = new Location(null, 0, 0, 0);
    public final BlockCache cache;
    public final REL from;
    public final REL to;

    public MoveInfo(final MCAccess mcAccess, REL from, REL to){
        cache = mcAccess.getBlockCache(null);
        this.from = from;
        this.to = to;
    }

    /**
     * Initialize from, and if given to. Note that useLoc and data are left
     * untouched.
     * 
     * @param entity
     * @param from
     *            Must not be null.
     * @param to
     *            Can be null.
     * @param yOnGround
     */
    public final void set(final E entity, final Location from, final Location to, final double yOnGround){
        this.cache.setAccess(from.getWorld());
        this.from.setBlockCache(cache);
        set(this.from, from, entity, yOnGround);
        if (to != null){
            this.to.setBlockCache(cache);
            set(this.to, to, entity, yOnGround);
        }
        // Note: using set to reset to by passing null won't work. 
    }

    /**
     * Called after setting BlockCache for the passed rLoc. (Needed to avoid
     * issues with generics.)
     * 
     * @param rLoc
     * @param loc
     * @param entity
     * @param yOnGround
     */
    protected abstract void set(REL rLoc, Location loc, E entity, double yOnGround);

    /**
     * Clear caches and remove World references and such. Also resets the world
     * of useLoc.
     */
    public final void cleanup(){
        useLoc.setWorld(null);
        from.cleanup();
        to.cleanup();
        cache.cleanup();
    }
}
