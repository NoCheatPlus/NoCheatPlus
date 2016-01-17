package fr.neatmonster.nocheatplus.workaround;

/**
 * Implementing the minimum features for counting use, plus the ability to trigger a parent
 * count.
 * 
 * @author asofold
 *
 */
public abstract class AbstractWorkaround implements Workaround {

    private final String id;
    private final Workaround parent;
    private int useCount = 0;

    public AbstractWorkaround(String id) {
        this(id, null); // No parent.
    }

    /**
     * 
     * @param id
     * @param parent
     *            For (global) count: parent.use() is called from within
     *            this.use(), but the result is not evaluate.
     */
    public AbstractWorkaround(String id, Workaround parent) {
        this.id = id;
        this.parent = parent;
    }

    public Workaround getParent() {
        return parent;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public int getUseCount() {
        return useCount;
    }

    @Override
    public boolean use() {
        if (canUse()) {
            useCount ++;
            if (parent != null) {
                // TODO: Might consider a hierarchy (parent could overrule the result to false).
                parent.use();
            }
            return true;
        }
        else {
            // DenyUseCound is handled in sub-classes, if needed.
            return false;
        }
    }

}
