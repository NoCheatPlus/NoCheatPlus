package fr.neatmonster.nocheatplus.compat.versions;

/**
 * Static utility that stores the Minecraft version, providing some parsing methods.
 * This has to be initialized from an external source (e.g. a plugin calling BukkitVersion.init).
 * <hr/>
 * Taken from the TrustCore plugin.
 * @author mc_dev
 *
 */
public class ServerVersion {

    public static final String UNKNOWN_VERSION = "unknown";

    private static String minecraftVersion = UNKNOWN_VERSION; 

    private static final String[][] versionTagsMatch = {
        {"1.7.2-r", "1.7.2"}, // Example. Probably this method will just be removed.
    };

    /**
     * Attempt to return the Minecraft version for a given server version string.
     * @param serverVersion As returned by Bukkit.getServer().getVersion();
     * @return null if not known/parsable.
     */
    public static String parseMinecraftVersion(String... versionCandidates) {
        for (String serverVersion : versionCandidates) {
            serverVersion = serverVersion.trim();
            for (String minecraftVersion : new String[]{
                    collectVersion(serverVersion, 0),
                    parseMinecraftVersionGeneric(serverVersion),
                    parseMinecraftVersionTokens(serverVersion)
            }) {
                // Validate if not null, might mean double validation.
                if (minecraftVersion != null && validateMinecraftVersion(minecraftVersion)) {
                    return minecraftVersion;
                } 
            }
        }
        return null;
    }

    /**
     * Simple consistency check.
     * @param minecraftVersion
     * @return
     */
    private static boolean validateMinecraftVersion(String minecraftVersion) {
        return collectVersion(minecraftVersion, 0) != null;
    }

    /**
     * Match directly versus hard coded examples. Not for direct use.
     * @param serverVersion
     * @return
     */
    private static String parseMinecraftVersionTokens(String serverVersion) {
        serverVersion = serverVersion.trim().toLowerCase();
        for (String[] entry : versionTagsMatch) {
            if (serverVersion.contains(entry[0])) {
                return entry[1];
            }
        }
        return null;
    }

    /**
     * Collect a version of the type X.Y.Z with X, Y, Z being numbers. Demands at least one number, 
     * but allows an arbitrary amount of sections X....Y. Rigid character check, probably not fit for snapshots.
     * @param input
     * @param beginIndex
     * @return null if not successful.
     */
    private static String collectVersion(String input, int beginIndex) {
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

    /**
     * 
     * @param serverVersion
     * @return
     */
    private static String parseMinecraftVersionGeneric(String serverVersion) {
        String lcServerVersion = serverVersion.trim().toLowerCase();
        for (String candidate : new String[] {
                parseVersionDelimiters(lcServerVersion, "(mc:", ")"),
                parseVersionDelimiters(lcServerVersion, "git-bukkit-", "-r"),
                parseVersionDelimiters(lcServerVersion, "", "-r"),
                // TODO: Other server mods + custom builds !?.
        }) {
            if (candidate != null) {
                return candidate;
            }
        }
        return null;
    }

    /**
     * Exact case, no trim.
     * @param input
     * @param prefix
     * @param suffix
     * @return
     */
    private static String parseVersionDelimiters(String input, String prefix, String suffix) {
        int preIndex = prefix.isEmpty() ? 0 : input.indexOf(prefix);
        if (preIndex != -1) {
            String candidate = input.substring(preIndex + prefix.length());
            int postIndex = suffix.isEmpty() ? candidate.length() : candidate.indexOf(suffix);
            if (postIndex != -1) {
                return collectVersion(candidate.substring(0, postIndex).trim(), 0);
            }
        }
        return null;
    }

    /**
     * Sets lower-case Minecraft version - or UNKNOWN_VERSION, if null or empty.
     * @param version Can be null (resulting in UNKNOWN_VERSION).
     * @return The String that minecraftVersion has been set to.
     */
    public static String setMinecraftVersion(String version) {
        if (version == null) {
            minecraftVersion = UNKNOWN_VERSION;
        } else {
            version = version.trim().toLowerCase();
            if (version.isEmpty()) {
                minecraftVersion = UNKNOWN_VERSION;
            } else {
                minecraftVersion = version;
            }
        }
        return minecraftVersion;
    }

    /**
     * 
     * @return Some version string, or UNKNOWN_VERSION.
     */
    public static String getMinecraftVersion() {
        return minecraftVersion;
    }

    /**
     * Simple x.y.z versions. Returns 0 on equality, -1 if version 1 is smaller than version 2, 1 if version 1 is greater than version 2.
     * @param version1 Can be unknown.
     * @param version2 Must not be unknown.
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

    public static int[] versionToInt(String version) {
        String[] split = version.split("\\.");
        int[] num = new int[split.length];
        for (int i = 0; i < split.length; i++) {
            num[i] = Integer.parseInt(split[i]);
        }
        return num;
    }

    public static <V> V select(final String cmpVersion, final V valueLT, final V valueEQ, final V valueGT, final V valueUnknown) {
        final String mcVersion = ServerVersion.getMinecraftVersion();
        if (mcVersion == ServerVersion.UNKNOWN_VERSION) {
            return valueUnknown;
        } else {
            final int cmp = ServerVersion.compareVersions(mcVersion, cmpVersion);
            if (cmp == 0) {
                return valueEQ;
            } else if (cmp < 0) {
                return valueLT;
            } else {
                return valueGT;
            }
        }
    }

}
