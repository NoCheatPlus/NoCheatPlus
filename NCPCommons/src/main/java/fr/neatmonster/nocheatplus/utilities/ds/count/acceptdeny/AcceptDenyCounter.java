package fr.neatmonster.nocheatplus.utilities.ds.count.acceptdeny;

/**
 * Default implementation for accept-deny counters,
 * 
 * @author asofold
 *
 */
public class AcceptDenyCounter implements IResettableAcceptDenyCounter, ICounterWithParent {

    private int acceptCount = 0;
    private int denyCount = 0;

    private IAcceptDenyCounter parent = null;

    @Override
    public AcceptDenyCounter setParentCounter(IAcceptDenyCounter parent) {
        this.parent = parent;
        return this;
    }

    @Override
    public IAcceptDenyCounter getParentCounter() {
        return parent;
    }

    @Override
    public void accept() {
        acceptCount ++;
        if (parent != null) {
            parent.accept();
        }
    }

    @Override
    public int getAcceptCount() {
        return acceptCount;
    }

    @Override
    public void deny() {
        denyCount ++;
        if (parent != null) {
            parent.deny();
        }
    }

    @Override
    public int getDenyCount() {
        return denyCount;
    }

    @Override
    public void resetCounter() {
        acceptCount = denyCount = 0;
    }

}
