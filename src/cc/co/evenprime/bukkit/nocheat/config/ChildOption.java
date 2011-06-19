package cc.co.evenprime.bukkit.nocheat.config;

import cc.co.evenprime.bukkit.nocheat.wizard.gui.Explainations;


public abstract class ChildOption extends Option {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4648294833934457776L;

	public ChildOption(String identifier) {
		super(identifier);
	}


	public abstract String getValue();

	@Override
	public String toYAMLString(String prefix) {
		return prefix + getIdentifier() + ": \"" + getValue() + "\"\r\n";
	}

	@Override
	public String toDescriptionString(String prefix) {
		return prefix + getIdentifier() + ": \"" + Explainations.get(getFullIdentifier()).replace("\n", "\r\n" + prefix + "\t\t") + "\"\r\n";
	}
}
