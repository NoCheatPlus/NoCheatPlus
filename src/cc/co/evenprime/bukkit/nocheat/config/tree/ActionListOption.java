package cc.co.evenprime.bukkit.nocheat.config.tree;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;


/**
 * A special node of the configuration tree
 * 
 * @author Evenprime
 *
 */
public class ActionListOption extends ChildOption {

    private final LinkedList<ActionOption> actionOptions = new LinkedList<ActionOption>();
    
    public ActionListOption(String identifier) {
        super(identifier);
    }
    
    public void add(Integer treshold, String actions) {
        actionOptions.add(new ActionOption(treshold, actions));
        Collections.sort(actionOptions);
    }
    
    public List<ActionOption> getChildOptions() {
        return actionOptions;
    }
    
    public void remove(ActionOption actionOption) {
        this.actionOptions.remove(actionOption);
    }
    
    @Override
    public ActionListOption clone() {
        
        ActionListOption o = new ActionListOption(this.getIdentifier());
        for(ActionOption ao : getChildOptions()) {
            o.add(ao.getTreshold(), ao.getStringValue());
        }
        
        return o;
    }

    public void clear() {
        actionOptions.clear();
        
    }
}
