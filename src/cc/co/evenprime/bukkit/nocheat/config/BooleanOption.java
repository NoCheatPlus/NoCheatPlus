package cc.co.evenprime.bukkit.nocheat.config;


public class BooleanOption extends ChildOption {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2258827414736580449L;
	
	private boolean value;
		
	public BooleanOption(String name, String parentName, boolean initialValue) {
		
		super(name, parentName);
		this.value = initialValue;
	}
	
	public void setValue(boolean value) {
		this.value = value;
	}
	
	@Override
	public String getValue() {
		return Boolean.toString(value);
	}
	
	public boolean getBooleanValue() {
		return value;
	}
}
