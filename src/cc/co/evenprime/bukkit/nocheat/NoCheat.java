package cc.co.evenprime.bukkit.nocheat;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginManager;

import cc.co.evenprime.bukkit.nocheat.actions.Action;
import cc.co.evenprime.bukkit.nocheat.checks.AirbuildCheck;
import cc.co.evenprime.bukkit.nocheat.checks.BedteleportCheck;
import cc.co.evenprime.bukkit.nocheat.checks.MovingCheck;
import cc.co.evenprime.bukkit.nocheat.checks.SpeedhackCheck;
import cc.co.evenprime.bukkit.nocheat.listeners.AirbuildListener;
import cc.co.evenprime.bukkit.nocheat.listeners.BedteleportListener;
import cc.co.evenprime.bukkit.nocheat.listeners.MovingListener;
import cc.co.evenprime.bukkit.nocheat.listeners.MovingMonitor;
import cc.co.evenprime.bukkit.nocheat.listeners.MovingEntityListener;
import cc.co.evenprime.bukkit.nocheat.listeners.SpeedhackListener;

import com.ensifera.animosity.craftirc.CraftIRC;
import com.nijikokun.bukkit.Permissions.Permissions;
import com.nijiko.permissions.PermissionHandler;
import org.bukkit.plugin.Plugin;

/**
 * 
 * NoCheat
 * 
 * Check various player events for their plausibility and log/deny them based on configuration
 * 
 * @author Evenprime
 */
public class NoCheat extends JavaPlugin {

	public final MovingCheck movingCheck;
	public final BedteleportCheck bedteleportCheck;
	public final SpeedhackCheck speedhackCheck;
	public final AirbuildCheck airbuildCheck;

	private NoCheatConfiguration config;

	private boolean exceptionWithPermissions = false;

	private boolean cleanUpTaskSetup = false;

	// Permissions 2.x, if available
	private PermissionHandler permissions;

	// CraftIRC 2.x, if available
	private CraftIRC irc;

	// Store data between Events
	private final Map<Player, NoCheatData> playerData = new HashMap<Player, NoCheatData>();

	public NoCheat() { 	
		movingCheck = new MovingCheck(this);
		bedteleportCheck = new BedteleportCheck(this);
		speedhackCheck = new SpeedhackCheck(this);
		airbuildCheck = new AirbuildCheck(this);

		// parse the nocheat.yml config file
		setupConfig();
	}

	/**
	 * Main access to data that needs to be stored between different events.
	 * Always returns a NoCheatData object, because if there isn't one
	 * for the specified player, one will be created.
	 * 
	 * @param p
	 * @return
	 */
	public NoCheatData getPlayerData(Player p) {
		NoCheatData data = null;

		if((data = playerData.get(p)) == null ) {
			synchronized(playerData) {
				data = playerData.get(p);
				if(data == null) {
					// If we have no data for the player, create some
					data = new NoCheatData();
					playerData.put(p, data);
				}
			}
		}

		return data;
	}

