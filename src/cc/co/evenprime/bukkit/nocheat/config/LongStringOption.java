package cc.co.evenprime.bukkit.nocheat.config;

public class LongStringOption extends TextFieldOption {

    /**
	 * 
	 */
    private static final long serialVersionUID = 2258827414736580449L;

    public LongStringOption(String name, String initialValue) {
        super(name, initialValue, 60);
    }
}
