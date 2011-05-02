package cc.co.evenprime.bukkit.nocheat.wizard;


public class IntegerOption extends TextFieldOption {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2258827414736580449L;
		
	public IntegerOption(String name, int initialValue, int width) {
		
		super(name, String.valueOf(initialValue), width, new IntegerVerifier("Only integers are allowed in this field."));
		
	}
}
