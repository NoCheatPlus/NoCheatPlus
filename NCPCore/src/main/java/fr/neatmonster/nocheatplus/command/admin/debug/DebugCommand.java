package fr.neatmonster.nocheatplus.command.admin.debug;

import org.bukkit.plugin.java.JavaPlugin;

import fr.neatmonster.nocheatplus.command.BaseCommand;
import fr.neatmonster.nocheatplus.permissions.Permissions;

public class DebugCommand extends BaseCommand {
    
    public DebugCommand(JavaPlugin access) {
        super(access, "debug", Permissions.COMMAND_DEBUG);
        addSubCommands(new DebugPlayerCommand(access));
        // TODO: Sub command check, plus check type, plus -(-)off switch
    }

}
