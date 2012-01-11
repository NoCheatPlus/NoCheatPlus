package cc.co.evenprime.bukkit.nocheat.actions;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.permissions.PermissibleBase;
import org.bukkit.permissions.ServerOperator;

public class NoCheatCommandSender extends PermissibleBase implements ConsoleCommandSender {

    private static final ServerOperator serverOperator = new ServerOperator() {

                                                           @Override
                                                           public boolean isOp() {
                                                               return true;
                                                           }

                                                           @Override
                                                           public void setOp(boolean value) {
                                                               //
                                                           }
                                                       };

    public NoCheatCommandSender() {
        super(serverOperator);
    }

    @Override
    public String getName() {
        return "NoCheatCommandSender";
    }

    @Override
    public void sendMessage(String message) {
        // We don't want messages
    }

    @Override
    public Server getServer() {
        return Bukkit.getServer();
    }

}
