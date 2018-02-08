package fr.neatmonster.nocheatplus.hooks;

/**
 * Both hashCode and equals only compare the ids, so equals is not fit for
 * comparing side conditions - AWAIT a method like
 * isEquivalentTo(ExemptionContext) for that purpose, rather.
 * <hr>
 * Note that some ids like 0 and -1 are reserved. The registry will only deal
 * positive ids.
 * 
 * @author asofold
 *
 */
public class ExemptionContext {

    /** Id is -1. */
    public static final ExemptionContext LEGACY_NON_NESTED = new ExemptionContext(-1);
    /** Id is 0. */
    public static final ExemptionContext ANONYMOUS_NESTED = new ExemptionContext(0);

    ///////////////
    // Instance
    ///////////////

    /*
     * 
     *  TODO: How to use (one context = one thing, or one context contains multiple ids.
     *  -> so contexts contain contexts (...).
     */

    private Integer id;

    public ExemptionContext(final Integer id) {
        if (id == null) {
            throw new NullPointerException("The id must not be null.");
        }
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        else if (obj instanceof ExemptionContext) {
            return id.equals(((ExemptionContext) obj).getId());
        }
        else if (obj instanceof Integer) {
            return id.equals((Integer) obj);
        }
        else {
            return false;
        }
    }

}
