package fr.neatmonster.nocheatplus.command.admin.debug;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.access.CheckDataFactory;
import fr.neatmonster.nocheatplus.checks.access.ICheckData;
import fr.neatmonster.nocheatplus.command.BaseCommand;
import fr.neatmonster.nocheatplus.players.DataManager;

public class DebugPlayerCommand extends BaseCommand {

    public DebugPlayerCommand(JavaPlugin plugin) {
        super(plugin, "player", null);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return null; // Tab-complete player names. 
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
        
        // TODO: This is a minimal version just to turn it on (!). Further: check types, -off.
        for (int i = 2; i < args.length; i++) {
            final String name = args[i];
            final Player player = DataManager.getPlayer(name);
            if (player == null) {
                sender.sendMessage("Not online: " + name);
            } else {
                setDebugAll(player);
                sender.sendMessage("Set all checks to debug for player: " + player.getName());
            }
        }
        return true;
    }

    private void setDebugAll(final Player player) {
        for (final CheckType type : CheckType.values()) {
            CheckDataFactory factory = type.getDataFactory();
            if (factory != null) {
                ICheckData data = factory.getData(player);
                if (data != null) {
                    data.setDebug(true);
                }
            }
        }
    }

    

}
