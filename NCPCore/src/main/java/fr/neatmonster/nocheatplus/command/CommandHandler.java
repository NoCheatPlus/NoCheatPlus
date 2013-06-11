package fr.neatmonster.nocheatplus.command;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import fr.neatmonster.nocheatplus.command.actions.BanCommand;
import fr.neatmonster.nocheatplus.command.actions.DelayCommand;
import fr.neatmonster.nocheatplus.command.actions.KickCommand;
import fr.neatmonster.nocheatplus.command.actions.KickListCommand;
import fr.neatmonster.nocheatplus.command.actions.TellCommand;
import fr.neatmonster.nocheatplus.command.actions.TempKickCommand;
import fr.neatmonster.nocheatplus.command.actions.UnKickCommand;
import fr.neatmonster.nocheatplus.command.admin.CommandsCommand;
import fr.neatmonster.nocheatplus.command.admin.ExemptCommand;
import fr.neatmonster.nocheatplus.command.admin.ExemptionsCommand;
import fr.neatmonster.nocheatplus.command.admin.InfoCommand;
import fr.neatmonster.nocheatplus.command.admin.LagCommand;
import fr.neatmonster.nocheatplus.command.admin.NCPVersionCommand;
import fr.neatmonster.nocheatplus.command.admin.ReloadCommand;
import fr.neatmonster.nocheatplus.command.admin.RemovePlayerCommand;
import fr.neatmonster.nocheatplus.command.admin.UnexemptCommand;
import fr.neatmonster.nocheatplus.components.INotifyReload;
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
public class CommandHandler implements TabExecutor {

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

	private Set<String> rootLabels = new LinkedHashSet<String>();

    /**
     * Instantiates a new command handler.
     * 
     * @param plugin
     *            the instance of NoCheatPlus
     */
    public CommandHandler(final JavaPlugin plugin, final Collection<INotifyReload> notifyReload) {
        // Register sub commands:
        for (NCPCommand cmd : new NCPCommand[]{
        		new BanCommand(plugin),
        		new CommandsCommand(plugin),
        		new DelayCommand(plugin),
        		new ExemptCommand(plugin),
        		new ExemptionsCommand(plugin),
        		new InfoCommand(plugin),
        		new KickCommand(plugin),
        		new KickListCommand(plugin),
        		new LagCommand(plugin),
        		new NCPVersionCommand(plugin),
        		new ReloadCommand(plugin, notifyReload),
        		new RemovePlayerCommand(plugin),
        		new TellCommand(plugin),
        		new TempKickCommand(plugin),
        		new UnexemptCommand(plugin),
        		new UnKickCommand(plugin),
        }){
        	addCommand(cmd);
        }
    }
    
    public void addCommand(NCPCommand command){
    	rootLabels.add(command.label);
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

	/**
	 * Check which of the choices starts with prefix
	 * @param sender
	 * @param choices
	 * @return
	 */
	protected List<String> getTabMatches(CommandSender sender, Collection<String> choices, String prefix){
		final List<String> res = new ArrayList<String>(choices.size());
		final Set<NCPCommand> done = new HashSet<NCPCommand>();
		for (final String label : choices){
			if (!label.startsWith(prefix)) continue;
			final NCPCommand cmd = commands.get(label);
			if (done.contains(cmd)) continue;
			done.add(cmd);
			if (sender.hasPermission(cmd.permission)) res.add(cmd.label);
		}
		if (!res.isEmpty()){
			Collections.sort(res);
			return res;
		}
		return null;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args)
	{
		// TODO: TabComplete check ?
		if (args.length == 0 || args.length == 1 && args[0].trim().isEmpty()){
			// Add labels without aliases.
			return getTabMatches(sender, rootLabels, "");
		}
		else {
			final String subLabel = args[0].trim().toLowerCase();
			if (args.length == 1){
				// Also check aliases for matches.
				return getTabMatches(sender, commands.keySet(), subLabel);
			}
			else{
				final NCPCommand cmd = commands.get(subLabel);
				if (cmd != null && sender.hasPermission(cmd.permission)){
					// Delegate the tab-completion.
					return cmd.onTabComplete(sender, command, alias, args);
				}
			}
		}
		return null;
	}
}
