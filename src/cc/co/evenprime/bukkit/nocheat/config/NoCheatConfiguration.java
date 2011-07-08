package cc.co.evenprime.bukkit.nocheat.config;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import cc.co.evenprime.bukkit.nocheat.ConfigurationException;
import cc.co.evenprime.bukkit.nocheat.LogFileFormatter;
import cc.co.evenprime.bukkit.nocheat.actions.Action;
import cc.co.evenprime.bukkit.nocheat.actions.CancelAction;
import cc.co.evenprime.bukkit.nocheat.actions.LogAction;
import cc.co.evenprime.bukkit.nocheat.config.LevelOption.LogLevel;
import cc.co.evenprime.bukkit.nocheat.yaml.SimpleYaml;

/**
 * Central location for everything that's described in the configuration file
 * 
 * @author Evenprime
 *
 */
public class NoCheatConfiguration {


	public final static String configFile = "plugins/NoCheat/nocheat.yml";
	public final static String descriptionsFile = "plugins/NoCheat/descriptions.txt";

	private ParentOption root;

	private Map<String, Action> actionMap = new HashMap<String,Action>();

	private Map<String, Object> yamlContent = new HashMap<String, Object>();

	// Our personal logger
	private final static String loggerName = "cc.co.evenprime.nocheat";
	public final Logger logger = Logger.getLogger(loggerName);

	// Our log output to a file
	private FileHandler fh = null;

	public NoCheatConfiguration(File configurationFile, File descriptionsFile) {

		// Setup the configuration tree
		config(configurationFile, descriptionsFile);
	}

