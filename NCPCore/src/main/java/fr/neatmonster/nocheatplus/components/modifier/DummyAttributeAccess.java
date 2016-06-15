package fr.neatmonster.nocheatplus.components.modifier;

import org.bukkit.entity.Player;

/**
 * Default implementation for access not being available.
 * 
 * @author asofold
 *
 */
public class DummyAttributeAccess implements IAttributeAccess {

    @Override
    public double getSpeedAttributeMultiplier(Player player) {
        return Double.MAX_VALUE;
    }

    @Override
    public double getSprintAttributeMultiplier(Player player) {
        return Double.MAX_VALUE;
    }

}
