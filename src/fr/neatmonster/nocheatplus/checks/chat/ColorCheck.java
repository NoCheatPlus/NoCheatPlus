package fr.neatmonster.nocheatplus.checks.chat;

import org.bukkit.Bukkit;

import fr.neatmonster.nocheatplus.actions.ParameterName;
import fr.neatmonster.nocheatplus.actions.types.ActionList;
import fr.neatmonster.nocheatplus.players.NCPPlayer;
import fr.neatmonster.nocheatplus.players.informations.Statistics.Id;

public class ColorCheck extends ChatCheck {

    public class ColorCheckEvent extends ChatEvent {

        public ColorCheckEvent(final ColorCheck check, final NCPPlayer player, final ActionList actions, final double vL) {
            super(check, player, actions, vL);
        }
    }

    public ColorCheck() {
        super("color");
    }

    @Override
    public boolean check(final NCPPlayer player, final Object... args) {
        final ChatConfig cc = getConfig(player);
        final ChatData data = getData(player);

        if (data.message.contains("\247")) {

            data.colorVL += 1;
            incrementStatistics(player, Id.CHAT_COLOR, 1);

            final boolean filter = executeActions(player, cc.colorActions, data.colorVL);

            if (filter)
                // Remove color codes
                data.message = data.message.replaceAll("\302\247.", "").replaceAll("\247.", "");
        }

        return false;
    }

    @Override
    protected boolean executeActions(final NCPPlayer player, final ActionList actionList, final double violationLevel) {
        final ColorCheckEvent event = new ColorCheckEvent(this, player, actionList, violationLevel);
        Bukkit.getPluginManager().callEvent(event);
        if (!event.isCancelled())
            return super.executeActions(player, event.getActions(), event.getVL());
        return false;
    }

    @Override
    public String getParameter(final ParameterName wildcard, final NCPPlayer player) {

        if (wildcard == ParameterName.VIOLATIONS)
            return String.valueOf(Math.round(getData(player).colorVL));
        else
            return super.getParameter(wildcard, player);
    }
}
