package cc.co.evenprime.bukkit.nocheat;

import java.util.Set;

import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;

public class CustomCommandSender implements CommandSender {

    private final Server server;

    public CustomCommandSender(Server server) {
        this.server = server;
    }

    @Override
    public boolean isPermissionSet(String name) {
        // Just pretend that we have a permission, no matter which one
        return true;
    }

    @Override
    public boolean isPermissionSet(Permission perm) {
        // Just pretend that we have a permission, no matter which one
        return true;
    }

    @Override
    public boolean hasPermission(String name) {
        // Just pretend that we have a permission, no matter which one
        return true;
    }

    @Override
    public boolean hasPermission(Permission perm) {
        // Just pretend that we have a permission, no matter which one
        return true;
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, String name, boolean value) {
        // Whatever it is, I don't care
        return null;
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin) {
        // Whatever it is, I don't care
        return null;
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, String name, boolean value, int ticks) {
        // Whatever it is, I don't care
        return null;
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, int ticks) {
        // Whatever it is, I don't care
        return null;
    }

    @Override
    public void removeAttachment(PermissionAttachment attachment) {
        // Whatever it is, I don't care
    }

    @Override
    public void recalculatePermissions() {
        // Nothing to calculate
    }

    @Override
    public Set<PermissionAttachmentInfo> getEffectivePermissions() {
        // Nothing
        return null;
    }

    @Override
    public void setOp(boolean value) {
        // Nothing
    }

    @Override
    public void sendMessage(String message) {
        // we don't receive messages
    }

    @Override
    public boolean isOp() {
        // We declare ourselves to be OP to be allowed to do more commands
        return true;
    }

    @Override
    public Server getServer() {
        return server;
    }
}
