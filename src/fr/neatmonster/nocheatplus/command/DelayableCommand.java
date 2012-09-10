package fr.neatmonster.nocheatplus.command;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import fr.neatmonster.nocheatplus.NoCheatPlus;

/**
 * A command that allows to specify a delay for running.
 * @author mc_dev
 *
 */
public abstract class DelayableCommand extends NCPCommand {
	
	/**
	 * Parse an argument for a delay in ticks. The delay is specified with "delay=...".
	 * @param args
	 * @param index
	 * @return
	 */
	public static long parseDelay(String[] args, int index){
		return parseDelay(args, index, -1);
	}
	
	/**
	 * Parse an argument for a delay in ticks. The delay is specified with "delay=...".
	 * @param args
	 * @param index
	 * @param preset Preset delay if none is given.
	 * @return ticks or -1 if no delay found.
	 */
	public static long parseDelay(String[] args, int index, int preset){
		if (args.length <= index) return preset;
		String arg = args[index].trim().toLowerCase();
		if (!arg.startsWith("delay=")) return preset;
		if (arg.length() < 7) return -1;
		try{
			long res = Long.parseLong(arg.substring(6));
			if (res < 0) 
				return -1;
			else 
				return res;
		} catch (NumberFormatException e){
			return -1;
		}
	}

	protected final int delayIndex;
	protected final boolean mustHaveDelay;
	protected final int delayPreset;
	
	/**
	 * (Delay is not obligatory, inserted after the first argument.)
	 * @param plugin
	 * @param label
	 */
	public DelayableCommand(NoCheatPlus plugin, String label, String permission){
		this(plugin, label, permission, 1);
	}
	
	/**
	 * (Delay is not obligatory.)
	 * @param plugin
	 * @param label
	 * @param delayIndex
	 */
	public DelayableCommand(NoCheatPlus plugin, String label, String permission, int delayIndex){
		this(plugin, label, permission, delayIndex, -1, false);
	}

	/**
	 * 
	 * @param plugin
	 * @param label Sub command label.
	 * @param delayIndex Index at which to look for the delay specification.
	 * @param mustHaveDelay If specifying a delay is obligatory.
	 */
	public DelayableCommand(NoCheatPlus plugin, String label, String permission, int delayIndex, int delayPreset, boolean mustHaveDelay) {
		this(plugin, label, permission, null, delayIndex, delayPreset, mustHaveDelay);
	}
	
	/**
	 * 
	 * @param plugin
	 * @param label
	 * @param permission
	 * @param aliases Sub command label aliases.
	 * @param delayIndex Index at which to look for the delay spec.
	 * @param delayPreset Preset if no delay is given.
	 * @param mustHaveDelay If delay must be specified.
	 */
	public DelayableCommand(NoCheatPlus plugin, String label, String permission, String[] aliases, int delayIndex, int delayPreset, boolean mustHaveDelay) {
		super(plugin, label, permission, aliases);
		this.delayIndex = delayIndex;
		this.mustHaveDelay = mustHaveDelay;
		this.delayPreset = delayPreset;
	}

	/**
	 * Execute the command, check validity and schedule a task for delayed execution (use schedule(...)).
	 * @param sender
	 * @param command
	 * @param label Command label, this is not necessarily this.label (!), this.label can be the first argument.
	 * @param alteredArgs args with the delay specification removed.
	 * @param delay
	 * @return If the command was understood in general.
	 */
	public abstract boolean execute(CommandSender sender, Command command, String label, 
			String[] alteredArgs, long delay);
	
	@Override
	public boolean onCommand(final CommandSender sender, final Command command, 
			final String label, final String[] args ) {
		// Parse the delay and alter the args accordingly.
		long delay = parseDelay(args, delayIndex, delayPreset);
		String[] alteredArgs;
		if (delay == -1){
			// No delay found, if demanded return.
			if (mustHaveDelay) return false;
			alteredArgs = args;
		}
		else{
			boolean hasDef = args[delayIndex].startsWith("delay=") && delay != -1;
			alteredArgs = new String[args.length + (hasDef ? -1 : 0)];
			if (alteredArgs.length > 0){
				int increment = 0;
				for (int i = 0; i < args.length; i++){
					if (i == delayIndex && hasDef){
						// ignore this one.
						increment = -1;
						continue;
					}
					alteredArgs[i + increment] = args[i];
				}
			}
		}
		return execute(sender, command, label, alteredArgs, delay);
	}
	
	/**
	 * Execute directly or schedule the task for later execution.
	 * @param runnable
	 * @param delay Delay in ticks.
	 */
	protected void schedule(Runnable runnable, long delay){
		if (delay < 0) 
			runnable.run();
		else if (delay == 0)
			Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, runnable);
		else 
			Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, runnable, delay);
	}

}
