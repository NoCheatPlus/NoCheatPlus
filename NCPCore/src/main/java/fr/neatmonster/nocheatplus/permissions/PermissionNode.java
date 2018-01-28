package fr.neatmonster.nocheatplus.permissions;

import fr.neatmonster.nocheatplus.compat.AlmostBoolean;
import fr.neatmonster.nocheatplus.permissions.PermissionPolicy.FetchingPolicy;

/**
 * Per player per permission runtime information (PlayerData). This is kept
 * minimal, in order to fit many into some cache - more properties and mapping
 * has to be relayed to the registry.
 * 
 * @author asofold
 *
 */
public class PermissionNode {

    private final PermissionInfo info;
    private long lastFetch = 0;
    private AlmostBoolean lastState = AlmostBoolean.MAYBE;

    public PermissionNode(PermissionInfo info) {
        this.info = info;
        /*
         * Might store extras like fetchingPolicy here, for most efficient
         * access. Affects performance of config reload too / to be seen.
         */
    }

    public PermissionInfo getPermissionInfo() {
        return info;
    }

    public FetchingPolicy getFetchingPolicy() {
        return info.fetchingPolicy();
    }

    /**
     * 
     * @param state
     * @param time 
     */
    public void setState(AlmostBoolean state, long time) {
        this.lastState = state;
        this.lastFetch = time;
    }

    /**
     * In case of 0, the entry has not been fetched yet. Note that invalidation
     * doesn't reset the time.
     * 
     * @return
     */
    public long getLastFetch() {
        return lastFetch;
    }

    public long getFetchInterval() {
        return info.fetchingInterval();
    }

    /**
     * 
     * @return MAYBE means 'invalid', including 'not set'.
     */
    public AlmostBoolean getLastState() {
        return lastState;
    }

    public void invalidate() {
        lastState = AlmostBoolean.MAYBE;
    }

}
