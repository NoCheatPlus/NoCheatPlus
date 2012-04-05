package me.neatmonster.nocheatplus.checks.chat;

import java.util.Locale;

import me.neatmonster.nocheatplus.NoCheatPlus;
import me.neatmonster.nocheatplus.NoCheatPlusPlayer;
import me.neatmonster.nocheatplus.actions.ParameterName;
import me.neatmonster.nocheatplus.data.Statistics.Id;

public class ColorCheck extends ChatCheck {

    public ColorCheck(final NoCheatPlus plugin) {
        super(plugin, "chat.color");
    }

    public boolean check(final NoCheatPlusPlayer player, final ChatData data, final ChatConfig cc) {

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
    public String getParameter(final ParameterName wildcard, final NoCheatPlusPlayer player) {

        if (wildcard == ParameterName.VIOLATIONS)
            return String.format(Locale.US, "%d", getData(player).colorVL);
        else
            return super.getParameter(wildcard, player);
    }
}
