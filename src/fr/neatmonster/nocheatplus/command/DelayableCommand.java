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
	 * @return ticks or -1 if no delay found.
	 */
	public static long parseDelay(String[] args, int index){
		if (args.length <= index) return -1;
		String arg = args[index].trim().toLowerCase();
		if (!arg.startsWith("delay=")) return -1;
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

	private int delayIndex;
	private boolean mustHaveDelay;
	
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
		this(plugin, label, permission, delayIndex, false);
	}

	/**
	 * 
	 * @param plugin
	 * @param label Sub command label.
	 * @param delayIndex Index at which to look for the delay specification.
	 * @param mustHaveDelay If specifying a delay is obligatory.
	 */
	public DelayableCommand(NoCheatPlus plugin, String label, String permission, int delayIndex, boolean mustHaveDelay) {
		super(plugin, label, permission);
		this.delayIndex = delayIndex;
		this.mustHaveDelay = mustHaveDelay;
	}
	
	/**
	 * Execute the command, check validity and schedule a task for delayed execution (use schedule(...)).
	 * @param sender
	 * @param command
	 * @param label Command label, this is not necessarily this.label (!), this.label can be the first argument.
	 * @param alteredArgs args with the delay specification removed.
	 * @param delay
	 */
	public abstract boolean execute(CommandSender sender, Command command, String label, 
			String[] alteredArgs, long delay);
	
	@Override
	public boolean onCommand(final CommandSender sender, final Command command, 
			final String label, final String[] args ) {
		// Parse the delay and alter the args accordingly.
		long delay = parseDelay(args, delayIndex);
		String[] alteredArgs;
		if (delay == -1){
			// No delay found, if demanded return.
			if (mustHaveDelay) return false;
			alteredArgs = args;
		}
		else{
			alteredArgs = new String[args.length -1];
			int increment = 0;
			for (int i = 0; i < args.length; i++){
				if (i == delayIndex){
					// ignore this one.
					increment = -1;
					continue;
				}
				alteredArgs[i + increment] = args[i];
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
