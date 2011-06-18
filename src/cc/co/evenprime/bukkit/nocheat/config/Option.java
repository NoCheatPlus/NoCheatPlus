package cc.co.evenprime.bukkit.nocheat.config;

import cc.co.evenprime.bukkit.nocheat.wizard.gui.Explainations;

public abstract class Option {
			
	private final String identifier;
	private final String parentIdentifier;
	private final String description;
	
	public Option(String identifier, String parentIdentifier) {
		this.identifier = identifier;
		this.parentIdentifier = parentIdentifier;
		this.description = Explainations.get(parentIdentifier == null || parentIdentifier.equals("") ? identifier : parentIdentifier + "." + identifier);
	}
	
	public final String getIdentifier() {
		return identifier;
	}
	
	public final String getDescription() {
		return description;
	}
	
	public final String getFullIdentifier() {
		return (parentIdentifier == null || parentIdentifier == "") ? identifier : parentIdentifier + "." + identifier;
	}
	
	public abstract String toYAMLString(String prefix);
	
	public abstract String toDescriptionString(String prefix);
}
