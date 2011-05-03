package cc.co.evenprime.bukkit.nocheat.wizard.options;


public class LogLevelOption extends ChildOption {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1609308017422576285L;

	private Options value;


	public enum Options {
		OFF("never"), LOW("all messages"), MED("important messages"), HIGH("very important messages");

		private final String value;

		private Options(String s) {
			this.value = s;
		}

		public String getValue() { return this.value; }

		public static Options getOption(String s) {
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
			return this.name() + ": " + getValue();
		}
	}

	public LogLevelOption(String identifier, Options initialValue) {

		super(identifier);
		this.value = initialValue;
	}


	@Override
	public String getValue() {
		return value.getValue();
	}

	public void setValue(Options value) {
		this.value = value;
	}

	public Options getOptionValue() {
		return this.value;
	}
}
