package cc.co.evenprime.bukkit.nocheat.wizard;

public class StringOption extends TextFieldOption {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2258827414736580449L;
		
	public StringOption(String name, String initialValue, int width) {
		super(name, initialValue, width, new EmptyVerifier());
		
	}
}
