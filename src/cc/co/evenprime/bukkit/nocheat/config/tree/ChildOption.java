package cc.co.evenprime.bukkit.nocheat.config.tree;

/**
 * A leaf of the configuration tree
 * 
 * @author Evenprime
 * 
 */
public abstract class ChildOption extends Option implements Cloneable {

    /**
	 * 
	 */
    private static final long serialVersionUID = -4648294833934457776L;

    private String            value;

    public ChildOption(String identifier) {
        super(identifier);
    }

    public String getStringValue() {
        return value;
    }

    public boolean setStringValue(String string) {
        this.value = string;
        return true;
    }

    public abstract ChildOption clone();
}
