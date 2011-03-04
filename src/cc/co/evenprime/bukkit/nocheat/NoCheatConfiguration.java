package cc.co.evenprime.bukkit.nocheat;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.ConsoleHandler;
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
	public static final String loggerName = "cc.co.evenprime.bukkit.nocheat";
	public static final Logger logger = Logger.getLogger(loggerName);
	
	// Which checks are active
	public static boolean speedhackCheckActive;
	public static boolean movingCheckActive;
	public static boolean airbuildCheckActive;
	public static boolean dupebydeathCheckActive;
	public static boolean bedteleportCheckActive;
	
	// Limits for the speedhack check
	public static int speedhackLimitLow;
	public static int speedhackLimitMed;
	public static int speedhackLimitHigh;
	
	// How should speedhack violations be treated?
	public static String speedhackActionMinor = "";
	public static String speedhackActionNormal = "";
	public static String speedhackActionHeavy = "";
	
	public static int movingFreeMoves = 10;
	
	// How should moving violations be treated?
	public static String movingActionMinor = "";
	public static String movingActionNormal = "";
	public static String movingActionHeavy = "";
	
	// How should airbuild violations be treated?
	public static String airbuildAction = "";
	
	// The log level above which players with the permission nocheat.notify will get informed about violations
	public static Level notifyLevel = Level.OFF;
	
	// Our two log outputs, the console and a file
	private static ConsoleHandler ch = null;
	private static FileHandler fh = null;
	
	private NoCheatConfiguration() {}
	
	/**
	 * Read the configuration file and assign either standard values or whatever is declared in the file
	 * @param configurationFile
	 */
	public static void config(File configurationFile) {
		
		if(!configurationFile.exists()) {
			createStandardConfigFile(configurationFile);
		}
		Configuration c = new Configuration(configurationFile);
		c.load();
		
		logger.setLevel(Level.INFO);
		logger.setUseParentHandlers(false);

		
		if(ch == null) {
			ch = new ConsoleHandler();
	
			ch.setLevel(stringToLevel(c.getString("logging.logtoconsole")));
			ch.setFormatter(Logger.getLogger("Minecraft").getHandlers()[0].getFormatter());
			logger.addHandler(ch);
		}
		
		
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
		
		notifyLevel = stringToLevel(c.getString("logging.logtonotify"));
		
		speedhackCheckActive = c.getBoolean("active.speedhack", true);
		movingCheckActive = c.getBoolean("active.moving", true);
		airbuildCheckActive = c.getBoolean("active.airbuild", false);
		dupebydeathCheckActive = c.getBoolean("active.dupebydeath", true);
		bedteleportCheckActive = c.getBoolean("active.bedteleport", true);
		
		speedhackLimitLow = c.getInt("speedhack.limits.low", 30);
		speedhackLimitMed = c.getInt("speedhack.limits.med", 45);
		speedhackLimitHigh = c.getInt("speedhack.limits.high", 60);
			
		movingFreeMoves = c.getInt("moving.freemoves", 10);
		
		movingActionMinor = c.getString("moving.action.low", "loglow reset");
		movingActionNormal = c.getString("moving.action.med", "logmed reset");
		movingActionHeavy = c.getString("moving.action.high", "loghigh reset");
		
		speedhackActionMinor = c.getString("speedhack.action.low", "loglow");
		speedhackActionNormal = c.getString("speedhack.action.med", "logmed");
		speedhackActionHeavy = c.getString("speedhack.action.high", "loghigh");
		
		airbuildAction = c.getString("airbuild.action", "logmed deny");
		
		if(movingFreeMoves < 10) movingFreeMoves = 10;
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
	private static void createStandardConfigFile(File f) {
		try {
			f.getParentFile().mkdirs();
			f.createNewFile();
			BufferedWriter w = new BufferedWriter(new FileWriter(f));
			
			w.write("# Logging: potential log levels are low (info), med (warn), high (severe), off"); w.newLine();
			w.write("logging:"); w.newLine();
			w.write("    filename: plugins/NoCheat/nocheat.log"); w.newLine();
			w.write("    logtofile: low"); w.newLine();
			w.write("    logtoconsole: high"); w.newLine();
			w.write("    logtonotify: med"); w.newLine();
			w.write("# Checks and Bugfixes that are activated (true or false)"); w.newLine();
			w.write("active:");  w.newLine();
			w.write("    speedhack: true"); w.newLine();
			w.write("    moving: true"); w.newLine();
			w.write("    airbuild: false"); w.newLine();
			w.write("    dupebydeath: true"); w.newLine();
			w.write("    bedteleport: true"); w.newLine();
			w.write("# Speedhack specific options"); w.newLine();
			w.write("speedhack:"); w.newLine();
			w.write("    limits:"); w.newLine();
			w.write("        low: 30"); w.newLine();
			w.write("        med: 45"); w.newLine();
			w.write("        high: 60"); w.newLine();
			w.write("#   Speedhack Action, one or more of loglow logmed loghigh reset"); w.newLine();
			w.write("    action:"); w.newLine();
			w.write("        low: loglow reset"); w.newLine();
			w.write("        med: logmed reset"); w.newLine();
			w.write("        high: loghigh reset"); w.newLine();
			w.write("# Moving specific optionse") ;w.newLine();
			w.write("moving:"); w.newLine();
			w.write("    freemoves: 10"); w.newLine();
			w.write("#   Moving Action, one or more of loglow logmed loghigh reset"); w.newLine();
			w.write("    action:"); w.newLine();
			w.write("        low: loglow reset"); w.newLine();
			w.write("        med: logmed reset"); w.newLine();
			w.write("        high: loghigh reset"); w.newLine();
			w.write("# Airbuild specific options"); w.newLine();
			w.write("airbuild:"); w.newLine();
			w.write("#   Airbuild Action, one or more of loglow logmed loghigh deny"); w.newLine();
			w.write("    action: logmed deny"); w.newLine();
			w.write("# Dupebydeath specific options (none exist yet)"); w.newLine();
			w.write("dupebydeath:"); w.newLine();
			w.write("# Bedteleport specific options (none exist yet)"); w.newLine();
			w.write("bedteleport:"); w.newLine();

			w.flush(); w.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
