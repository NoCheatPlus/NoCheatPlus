package cc.co.evenprime.bukkit.nocheat.config.tree;

/**
 * A special node of the configuration tree
 * 
 * @author Evenprime
 * 
 */
public class ActionOption extends ChildOption implements Comparable<ActionOption> {

    private int    treshold;
    private String value;

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

    /**
     * It is recommended to take further action if the treshold
     * gets changed: If successful, "clone()" this object and
     * replace the original with it.
     * 
     * @param treshold
     * @return
     */
    public boolean setTreshold(String treshold) {
        try {
            this.treshold = Integer.parseInt(treshold);
            return true;
        } catch(NumberFormatException e) {
            return false;
        }
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
