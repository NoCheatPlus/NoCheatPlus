package fr.neatmonster.nocheatplus.workaround;

/**
 * Count down to 0, use is only possible if the currentCounter is greater than
 * 0. An initialCounter of 0 together with setting the currentCount manually,
 * allows to activate a workaround on base of a precondition.
 * 
 * @author asofold
 *
 */
public class WorkaroundCountDown extends AbstractWorkaround {

    private final int initialCount;
    private int currentCount;

    public WorkaroundCountDown(String id, int initialCount, Workaround parent) {
        super(id, parent);
        this.initialCount = initialCount;
        this.currentCount = initialCount;
    }

    public void setCurrentCount(int currentCount) {
        this.currentCount = currentCount;
    }

    @Override
    public boolean use() {
        // Adjust currentCount based on super.use().
        if (super.use()) {
            currentCount --;
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean canUse() {
        return currentCount > 0;
    }

    @Override
    public void resetConditions() {
        currentCount = initialCount;
    }

    @Override
    public WorkaroundCountDown getNewInstance() {
        return new WorkaroundCountDown(getId(), initialCount, getParent());
    }

}
