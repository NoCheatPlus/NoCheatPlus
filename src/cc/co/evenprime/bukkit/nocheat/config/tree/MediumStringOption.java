package cc.co.evenprime.bukkit.nocheat.config.tree;

/**
 * A node for the configuration tree
 * 
 * @author Evenprime
 * 
 */
public class MediumStringOption extends StringOption {

    /**
	 * 
	 */
    private static final long serialVersionUID = 2258827414736580449L;

    public MediumStringOption(String name, String initialValue) {
        super(name, initialValue, 30);
    }

    public MediumStringOption clone() {
        return new MediumStringOption(this.getIdentifier(), this.getStringValue());
    }
}
