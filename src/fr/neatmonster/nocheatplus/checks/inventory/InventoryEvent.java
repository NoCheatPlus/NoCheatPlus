package fr.neatmonster.nocheatplus.checks.inventory;

import fr.neatmonster.nocheatplus.actions.types.ActionList;
import fr.neatmonster.nocheatplus.checks.CheckEvent;
import fr.neatmonster.nocheatplus.players.NCPPlayer;

public class InventoryEvent extends CheckEvent {

    public InventoryEvent(final InventoryCheck check, final NCPPlayer player, final ActionList actions, final double vL) {
        super(check, player, actions, vL);
    }

    @Override
    public InventoryCheck getCheck() {
        return (InventoryCheck) super.getCheck();
    }
}
