package fr.neatmonster.nocheatplus.components.location;

import org.bukkit.World;

/**
 * Bukkit specific access methods for a location.
 * 
 * @author asofold
 *
 */
public interface IBukkitLocation extends ILocationWithLook {

    public World getWorld();

}
