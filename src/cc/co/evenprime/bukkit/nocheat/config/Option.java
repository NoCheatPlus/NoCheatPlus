package cc.co.evenprime.bukkit.nocheat.config;


public abstract class Option {
			
	private final String identifier;
	private final String parentIdentifier;
	
	public Option(String identifier, String parentIdentifier) {
		this.identifier = identifier;
		this.parentIdentifier = parentIdentifier;
	}
	
	public String getIdentifier() {
		return identifier;
	}
	
	public String getFullIdentifier() {
		return (parentIdentifier == null || parentIdentifier == "") ? identifier : parentIdentifier + "." + identifier;
	}
	
	public String toYAMLString(String prefix) {
		return prefix + "\r\n";
	}
}
