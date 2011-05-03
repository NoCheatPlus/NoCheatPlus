package cc.co.evenprime.bukkit.nocheat.wizard.options;

public abstract class ChildOption extends Option {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4648294833934457776L;
	
	public ChildOption(String identifier) {
		
		super(identifier);
	}
	
	
	public abstract String getValue();
}
