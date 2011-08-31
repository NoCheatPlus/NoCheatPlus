package cc.co.evenprime.bukkit.nocheat.config.tree;

/**
 * A node for the configuration tree
 * 
 * @author Evenprime
 * 
 */
public abstract class Option {

    private final String identifier;
    private Option       parent;

    private boolean      active = true;

    public Option(String identifier) {
        this.identifier = identifier;
    }

    public final String getIdentifier() {
        return identifier;
    }

    public final void setParent(Option parent) {
        this.parent = parent;
    }

    public final Option getParent() {
        return parent;
    }

    public final String getFullIdentifier() {
        return (parent == null || parent.getFullIdentifier() == "") ? identifier : parent.getFullIdentifier() + "." + identifier;
    }

    public void setActive(boolean b) {
        active = b;
    }

    public boolean isActive() {
        return active;
    }
}
