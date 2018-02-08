/*
 * This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package fr.neatmonster.nocheatplus.permissions;

import java.util.List;

import fr.neatmonster.nocheatplus.utilities.StringUtil;

public class PermissionPolicy {

    // TODO: Consider IPermissionPolicy with read only access.

    /**
     * The default fetching policy for a permission entry. Note that further
     * invalidation policies can apply.
     * 
     * @author asofold
     *
     */
    public static enum FetchingPolicy {
        /** Have this permission by default. */
        TRUE,
        /** Not have this permission by default. */
        FALSE,
        /** Check once. */
        ONCE,
        /** Check once per interval. */
        INTERVAL,
        /** Always check this permission. */
        ALWAYS;
    }

    /** Invalidate with changing to another world. */
    public static final int INVALIDATION_WORLD              = 0x001;

    /** Invalidate with join/leave. */
    public static final int INVALIDATION_OFFLINE            = 0x002;


    //////////////////////////
    // Instance
    //////////////////////////

    private FetchingPolicy fetchingPolicy = FetchingPolicy.ALWAYS;
    private long fetchingInterval = 0;
    /** Invalidation with special rare events, such as world change or quit. */
    private int invalidationFlags = INVALIDATION_WORLD | INVALIDATION_OFFLINE;

    /**
     * Default constructor: ALWAYS fetch, invalidate with world/offline to be
     * sure.
     */
    public PermissionPolicy() {

    }

    /**
     * Copy constructor.
     * 
     * @param bluePrint
     */
    public PermissionPolicy(PermissionPolicy bluePrint) {
        set(bluePrint);
    }

    /**
     * Set to the same values as the bluePrint.
     * 
     * @param bluePrint
     */
    public void set(PermissionPolicy bluePrint) {
        this.fetchingInterval = bluePrint.fetchingInterval; // Set first, in case it's already INTERVAL.
        this.fetchingPolicy = bluePrint.fetchingPolicy;
        this.invalidationFlags = bluePrint.invalidationFlags; // No sanitizing here, yet.
    }

    /**
     * Convenience method to set FetchingPolicy.INTERVAL and the interval at the
     * same time.
     * 
     * @param fetchingInterval
     */
    public void fetchingPolicyInterval(long fetchingInterval) {
        fetchingInterval(fetchingInterval); // Fail first.
        fetchingPolicy(FetchingPolicy.INTERVAL);
    }

    public void fetchingPolicy(FetchingPolicy fetchingPolicy) {
        this.fetchingPolicy = fetchingPolicy;
    }

    public FetchingPolicy fetchingPolicy() {
        return fetchingPolicy;
    }

    /**
     * This does not set the fetching policy to INTERVAL, use instead:
     * {@link #fetchingPolicyInterval(long)}.
     * 
     * @param fetchingInterval
     * @throws IllegalArgumentException
     *             If fetchingInterval is smaller than zero.
     */
    public void fetchingInterval(long fetchingInterval) {
        if (fetchingInterval < 0) {
            throw new IllegalArgumentException("The fetchingInterval must be greater than or equal to zero.");
        }
        this.fetchingInterval = fetchingInterval;
    }

    public long fetchingInterval() {
        return this.fetchingInterval;
    }

    private boolean hasFlag(int flag) {
        return (invalidationFlags & flag) != 0;
    }

    private void setFlag(int flag, boolean state) {
        invalidationFlags = state ? (invalidationFlags | flag) : (invalidationFlags & ~flag);
    }

    public void invalidationWorld(boolean state) {
        setFlag(INVALIDATION_WORLD, state);
    }

    public boolean invalidationWorld() {
        return hasFlag(INVALIDATION_WORLD);
    }

    public void invalidationOffline(boolean state) {
        setFlag(INVALIDATION_OFFLINE, state);
    }

    public boolean invalidationOffline() {
        return hasFlag(INVALIDATION_OFFLINE);
    }

    /**
     * Only alters this instance, if all values are correct.
     * 
     * @param input
     * @return This instance for chaining.
     * @throws IllegalArgumentException
     *             on errors.
     */
    public PermissionPolicy setPolicyFromConfigLine(final String input) {
        final List<String> split = StringUtil.getNonEmpty(StringUtil.splitChars(input.trim(), ' ', ':', ','), true);
        FetchingPolicy fetchingPolicy;
        long interval = 0;
        String item = split.get(0).toUpperCase();
        final String ref = FetchingPolicy.INTERVAL.name().toUpperCase();
        int flagIndex = 1; // Start here with flags.
        if (item.startsWith(ref)) {
            if (item.length() == ref.length()) {
                flagIndex = 2;
                if (split.size() < 2) {
                    throw new IllegalArgumentException("Must specify an interval in seconds.");
                }
                item = split.get(1).toUpperCase();
            }
            else {
                item = item.substring(ref.length()).trim();
            }
            try {
                interval = (long) (1000.0 * Double.parseDouble(item));
            }
            catch (NumberFormatException e) {
                throw new IllegalArgumentException("Must specify an interval in seconds.", e);
            }
            if (interval < 0L) {
                throw new IllegalArgumentException("Interval must be equal to or greater than zero.");
            }
            fetchingPolicy = FetchingPolicy.INTERVAL;
        }
        else {
            try {
                fetchingPolicy = FetchingPolicy.valueOf(item);
            }
            catch (Exception e) {
                throw new IllegalArgumentException(e);
            }
        }
        boolean invalidateWorld = true;
        boolean invalidateOffline = true;
        for (int i = flagIndex; i < split.size(); i++) {
            item = split.get(i).toLowerCase();
            boolean flag = true;
            if (item.charAt(0) == '+') {
                flag = true;
                item = item.substring(1);
            }
            else if (item.charAt(0) == '-') {
                flag = false;
                item = item.substring(1);
            }
            if (item.equals("world")) {
                invalidateWorld = flag;
            }
            else if (item.equals("offline")) {
                invalidateOffline = flag;
            }
            else {
                throw new IllegalArgumentException("Bad flag name: " + item);
            }
        }

        // Assign
        fetchingInterval(interval); // Interval first for most useful update.
        fetchingPolicy(fetchingPolicy);
        invalidationWorld(invalidateWorld);
        invalidationOffline(invalidateOffline);
        return this;
    }

    /**
     * Minimized config line, skipping some defaults.
     * 
     * @return
     */
    public String policyToConfigLine() {
        return policyToConfigLine(true);
    }

    /**
     * 
     * @param skipDefaults
     *            Only applies to extras like invalidation policy. The fetching
     *            policy and in case of INTERVAL the interval, are always
     *            included.
     * @return
     */
    public String policyToConfigLine(final boolean skipDefaults) {
        final StringBuilder builder = new StringBuilder(124);
        builder.append(fetchingPolicy.name());
        if (fetchingPolicy == FetchingPolicy.INTERVAL) {
            builder.append(':');
            builder.append(((double) fetchingInterval) / 1000.0);
        }
        // Only add flags set to false.
        if (!skipDefaults || !invalidationOffline()) {
            builder.append("," + (invalidationOffline() ? "+" : "-") + "offline");
        }
        if (!skipDefaults || !invalidationWorld()) {
            builder.append("," + (invalidationWorld() ? "+" : "-") + "world");
        }
        return builder.toString();
    }

    public boolean isPolicyEquivalent(final PermissionPolicy other) {
        return fetchingPolicy == other.fetchingPolicy 
                && (fetchingPolicy != FetchingPolicy.INTERVAL || fetchingInterval == other.fetchingInterval)
                // TODO: Flags: might ignore offline, in case world is set - doesn't seem right, though.
                && invalidationFlags == other.invalidationFlags;
    }

}
