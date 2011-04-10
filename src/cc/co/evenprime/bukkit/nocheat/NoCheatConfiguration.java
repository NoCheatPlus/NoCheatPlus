package cc.co.evenprime.bukkit.nocheat;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.util.config.Configuration;

import cc.co.evenprime.bukkit.nocheat.actions.Action;
import cc.co.evenprime.bukkit.nocheat.actions.CancelAction;
import cc.co.evenprime.bukkit.nocheat.actions.LogAction;

/**
 * Central location for everything that's described in the configuration file
 * 
 * @author Evenprime
 *
 */
public class NoCheatConfiguration {


	public final String configFile = "plugins/NoCheat/nocheat.yml";

	// Our personal logger
	private final String loggerName = "cc.co.evenprime.nocheat";
	public final Logger logger = Logger.getLogger(loggerName);

	// The log level above which information gets logged to the specified logger
	public Level chatLevel = Level.WARNING;
	public Level ircLevel = Level.WARNING;
	public Level consoleLevel = Level.SEVERE;
	public Level fileLevel = Level.INFO;

	public String fileName = "plugins/NoCheat/nocheat.log";

	public String ircTag = "nocheat";

	// Our log output to a file
	private FileHandler fh = null;

	private final NoCheat plugin;

	public NoCheatConfiguration(NoCheat plugin) {

		this.plugin = plugin;

		config();
	}

