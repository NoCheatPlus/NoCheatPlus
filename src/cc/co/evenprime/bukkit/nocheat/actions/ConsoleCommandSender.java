package cc.co.evenprime.bukkit.nocheat.actions;

import java.util.Set;

import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.PermissibleBase;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;

/**
 * The ConsoleCommandSender imitates the admin writing commands into the server
 * console. It is used to execute consolecommand actions.
 * 
 * @author Evenprime
 * 
 */
public class ConsoleCommandSender implements CommandSender {

    private Server                server;
    private final PermissibleBase perm = new PermissibleBase(this);

    public ConsoleCommandSender(Server server) {
        this.server = server;
    }

    public void sendMessage(String message) {
        // We have no interest in returning messages
    }

    public boolean isOp() {
        return true;
    }

    public void setOp(boolean value) {
        // We are OP, or at least we claim to be :)
    }

    public boolean isPlayer() {
        return false;
    }

    public Server getServer() {
        return server;
    }

    public boolean isPermissionSet(String name) {
        return perm.isPermissionSet(name);
    }

    public boolean isPermissionSet(Permission perm) {
        return this.perm.isPermissionSet(perm);
    }

    public boolean hasPermission(String name) {
        return true; // We have ALL permissions ;)
    }

    public boolean hasPermission(Permission perm) {
        return true; // We have ALL permissions ;)
    }

    public PermissionAttachment addAttachment(Plugin plugin, String name, boolean value) {
        return perm.addAttachment(plugin, name, value);
    }

    public PermissionAttachment addAttachment(Plugin plugin) {
        return perm.addAttachment(plugin);
    }

    public PermissionAttachment addAttachment(Plugin plugin, String name, boolean value, int ticks) {
        return perm.addAttachment(plugin, name, value, ticks);
    }

    public PermissionAttachment addAttachment(Plugin plugin, int ticks) {
        return perm.addAttachment(plugin, ticks);
    }

    public void removeAttachment(PermissionAttachment attachment) {
        perm.removeAttachment(attachment);
    }

    public void recalculatePermissions() {
        perm.recalculatePermissions();
    }

    public Set<PermissionAttachmentInfo> getEffectivePermissions() {
        return perm.getEffectivePermissions();
    }

    public void executeConsoleCommand(String command) {
        try {
            server.dispatchCommand(this, command);
        } catch(Exception e) {
            // TODO: Better error handling
        }
    }
}
