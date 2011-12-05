package cc.co.evenprime.bukkit.nocheat.checks;

import java.util.Locale;

import org.bukkit.Location;

import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.NoCheatPlayer;
import cc.co.evenprime.bukkit.nocheat.actions.types.Action;
import cc.co.evenprime.bukkit.nocheat.actions.types.ConsolecommandAction;
import cc.co.evenprime.bukkit.nocheat.actions.types.DummyAction;
import cc.co.evenprime.bukkit.nocheat.actions.types.LogAction;
import cc.co.evenprime.bukkit.nocheat.actions.types.SpecialAction;
import cc.co.evenprime.bukkit.nocheat.actions.types.WildCard;
import cc.co.evenprime.bukkit.nocheat.config.cache.ConfigurationCache;
import cc.co.evenprime.bukkit.nocheat.data.ExecutionHistory;

public abstract class Check {

    private final String    name;
    private final String    permission;
    protected final NoCheat plugin;

    public Check(NoCheat plugin, String name, String permission) {

        this.plugin = plugin;
        this.name = name;
        this.permission = permission;
    }

    public final String getName() {
        return name;
    }

    public final String getPermission() {
        return permission;
    }

    protected final boolean executeActions(NoCheatPlayer player, Action[] actions) {

        boolean special = false;

        final long time = System.currentTimeMillis() / 1000;

        final ConfigurationCache cc = player.getConfiguration();

        for(Action ac : actions) {
            if(getHistory(player).executeAction(ac, time)) {
                if(ac instanceof LogAction) {
                    executeLogAction((LogAction) ac, this, player, cc);
                } else if(ac instanceof SpecialAction) {
                    special = true;
                } else if(ac instanceof ConsolecommandAction) {
                    executeConsoleCommand((ConsolecommandAction) ac, this, player, cc);
                } else if(ac instanceof DummyAction) {
                    // nothing - it's a "DummyAction" after all
                }
            }
        }

        return special;
    }

    protected abstract ExecutionHistory getHistory(NoCheatPlayer player);

    private final void executeLogAction(LogAction l, Check check, NoCheatPlayer player, ConfigurationCache cc) {
        plugin.log(l.level, cc.logging.prefix + l.getLogMessage(player, check), cc);
    }

    private final void executeConsoleCommand(ConsolecommandAction action, Check check, NoCheatPlayer player, ConfigurationCache cc) {
        String command = action.getCommand(player, check);
        try {
            plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), command);
        } catch(Exception e) {
            System.out.println("[NoCheat] failed to execute the command '" + command + "', please check if everything is setup correct. ");
            e.printStackTrace();
        }
    }

    public String getParameter(WildCard wildcard, NoCheatPlayer player) {

        switch (wildcard) {
        case PLAYER:
            return player.getName();

        case CHECK:
            return getName();

        case LOCATION: {
            Location l = player.getPlayer().getLocation();
            return String.format(Locale.US, "%.2f,%.2f,%.2f", l.getX(), l.getY(), l.getZ());
        }

        case WORLD: {
            return player.getPlayer().getWorld().getName();
        }

        default:
            return "Evenprime was lazy and forgot to define " + wildcard + ".";
        }
    }
}
