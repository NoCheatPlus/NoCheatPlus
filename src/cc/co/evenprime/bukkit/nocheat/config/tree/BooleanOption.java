package cc.co.evenprime.bukkit.nocheat.config.tree;

/**
 * A node of the configuration tree
 * 
 * @author Evenprime
 * 
 */
public class BooleanOption extends ChildOption {

    /**
	 * 
	 */
    private static final long serialVersionUID = 2258827414736580449L;

    private boolean           value;
    private boolean           isMaster         = false;

    public BooleanOption(String name, boolean initialValue, boolean isMaster) {

        super(name);
        this.value = initialValue;
        this.isMaster = isMaster;
    }

    public void setBooleanValue(boolean value) {
        this.value = value;
    }

    @Override
    public String getStringValue() {
        return Boolean.toString(value);
    }

    @Override
    public boolean setStringValue(String value) {
        try {
            this.value = Boolean.parseBoolean(value);
            return true;
        } catch(Exception e) {
            return false;
        }
    }

    public boolean getBooleanValue() {
        return value;
    }

    @Override
    public BooleanOption clone() {
        return new BooleanOption(this.getIdentifier(), value, isMaster);
    }

    public boolean isMaster() {
        return isMaster;
    }

    public void setMaster(boolean master) {
        this.isMaster = master;
    }

    public String toString() {
        return "BooleanOption " + this.getFullIdentifier() + " " + getStringValue() + (isMaster ? " master" : "");
    }
}
