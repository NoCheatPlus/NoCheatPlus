package cc.co.evenprime.bukkit.nocheat.config;

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

import cc.co.evenprime.bukkit.nocheat.ConfigurationException;
import cc.co.evenprime.bukkit.nocheat.actions.Action;
import cc.co.evenprime.bukkit.nocheat.actions.CancelAction;
import cc.co.evenprime.bukkit.nocheat.actions.LogAction;
import cc.co.evenprime.bukkit.nocheat.config.LevelOption.LogLevel;

/**
 * Central location for everything that's described in the configuration file
 * 
 * @author Evenprime
 *
 */
public class NoCheatConfiguration {


	public final static String configFile = "plugins/NoCheat/nocheat.yml";

	private ParentOption root;

	// Our personal logger
	private final static String loggerName = "cc.co.evenprime.nocheat";
	public final Logger logger = Logger.getLogger(loggerName);

	// Our log output to a file
	private FileHandler fh = null;

	public NoCheatConfiguration(File configurationFile) {

		// Setup the configuration tree
		config(configurationFile);
	}

	/**
	 * Read the configuration file and assign either standard values or whatever is declared in the file
	 * @param configurationFile
	 */
	public void config(File configurationFile) {

		Configuration CONFIG = new Configuration(configurationFile);
		CONFIG.load();

		root = new ParentOption("");


		/*** LOGGING section ***/
		{
			ParentOption loggingNode = new ParentOption("logging");
			root.add(loggingNode);

			loggingNode.add(new MediumStringOption("filename", 
					CONFIG.getString("logging.filename", "plugins/NoCheat/nocheat.log")));

			loggingNode.add(new LevelOption("logtofile", 
					LogLevel.getLogLevelFromString(CONFIG.getString("logging.logtofile", LogLevel.LOW.asString()))));
			loggingNode.add(new LevelOption("logtoconsole",
					LogLevel.getLogLevelFromString(CONFIG.getString("logging.logtoconsole", LogLevel.HIGH.asString()))));
			loggingNode.add(new LevelOption("logtochat", 
					LogLevel.getLogLevelFromString(CONFIG.getString("logging.logtochat", LogLevel.MED.asString()))));
			loggingNode.add(new LevelOption("logtoirc",
					LogLevel.getLogLevelFromString(CONFIG.getString("logging.logtoirc", LogLevel.MED.asString()))));

			loggingNode.add(new ShortStringOption("logtoirctag", 
					CONFIG.getString("logging.logtoirctag", "nocheat")));
		}

		/*** ACTIVE section ***/
		{
			ParentOption activeNode = new ParentOption("active");
			root.add(activeNode);

			activeNode.add(new BooleanOption("speedhack", 
					CONFIG.getBoolean("active.speedhack", true)));
			activeNode.add(new BooleanOption("moving", 
					CONFIG.getBoolean("active.moving", true)));
			activeNode.add(new BooleanOption("airbuild",
					CONFIG.getBoolean("active.airbuild", false)));
			activeNode.add(new BooleanOption("bedteleport",
					CONFIG.getBoolean("active.bedteleport", true)));
			activeNode.add(new BooleanOption("itemdupe", 
					CONFIG.getBoolean("active.itemdupe", true)));
			activeNode.add(new BooleanOption("bogusitems",
					CONFIG.getBoolean("active.bogusitems", false)));
		}

		/*** SPEEDHACK section ***/
		{
			ParentOption speedhackNode = new ParentOption("speedhack");
			root.add(speedhackNode);

			speedhackNode.add(new LongStringOption("logmessage", 
					CONFIG.getString("logging.filename", "%1$s sent %2$d move events, but only %3$d were allowed. Speedhack?")));

			/*** SPEEDHACK LIMITS section ***/
			{
				ParentOption speedhackLimitsNode = new ParentOption("limits");
				speedhackNode.add(speedhackLimitsNode);

				speedhackLimitsNode.add(new IntegerOption("low", 
						CONFIG.getInt("speedhack.limits.low", 30)));
				speedhackLimitsNode.add(new IntegerOption("med",
						CONFIG.getInt("speedhack.limits.med", 45)));
				speedhackLimitsNode.add(new IntegerOption("high",
						CONFIG.getInt("speedhack.limits.high", 60)));
			}

			/*** SPEEDHACK ACTIONS section ***/
			{
				ParentOption speedhackActionNode = new ParentOption("action");
				speedhackNode.add(speedhackActionNode);

				speedhackActionNode.add(new MediumStringOption("low", 
						CONFIG.getString("speedhack.action.low", "loglow cancel")));
				speedhackActionNode.add(new MediumStringOption("med", 
						CONFIG.getString("speedhack.action.med", "logmed cancel")));
				speedhackActionNode.add(new MediumStringOption("high", 
						CONFIG.getString("speedhack.action.high", "loghigh cancel")));
			}
		}

		/*** MOVING section ***/
		{
			ParentOption movingNode = new ParentOption("moving");
			root.add(movingNode);

			movingNode.add(new LongStringOption("logmessage",
					CONFIG.getString("moving.logmessage", "Moving violation: %1$s from %2$s (%4$.1f, %5$.1f, %6$.1f) to %3$s (%7$.1f, %8$.1f, %9$.1f)")));
			movingNode.add(new LongStringOption("summarymessage", 
					CONFIG.getString("moving.summarymessage", "Moving summary of last ~%2$d seconds: %1$s total Violations: (%3$d,%4$d,%5$d)")));
			movingNode.add(new BooleanOption("allowflying", 
					CONFIG.getBoolean("moving.allowflying", false)));
			movingNode.add(new BooleanOption("allowfakesneak", 
					CONFIG.getBoolean("moving.allowfakesneak", true)));

			/*** MOVING ACTION section ***/
			{
				ParentOption movingActionNode = new ParentOption("action");
				movingNode.add(movingActionNode);

				movingActionNode.add(new MediumStringOption("low", 
						CONFIG.getString("moving.action.low", "loglow cancel")));
				movingActionNode.add(new MediumStringOption("med", 
						CONFIG.getString("moving.action.med", "logmed cancel")));
				movingActionNode.add(new MediumStringOption("high", 
						CONFIG.getString("moving.action.high", "loghigh cancel")));
			}
		}

		/*** AIRBUILD section ***/
		{
			ParentOption airbuildNode = new ParentOption("airbuild");
			root.add(airbuildNode);

			/*** AIRBUILD LIMITS section ***/
			{
				ParentOption airbuildLimitsNode = new ParentOption("limits");
				airbuildNode.add(airbuildLimitsNode);

				airbuildLimitsNode.add(new IntegerOption("low", 
						CONFIG.getInt("airbuild.limits.low", 1)));
				airbuildLimitsNode.add(new IntegerOption("med",
						CONFIG.getInt("airbuild.limits.med", 3)));
				airbuildLimitsNode.add(new IntegerOption("high",
						CONFIG.getInt("airbuild.limits.high", 10)));
			}

			/*** AIRBUILD ACTION section ***/
			{
				ParentOption airbuildActionNode = new ParentOption("action");
				airbuildNode.add(airbuildActionNode);

				airbuildActionNode.add(new MediumStringOption("low", 
						CONFIG.getString("airbuild.action.low", "loglow cancel")));
				airbuildActionNode.add(new MediumStringOption("med",
						CONFIG.getString("airbuild.action.med", "logmed cancel")));
				airbuildActionNode.add(new MediumStringOption("high",
						CONFIG.getString("airbuild.action.high", "loghigh cancel")));
			}
		}

		/*** BEDTELEPORT section ***/
		{
			ParentOption bedteleportNode = new ParentOption("bedteleport");
			root.add(bedteleportNode);
		}

		/*** ITEMDUPE section ***/
		{
			ParentOption itemdupeNode = new ParentOption("itemdupe");
			root.add(itemdupeNode);
		}

		/*** BOGUSITEMS section ***/
		{
			ParentOption bogusitemsNode = new ParentOption("bogusitems");
			root.add(bogusitemsNode);
		}
		
		/*** CUSTOMACTIONS section ***/
		{
			ParentOption customActionsNode = new ParentOption("customactions");
			root.add(customActionsNode);
			
			customActionsNode.add(new CustomActionOption("mycommand", 
					CONFIG.getString("customactions.mycommand", "[4,8] TESTCOMMAND")));
			
			customActionsNode.add(new CustomActionOption("mycommand2", 
					CONFIG.getString("customactions.mycommand2", "TESTCOMMAND2")));
			
			customActionsNode.add(new CustomActionOption("mycommand3", 
					CONFIG.getString("customactions.mycommand3", "[7] TESTCOMMAND3")));
		}

		if(!configurationFile.exists()) {
			writeConfigFile(configurationFile, this);
		}
	}

