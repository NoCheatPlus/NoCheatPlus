package fr.neatmonster.nocheatplus.checks.inventory;

import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.players.NCPPlayer;

/**
 * Abstract base class for Inventory checks, provides some convenience
 * methods for access to data and config that's relevant to this checktype
 */
public abstract class InventoryCheck extends Check {

    public InventoryCheck(final String name) {
        super("inventory." + name, InventoryConfig.class, InventoryData.class);
    }

    public abstract boolean check(final NCPPlayer player, final Object... args);

    public InventoryConfig getConfig(final NCPPlayer player) {
        return (InventoryConfig) player.getConfig(this);
    }

    public InventoryData getData(final NCPPlayer player) {
        return (InventoryData) player.getData(this);
    }
}
