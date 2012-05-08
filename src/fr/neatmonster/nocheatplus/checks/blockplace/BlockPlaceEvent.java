package fr.neatmonster.nocheatplus.checks.blockplace;

import fr.neatmonster.nocheatplus.actions.types.ActionList;
import fr.neatmonster.nocheatplus.checks.CheckEvent;
import fr.neatmonster.nocheatplus.players.NCPPlayer;

public class BlockPlaceEvent extends CheckEvent {

    public BlockPlaceEvent(final BlockPlaceCheck check, final NCPPlayer player, final ActionList actions,
            final double vL) {
        super(check, player, actions, vL);
    }

    @Override
    public BlockPlaceCheck getCheck() {
        return (BlockPlaceCheck) super.getCheck();
    }
}
