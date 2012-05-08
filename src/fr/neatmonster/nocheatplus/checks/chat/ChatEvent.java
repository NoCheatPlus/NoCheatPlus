package fr.neatmonster.nocheatplus.checks.chat;

import fr.neatmonster.nocheatplus.actions.types.ActionList;
import fr.neatmonster.nocheatplus.checks.CheckEvent;
import fr.neatmonster.nocheatplus.players.NCPPlayer;

public class ChatEvent extends CheckEvent {

    public ChatEvent(final ChatCheck check, final NCPPlayer player, final ActionList actions, final double vL) {
        super(check, player, actions, vL);
    }

    @Override
    public ChatCheck getCheck() {
        return (ChatCheck) super.getCheck();
    }
}
