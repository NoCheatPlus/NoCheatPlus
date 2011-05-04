package cc.co.evenprime.bukkit.nocheat.config;


public abstract class Option {
			
	private final String identifier;
	
	public Option(String identifier) {
		this.identifier = identifier;
	}
	
	public String getIdentifier() {
		return identifier;
	}
	
	public String toYAMLString(String prefix) {
		return prefix + "\r\n";
	}
}
