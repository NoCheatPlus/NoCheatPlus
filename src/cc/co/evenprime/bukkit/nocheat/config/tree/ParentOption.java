package cc.co.evenprime.bukkit.nocheat.config.tree;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

/**
 * A node for the configuration tree
 * 
 * @author Evenprime
 * 
 */
public class ParentOption extends Option {

    /**
	 * 
	 */
    private static final long        serialVersionUID = 3162246550749560727L;

    private final LinkedList<Option> children         = new LinkedList<Option>();

    public ParentOption(String identifier) {
        super(identifier);
    }

    public final Collection<Option> getChildOptions() {
        return Collections.unmodifiableCollection(children);
    }

    public final void add(Option option) {
        children.addLast(option);
        option.setParent(this);
    }

    public Option getChild(String identifier) {

        for(Option o : children) {
            if(o.getIdentifier().equals(identifier)) {
                return o;
            }
        }

        return null;
    }
}
