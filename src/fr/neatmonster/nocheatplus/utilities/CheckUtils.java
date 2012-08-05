package fr.neatmonster.nocheatplus.utilities;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

/**
 * The Class CheckUtils.
 */
public class CheckUtils {

    /**
     * Calculates the distance between the player and the intersection of the player's line of sight with the targeted
     * block.
     * 
     * @param player
     *            the player
     * @param location
     *            the location
     * @return the double
     */
    public static double distance(final Player player, final Location location) {
        final Location eyes = player.getEyeLocation();
        final Vector directionUnit = eyes.getDirection().normalize();
        final double xMin = (location.getX() - eyes.getX()) / directionUnit.getX();
        final double xMax = (location.getX() + 1D - eyes.getX()) / directionUnit.getX();
        final double yMin = (location.getY() - eyes.getY()) / directionUnit.getY();
        final double yMax = (location.getY() + 1D - eyes.getY()) * directionUnit.getY();
        final double zMin = (location.getZ() - eyes.getZ()) / directionUnit.getZ();
        final double zMax = (location.getZ() + 1D - eyes.getZ()) / directionUnit.getZ();
        final double min = Math.max(Math.max(Math.min(xMin, xMax), Math.min(yMin, yMax)), Math.min(zMin, zMax));
        final double max = Math.min(Math.min(Math.max(xMin, xMax), Math.max(yMin, yMax)), Math.max(zMin, zMax));
        if (max < 0D || min > max)
            return max;
        else
            return min;
    }

    /**
     * Check if the location is in the line of sight of the player.
     * 
     * @param player
     *            the player
     * @param minimum
     *            the minimum location
     * @param maximum
     *            the maximum location
     * @param offset
     *            the offset
     * @return true, if successful
     */
    public static boolean intersects(final Player player, final Location minimum, final Location maximum,
            final double offset) {
        final double x1 = minimum.getX() - offset;
        final double y1 = minimum.getY() - offset;
        final double z1 = minimum.getZ() - offset;
        final double xH = maximum.getX() + offset;
        final double yH = maximum.getY() + offset;
        final double zH = maximum.getZ() + offset;
        final double x0 = player.getEyeLocation().getX();
        final double y0 = player.getEyeLocation().getY();
        final double z0 = player.getEyeLocation().getZ();
        final double xD = player.getEyeLocation().getDirection().getX();
        final double yD = player.getEyeLocation().getDirection().getY();
        final double zD = player.getEyeLocation().getDirection().getZ();
        double tNear = Double.NEGATIVE_INFINITY;
        double tFar = Double.POSITIVE_INFINITY;
        if (xD == 0D) {
            if (x0 < x1 || x0 > xH)
                return false;
        } else {
            double t1 = (x1 - x0) / xD;
            double t2 = (xH - x0) / xD;
            if (t1 > t2) {
                final double tTemp = t1;
                t1 = t2;
                t2 = tTemp;
            }
            if (t1 > tNear)
                tNear = t1;
            if (t2 < tFar)
                tFar = t2;
            if (tNear > tFar)
                return false;
            if (tFar < 0D)
                return false;
        }
        if (yD == 0D) {
            if (y0 < y1 || y0 > yH)
                return false;
        } else {
            double t1 = (y1 - y0) / yD;
            double t2 = (yH - y0) / yD;
            if (t1 > t2) {
                final double tTemp = t1;
                t1 = t2;
                t2 = tTemp;
            }
            if (t1 > tNear)
                tNear = t1;
            if (t2 < tFar)
                tFar = t2;
            if (tNear > tFar)
                return false;
            if (tFar < 0D)
                return false;
        }
        if (zD == 0D) {
            if (z0 < z1 || z0 > zH)
                return false;
        } else {
            double t1 = (z1 - z0) / zD;
            double t2 = (zH - z0) / zD;
            if (t1 > t2) {
                final double tTemp = t1;
                t1 = t2;
                t2 = tTemp;
            }
            if (t1 > tNear)
                tNear = t1;
            if (t2 < tFar)
                tFar = t2;
            if (tNear > tFar)
                return false;
            if (tFar < 0D)
                return false;
        }
        return true;
    }

    /**
     * Return if the two Strings are similar based on the given threshold.
     * 
     * @param s
     *            the first String, must not be null
     * @param t
     *            the second String, must not be null
     * @param threshold
     *            the minimum value of the correlation coefficient
     * @return result true if the two Strings are similar, false otherwise
     */
    public static boolean isSimilar(final String s, final String t, final float threshold) {
        return 1.0f - (float) levenshteinDistance(s, t) / Math.max(s.length(), t.length()) > threshold;
    }

    /**
     * Find the Levenshtein distance between two Strings.
     * 
     * This is the number of changes needed to change one String into another, where each change is a single character
     * modification (deletion, insertion or substitution).
     * 
     * @param s
     *            the first String, must not be null
     * @param t
     *            the second String, must not be null
     * @return result distance
     * @throws IllegalArgumentException
     *             if either String input is null
     */
    private static int levenshteinDistance(CharSequence s, CharSequence t) {
        if (s == null || t == null)
            throw new IllegalArgumentException("Strings must not be null");

        int n = s.length();
        int m = t.length();

        if (n == 0)
            return m;
        else if (m == 0)
            return n;

        if (n > m) {
            final CharSequence tmp = s;
            s = t;
            t = tmp;
            n = m;
            m = t.length();
        }

        int p[] = new int[n + 1];
        int d[] = new int[n + 1];
        int _d[];

        int i;
        int j;

        char t_j;

        int cost;

        for (i = 0; i <= n; i++)
            p[i] = i;

        for (j = 1; j <= m; j++) {
            t_j = t.charAt(j - 1);
            d[0] = j;

            for (i = 1; i <= n; i++) {
                cost = s.charAt(i - 1) == t_j ? 0 : 1;
                d[i] = Math.min(Math.min(d[i - 1] + 1, p[i] + 1), p[i - 1] + cost);
            }

            _d = p;
            p = d;
            d = _d;
        }

        return p[n];
    }
}
