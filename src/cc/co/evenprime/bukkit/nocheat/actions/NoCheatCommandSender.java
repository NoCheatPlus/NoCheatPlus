package cc.co.evenprime.bukkit.nocheat.actions;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.PermissibleBase;

public class NoCheatCommandSender extends PermissibleBase implements CommandSender {

    public NoCheatCommandSender() {
        super(null);
    }

    @Override
    public String getName() {
        return "NoCheatCommandSender";
    }

    @Override
    public void sendMessage(String message) {
        // We don't want to receive messages, as we can't do anything with them
        // anyway
    }

    @Override
    public Server getServer() {
        return Bukkit.getServer();
    }

}
