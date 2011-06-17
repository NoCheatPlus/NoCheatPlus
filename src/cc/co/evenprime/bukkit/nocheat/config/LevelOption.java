package cc.co.evenprime.bukkit.nocheat.config;

import java.util.logging.Level;


public class LevelOption extends ChildOption {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1609308017422576285L;

	private LogLevel option;

	public enum LogLevel {
		
		
		OFF("off", "never", Level.OFF), 
		LOW("low", "all messages", Level.INFO), 
		MED("med", "important messages", Level.WARNING), 
		HIGH("high", "very important messages", Level.SEVERE);

		private final String value;
		private String description;
		private Level level;

		private LogLevel(String value, String description, Level level) {
			this.value = value;
			this.description = description;
			this.level = level;
		}

		public String asString() { return this.value; }

		public static LogLevel getLogLevelFromString(String s) {
			if(s == null) return OFF;
			if("off".equals(s))
				return OFF;
			else if("low".equals(s))
				return LOW;
			else if("med".equals(s))
				return MED;
			else if("high".equals(s))
				return HIGH;
			else
				return OFF;
		}


		public String toString() {
			return this.name() + ": " + description;
		}

		public Level getLevel() {
			return level;
		}
	}

	public LevelOption(String identifier, String parentName, LogLevel initialValue) {

		super(identifier, parentName);
		this.option = initialValue;
	}


	@Override
	public String getValue() {
		return option.asString();
	}

	public void setValue(LogLevel value) {
		this.option = value;
	}

	public LogLevel getOptionValue() {
		return this.option;
	}
	
	public Level getLevelValue() {
		return this.option.getLevel();
	}
}
