package cc.co.evenprime.bukkit.nocheat.config;

public abstract class Option {
			
	private final String identifier;
	private Option parent;
	
	public Option(String identifier) {
		this.identifier = identifier;
	}
	
	public final String getIdentifier() {
		return identifier;
	}
		
	public final void setParent(Option parent) {
		this.parent = parent;
	}
	
	public final String getFullIdentifier() {
		return (parent == null || parent.getFullIdentifier() == "") ? identifier : parent.getFullIdentifier() + "." + identifier;
	}
	
	public abstract String toYAMLString(String prefix);
	
	public abstract String toDescriptionString(String prefix);
}
