package cc.co.evenprime.bukkit.nocheat.config;


public class IntegerOption extends TextFieldOption {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2258827414736580449L;
		
	public IntegerOption(String name, String parentName, int initialValue) {
		
		super(name, parentName, String.valueOf(initialValue), 5);		
	}

	@Override
	public boolean isValid(String value) {
		
		if(!super.isValid(value)) return false;
		
		try {
			Integer.parseInt(value);
			return true;
		}
		catch(Exception e) {
			return false;
		}
	}

	public int getIntegerValue() {
		return Integer.parseInt(this.getValue());
	}
}
