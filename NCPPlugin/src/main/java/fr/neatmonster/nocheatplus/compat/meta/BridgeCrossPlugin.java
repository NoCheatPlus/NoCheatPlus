package fr.neatmonster.nocheatplus.compat.meta;

import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.compat.MCAccess;
import fr.neatmonster.nocheatplus.components.registry.feature.MCAccessHolder;

/**
 * Utility to probe for cross-plugin issues, such as Player delegates.
 * Registered as generic instance for ICrossPlugin.
 * 
 * @author asofold
 *
 */
public class BridgeCrossPlugin implements MCAccessHolder {

    private MCAccess mcAccess;

    public BridgeCrossPlugin(MCAccess mcAccess) {
        this.mcAccess = mcAccess;
    }

    @Override
    public void setMCAccess(MCAccess mcAccess) {
        this.mcAccess = mcAccess;
    }

    @Override
    public MCAccess getMCAccess() {
        return mcAccess;
    }

    public boolean isNativePlayer(final Player player) {
        // Possibly better within MCAccess later on.
        return player.getClass().getSimpleName().equals("CraftPlayer");
    }

}
