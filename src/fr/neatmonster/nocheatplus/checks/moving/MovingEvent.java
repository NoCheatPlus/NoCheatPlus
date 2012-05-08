package fr.neatmonster.nocheatplus.checks.moving;

import fr.neatmonster.nocheatplus.actions.types.ActionList;
import fr.neatmonster.nocheatplus.checks.CheckEvent;
import fr.neatmonster.nocheatplus.players.NCPPlayer;

public class MovingEvent extends CheckEvent {

    public MovingEvent(final MovingCheck check, final NCPPlayer player, final ActionList actions, final double vL) {
        super(check, player, actions, vL);
    }

    @Override
    public MovingCheck getCheck() {
        return (MovingCheck) super.getCheck();
    }
}
