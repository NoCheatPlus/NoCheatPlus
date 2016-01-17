package fr.neatmonster.nocheatplus.workaround;

/**
 * Simply count times used.
 * @author asofold
 *
 */
public class WorkaroundCounter extends AbstractWorkaround {

    public WorkaroundCounter(String id) {
        super(id);
    }

    @Override
    public boolean testUse(final boolean isUse) {
        // Just counting.
        return true;
    }

    @Override
    public WorkaroundCounter getNewInstance() {
        return setParentCounters(new WorkaroundCounter(getId()));
    }

}
