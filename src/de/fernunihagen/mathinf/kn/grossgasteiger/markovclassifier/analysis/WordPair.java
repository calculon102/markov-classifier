package de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.analysis;

import static java.util.Objects.requireNonNull;

import java.io.Serializable;
import java.util.Objects;

public class WordPair implements Serializable {
	/** SERIAL-ID */
	private static final long serialVersionUID = 20150920L;

	/** Represents a gap. */
	public static final WordPair VOID = new WordPair(null, null);

	private final String word1;
	private final String word2;

	public static WordPair of(final String word1, final String word2) {
		requireNonNull(word1);
		requireNonNull(word2);
		//
		if (word1.compareTo(word2) <= 0) {
			return new WordPair(word1, word2);
		} else {
			return new WordPair(word2, word1);
		}
	}

	private WordPair(final String word1, final String word2) {
		this.word1 = word1;
		this.word2 = word2;
	}

	@Override
	public String toString() {
		if (word1 == null && word2 == null) {
			return "VOID";
		}
		//
		return "" + word1 + " | " + word2 + "";
	}

	@Override
	public int hashCode() {
		return Objects.hash(word1, word2);
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
		final WordPair other = (WordPair) obj;

		if ((word1 == null && other.word1 != null) || (word2 == null && other.word2 != null)) {
			return false;
		}

		if ((word1.equals(other.word1) && word2.equals(other.word2))
				|| (word1.equals(other.word2) && word2.equals(other.word1))) {
			return true;
		}

		return false;
	}

}
