package cc.co.evenprime.bukkit.nocheat.config;

public abstract class ChildOption extends Option {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4648294833934457776L;
	
	public ChildOption(String identifier, String parentIdentifier) {
		
		super(identifier, parentIdentifier);
	}
	
	
	public abstract String getValue();
	
	@Override
	public String toYAMLString(String prefix) {
		return prefix + getIdentifier() + ": \"" + getValue() + "\"\r\n";
	}
}
