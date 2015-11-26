package fr.neatmonster.nocheatplus.compat.versions;

/**
 * Some generic version parsing and comparison utility methods.
 * @author asofold
 *
 */
public class GenericVersion {

    /**
     * The unknown version, meant to work with '=='. All internal version
     * parsing will return this for unknown versions.
     */
    public static final String UNKNOWN_VERSION = "unknown";

    /**
     * Access method meant to stay, test vs. UNKNOWN_VERSION with equals.
     * 
     * @param version
     * @return
     */
    public static boolean isVersionUnknown(String version) {
        return UNKNOWN_VERSION.equals(version);
    }

    /**
     * Split a version tag consisting of integers and dots between the numbers
     * into an int array. Not fail safe, may throw NumberFormatException.
     * 
     * @param version
     * @return
     */
    public static int[] versionToInt(String version) {
        String[] split = version.split("\\.");
        int[] num = new int[split.length];
        for (int i = 0; i < split.length; i++) {
            num[i] = Integer.parseInt(split[i]);
        }
        return num;
    }

    /**
     * Simple x.y.z versions. Returns 0 on equality, -1 if version 1 is smaller
     * than version 2, 1 if version 1 is greater than version 2.
     * 
     * @param version1
     *            Can be unknown.
     * @param version2
     *            Must not be unknown.
     * @return
     */
    public static int compareVersions(String version1, String version2) {
        if (version2.equals(UNKNOWN_VERSION)) {
            throw new IllegalArgumentException("version2 must not be 'unknown'.");
        } else if (version1.equals(UNKNOWN_VERSION)) {
            // Assume smaller than any given version.
            return -1;
        }
        if (version1.equals(version2)) {
            return 0;
        }
        try {
            int[] v1Int = versionToInt(version1);
            int[] v2Int = versionToInt(version2);
            for (int i = 0; i < Math.min(v1Int.length, v2Int.length); i++) {
                if (v1Int[i] < v2Int[i]) {
                    return -1;
                } else if (v1Int[i] > v2Int[i]) {
                    return 1;
                }
            }
            // Support sub-sub-sub-...-....-marines.
            if (v1Int.length < v2Int.length) {
                return -1;
            }
            else if (v1Int.length > v2Int.length) {
                return 1;
            }
        } catch (NumberFormatException e) {}

        // Equality was tested above, so it would seem.
        throw new IllegalArgumentException("Bad version input.");
    }

    /**
     * Exact case, no trim.
     * 
     * @param input
     * @param prefix
     * @param suffix
     * @return
     */
    protected static String parseVersionDelimiters(String input, String prefix, String suffix) {
        int preIndex = prefix.isEmpty() ? 0 : input.indexOf(prefix);
        if (preIndex != -1) {
            String candidate = input.substring(preIndex + prefix.length());
            int postIndex = suffix.isEmpty() ? candidate.length() : candidate.indexOf(suffix);
            if (postIndex != -1) {
                return GenericVersion.collectVersion(candidate.substring(0, postIndex).trim(), 0);
            }
        }
        return null;
    }

    /**
     * Collect a version of the type X.Y.Z with X, Y, Z being numbers. Demands
     * at least one number, but allows an arbitrary amount of sections X....Y.
     * Rigid character check, probably not fit for snapshots.
     * 
     * @param input
     * @param beginIndex
     * @return null if not successful.
     */
    protected static String collectVersion(String input, int beginIndex) {
        StringBuilder buffer = new StringBuilder(128);
        // Rigid scan by character.
        boolean numberFound = false;
        char[] chars = input.toCharArray();
        for (int i = beginIndex; i < input.length(); i++) {
            char c = chars[i];
            if (c == '.') {
                if (numberFound) {
                    // Reset, expecting a number to follow.
                    numberFound = false;
                } else {
                    //  Failure.
                    return null;
                }
            } else if (!Character.isDigit(c)) {
                // Failure.
                return null;
            } else {
                numberFound = true;
            }
            buffer.append(c);
        }
        if (numberFound) {
            return buffer.toString();
        } else {
            return null;
        }
    }

}
