package cc.co.evenprime.bukkit.nocheat.wizard;

public abstract class ChildOption extends Option {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4648294833934457776L;

	private String value;
	
	public ChildOption(String identifier, String value) {
		super(identifier);
		this.value = value;
	}
	
	public final void setValue(String value) {
		this.value = value;
	}
	
	public final String getValue() {
		return value;
	}
}
