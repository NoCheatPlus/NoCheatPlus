package fr.neatmonster.nocheatplus.checks.blockbreak;

import fr.neatmonster.nocheatplus.actions.types.ActionList;
import fr.neatmonster.nocheatplus.checks.CheckEvent;
import fr.neatmonster.nocheatplus.players.NCPPlayer;

public class BlockBreakEvent extends CheckEvent {

    public BlockBreakEvent(final BlockBreakCheck check, final NCPPlayer player, final ActionList actions,
            final double vL) {
        super(check, player, actions, vL);
    }

    @Override
    public BlockBreakCheck getCheck() {
        return (BlockBreakCheck) super.getCheck();
    }
}
