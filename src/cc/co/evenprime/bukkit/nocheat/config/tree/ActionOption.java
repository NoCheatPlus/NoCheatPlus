package cc.co.evenprime.bukkit.nocheat.config.tree;

/**
 * A special node of the configuration tree
 * 
 * @author Evenprime
 * 
 */
public class ActionOption extends ChildOption implements Comparable<ActionOption> {

    private final int treshold;
    private String    value;

    public ActionOption(Integer treshold, String value) {

        super(treshold.toString());
        this.treshold = treshold;
        this.value = value;

    }

    @Override
    public String getStringValue() {
        return value;
    }

    @Override
    public boolean setStringValue(String value) {
        this.value = value;
        return true;
    }

    public int getTreshold() {
        return treshold;
    }

    @Override
    public ActionOption clone() {
        return new ActionOption(treshold, value);
    }

    @Override
    public int compareTo(ActionOption o) {
        if(treshold < o.treshold) {
            return -1;
        } else if(treshold == o.treshold) {
            return 0;
        } else
            return 1;
    }

    public String toString() {
        return "ActionOption " + getFullIdentifier() + " " + getStringValue();
    }

}
