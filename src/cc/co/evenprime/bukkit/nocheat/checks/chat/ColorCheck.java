package cc.co.evenprime.bukkit.nocheat.checks.chat;

import java.util.Locale;
import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.NoCheatPlayer;
import cc.co.evenprime.bukkit.nocheat.actions.ParameterName;
import cc.co.evenprime.bukkit.nocheat.data.Statistics.Id;

public class ColorCheck extends ChatCheck {

    public ColorCheck(NoCheat plugin) {
        super(plugin, "chat.color");
    }

    public boolean check(NoCheatPlayer player, ChatData data, ChatConfig cc) {

        if(data.message.matches(".*\247.*")) {

            data.colorVL += 1;
            incrementStatistics(player, Id.CHAT_COLOR, 1);

            boolean filter = executeActions(player, cc.colorActions.getActions(data.colorVL));

            if(filter) {
                // Remove color codes
                data.message = data.message.replaceAll("\302\247.", "").replaceAll("\247.", "");
            }
        }

        return false;
    }

    public String getParameter(ParameterName wildcard, NoCheatPlayer player) {

        if(wildcard == ParameterName.VIOLATIONS)
            return String.format(Locale.US, "%d", (int) getData(player.getDataStore()).colorVL);
        else
            return super.getParameter(wildcard, player);
    }
}
