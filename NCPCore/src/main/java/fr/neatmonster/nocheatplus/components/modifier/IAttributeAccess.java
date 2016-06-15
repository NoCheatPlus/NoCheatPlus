package fr.neatmonster.nocheatplus.components.modifier;

import org.bukkit.entity.Player;

/**
 * Encapsulate attribute access. Note that some of the methods may exclude
 * specific modifiers, or otherwise perform calculations, e.g. in order to
 * return a multiplier to be applied to typical walking speed.
 * 
 * @author asofold
 *
 */
public interface IAttributeAccess {

    /**
     * Generic speed modifier as a multiplier.
     * 
     * @param player
     * @return A multiplier for the allowed speed, excluding the sprint boost
     *         modifier (!). If not possible to determine, it should
     *         Double.MAX_VALUE.
     */
    public double getSpeedAttributeMultiplier(Player player);

    /**
     * Sprint boost modifier as a multiplier.
     * 
     * @param player
     * @return The sprint boost modifier as a multiplier. If not possible to
     *         determine, it should be Double.MAX_VALUE.
     */
    public double getSprintAttributeMultiplier(Player player);

}
