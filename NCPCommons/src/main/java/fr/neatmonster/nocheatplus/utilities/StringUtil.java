package fr.neatmonster.nocheatplus.utilities;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Collection;


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
	
}