	/**
	 * Read the configuration file and assign either standard values or whatever is declared in the file
	 * @param configurationFile
	 */
	public void config(File configurationFile, File descriptionsFile) {


		try {
			yamlContent = (Map<String, Object>) SimpleYaml.read(configurationFile);
		} catch (Exception e) {
			System.out.println("Couldn't use existing nocheat.yml, creating a new file.");
			yamlContent = new HashMap<String, Object>();
		}


		root = new ParentOption("", false);


		/*** LOGGING section ***/
		{
			ParentOption loggingNode = new ParentOption("logging", false);
			root.add(loggingNode);

			loggingNode.add(new MediumStringOption("filename",
					SimpleYaml.getString("logging.filename", "plugins/NoCheat/nocheat.log", yamlContent)));

			loggingNode.add(new LevelOption("logtofile", 
					LogLevel.getLogLevelFromString(SimpleYaml.getString("logging.logtofile", LogLevel.LOW.asString(), yamlContent))));
			loggingNode.add(new LevelOption("logtoconsole", 
					LogLevel.getLogLevelFromString(SimpleYaml.getString("logging.logtoconsole", LogLevel.HIGH.asString(), yamlContent))));
			loggingNode.add(new LevelOption("logtochat", 
					LogLevel.getLogLevelFromString(SimpleYaml.getString("logging.logtochat", LogLevel.MED.asString(), yamlContent))));
			loggingNode.add(new LevelOption("logtoirc", 
					LogLevel.getLogLevelFromString(SimpleYaml.getString("logging.logtoirc", LogLevel.MED.asString(), yamlContent))));

			loggingNode.add(new ShortStringOption("logtoirctag",
					SimpleYaml.getString("logging.logtoirctag", "nocheat", yamlContent)));
		}

		/*** ACTIVE section ***/
		{
			ParentOption activeNode = new ParentOption("active", false);
			root.add(activeNode);

			activeNode.add(new BooleanOption("speedhack", 
					SimpleYaml.getBoolean("active.speedhack", true, yamlContent)));
			activeNode.add(new BooleanOption("moving", 
					SimpleYaml.getBoolean("active.moving", true, yamlContent)));
			activeNode.add(new BooleanOption("airbuild",
					SimpleYaml.getBoolean("active.airbuild", false, yamlContent)));
			activeNode.add(new BooleanOption("bogusitems", 
					SimpleYaml.getBoolean("active.bogusitems", false, yamlContent)));
			activeNode.add(new BooleanOption("nuke", 
					SimpleYaml.getBoolean("active.nuke", false, yamlContent)));
		}

		/*** SPEEDHACK section ***/
		{
			ParentOption speedhackNode = new ParentOption("speedhack",  false);
			root.add(speedhackNode);

			speedhackNode.add(new LongStringOption("logmessage", 
					SimpleYaml.getString("speedhack.logmessage", "[player] sent [events] move events, but only [limit] were allowed. Speedhack?", yamlContent)));

			speedhackNode.add(new BooleanOption("checkops", 
					SimpleYaml.getBoolean("speedhack.checkops", false, yamlContent)));

			/*** SPEEDHACK LIMITS section ***/
			{
				ParentOption speedhackLimitsNode = new ParentOption("limits", false);
				speedhackNode.add(speedhackLimitsNode);

				speedhackLimitsNode.add(new IntegerOption("low", 
						SimpleYaml.getInt("speedhack.limits.low", 22, yamlContent)));
				speedhackLimitsNode.add(new IntegerOption("med", 
						SimpleYaml.getInt("speedhack.limits.med", 33, yamlContent)));
				speedhackLimitsNode.add(new IntegerOption("high", 
						SimpleYaml.getInt("speedhack.limits.high", 44, yamlContent)));
			}

			/*** SPEEDHACK ACTIONS section ***/
			{
				ParentOption speedhackActionNode = new ParentOption("action", false);
				speedhackNode.add(speedhackActionNode);

				speedhackActionNode.add(new MediumStringOption("low", 
						SimpleYaml.getString("speedhack.action.low", "loglow cancel", yamlContent)));
				speedhackActionNode.add(new MediumStringOption("med", 
						SimpleYaml.getString("speedhack.action.med", "logmed cancel", yamlContent)));
				speedhackActionNode.add(new MediumStringOption("high", 
						SimpleYaml.getString("speedhack.action.high", "loghigh cancel", yamlContent)));
			}
		}

		/*** MOVING section ***/
		{
			ParentOption movingNode = new ParentOption("moving", false);
			root.add(movingNode);

			movingNode.add(new LongStringOption("logmessage", 
					SimpleYaml.getString("moving.logmessage", "Moving violation: [player] from [world] [from] to [to] distance [distance]", yamlContent)));

			movingNode.add(new LongStringOption("summarymessage", 
					SimpleYaml.getString("moving.summarymessage", "Moving summary of last ~[timeframe] seconds: [player] total Violations: [violations]", yamlContent)));

			movingNode.add(new BooleanOption("allowflying", 
					SimpleYaml.getBoolean("moving.allowflying", false, yamlContent)));
			movingNode.add(new BooleanOption("allowfakesneak", 
					SimpleYaml.getBoolean("moving.allowfakesneak", true, yamlContent)));
			movingNode.add(new BooleanOption("allowfastswim", 
					SimpleYaml.getBoolean("moving.allowfastswim", false, yamlContent)));
			movingNode.add(new BooleanOption("waterelevators", 
					SimpleYaml.getBoolean("moving.waterelevators", false, yamlContent)));

			movingNode.add(new BooleanOption("checkops", 
					SimpleYaml.getBoolean("moving.checkops", false, yamlContent)));

			movingNode.add(new BooleanOption("enforceteleport", 
					SimpleYaml.getBoolean("moving.enforceteleport", false, yamlContent)));

			/*** MOVING ACTION section ***/
			{
				ParentOption movingActionNode = new ParentOption("action", false);
				movingNode.add(movingActionNode);

				movingActionNode.add(new MediumStringOption("low", 
						SimpleYaml.getString("moving.action.low", "loglow cancel", yamlContent)));
				movingActionNode.add(new MediumStringOption("med", 
						SimpleYaml.getString("moving.action.med", "logmed cancel", yamlContent)));
				movingActionNode.add(new MediumStringOption("high", 
						SimpleYaml.getString("moving.action.high", "loghigh cancel", yamlContent)));
			}
		}

		/*** AIRBUILD section ***/
		{
			ParentOption airbuildNode = new ParentOption("airbuild", false);
			root.add(airbuildNode);

			airbuildNode.add(new BooleanOption("checkops", 
					SimpleYaml.getBoolean("airbuild.checkops", false, yamlContent)));

			/*** AIRBUILD LIMITS section ***/
			{
				ParentOption airbuildLimitsNode = new ParentOption("limits", false);
				airbuildNode.add(airbuildLimitsNode);

				airbuildLimitsNode.add(new IntegerOption("low", 
						SimpleYaml.getInt("airbuild.limits.low", 1, yamlContent)));
				airbuildLimitsNode.add(new IntegerOption("med", 
						SimpleYaml.getInt("airbuild.limits.med", 3, yamlContent)));
				airbuildLimitsNode.add(new IntegerOption("high", 
						SimpleYaml.getInt("airbuild.limits.high", 10, yamlContent)));
			}

			/*** AIRBUILD ACTION section ***/
			{
				ParentOption airbuildActionNode = new ParentOption("action", false);
				airbuildNode.add(airbuildActionNode);

				airbuildActionNode.add(new MediumStringOption("low", 
						SimpleYaml.getString("airbuild.action.low", "loglow cancel", yamlContent)));
				airbuildActionNode.add(new MediumStringOption("med", 
						SimpleYaml.getString("airbuild.action.med", "logmed cancel", yamlContent)));
				airbuildActionNode.add(new MediumStringOption("high", 
						SimpleYaml.getString("airbuild.action.high", "loghigh cancel", yamlContent)));
			}


		}

		/*** BOGUSITEMS section ***/
		{
			ParentOption bogusitemsNode = new ParentOption("bogusitems", false);
			root.add(bogusitemsNode);

			bogusitemsNode.add(new BooleanOption("checkops", 
					SimpleYaml.getBoolean("bogusitems.checkops", false, yamlContent)));
		}
		

		/*** NUKE section ***/
		{
			ParentOption nukeNode = new ParentOption("nuke", false);
			root.add(nukeNode);
			
			nukeNode.add(new BooleanOption("checkops", 
					SimpleYaml.getBoolean("nuke.checkops", false, yamlContent)));
			nukeNode.add(new LongStringOption("logmessage", 
					SimpleYaml.getString("nuke.logmessage", "Nuke: [player] tried to nuke the world", yamlContent)));
			nukeNode.add(new LongStringOption("kickmessage", 
					SimpleYaml.getString("nuke.kickmessage", "No nuking allowed", yamlContent)));
			
		}

		/*** CUSTOMACTIONS section ***/
		{
			ParentOption customActionsNode = new ParentOption("customactions", true);
			root.add(customActionsNode);

			Set<String> customs = SimpleYaml.getKeys("customactions", yamlContent);

			for(String s : customs) {

				CustomActionOption o = new CustomActionOption(s, SimpleYaml.getString("customactions."+s, "unknown", yamlContent));

				customActionsNode.add(o);
				actionMap.put(s, o.getCustomActionValue());
			}
		}

		writeConfigFile(configurationFile, this);		
		//writeDescriptionFile(descriptionsFile, this);
	}

