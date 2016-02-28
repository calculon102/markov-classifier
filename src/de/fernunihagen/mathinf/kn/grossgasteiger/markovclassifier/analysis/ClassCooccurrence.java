package de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.analysis;

import static java.util.Objects.requireNonNull;

import java.io.Serializable;
import java.util.Objects;

/**
 * An emission is a single cooccurrence and its recognized class.
 *
 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
 */
final class ClassCooccurrence implements Serializable {
	/** SERIAL-ID */
	private static final long serialVersionUID = 20150920L;

	/** Special emission, representing a "gap". */
	public static final ClassCooccurrence VOID = new ClassCooccurrence(null, "", -1);

	public static ClassCooccurrence of(final WordPair cooc, final String className, final double significance) {
		requireNonNull(cooc);
		requireNonNull(className);
		//
		if (significance < 0.0) {
			throw new IllegalArgumentException("Significane-value must be zero or positive!");
		}
		//
		if (className.isEmpty()) {
			throw new IllegalArgumentException("No argument must be empty!");
		}
		//
		return new ClassCooccurrence(cooc, className, significance);
	}

	private final WordPair cooc;
	private final String className;
	private final double significance;

	private ClassCooccurrence(final WordPair cooc, final String className, final double significance) {
		this.cooc = cooc;
		this.className = className;
		this.significance = significance;
	}

	public WordPair getCooc() {
		return cooc;
	}

	public String getClassName() {
		return className;
	}

	public double getSignificance() {
		return significance;
	}

	public boolean isVoid() {
		return this == VOID;
	}

	@Override
	public int hashCode() {
		return Objects.hash(cooc, className); // There must not be two of the same cooc and class in a set. Despite significance.
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final ClassCooccurrence other = (ClassCooccurrence) obj;
		if (className == null) {
			if (other.className != null) {
				return false;
			}
		} else if (!className.equals(other.className)) {
			return false;
		}
		if (cooc == null) {
			if (other.cooc != null) {
				return false;
			}
		} else if (!cooc.equals(other.cooc)) {
			return false;
		}
		if (Double.doubleToLongBits(significance) != Double.doubleToLongBits(other.significance)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return className + "(" + cooc + "): " + significance;
	}
}
