package cc.co.evenprime.bukkit.nocheat.config;

public class LongStringOption extends TextFieldOption {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2258827414736580449L;
		
	public LongStringOption(String name, String parentName, String initialValue) {
		super(name, parentName, initialValue, 60);
	}
}
