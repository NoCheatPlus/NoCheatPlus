package cc.co.evenprime.bukkit.nocheat.config.tree;

/**
 * A node for the configuration tree
 * 
 * @author Evenprime
 * 
 */
public class IntegerOption extends StringOption {

    /**
	 * 
	 */
    private static final long serialVersionUID = 2258827414736580449L;

    public IntegerOption(String name, int initialValue) {

        super(name, String.valueOf(initialValue), 5);
    }

    @Override
    public boolean isValid(String value) {

        if(!super.isValid(value))
            return false;

        try {
            Integer.parseInt(value);
            return true;
        } catch(Exception e) {
            return false;
        }
    }

    public int getIntegerValue() {
        return Integer.parseInt(this.getStringValue());
    }

    public IntegerOption clone() {
        return new IntegerOption(this.getIdentifier(), this.getIntegerValue());
    }
}