	/**
	 * Go through the playerData HashMap and remove players that are no longer online
	 * from the map. This should be called in long, regular intervals (e.g. every 10 minutes)
	 * to keep the memory footprint of the plugin low
	 */
	public void cleanPlayerDataCollection() {
		synchronized(playerData) {
			Iterator<Map.Entry<Player, NoCheatData>> it = playerData.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry<Player, NoCheatData> pairs = (Map.Entry<Player, NoCheatData>)it.next();
				if(!pairs.getKey().isOnline())
					it.remove();
			}
		}
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args)
	{
		if(sender instanceof Player) {
			if(!hasPermission((Player)sender, "nocheat.p")) {
				sender.sendMessage("NC: You are not allowed to use this command.");
				return false;
			}
		}

		if(args.length == 0) {
			sender.sendMessage("NC: Using "+ ((permissions == null) ? "isOp()" : "Permissions") + ". Activated checks/bugfixes: " + getActiveChecksAsString());
			return true;
		}
		else if(args.length == 1 && args[0] != null && args[0].trim().equals("-p")) { 
			if(sender instanceof Player) {
				Player p = (Player) sender;

				sender.sendMessage("NC: You have permissions: " + getPermissionsForPlayerAsString(p));
				return true;
			}
			else {
				sender.sendMessage("NC: You have to be a player to use this command");
				return true;
			}
		}
		else if(args.length == 2 && args[0] != null && args[0].trim().equals("-p")) {
			Player p = getServer().getPlayer(args[1]);

			if(p != null) {
				sender.sendMessage("NC: "+p.getName() + " has permissions: " + getPermissionsForPlayerAsString(p));
				return true;
			}
			else {
				sender.sendMessage("NC: Player " + args[1] + " was not found.");
				return true;
			}
		}

		return false;
	}



	public void onDisable() { 

		PluginDescriptionFile pdfFile = this.getDescription();
		
		if(config != null)
			config.cleanup();
		
		Logger.getLogger("Minecraft").info( "[NoCheat] version [" + pdfFile.getVersion() + "] is disabled.");
	}

	public void onEnable() {
		// Create our listeners and feed them with neccessary information

		PluginManager pm = getServer().getPluginManager();

		// parse the nocheat.yml config file
		setupConfig();

		// Register listeners for moving check
		pm.registerEvent(Event.Type.PLAYER_MOVE, new MovingListener(movingCheck), Priority.Lowest, this);
		pm.registerEvent(Event.Type.PLAYER_TELEPORT, new MovingMonitor(movingCheck), Priority.Monitor, this);
		pm.registerEvent(Event.Type.PLAYER_INTERACT, new MovingMonitor(movingCheck), Priority.Monitor, this);
		pm.registerEvent(Event.Type.PLAYER_MOVE, new MovingMonitor(movingCheck), Priority.Monitor, this);
		pm.registerEvent(Event.Type.ENTITY_DAMAGE, new MovingEntityListener(movingCheck), Priority.Monitor, this);
		
		// Register listeners for speedhack check
		pm.registerEvent(Event.Type.PLAYER_MOVE, new SpeedhackListener(speedhackCheck), Priority.High, this);

		// Register listeners for airbuild check
		pm.registerEvent(Event.Type.BLOCK_PLACE, new AirbuildListener(airbuildCheck), Priority.Low, this);

		// Register listeners for bedteleport check
		pm.registerEvent(Event.Type.PLAYER_TELEPORT, new BedteleportListener(bedteleportCheck), Priority.Lowest, this);

		PluginDescriptionFile pdfFile = this.getDescription();



		// Get, if available, the Permissions and irc plugin
		setupPermissions();
		setupIRC();

		Logger.getLogger("Minecraft").info( "[NoCheat] version [" + pdfFile.getVersion() + "] is enabled with the following checks: "+getActiveChecksAsString());

		setupCleanupTask();
	}

	private void setupCleanupTask() {

		if(cleanUpTaskSetup) return;

		cleanUpTaskSetup = true;

		Bukkit.getServer().getScheduler().scheduleAsyncRepeatingTask(this, new Runnable() {

			@Override
			public void run() {
				cleanPlayerDataCollection();
			}

		}, 5000, 5000);

	}

	/**
	 * Get, if available, a reference to the Permissions-plugin
	 */
	private void setupPermissions() {
		PermissionHandler p = null;

		Plugin test = this.getServer().getPluginManager().getPlugin("Permissions");

		if(test != null && test instanceof Permissions) {
			p = ((Permissions)test).getHandler();
			if(p == null) {
				this.getServer().getPluginManager().enablePlugin(test);
			}
			p = ((Permissions)test).getHandler();
		}

		if(p == null) {
			PluginDescriptionFile pdfFile = this.getDescription();
			Logger.getLogger("Minecraft").warning("[NoCheat] version [" + pdfFile.getVersion() + "] couldn't find Permissions plugin. Fallback to 'isOp()' equals 'nocheat.*'");
		}

		permissions = p;
	}

	/**
	 * Get, if available, a reference to the Permissions-plugin
	 */
	private void setupIRC() {
		CraftIRC p = null;

		Plugin test = this.getServer().getPluginManager().getPlugin("CraftIRC");

		if(test != null && test instanceof CraftIRC) {
			p = (CraftIRC)test;
		}

		if(p == null) {
			PluginDescriptionFile pdfFile = this.getDescription();
			Logger.getLogger("Minecraft").warning("[NoCheat] version [" + pdfFile.getVersion() + "] couldn't find CrafTIRC plugin. Disabling logging to IRC.");
		}

		irc = p;
	}

	/**
	 * Log a violation message to all locations declared in the config file
	 * @param message
	 */
	public void log(Level l, String message) {
		if(l != null && message != null) {
			message = "NC: " + message;
			config.logger.log(l, message);
			logToConsole(l, message);
			logToChat(l, message);
			logToIRC(l, message);

		}
	}

	private void logToChat(Level l, String message) {
		if(config.chatLevel.intValue() <= l.intValue()) {
			for(Player player : getServer().getOnlinePlayers()) {
				if(hasPermission(player, "nocheat.notify")) {
					player.sendMessage("["+l.getName()+"] " + message);
				}
			}
		}
	}

	private void logToIRC(Level l, String message) {
		if(irc != null && config.ircLevel.intValue() <= l.intValue()) {
			irc.sendMessageToTag("["+l.getName()+"] " + message , config.ircTag);
		}
	}

	private void logToConsole(Level l, String message) {
		if( config.consoleLevel.intValue() <= l.intValue()) {
			Logger.getLogger("Minecraft").log(l, message);
		}
	}


	public boolean hasPermission(Player player, String permission) {

		if(player == null || permission == null) {
			return false;
		}
		try {
			if(permissions != null && permissions.has(player, permission))
				return true;
			else if(permissions == null && player.isOp())
				return true;
			else
				return false;
		}
		catch(Throwable e) {
			if(!this.exceptionWithPermissions) {
				// Prevent spam and recursion by definitely doing this only once
				this.exceptionWithPermissions = true;

				String logtext = "Asking Permissions-Plugin if "+player.getName()+" has permission "+permission+" caused an Exception "+ e.getMessage() + ". Please review your permissions config file. This message is only displayed once, the player is considered to not have that permission from now on. The full stack trace is written into the nocheat logfile.";
				log(Level.SEVERE, logtext);
				for(StackTraceElement s : e.getStackTrace()) {
					config.logger.log(Level.SEVERE, s.toString());
				}
			}
			return false;
		}
	}

	/**
	 * Read the config file
	 */
	private void setupConfig() {
		if(this.config == null)
			this.config = new NoCheatConfiguration(this);
		else
			this.config.config();
	}


	private String getActiveChecksAsString() {
		return (movingCheck.isActive() ? movingCheck.getName() + " " : "") + 
		(speedhackCheck.isActive() ? speedhackCheck.getName() + " " : "") +
		(airbuildCheck.isActive() ? airbuildCheck.getName() + " " : "") +
		(bedteleportCheck.isActive() ? bedteleportCheck.getName() + " " : "");
	}


	private String getPermissionsForPlayerAsString(Player p) {
		return (!movingCheck.isActive() ? movingCheck.getName() + "* " : (hasPermission(p, "nocheat.moving") ? movingCheck.getName() + " " : "") + 
				(!movingCheck.isActive() ? "flying* " : (hasPermission(p, "nocheat.flying") ? "flying " : "")) + 
				(!speedhackCheck.isActive() ? speedhackCheck.getName() + "* " : (hasPermission(p, "nocheat.speedhack") ? speedhackCheck.getName() + " " : "")) +
				(!airbuildCheck.isActive() ? airbuildCheck.getName() + "* " : (hasPermission(p, "nocheat.airbuild") ? airbuildCheck.getName() + " " : "")) +
				(!bedteleportCheck.isActive() ? bedteleportCheck.getName() + "* " : (hasPermission(p, "nocheat.bedteleport") ? bedteleportCheck.getName() + " " : "")) +
				(hasPermission(p, "nocheat.notify") ? "notify " : ""));
	}

	public void handleCustomAction(Action a, Player player) {
		// TODO Auto-generated method stub

	}
}