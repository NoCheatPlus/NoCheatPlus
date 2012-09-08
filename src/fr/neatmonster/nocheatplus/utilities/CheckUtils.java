package fr.neatmonster.nocheatplus.utilities;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

/**
 * The Class CheckUtils.
 */
public class CheckUtils {

    /** The file logger. */
    public static Logger fileLogger = null;
    
    /** Decimal format for "#.###" */
    public static final DecimalFormat fdec3 = new DecimalFormat();
    
    static{
    	DecimalFormatSymbols sym = fdec3.getDecimalFormatSymbols();
    	sym.setDecimalSeparator('.');
    	fdec3.setDecimalFormatSymbols(sym);
    	fdec3.setMaximumFractionDigits(3);
    	fdec3.setMinimumIntegerDigits(1);
    }

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
     * Calculate the distance between two location, because for Bukkit distance is the distance squared and
     * distanceSquared is the distance non-squared.
     * 
     * @param location1
     *            the location1
     * @param location2
     *            the location2
     * @return the double
     */
    public static double distance(final Location location1, final Location location2) {
        return Math.sqrt(Math.pow(location2.getX() - location1.getX(), 2)
                + Math.pow(location2.getY() - location1.getY(), 2) + Math.pow(location2.getZ() - location1.getZ(), 2));
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
    
    /**
     * Join parts with link. 
     * @param input
     * @param link
     * @return
     */
    public static <O extends Object> String join(final Collection<O> input, final String link){
    	final StringBuilder builder = new StringBuilder(Math.max(300, input.size() * 10));
    	boolean first = true;
    	for (final Object obj : input){
    		if (!first) builder.append(link);
    		builder.append(obj.toString());
    		first = false;
    	}
    	return builder.toString();
    }
    
    /**
     * Convenience method.
     * @param parts
     * @param link
     * @return
     */
    public static <O extends Object> boolean scheduleOutputJoined(final List<O> parts, String link){
    	return scheduleOutput(join(parts, link));
    }
    
    /**
     * Schedule a message to be output by the bukkit logger.
     * @param message
     * @return If scheduled successfully.
     */
    public static boolean scheduleOutput(final String message){
    	try{
    		return Bukkit.getScheduler().scheduleSyncDelayedTask(Bukkit.getPluginManager().getPlugin("NoCheatPlus"),
    				new Runnable() {
						@Override
						public void run() {
							Bukkit.getLogger().info(message);
						}
					}) != -1;
    	}
    	catch (final Exception exc){
    		return false;
    	}
    }

    /**
     * Removes the colors of a message.
     * 
     * @param text
     *            the text
     * @return the string
     */
    public static String removeColors(String text) {
        for (final ChatColor c : ChatColor.values())
            text = text.replace("&" + c.getChar(), "");
        return text;
    }

    /**
     * Replace colors of a message.
     * 
     * @param text
     *            the text
     * @return the string
     */
    public static String replaceColors(String text) {
        for (final ChatColor c : ChatColor.values())
            text = text.replace("&" + c.getChar(), c.toString());
        return text;
    }

	public static void scheduleOutput(final Exception e) {
		final PrintWriter pw = new PrintWriter(new StringWriter(340));
		e.printStackTrace(pw);
		scheduleOutput(pw.toString());
	}
}