	public void setupFileLogger() {

		logger.setLevel(Level.INFO);
		logger.setUseParentHandlers(false);

		if(fh == null) {
			try {
				fh = new FileHandler(getStringValue("logging.filename"), true);
				fh.setLevel(getLogLevelValue("logging.logtofile"));
				fh.setFormatter(new LogFileFormatter());
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

	public Action[] stringToActions(String string) {

		List<Action> as = new LinkedList<Action>();
		String[] parts = string.split(" ");

		for(String s : parts) {
			if(s.equals("loglow"))
				as.add(LogAction.loglow);
			else if(s.equals("logmed"))
				as.add(LogAction.logmed);
			else if(s.equals("loghigh"))
				as.add(LogAction.loghigh);
			else if(s.equals("cancel"))
				as.add(CancelAction.cancel);
			else if(actionMap.get(s) != null)
				as.add(actionMap.get(s));
			else
			{
				System.out.println("NC: Couldn't parse custom action '" + s + "'");
			}
		}

		return as.toArray(new Action[as.size()]);
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

	/**
	 * Write a file with the descriptions of all options
	 * @param f
	 */
	public static void writeDescriptionFile(File f, NoCheatConfiguration configuration) {
		try {
			if(f.getParentFile() != null)
				f.getParentFile().mkdirs();

			f.createNewFile();
			BufferedWriter w = new BufferedWriter(new FileWriter(f));

			w.write(configuration.getRoot().toDescriptionString(""));

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
