package cc.co.evenprime.bukkit.nocheat.config.tree;

import java.util.ArrayList;
import java.util.List;

import cc.co.evenprime.bukkit.nocheat.actions.ActionList;
import cc.co.evenprime.bukkit.nocheat.config.Configuration;
import cc.co.evenprime.bukkit.nocheat.log.LogLevel;

/**
 * Only used during parsing of the configuration files and for the GUI wizard
 * 
 * @author Evenprime
 * 
 */
public class ConfigurationTree implements Configuration {

    // Each tree has a root option that does nothing
    private final ParentOption root   = new ParentOption("");

    // Each tree can have a "parent" tree
    private ConfigurationTree  parent = null;

    /**
     * Start a new tree
     * 
     * @param parent
     */
    public ConfigurationTree() {}

    public void setParent(ConfigurationTree parent) {
        this.parent = parent;
    }

    public ConfigurationTree getParent() {
        return parent;
    }

    /**
     * Get a specific option from this tree or its parent(s), if this
     * tree doesn't contain this option
     * 
     * @param fullIdentifier
     * @return
     */
    private Option getOptionRecursive(String fullIdentifier) {

        // Find out the partial identifiers
        String[] identifiers = fullIdentifier.split("\\.");

        // Start at the root
        Option o = root;

        // Go through all partial identifiers
        for(int i = 0; i < identifiers.length; i++) {
            if(o instanceof ParentOption) {
                for(Option o2 : ((ParentOption) o).getChildOptions()) {
                    if(o2.getIdentifier().equals(identifiers[i])) {
                        o = o2;
                        break;
                    }
                }
            } else
                break;
        }

        // Does the node we last met match our searched node and is it enabled?
        if(o.getFullIdentifier().equals(fullIdentifier) && o.isActive()) {
            return o;
        }
        // No, then ask our parent (if possible)
        else if(parent != null) {
            return parent.getOptionRecursive(fullIdentifier);
        } else {
            return null;
        }
    }

    /**
     * Get a specific option from this tree
     * 
     * @param fullIdentifier
     * @return
     */
    public Option getOption(String fullIdentifier) {

        // Find out the partial identifiers
        String[] identifiers = fullIdentifier.split("\\.");

        // Start at the root
        Option o = root;

        // Go through all partial identifiers
        for(int i = 0; i < identifiers.length; i++) {
            if(o instanceof ParentOption) {
                for(Option o2 : ((ParentOption) o).getChildOptions()) {
                    if(o2.getIdentifier().equals(identifiers[i])) {
                        o = o2;
                        break;
                    }
                }
            } else
                break;
        }

        // Does the node we last met match our searched node?
        if(o.getFullIdentifier().equals(fullIdentifier)) {
            return o;
        } else {
            return null;
        }
    }

    /**
     * Add a option to this tree, at the specified parent
     * 
     * @param parent
     * @param option
     */
    public void add(String parent, Option option) {
        try {
            if(parent == null || parent == "") {
                add(option);
            } else {
                ParentOption po = (ParentOption) getOption(parent);
                po.add(option);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Add a option at the root
     * 
     * @param option
     */
    public void add(Option option) {
        root.add(option);
    }

    /**
     * 
     * @return
     */
    public List<Option> getAllOptions() {
        List<Option> options = new ArrayList<Option>();

        ParentOption o = root;

        for(Option o2 : o.getChildOptions()) {
            if(o2.isActive()) {
                options.addAll(getAllOptions(o2));
            }
        }

        return options;
    }

    private List<Option> getAllOptions(Option subtree) {

        List<Option> options = new ArrayList<Option>();

        options.add(subtree);

        if(subtree instanceof ParentOption) {
            for(Option child : ((ParentOption) subtree).getChildOptions()) {
                options.addAll(getAllOptions(child));
            }
        }

        return options;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * cc.co.evenprime.bukkit.nocheat.config.tree.ConfigurationProvider#getBoolean
     * (java.lang.String)
     */
    @Override
    public boolean getBoolean(String string) {
        return ((BooleanOption) this.getOptionRecursive(string)).getBooleanValue();
    }

    /*
     * (non-Javadoc)
     * 
     * @see cc.co.evenprime.bukkit.nocheat.config.tree.ConfigurationProvider#
     * getActionList(java.lang.String)
     */
    @Override
    public ActionList getActionList(String string) {
        ActionList actionList = new ActionList();
        ActionListOption option = (ActionListOption) this.getOptionRecursive(string);

        for(ActionOption ao : option.getChildOptions()) {
            actionList.addEntry(ao.getTreshold(), ao.getStringValue().split(" "));
        }
        return actionList;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * cc.co.evenprime.bukkit.nocheat.config.tree.ConfigurationProvider#getInteger
     * (java.lang.String)
     */
    @Override
    public int getInteger(String string) {
        return ((IntegerOption) this.getOptionRecursive(string)).getIntegerValue();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * cc.co.evenprime.bukkit.nocheat.config.tree.ConfigurationProvider#getString
     * (java.lang.String)
     */
    @Override
    public String getString(String string) {
        return ((ChildOption) this.getOptionRecursive(string)).getStringValue();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * cc.co.evenprime.bukkit.nocheat.config.tree.ConfigurationProvider#getLogLevel
     * (java.lang.String)
     */
    @Override
    public LogLevel getLogLevel(String string) {
        return ((LogLevelOption) this.getOptionRecursive(string)).getLogLevelValue();
    }
}
