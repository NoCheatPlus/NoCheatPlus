package fr.neatmonster.nocheatplus.checks.fight;

import fr.neatmonster.nocheatplus.actions.types.ActionList;
import fr.neatmonster.nocheatplus.checks.CheckEvent;
import fr.neatmonster.nocheatplus.players.NCPPlayer;

public class FightEvent extends CheckEvent {

    public FightEvent(final FightCheck check, final NCPPlayer player, final ActionList actions, final double vL) {
        super(check, player, actions, vL);
    }

    @Override
    public FightCheck getCheck() {
        return (FightCheck) super.getCheck();
    }
}