	/**
	 * Read the configuration file and assign either standard values or whatever is declared in the file
	 * @param configurationFile
	 */
	public void config() {

		File configurationFile = new File(configFile);

		if(!configurationFile.exists()) {
			createStandardConfigFile(configurationFile);
		}

		Configuration c = new Configuration(configurationFile);
		c.load();

		logger.setLevel(Level.INFO);
		logger.setUseParentHandlers(false);

		chatLevel = stringToLevel(c.getString("logging.logtochat"), chatLevel);
		consoleLevel = stringToLevel(c.getString("logging.logtoconsole"), consoleLevel);
		fileLevel = stringToLevel(c.getString("logging.logtofile"), fileLevel);
		ircLevel = stringToLevel(c.getString("logging.logtoirc"), ircLevel);
		ircTag = c.getString("logging.logtoirctag", ircTag);

		if(fh == null) {
			try {
				fh = new FileHandler(fileName, true);
				fh.setLevel(fileLevel);
				fh.setFormatter(Logger.getLogger("Minecraft").getHandlers()[0].getFormatter());
				logger.addHandler(fh);

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		plugin.speedhackCheck.limits[0] = c.getInt("speedhack.limits.low", plugin.speedhackCheck.limits[0]);
		plugin.speedhackCheck.limits[1] = c.getInt("speedhack.limits.med", plugin.speedhackCheck.limits[1]);
		plugin.speedhackCheck.limits[2] = c.getInt("speedhack.limits.high", plugin.speedhackCheck.limits[2]);

		plugin.speedhackCheck.logMessage = c.getString("speedhack.logmessage", plugin.speedhackCheck.logMessage);
		
		plugin.movingCheck.actions[0] = stringToActions(c.getString("moving.action.low"), plugin.movingCheck.actions[0]);
		plugin.movingCheck.actions[1] = stringToActions(c.getString("moving.action.med"), plugin.movingCheck.actions[1]);
		plugin.movingCheck.actions[2] = stringToActions(c.getString("moving.action.high"), plugin.movingCheck.actions[2]);

		
		plugin.movingCheck.logMessage = c.getString("moving.logmessage", plugin.movingCheck.logMessage);
		plugin.movingCheck.summaryMessage = c.getString("moving.summarymessage", plugin.movingCheck.summaryMessage);
		
		plugin.movingCheck.preciseVelocity = c.getBoolean("moving.precisevelocity", plugin.movingCheck.preciseVelocity);
		
		plugin.speedhackCheck.actions[0] = stringToActions(c.getString("speedhack.action.low"), plugin.speedhackCheck.actions[0]);
		plugin.speedhackCheck.actions[1] = stringToActions(c.getString("speedhack.action.med"), plugin.speedhackCheck.actions[1]);
		plugin.speedhackCheck.actions[2] = stringToActions(c.getString("speedhack.action.high"), plugin.speedhackCheck.actions[2]);

		plugin.airbuildCheck.limits[0] = c.getInt("airbuild.limits.low", plugin.airbuildCheck.limits[0]);
		plugin.airbuildCheck.limits[1] = c.getInt("airbuild.limits.med", plugin.airbuildCheck.limits[1]);
		plugin.airbuildCheck.limits[2] = c.getInt("airbuild.limits.high", plugin.airbuildCheck.limits[2]);

		plugin.airbuildCheck.actions[0] = stringToActions(c.getString("airbuild.action.low"), plugin.airbuildCheck.actions[0]);
		plugin.airbuildCheck.actions[1] = stringToActions(c.getString("airbuild.action.med"), plugin.airbuildCheck.actions[1]);
		plugin.airbuildCheck.actions[2] = stringToActions(c.getString("airbuild.action.high"), plugin.airbuildCheck.actions[2]);

		plugin.speedhackCheck.setActive(c.getBoolean("active.speedhack", plugin.speedhackCheck.isActive()));
		plugin.movingCheck.setActive(c.getBoolean("active.moving", plugin.movingCheck.isActive()));
		plugin.airbuildCheck.setActive(c.getBoolean("active.airbuild", plugin.airbuildCheck.isActive()));
		plugin.bedteleportCheck.setActive(c.getBoolean("active.bedteleport", plugin.bedteleportCheck.isActive()));
	}

	private Action[] stringToActions(String string, Action[] def) {

		if(string == null) return def;

		List<Action> as = new LinkedList<Action>();
		String[] parts = string.split(" ");

		for(String s : parts) {
			if(s.equals("loglow"))
				as.add(LogAction.loglow);
			else if(s.equals("logmed"))
				as.add(LogAction.logmed);
			else if(s.equals("loghigh"))
				as.add(LogAction.loghigh);
			else if(s.equals("deny"))
				as.add(CancelAction.cancel);
			else if(s.equals("reset"))
				as.add(CancelAction.cancel);
			else if(s.equals("cancel"))
				as.add(CancelAction.cancel);
			else if(s.startsWith("custom")) {
				try {
					// TODO: Implement Custom Action
					//as.add(new CustomAction(Integer.parseInt(s.substring(6))));
				}
				catch(Exception e) {
					System.out.println("NC: Couldn't parse number of custom action '" + s + "'");
				}
			}
			else {
				System.out.println("NC: Can't parse action "+ s);
			}
		}


		return as.toArray(def);
	}

	private String actionsToString(Action[] actions) {

		String s = "";

		if(actions != null) {
			for(Action a : actions) {
				s = s + " " + a.getName();
			}
		}

		return s.trim();
	}
	/**
	 * Convert a string into a log level
	 * @param string
	 * @return
	 */
	private static Level stringToLevel(String string, Level def) {

		if(string == null) {
			return def;
		}

		if(string.trim().equals("info") || string.trim().equals("low")) return Level.INFO;
		if(string.trim().equals("warn") || string.trim().equals("med")) return Level.WARNING;
		if(string.trim().equals("severe")|| string.trim().equals("high")) return Level.SEVERE;

		return Level.OFF;
	}

	private static String levelToString(Level level) {

		if(level == null) {
			return "off";
		}

		if(level.equals(Level.INFO)) return "low";
		else if(level.equals(Level.WARNING)) return "med";
		else if(level.equals(Level.SEVERE)) return "high";

		return "off";
	}

	/**
	 * Standard configuration file for people who haven't got one yet
	 * @param f
	 */
	private void createStandardConfigFile(File f) {
		try {
			f.getParentFile().mkdirs();
			f.createNewFile();
			BufferedWriter w = new BufferedWriter(new FileWriter(f));

			w.write("# Logging: potential log levels are low (info), med (warn), high (severe), off"); w.newLine();
			w.write("logging:"); w.newLine();
			w.write("    filename: "+fileName); w.newLine();
			w.write("    logtofile: "+levelToString(fileLevel)); w.newLine();
			w.write("    logtoconsole: "+levelToString(consoleLevel)); w.newLine();
			w.write("    logtochat: "+levelToString(chatLevel)); w.newLine();
			w.write("    logtoirc: "+levelToString(ircLevel)); w.newLine();
			w.write("    logtoirctag: "+ircTag); w.newLine();
			w.write("# Checks and Bugfixes that are activated (true or false)"); w.newLine();
			w.write("active:");  w.newLine();
			w.write("    speedhack: "+plugin.speedhackCheck.isActive()); w.newLine();
			w.write("    moving: "+plugin.movingCheck.isActive()); w.newLine();
			w.write("    airbuild: "+plugin.airbuildCheck.isActive()); w.newLine();
			w.write("    bedteleport: "+plugin.bedteleportCheck.isActive()); w.newLine();
			w.write("# Speedhack specific options"); w.newLine();
			w.write("speedhack:"); w.newLine();
			w.write("    logmessage: \"" + plugin.speedhackCheck.logMessage+"\""); w.newLine();
			w.write("    limits:"); w.newLine();
			w.write("        low: "+plugin.speedhackCheck.limits[0]); w.newLine();
			w.write("        med: "+plugin.speedhackCheck.limits[1]); w.newLine();
			w.write("        high: "+plugin.speedhackCheck.limits[2]); w.newLine();
			w.write("#   Speedhack Action, one or more of 'loglow logmed loghigh cancel'"); w.newLine();
			w.write("    action:"); w.newLine();
			w.write("        low: "+actionsToString(plugin.speedhackCheck.actions[0])); w.newLine();
			w.write("        med: "+actionsToString(plugin.speedhackCheck.actions[1])); w.newLine();
			w.write("        high: "+actionsToString(plugin.speedhackCheck.actions[2])); w.newLine();
			w.write("# Moving specific options") ; w.newLine();
			w.write("moving:"); w.newLine();
			w.write("    logmessage: \"" + plugin.movingCheck.logMessage+"\""); w.newLine();
			w.write("    summarymessage: \"" + plugin.movingCheck.summaryMessage+"\""); w.newLine();
			w.write("#   If you get problems with plugins that accellerate players movement, try setting this to false"); w.newLine();
			w.write("    precisevelocity: \"" + plugin.movingCheck.preciseVelocity+"\""); w.newLine();
			w.write("#   Moving Action, one or more of 'loglow logmed loghigh cancel'"); w.newLine();
			w.write("    action:"); w.newLine();
			w.write("        low: "+actionsToString(plugin.movingCheck.actions[0])); w.newLine();
			w.write("        med: "+actionsToString(plugin.movingCheck.actions[1])); w.newLine();
			w.write("        high: "+actionsToString(plugin.movingCheck.actions[2])); w.newLine();
			w.write("# Airbuild specific options"); w.newLine();
			w.write("airbuild:"); w.newLine();
			w.write("#   How many blocks per second are placed by the player in midair to trigger corresponding action"); w.newLine();
			w.write("    limits:"); w.newLine();
			w.write("        low: "+plugin.airbuildCheck.limits[0]); w.newLine();
			w.write("        med: "+plugin.airbuildCheck.limits[1]); w.newLine();
			w.write("        high: "+plugin.airbuildCheck.limits[2]); w.newLine();
			w.write("#   Airbuild Action, one or more of 'loglow logmed loghigh cancel'"); w.newLine();
			w.write("    action:"); w.newLine();
			w.write("        low: "+actionsToString(plugin.airbuildCheck.actions[0])); w.newLine();
			w.write("        med: "+actionsToString(plugin.airbuildCheck.actions[1])); w.newLine();
			w.write("        high: "+actionsToString(plugin.airbuildCheck.actions[2])); w.newLine();
			w.write("# Bedteleport specific options (none exist yet)"); w.newLine();
			w.write("bedteleport:"); w.newLine();

			w.flush(); w.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
