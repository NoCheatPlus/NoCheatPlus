package cc.co.evenprime.bukkit.nocheat.wizard.options;


public class BooleanOption extends ChildOption {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2258827414736580449L;
	
	private boolean value;
		
	public BooleanOption(String name, boolean initialValue) {
		
		super(name);
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
