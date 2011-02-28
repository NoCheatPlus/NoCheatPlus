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
	public static boolean speedhackCheckActive = true;
	public static boolean movingCheckActive = true;
	public static boolean airbuildCheckActive = false;
	public static boolean dupebydeathCheckActive = false;
	
	// Limits for the speedhack check
	public static int speedhackLow = 30;
	public static int speedhackMed = 45;
	public static int speedhackHigh = 60;
	
	public static int movingFreeMoves = 10;
	
	// Should moving violations be punished?
	public static boolean movingLogOnly = false;
	
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
		dupebydeathCheckActive = c.getBoolean("active.dupebydeath", false);
		
		speedhackLow = c.getInt("speedhack.limits.low", 30);
		speedhackMed = c.getInt("speedhack.limits.med", 45);
		speedhackHigh = c.getInt("speedhack.limits.high", 60);
		
		movingLogOnly = c.getBoolean("moving.logonly", false);
		movingFreeMoves = c.getInt("moving.freemoves", 10);
		
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
		
		if(string.trim().equals("info")) return Level.INFO;
		if(string.trim().equals("warn")) return Level.WARNING;
		if(string.trim().equals("severe")) return Level.SEVERE;
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
			
			w.write("# Logging: potential log levels are info, warn, severe, off"); w.newLine();
			w.write("logging:"); w.newLine();
			w.write("    filename: plugins/NoCheat/nocheat.log"); w.newLine();
			w.write("    logtofile: info"); w.newLine();
			w.write("    logtoconsole: severe"); w.newLine();
			w.write("    logtonotify: warn"); w.newLine();
			w.write("# Checks that are activated (true or false)"); w.newLine();
			w.write("active:");  w.newLine();
			w.write("    speedhack: true"); w.newLine();
			w.write("    moving: true"); w.newLine();
			w.write("    airbuild: false"); w.newLine();
			w.write("    dupebydeath: false"); w.newLine();
			w.write("# Speedhack: interval in milliseconds, limits are events in that interval") ;w.newLine();
			w.write("speedhack:"); w.newLine();
			w.write("    limits:"); w.newLine();
			w.write("        low: 30"); w.newLine();
			w.write("        med: 45"); w.newLine();
			w.write("        high: 60"); w.newLine();
			w.write("moving:"); w.newLine();
			w.write("    logonly: false"); w.newLine();
			w.write("    freemoves: 10"); w.newLine();
			w.flush(); w.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
