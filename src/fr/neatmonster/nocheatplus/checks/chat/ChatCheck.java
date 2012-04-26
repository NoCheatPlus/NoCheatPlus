package fr.neatmonster.nocheatplus.checks.chat;

import fr.neatmonster.nocheatplus.actions.ParameterName;
import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.players.NCPPlayer;

/**
 * Abstract base class for Chat checks, provides some convenience
 * methods for access to data and config that's relevant to this checktype
 */
public abstract class ChatCheck extends Check {

    public ChatCheck(final String name) {
        super("chat." + name, ChatConfig.class, ChatData.class);
    }

    public abstract boolean check(final NCPPlayer player, final Object... args);

    public ChatConfig getConfig(final NCPPlayer player) {
        return (ChatConfig) player.getConfig(this);
    }

    public ChatData getData(final NCPPlayer player) {
        return (ChatData) player.getData(this);
    }

    @Override
    public String getParameter(final ParameterName wildcard, final NCPPlayer player) {

        if (wildcard == ParameterName.TEXT)
            // Filter colors from the players message when logging
            return getData(player).message.replaceAll("\302\247.", "").replaceAll("\247.", "");
        else
            return super.getParameter(wildcard, player);
    }
}
