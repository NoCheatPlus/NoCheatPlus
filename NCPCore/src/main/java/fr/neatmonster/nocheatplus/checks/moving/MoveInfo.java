package fr.neatmonster.nocheatplus.checks.moving;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.compat.MCAccess;
import fr.neatmonster.nocheatplus.utilities.BlockCache;
import fr.neatmonster.nocheatplus.utilities.PlayerLocation;

/**
 * Coupling from and to PlayerLocation objects with a block cache for easy storage and reuse.
 * @author mc_dev
 *
 */
public class MoveInfo {
	public final BlockCache cache;
    public final PlayerLocation from;
    public final PlayerLocation to;
    /** For temporary use. Might need cloning for passing to external API. */
    public final Location useLoc = new Location(null, 0, 0, 0);
    
    public MoveInfo(final MCAccess mcAccess){
    	cache = mcAccess.getBlockCache(null);
    	from = new PlayerLocation(mcAccess, null);
    	to = new PlayerLocation(mcAccess, null);
    }
    
    /**
     * Demands at least setting from.
     * @param player
     * @param from
     * @param to
     * @param yOnGround
     */
    public final void set(final Player player, final Location from, final Location to, final double yOnGround){
        this.from.set(from, player, yOnGround);
        this.cache.setAccess(from.getWorld());
        this.from.setBlockCache(cache);
        if (to != null){
            this.to.set(to, player, yOnGround);
            this.to.setBlockCache(cache);
        }
        useLoc.setWorld(null); // Just in case of repeated setting.
    }
    public final void cleanup(){
        from.cleanup();
        to.cleanup();
        cache.cleanup();
        useLoc.setWorld(null);
    }
}