	public void setupFileLogger() {

		logger.setLevel(Level.INFO);
		logger.setUseParentHandlers(false);

		if(fh == null) {
			try {
				fh = new FileHandler(getStringValue("logging.filename"), true);
				fh.setLevel(getLogLevelValue("logging.logtofile"));
				fh.setFormatter(Logger.getLogger("Minecraft").getHandlers()[0].getFormatter());
				logger.addHandler(fh);

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void cleanup() {

		if(fh != null) {
			try {
				logger.removeHandler(fh);
				fh.flush();
				fh.close();

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static Action[] stringToActions(String string) {

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

		return as.toArray(new Action[as.size()]);
	}

	private String actionsToString(Action[] actions) {

		StringBuffer s = new StringBuffer();

		if(actions != null) {
			for(Action a : actions) {
				s.append(' ').append(a.getName());
			}
		}

		return s.toString().trim();
	}

	/**
	 * Write configuration file to specific filename
	 * @param f
	 */
	public static void writeConfigFile(File f, NoCheatConfiguration configuration) {
		try {
			if(f.getParentFile() != null)
				f.getParentFile().mkdirs();
			
			f.createNewFile();
			BufferedWriter w = new BufferedWriter(new FileWriter(f));

			w.write(configuration.getRoot().toYAMLString(""));

			w.flush(); w.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Action[] getActionValue(String optionName) throws ConfigurationException {
		return stringToActions(getStringOption(optionName).getValue());
	}
	
	
	public int getIntegerValue(String optionName) throws ConfigurationException {	
		return getIntegerOption(optionName).getIntegerValue();
	}
	public IntegerOption getIntegerOption(String optionName) throws ConfigurationException {

		Option o = getOption(optionName) ;
		if(o instanceof IntegerOption) {
			return (IntegerOption)o;
		}

		throw new ConfigurationException("Config Node " + optionName + " is not an integer");
	}

	public String getStringValue(String optionName) throws ConfigurationException {
		return getStringOption(optionName).getValue();
	}
	public TextFieldOption getStringOption(String optionName) throws ConfigurationException {

		Option o = getOption(optionName);
		if(o instanceof TextFieldOption) {
			return (TextFieldOption)o;
		}

		throw new ConfigurationException("Config Node " + optionName + " is not a string");
	}

	public Level getLogLevelValue(String optionName) throws ConfigurationException {
		return getLogLevelOption(optionName).getLevelValue();
	}
	public LevelOption getLogLevelOption(String optionName) throws ConfigurationException {

		Option o = getOption(optionName);
		if(o instanceof LevelOption) {
			return (LevelOption)o;
		}

		throw new ConfigurationException("Config Node " + optionName + " is not a loglevel");
	}


	public boolean getBooleanValue(String optionName) throws ConfigurationException {
		return getBooleanOption(optionName).getBooleanValue();
	}
	public BooleanOption getBooleanOption(String optionName) throws ConfigurationException {

		Option o = getOption(optionName);
		if(o instanceof BooleanOption) {
			return (BooleanOption)o;
		}

		throw new ConfigurationException("Config Node " + optionName + " is not a boolean");
	}


	private Option getOption(String optionName) throws ConfigurationException {
		LinkedList<String> l = new LinkedList<String>();
		for(String s : optionName.split("\\.")) {
			l.addLast(s);
		}
		return getOption(root, l);
	}

	private Option getOption(Option parent, LinkedList<String> names) throws ConfigurationException {

		if(names.size() == 0) {
			return parent;
		}
		else if(parent instanceof ParentOption) {
			for(Option o2 : ((ParentOption)parent).getChildOptions()) {
				if(o2.getIdentifier().equals(names.getFirst())) {

					names.removeFirst();
					return getOption(o2, names);
				}
			}
		}

		throw new ConfigurationException("Config Node doesn't exist");
	}

	public ParentOption getRoot() {
		return root;
	}
}
