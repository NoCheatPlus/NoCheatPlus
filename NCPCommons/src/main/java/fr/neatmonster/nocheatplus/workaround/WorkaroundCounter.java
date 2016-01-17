package fr.neatmonster.nocheatplus.workaround;

/**
 * Simply count times used.
 * @author asofold
 *
 */
public class WorkaroundCounter extends AbstractWorkaround {

    public WorkaroundCounter(String id, Workaround parent) {
        super(id, parent);
    }

    public WorkaroundCounter(String id) {
        super(id);
    }

    @Override
    public boolean canUse() {
        // Just counting.
        return true;
    }

    @Override
    public void resetConditions() {
        // Nothing to do.
    }

    @Override
    public WorkaroundCounter getNewInstance() {
        return new WorkaroundCounter(getId());
    }

}
