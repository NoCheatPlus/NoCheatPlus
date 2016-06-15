package fr.neatmonster.nocheatplus.compat;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

/**
 * Registered as generic instance.
 * 
 * @author asofold
 *
 */
public interface IBridgeCrossPlugin {

    /**
     * Safety check, enabling to skip certain checks or tests for delegate
     * players.
     * 
     * @param player
     * @return
     */
    public boolean isNativePlayer(Player player);

    /**
     * Safety check, enabling to skip certain checks or tests for delegate
     * entities.
     * 
     * @param player
     * @return
     */
    public boolean isNativeEntity(Entity entity);

}
