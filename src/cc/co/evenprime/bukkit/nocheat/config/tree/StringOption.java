package cc.co.evenprime.bukkit.nocheat.config.tree;

/**
 * A node for the configuration tree
 * 
 * @author Evenprime
 * 
 */
public abstract class StringOption extends ChildOption {

    /**
	 * 
	 */
    private static final long serialVersionUID = -8189248456599421250L;

    private String            value;
    private final int         length;

    protected StringOption(String name, String initialValue, int preferredLength) {

        super(name);
        this.value = initialValue;
        this.length = preferredLength;
    }

    @Override
    public String getStringValue() {
        return this.value;
    }

    public boolean setStringValue(String value) {
        if(isValid(value)) {
            this.value = value;
            return true;
        } else
            return false;
    }

    protected boolean isValid(String value) {
        return value != null;
    }

    public int getPreferredLength() {
        return length;
    }

    public boolean hasPreferredLength() {
        return length != -1;
    }
}
