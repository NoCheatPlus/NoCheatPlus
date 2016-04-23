package fr.neatmonster.nocheatplus.checks.moving.model;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.compat.MCAccess;
import fr.neatmonster.nocheatplus.utilities.BlockCache;
import fr.neatmonster.nocheatplus.utilities.PlayerLocation;

/**
 * Coupling from and to PlayerLocation objects with a block cache for easy
 * storage and reuse.
 * 
 * @author mc_dev
 *
 */
public class MoveInfo {

    /**
     * Might need cloning for passing to external API. This is not initialized
     * in set. World is set to null on cleanup!
     */
    public final Location useLoc = new Location(null, 0, 0, 0);
    public final BlockCache cache;
    public final PlayerLocation from;
    public final PlayerLocation to;

    public MoveInfo(final MCAccess mcAccess){
        cache = mcAccess.getBlockCache(null);
        from = new PlayerLocation(mcAccess, null);
        to = new PlayerLocation(mcAccess, null);
    }

    /**
     * Initialize from, and if given to. Note that useLoc and data are left
     * untouched.
     * 
     * @param player
     * @param from
     *            Must not be null.
     * @param to
     *            Can be null.
     * @param yOnGround
     */
    public final void set(final Player player, final Location from, final Location to, final double yOnGround){
        this.cache.setAccess(from.getWorld());
        this.from.set(from, player, yOnGround);
        this.from.setBlockCache(cache);
        if (to != null){
            this.to.set(to, player, yOnGround);
            this.to.setBlockCache(cache);
        }
        // Note: using set to reset to by passing null won't work. 
    }

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
