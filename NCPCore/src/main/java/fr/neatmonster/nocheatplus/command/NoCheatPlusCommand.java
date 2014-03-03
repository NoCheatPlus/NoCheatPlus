package fr.neatmonster.nocheatplus.command;

import fr.neatmonster.nocheatplus.command.actions.*;
import fr.neatmonster.nocheatplus.command.actions.delay.DelayCommand;
import fr.neatmonster.nocheatplus.command.admin.*;
import fr.neatmonster.nocheatplus.command.admin.exemption.ExemptCommand;
import fr.neatmonster.nocheatplus.command.admin.exemption.ExemptionsCommand;
import fr.neatmonster.nocheatplus.command.admin.exemption.UnexemptCommand;
import fr.neatmonster.nocheatplus.command.admin.notify.NotifyCommand;
import fr.neatmonster.nocheatplus.components.INotifyReload;
import fr.neatmonster.nocheatplus.config.ConfPaths;
import fr.neatmonster.nocheatplus.config.ConfigFile;
import fr.neatmonster.nocheatplus.config.ConfigManager;
import fr.neatmonster.nocheatplus.permissions.Permissions;
import fr.neatmonster.nocheatplus.utilities.ColorUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * The /nocheatplus or /ncp command handler, delegates to sub commands.
 */
public class NoCheatPlusCommand extends BaseCommand{

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

	private Set<String> rootLabels = new LinkedHashSet<String>();

    /**
     * Instantiates a new command handler.
     * 
     * @param plugin
     *            the instance of NoCheatPlus
     */
    public NoCheatPlusCommand(final JavaPlugin plugin, final List<INotifyReload> notifyReload) {
    	super(plugin, "nocheatplus", null, new String[]{"ncp"});
        // Register sub commands:
        for (BaseCommand cmd : new BaseCommand[]{
        		new BanCommand(plugin),
        		new CommandsCommand(plugin),
        		new DelayCommand(plugin),
        		new ExemptCommand(plugin),
        		new ExemptionsCommand(plugin),
        		new InfoCommand(plugin),
        		new InspectCommand(plugin),
        		new KickCommand(plugin),
        		new KickListCommand(plugin),
        		new LagCommand(plugin),
        		new NCPVersionCommand(plugin),
        		new NotifyCommand(plugin),
        		new ReloadCommand(plugin, notifyReload),
        		new RemovePlayerCommand(plugin),
        		new TellCommand(plugin),
        		new TempKickCommand(plugin),
                new TopCommand(plugin),
        		new UnexemptCommand(plugin),
        		new UnKickCommand(plugin),
        }){
        	addSubCommands(cmd);
        	rootLabels.add(cmd.label);
        }
    }
    
    /**
     * Retrieve a collection with all sub-command permissions.
     * @return
     */
    public Collection<String> getAllSubCommandPermissions(){
    	final Set<String> set = new LinkedHashSet<String>(rootLabels.size());
    	for (final String label : rootLabels){
    		set.add(subCommands.get(label).permission);
    	}
    	return set;
    }

    /* (non-Javadoc)
     * @see org.bukkit.command.CommandExecutor#onCommand(org.bukkit.command.CommandSender, org.bukkit.command.Command,
     * java.lang.String, java.lang.String[])
     */
    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String commandLabel,
            final String[] args) {
    	
        if (!command.getName().equalsIgnoreCase("nocheatplus")){
        	// Not our command, how did it get here?
        	return false;
        }
        
        if (sender.hasPermission(Permissions.FILTER_COMMAND_NOCHEATPLUS)){
        	// Check sub-commands.
            if (args.length > 0){
            	AbstractCommand<?> subCommand = subCommands.get(args[0].trim().toLowerCase());
            	if (subCommand != null && subCommand.testPermission(sender, command, commandLabel, args)){
            		// Sender has permission to run the command.
            		return subCommand.onCommand(sender, command, commandLabel, args);
            	}
            }
        	// No sub command worked, print usage.
        	return false;
        }
        
        final ConfigFile config = ConfigManager.getConfigFile();
        if (config.getBoolean(ConfPaths.PROTECT_PLUGINS_HIDE_ACTIVE)){
        	// Prevent the NCP usage printout:
        	// TODO: GetColoredString
        	sender.sendMessage(ColorUtil.replaceColors(config.getString(ConfPaths.PROTECT_PLUGINS_HIDE_NOCOMMAND_MSG)));
        	return true; 
        }
        else{
        	return false;
        }
    }

//	/**
//	 * Check which of the choices starts with prefix
//	 * @param sender
//	 * @param choices
//	 * @return
//	 */
//	protected List<String> getTabMatches(CommandSender sender, Collection<String> choices, String prefix){
//		final List<String> res = new ArrayList<String>(choices.size());
//		final Set<BaseCommand> done = new HashSet<BaseCommand>();
//		for (final String label : choices){
//			if (!label.startsWith(prefix)) continue;
//			final BaseCommand cmd = commands.get(label);
//			if (done.contains(cmd)) continue;
//			done.add(cmd);
//			if (sender.hasPermission(cmd.permission)) res.add(cmd.label);
//		}
//		if (!res.isEmpty()){
//			Collections.sort(res);
//			return res;
//		}
//		return null;
//	}

//	@Override
//	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args)
//	{
//		// TODO: TabComplete check ?
//		if (args.length == 0 || args.length == 1 && args[0].trim().isEmpty()){
//			// Add labels without aliases.
//			return getTabMatches(sender, rootLabels, "");
//		}
//		else {
//			final String subLabel = args[0].trim().toLowerCase();
//			if (args.length == 1){
//				// Also check aliases for matches.
//				return getTabMatches(sender, commands.keySet(), subLabel);
//			}
//			else{
//				final NCPCommand cmd = commands.get(subLabel);
//				if (cmd.testPermission...){
//					// Delegate the tab-completion.
//					return cmd.onTabComplete(sender, command, alias, args);
//				}
//			}
//		}
//		return null;
//	}
}
