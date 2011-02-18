package cc.co.evenprime.bukkit.nocheat;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.bukkit.util.config.Configuration;

public class NoCheatConfiguration {
	
	public static final String loggerName = "cc.co.evenprime.bukkit.nocheat";
	public static final Logger logger = Logger.getLogger(loggerName);
	
	private NoCheatConfiguration() {}
	
	public static void config(File configurationFile) {
		
		if(!configurationFile.exists()) {
			createStandardConfigFile(configurationFile);
		}
		Configuration c = new Configuration(configurationFile);
		c.load();
		
		logger.setLevel(Level.INFO);
		logger.setUseParentHandlers(false);
				
		ConsoleHandler ch = new ConsoleHandler();

		ch.setLevel(stringToLevel(c.getString("logging.logtoconsole")));
		ch.setFormatter(Logger.getLogger("Minecraft").getHandlers()[0].getFormatter());
		logger.addHandler(ch);
		
		FileHandler fh = null;
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
	
	private static Level stringToLevel(String string) {
		
		if(string == null) {
			return Level.OFF;
		}
		
		if(string.trim().equals("info")) return Level.INFO;
		if(string.trim().equals("warn")) return Level.WARNING;
		if(string.trim().equals("severe")) return Level.SEVERE;
		return Level.OFF;
	}
	
	private static void createStandardConfigFile(File f) {
		try {
			BufferedWriter w = new BufferedWriter(new FileWriter(f));
			
			w.write("logging:"); w.newLine();
			w.write("    filename: nocheat.log"); w.newLine();
			w.write("    logtofile: info"); w.newLine();
			w.write("    logtoconsole: severe"); w.newLine();
			w.flush(); w.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
