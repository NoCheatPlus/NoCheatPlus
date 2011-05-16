package cc.co.evenprime.bukkit.nocheat;


import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import cc.co.evenprime.bukkit.nocheat.actions.Action;
import cc.co.evenprime.bukkit.nocheat.checks.AirbuildCheck;
import cc.co.evenprime.bukkit.nocheat.checks.BedteleportCheck;
import cc.co.evenprime.bukkit.nocheat.checks.BogusitemsCheck;
import cc.co.evenprime.bukkit.nocheat.checks.Check;
import cc.co.evenprime.bukkit.nocheat.checks.ItemdupeCheck;
import cc.co.evenprime.bukkit.nocheat.checks.MovingCheck;
import cc.co.evenprime.bukkit.nocheat.checks.SpeedhackCheck;
import cc.co.evenprime.bukkit.nocheat.data.PermissionData;

import com.ensifera.animosity.craftirc.CraftIRC;
import com.nijikokun.bukkit.Permissions.Permissions;
import com.nijiko.permissions.PermissionHandler;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.config.Configuration;

/**
 * 
 * NoCheat
 * 
 * Check various player events for their plausibility and log/deny them based on configuration
 * 
 * @author Evenprime
 */
public class NoCheat extends JavaPlugin {

	public MovingCheck movingCheck;
	public BedteleportCheck bedteleportCheck;
	public SpeedhackCheck speedhackCheck;
	public AirbuildCheck airbuildCheck;
	public ItemdupeCheck itemdupeCheck;
	public BogusitemsCheck bogusitemsCheck;

	private Check[] checks;

	private NoCheatConfiguration config;

	private boolean exceptionWithPermissions = false;

	private boolean cleanUpTaskSetup = false;
	private boolean serverLagMeasureTaskSetup = false;

	private int serverTicks = 0;
	private long serverLagInMilliSeconds = 0;
	private long lastServerTime = 0;

	// Permissions 2.x, if available
	private PermissionHandler permissions;

	// CraftIRC 2.x, if available
	private CraftIRC irc;
	private boolean allowFlightSet;

	public NoCheat() { 	

	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args)
	{
		if(args.length == 0) {
			sender.sendMessage("NC: Using "+ ((permissions == null) ? "isOp()" : "Permissions") + ". Activated checks/bugfixes: " + getActiveChecksAsString() + ". Total time used for moving check so far: " + (movingCheck.statisticElapsedTimeNano / 1000000L + " ms. Average time per move event: " + (movingCheck.statisticElapsedTimeNano/1000L)/movingCheck.statisticTotalEvents + " us"));
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

		movingCheck = new MovingCheck(this);
		bedteleportCheck = new BedteleportCheck(this);
		speedhackCheck = new SpeedhackCheck(this);
		airbuildCheck = new AirbuildCheck(this);
		itemdupeCheck = new ItemdupeCheck(this);
		bogusitemsCheck = new BogusitemsCheck(this);

		// just for convenience
		checks = new Check[] { movingCheck, bedteleportCheck, speedhackCheck, airbuildCheck, itemdupeCheck, bogusitemsCheck };

		// parse the nocheat.yml config file
		setupConfig();

		if(allowFlightSet && movingCheck.isActive()) {
			Logger.getLogger("Minecraft").warning( "[NoCheat] you have set \"allow-flight=false\" in your server.properties file. That builtin anti-flying-mechanism will likely conflict with this plugin. Please consider deactivating it by setting it to \"true\"");
		}

		PluginDescriptionFile pdfFile = this.getDescription();

		// Get, if available, the Permissions and irc plugin
		setupPermissions();
		setupIRC();

		Logger.getLogger("Minecraft").info( "[NoCheat] version [" + pdfFile.getVersion() + "] is enabled with the following checks: "+getActiveChecksAsString());

		setupCleanupTask();

		setupServerLagMeasureTask();
	}

	private void setupCleanupTask() {

		if(cleanUpTaskSetup) return;

		cleanUpTaskSetup = true;

		Bukkit.getServer().getScheduler().scheduleAsyncRepeatingTask(this, new Runnable() {

			@Override
			public void run() {
				NoCheatData.cleanPlayerDataCollection();
			}
		}, 5000, 5000);
	}

	private void setupServerLagMeasureTask() {

		if(serverLagMeasureTaskSetup) return;

		serverLagMeasureTaskSetup = true;

		Bukkit.getServer().getScheduler().scheduleAsyncRepeatingTask(this, new Runnable() {

			@Override
			public void run() {
				serverTicks += 10;
				long time = System.currentTimeMillis();
				serverLagInMilliSeconds = (time - lastServerTime - 500)*2;
				lastServerTime = time;
			}
		}, 10, 10);
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
			Logger.getLogger("Minecraft").info("[NoCheat] version [" + pdfFile.getVersion() + "] couldn't find CrafTIRC plugin. Disabling logging to IRC.");
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
				if(hasPermission(player, PermissionData.PERMISSION_NOTIFY)) {
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


	public boolean hasPermission(Player player, int permission) {

		if(player == null) return false;

		try {
			if(permissions == null)
				return player.isOp();
			else {
				PermissionData data = PermissionData.get(player);
				long time = System.currentTimeMillis();
				if(data.lastUpdate[permission] + 10000 < time) {
					data.lastUpdate[permission] = time;
					data.cache[permission] = permissions.has(player, PermissionData.permissionNames[permission]);
				}
				return data.cache[permission];
			}
		}
		catch(Throwable e) {
			if(!this.exceptionWithPermissions) {
				// Prevent spam and recursion by definitely doing this only once
				this.exceptionWithPermissions = true;

				String logtext = "Asking Permissions-Plugin if "+player.getName()+" has permission "+PermissionData.permissionNames[permission]+" caused an Exception "+ e.getMessage() + ". Please review your permissions config file. This message is only displayed once, the player is considered to not have that permission from now on. The full stack trace is written into the nocheat logfile.";
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
		
		Configuration serverConfig = new Configuration(new File("server.properties"));
		
		allowFlightSet = serverConfig.getBoolean("allow-flight", false);
	}


	private String getActiveChecksAsString() {

		String s = "";

		for(Check c : checks) {
			s = s + (c.isActive() ? c.getName() + " " : "");
		}

		s = s + (movingCheck.isActive() && !movingCheck.allowFlying ? "flying " : "");
		s = s + (movingCheck.isActive() && !movingCheck.allowFakeSneak ? "fakesneak " : "");

		return s;
	}


	private String getPermissionsForPlayerAsString(Player p) {

		String s = "";

		for(Check c : checks) {
			s = s + (!c.isActive() ? c.getName() + "* " : (c.skipCheck(p) ? c.getName() + " " : ""));
		}

		s = s + (!movingCheck.isActive() || movingCheck.allowFlying ? "flying* " : (hasPermission(p, PermissionData.PERMISSION_FLYING) ? "flying " : ""));
		s = s + (!movingCheck.isActive() || movingCheck.allowFakeSneak ? "fakesneak* " : (hasPermission(p, PermissionData.PERMISSION_FAKESNEAK) ? "fakesneak " : ""));
		s = s + (hasPermission(p, PermissionData.PERMISSION_NOTIFY) ? "notify " : "");

		return s;
	}

	public int getServerTicks() {
		return serverTicks;
	}

	public long getServerLag() {
		return this.serverLagInMilliSeconds;
	}

	public void handleCustomAction(Action a, Player player) {
		// TODO Auto-generated method stub

	}
}