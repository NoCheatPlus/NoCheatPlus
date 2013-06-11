package fr.neatmonster.nocheatplus.utilities;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;


/**
 * String utility methods (working with or returning strings).
 * @author mc_dev
 *
 */
public class StringUtil {
	
	/** Decimal format for "#.###" */
	public static final DecimalFormat fdec3 = new DecimalFormat();
	/** Decimal format for "#.#" */
	public static final DecimalFormat fdec1 = new DecimalFormat();
	
	static {
		// 3 digits.
		DecimalFormatSymbols sym = fdec3.getDecimalFormatSymbols();
		sym.setDecimalSeparator('.');
		fdec3.setDecimalFormatSymbols(sym);
		fdec3.setMaximumFractionDigits(3);
		fdec3.setMinimumIntegerDigits(1);
		// 1 digit.
		sym = fdec1.getDecimalFormatSymbols();
		sym.setDecimalSeparator('.');
		fdec1.setDecimalFormatSymbols(sym);
		fdec1.setMaximumFractionDigits(1);
		fdec1.setMinimumIntegerDigits(1);
	}

	/**
	 * Join parts with link.
	 * 
	 * @param input
	 * @param link
	 * @return
	 */
	public static <O extends Object> String join(final Collection<O> input, final String link)
	{
		final StringBuilder builder = new StringBuilder(Math.max(300, input.size() * 10));
		boolean first = true;
		for (final Object obj : input) {
			if (!first) builder.append(link);
			builder.append(obj.toString());
			first = false;
		}
		return builder.toString();
	}
	
	/**
	 * Split input by all characters given (convenience method).
	 * @param input
	 * @param chars
	 * @return
	 */
	public static List<String> split(String input, Character... chars){
		List<String> out = new LinkedList<String>();
		out.add(input);
		List<String> queue = new LinkedList<String>();
		for (final char c : chars){
			String hex = Integer.toHexString((int) c);
			switch (hex.length()){
			case 1:
				hex = "000" + hex;
				break;
			case 2:
				hex = "00" + hex;
				break;
			case 3:
				hex = "0" + hex;
			}
			for (final String s : out){
				final String[] split = s.split("\\u" + hex);
				for (final String _s : split){
					queue.add(_s);
				}
			}
			List<String> temp = out;
			out = queue;
			queue = temp;
			queue.clear();
		}
		return out;
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
	public static boolean isSimilar(final String s, final String t, final float threshold)
	{
		return 1.0f - (float) levenshteinDistance(s, t) / Math.max(1.0, Math.max(s.length(), t.length())) > threshold;
	}

	/**
	 * Find the Levenshtein distance between two Strings.
	 * 
	 * This is the number of changes needed to change one String into another,
	 * where each change is a single character modification (deletion, insertion or substitution).
	 * 
	 * @param s
	 *            the first String, must not be null
	 * @param t
	 *            the second String, must not be null
	 * @return result distance
	 */
	public static int levenshteinDistance(CharSequence s, CharSequence t) {
		if (s == null || t == null) throw new IllegalArgumentException("Strings must not be null");
	
		int n = s.length();
		int m = t.length();
	
		if (n == 0) return m;
		else if (m == 0) return n;
	
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
