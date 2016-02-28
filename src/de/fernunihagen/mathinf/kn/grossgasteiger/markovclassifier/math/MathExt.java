package de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.math;

import java.util.HashMap;
import java.util.Map;

/**
 * Suprisingly even Java 8 is still missing a log_2-function and a factorial-function
 *
 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
 */
public final class MathExt {
	/** Buffer for common use-case. */
	private static final double NATURAL_LOG_2 = Math.log(2);

	private static final Map<Long, Long> FACTORIAL_BUFFER = new HashMap<>();

	public static double log2(final double a) {
		return Math.log(a) / NATURAL_LOG_2;
	}

	public static long factorial(final long n) {
		final Long result = FACTORIAL_BUFFER.get(n);
		if (result != null) {
			return result;
		}
		//
		long fact = 1;
		//
		for (long i = 1; i <= n; i++) {
			fact *= i;
		}
		//
		FACTORIAL_BUFFER.put(n, fact);
		//
		return fact;
	}
}
