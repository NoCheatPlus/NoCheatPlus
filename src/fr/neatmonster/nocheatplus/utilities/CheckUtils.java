package fr.neatmonster.nocheatplus.utilities;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

/**
 * The Class CheckUtils.
 */
public class CheckUtils {

    /**
     * Check if a player looks at a target of a specific size, with a specific precision value (roughly).
     * 
     * @param player
     *            the player
     * @param targetX
     *            the target x
     * @param targetY
     *            the target y
     * @param targetZ
     *            the target z
     * @param targetWidth
     *            the target width
     * @param targetHeight
     *            the target height
     * @param precision
     *            the precision
     * @return the double
     */
    public static double directionCheck(final Player player, final double targetX, final double targetY,
            final double targetZ, final double targetWidth, final double targetHeight, final double precision) {

        // Get the eye location of the player.
        final Location eyes = player.getEyeLocation();

        final double factor = Math.sqrt(Math.pow(eyes.getX() - targetX, 2) + Math.pow(eyes.getY() - targetY, 2)
                + Math.pow(eyes.getZ() - targetZ, 2));

        // Get the view direction of the player.
        final Vector direction = eyes.getDirection();

        final double x = targetX - eyes.getX();
        final double y = targetY - eyes.getY();
        final double z = targetZ - eyes.getZ();

        final double xPrediction = factor * direction.getX();
        final double yPrediction = factor * direction.getY();
        final double zPrediction = factor * direction.getZ();

        double off = 0.0D;

        off += Math.max(Math.abs(x - xPrediction) - (targetWidth / 2 + precision), 0.0D);
        off += Math.max(Math.abs(z - zPrediction) - (targetWidth / 2 + precision), 0.0D);
        off += Math.max(Math.abs(y - yPrediction) - (targetHeight / 2 + precision), 0.0D);

        if (off > 1)
            off = Math.sqrt(off);

        return off;
    }

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
