package cc.co.evenprime.bukkit.nocheat;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.util.config.Configuration;

/**
 * Central location for everything that's described in the configuration file
 * 
 * @author Evenprime
 *
 */
public class NoCheatConfiguration {

	// Our personal logger
	private final String loggerName = "cc.co.evenprime.nocheat";
	public final Logger logger = Logger.getLogger(loggerName);

	// The log level above which information gets logged to the specified logger
	public Level chatLevel = Level.OFF;
	public Level ircLevel = Level.OFF;
	public Level consoleLevel = Level.OFF;

	public String ircTag = "";

	// Our log output to a file
	private FileHandler fh = null;

	private final NoCheatPlugin plugin;

	public NoCheatConfiguration(File configurationFile, NoCheatPlugin plugin) {

		this.plugin = plugin;
		
		config(configurationFile);
	}

	/**
	 * Read the configuration file and assign either standard values or whatever is declared in the file
	 * @param configurationFile
	 */
	public void config(File configurationFile) {
		
		if(!configurationFile.exists()) {
			createStandardConfigFile(configurationFile);
		}
		Configuration c = new Configuration(configurationFile);
		c.load();

		logger.setLevel(Level.INFO);
		logger.setUseParentHandlers(false);

		if(fh == null) {
			try {
				fh = new FileHandler(c.getString("logging.filename"), true);
				fh.setLevel(stringToLevel(c.getString("logging.logtofile")));
				fh.setFormatter(Logger.getLogger("Minecraft").getHandlers()[0].getFormatter());
				logger.addHandler(fh);

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		chatLevel = stringToLevel(c.getString("logging.logtonotify")); // deprecated, will be deleted eventually
		chatLevel = stringToLevel(c.getString("logging.logtochat"));
		consoleLevel = stringToLevel(c.getString("logging.logtoconsole"));

		ircLevel = stringToLevel(c.getString("logging.logtoirc"));
		ircTag = c.getString("logging.logtoirctag", "nocheat");

		plugin.speedhackCheck.limits[0] = c.getInt("speedhack.limits.low", plugin.speedhackCheck.limits[0]);
		plugin.speedhackCheck.limits[1] = c.getInt("speedhack.limits.med", plugin.speedhackCheck.limits[1]);
		plugin.speedhackCheck.limits[2] = c.getInt("speedhack.limits.high", plugin.speedhackCheck.limits[2]);

		plugin.movingCheck.actions[0] = c.getString("moving.action.low", plugin.movingCheck.actions[0]);
		plugin.movingCheck.actions[1] = c.getString("moving.action.med", plugin.movingCheck.actions[1]);
		plugin.movingCheck.actions[2] = c.getString("moving.action.high", plugin.movingCheck.actions[2]);

		plugin.speedhackCheck.actions[0] = c.getString("speedhack.action.low", plugin.speedhackCheck.actions[0]);
		plugin.speedhackCheck.actions[1] = c.getString("speedhack.action.med", plugin.speedhackCheck.actions[1]);
		plugin.speedhackCheck.actions[2] = c.getString("speedhack.action.high", plugin.speedhackCheck.actions[2]);

		plugin.airbuildCheck.limits[0] = c.getInt("airbuild.limits.low", plugin.airbuildCheck.limits[0]);
		plugin.airbuildCheck.limits[1] = c.getInt("airbuild.limits.med", plugin.airbuildCheck.limits[1]);
		plugin.airbuildCheck.limits[2] = c.getInt("airbuild.limits.high", plugin.airbuildCheck.limits[2]);

		plugin.airbuildCheck.actions[0] = c.getString("airbuild.action.low", plugin.airbuildCheck.actions[0]);
		plugin.airbuildCheck.actions[1] = c.getString("airbuild.action.med", plugin.airbuildCheck.actions[1]);
		plugin.airbuildCheck.actions[2] = c.getString("airbuild.action.high", plugin.airbuildCheck.actions[2]);

		plugin.speedhackCheck.setActive(c.getBoolean("active.speedhack", plugin.speedhackCheck.isActive()));
		plugin.movingCheck.setActive(c.getBoolean("active.moving", plugin.movingCheck.isActive()));
		plugin.airbuildCheck.setActive(c.getBoolean("active.airbuild", plugin.airbuildCheck.isActive()));
		plugin.bedteleportCheck.setActive(c.getBoolean("active.bedteleport", plugin.bedteleportCheck.isActive()));
	}

	/**
	 * Convert a string into a log level
	 * @param string
	 * @return
	 */
	private static Level stringToLevel(String string) {

		if(string == null) {
			return Level.OFF;
		}

		if(string.trim().equals("info") || string.trim().equals("low")) return Level.INFO;
		if(string.trim().equals("warn") || string.trim().equals("med")) return Level.WARNING;
		if(string.trim().equals("severe")|| string.trim().equals("high")) return Level.SEVERE;
		return Level.OFF;
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
			w.write("    filename: plugins/NoCheat/nocheat.log"); w.newLine();
			w.write("    logtofile: low"); w.newLine();
			w.write("    logtoconsole: high"); w.newLine();
			w.write("    logtochat: med"); w.newLine();
			w.write("    logtoirc: med"); w.newLine();
			w.write("    logtoirctag: nocheat"); w.newLine();
			w.write("# Checks and Bugfixes that are activated (true or false)"); w.newLine();
			w.write("active:");  w.newLine();
			w.write("    speedhack: "+plugin.speedhackCheck.isActive()); w.newLine();
			w.write("    moving: "+plugin.movingCheck.isActive()); w.newLine();
			w.write("    airbuild: "+plugin.airbuildCheck.isActive()); w.newLine();
			w.write("    bedteleport: "+plugin.bedteleportCheck.isActive()); w.newLine();
			w.write("# Speedhack specific options"); w.newLine();
			w.write("speedhack:"); w.newLine();
			w.write("    limits:"); w.newLine();
			w.write("        low: "+plugin.speedhackCheck.limits[0]); w.newLine();
			w.write("        med: "+plugin.speedhackCheck.limits[1]); w.newLine();
			w.write("        high: "+plugin.speedhackCheck.limits[2]); w.newLine();
			w.write("#   Speedhack Action, one or more of 'loglow logmed loghigh reset'"); w.newLine();
			w.write("    action:"); w.newLine();
			w.write("        low: "+plugin.speedhackCheck.actions[0]); w.newLine();
			w.write("        med: "+plugin.speedhackCheck.actions[1]); w.newLine();
			w.write("        high: "+plugin.speedhackCheck.actions[2]); w.newLine();
			w.write("# Moving specific options") ; w.newLine();
			w.write("moving:"); w.newLine();
			w.write("#   Moving Action, one or more of 'loglow logmed loghigh reset'"); w.newLine();
			w.write("    action:"); w.newLine();
			w.write("        low: "+plugin.movingCheck.actions[0]); w.newLine();
			w.write("        med: "+plugin.movingCheck.actions[1]); w.newLine();
			w.write("        high: "+plugin.movingCheck.actions[2]); w.newLine();
			w.write("# Airbuild specific options"); w.newLine();
			w.write("airbuild:"); w.newLine();
			w.write("#   How many blocks per second are placed by the player in midair (determines log level)"); w.newLine();
			w.write("    limits:"); w.newLine();
			w.write("        low: "+plugin.airbuildCheck.limits[0]); w.newLine();
			w.write("        med: "+plugin.airbuildCheck.limits[1]); w.newLine();
			w.write("        high: "+plugin.airbuildCheck.limits[2]); w.newLine();
			w.write("#   Airbuild Action, one or more of 'loglow logmed loghigh deny'"); w.newLine();
			w.write("    action:"); w.newLine();
			w.write("        low: "+plugin.airbuildCheck.actions[0]); w.newLine();
			w.write("        med: "+plugin.airbuildCheck.actions[1]); w.newLine();
			w.write("        high: "+plugin.airbuildCheck.actions[2]); w.newLine();
			w.write("# Bedteleport specific options (none exist yet)"); w.newLine();
			w.write("bedteleport:"); w.newLine();

			w.flush(); w.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
