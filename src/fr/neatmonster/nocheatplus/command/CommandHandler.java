package fr.neatmonster.nocheatplus.command;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import fr.neatmonster.nocheatplus.NoCheatPlus;
import fr.neatmonster.nocheatplus.config.ConfPaths;
import fr.neatmonster.nocheatplus.config.ConfigManager;

/*
 * MM'""""'YMM                                                        dP 
 * M' .mmm. `M                                                        88 
 * M  MMMMMooM .d8888b. 88d8b.d8b. 88d8b.d8b. .d8888b. 88d888b. .d888b88 
 * M  MMMMMMMM 88'  `88 88'`88'`88 88'`88'`88 88'  `88 88'  `88 88'  `88 
 * M. `MMM' .M 88.  .88 88  88  88 88  88  88 88.  .88 88    88 88.  .88 
 * MM.     .dM `88888P' dP  dP  dP dP  dP  dP `88888P8 dP    dP `88888P8 
 * MMMMMMMMMMM                                                           
 * 
 * M""MMMMM""MM                         dP dP                   
 * M  MMMMM  MM                         88 88                   
 * M         `M .d8888b. 88d888b. .d888b88 88 .d8888b. 88d888b. 
 * M  MMMMM  MM 88'  `88 88'  `88 88'  `88 88 88ooood8 88'  `88 
 * M  MMMMM  MM 88.  .88 88    88 88.  .88 88 88.  ... 88       
 * M  MMMMM  MM `88888P8 dP    dP `88888P8 dP `88888P' dP       
 * MMMMMMMMMMMM                                                 
 */
/**
 * This the class handling all the commands.
 */
public class CommandHandler implements CommandExecutor {

    /**
     * The event triggered when NoCheatPlus configuration is reloaded.
     */
    public static class NCPReloadEvent extends Event {

        /** The handlers list. */
        private static final HandlerList handlers = new HandlerList();

        /**
         * Gets the handler list.
         * 
         * @return the handler list
         */
        public static HandlerList getHandlerList() {
            return handlers;
        }

        /* (non-Javadoc)
         * @see org.bukkit.event.Event#getHandlers()
         */
        @Override
        public HandlerList getHandlers() {
            return handlers;
        }
    }

    /** The prefix of every message sent by NoCheatPlus. */
    static final String TAG = ChatColor.RED + "NCP: " + ChatColor.WHITE;

    /** Sub command map.  */
    private final Map<String, NCPCommand> commands = new HashMap<String, NCPCommand>();

    /**
     * Instantiates a new command handler.
     * 
     * @param plugin
     *            the instance of NoCheatPlus
     */
    public CommandHandler(final NoCheatPlus plugin, final Collection<INotifyReload> notifyReload) {
        // Register sub commands:
        for (NCPCommand cmd : new NCPCommand[]{
        		new BanCommand(plugin),
        		new InfoCommand(plugin),
        		new KickCommand(plugin),
        		new TempKickCommand(plugin),
        		new ReloadCommand(plugin, notifyReload),
        		new TellCommand(plugin),
        		new DelayCommand(plugin),
        }){
        	addCommand(cmd);
        }
    }
    
    public void addCommand(NCPCommand command){
    	Set<String> allLabels = new LinkedHashSet<String>();
    	allLabels.add(command.label);
    	if (command.aliases != null){
    		for (String alias : command.aliases){
    			allLabels.add(alias);
    		}
    	}
    	for (String label : allLabels){
    		label = label.trim().toLowerCase(); // future.
    		if (!commands.containsKey(label)) commands.put(label, command);
    	}
    }

    /* (non-Javadoc)
     * @see org.bukkit.command.CommandExecutor#onCommand(org.bukkit.command.CommandSender, org.bukkit.command.Command,
     * java.lang.String, java.lang.String[])
     */
    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String commandLabel,
            final String[] args) {
        /*
         *   ____                                          _ 
         *  / ___|___  _ __ ___  _ __ ___   __ _ _ __   __| |
         * | |   / _ \| '_ ` _ \| '_ ` _ \ / _` | '_ \ / _` |
         * | |__| (_) | | | | | | | | | | | (_| | | | | (_| |
         *  \____\___/|_| |_| |_|_| |_| |_|\__,_|_| |_|\__,_|
         */
        // Not our command, how did it get here?
        if (!command.getName().equalsIgnoreCase("nocheatplus"))
            return false;

        final boolean protectPlugins = ConfigManager.getConfigFile().getBoolean(ConfPaths.MISCELLANEOUS_PROTECTPLUGINS);
        
        if (args.length > 0){
        	NCPCommand subCommand = commands.get(args[0].trim().toLowerCase());
        	if (subCommand != null && sender.hasPermission(subCommand.permission)){
        		// Sender has permission to run the command.
        		return subCommand.onCommand(sender, command, commandLabel, args);
        	}
        }
        
        // Bit crude workaround:
        for (NCPCommand cmd : commands.values()){
        	if (sender.hasPermission(cmd.permission)) return false;
        }
        
        if (protectPlugins){
        	// Prevent the NCP usage printout:
        	sender.sendMessage("Unknown command. Type \"help\" for help.");
        	return true; 
        }
        else
            return false;
    }
}
