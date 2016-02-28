package de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.javafx.training;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Bean for a treetable of cooccurrences.
 *
 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
 */
public class SignificantCooccurrence implements Comparable<SignificantCooccurrence> {
	private final StringProperty className = new SimpleStringProperty("");
	private final StringProperty word1 = new SimpleStringProperty("");
	private final StringProperty word2 = new SimpleStringProperty("");
	private final DoubleProperty significance = new SimpleDoubleProperty(Double.NaN);
	private final LongProperty countCoocs = new SimpleLongProperty(Long.MIN_VALUE);
	private final DoubleProperty meanSignificance = new SimpleDoubleProperty(Double.NaN);
	private final DoubleProperty sumSignificance = new SimpleDoubleProperty(Double.NaN);
	private final LongProperty countWords = new SimpleLongProperty(Long.MIN_VALUE);
	private final LongProperty countSentences = new SimpleLongProperty(Long.MIN_VALUE);

	/**
	 * @param name Internal name of th root-bean
	 * @return
	 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
	 */
	public static SignificantCooccurrence createRoot(final String name) {
		final SignificantCooccurrence result = new SignificantCooccurrence();
		result.className.set(name);
		return result;
	}

	/**
	 * @param name
	 * @param countCoocs
	 * @param meanSignificance
	 * @param countWords
	 * @param countSentences
	 * @return
	 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
	 */
	public static SignificantCooccurrence createClass(final String name, final Long countCoocs, final Double meanSignificance, final Double sumSignificance, final Long countWords, final Long countSentences) {
		final SignificantCooccurrence result = new SignificantCooccurrence();
		result.className.set(name);
		result.countCoocs.set(countCoocs);
		result.meanSignificance.set(meanSignificance);
		result.sumSignificance.set(sumSignificance);
		result.countWords.set(countWords);
		result.countSentences.set(countSentences);
		return result;
	}

	/**
	 * Creates a clone of the original with updated statistics.
	 * @param original
	 * @param countCoocs
	 * @param meanSignificance
	 * @param countWords
	 * @param countSentences
	 * @return
	 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
	 */
	public static SignificantCooccurrence createCooc(final String word1, final String word2, final Double significance) {
		final SignificantCooccurrence result = new SignificantCooccurrence();
		result.word1.set(word1);
		result.word2.set(word2);
		result.significance.set(significance);
		return result;
	}

	public StringProperty classNameProperty() {
		return className;
	}

	public StringProperty word1Property() {
		return word1;
	}

	public StringProperty word2Property() {
		return word2;
	}

	public DoubleProperty significanceProperty() {
		return significance;
	}

	public LongProperty countCoocsProperty() {
		return countCoocs;
	}

	public DoubleProperty meanSignificanceProperty() {
		return meanSignificance;
	}

	public DoubleProperty sumSignificanceProperty() {
		return sumSignificance;
	}

	public LongProperty countWordsProperty() {
		return countWords;
	}

	public LongProperty countSentencesProperty() {
		return countSentences;
	}

	@Override
	public int compareTo(final SignificantCooccurrence o) {
		final int classNameCompare = className.get().compareTo(o.className.get());
		if (classNameCompare != 0) {
			return classNameCompare;
		}

		final int word1Compare = word1.get().compareTo(o.word1.get());
		if (word1Compare != 0) {
			return word1Compare;
		}

		final int word2Compare = word2.get().compareTo(o.word2.get());
		if (word2Compare != 0) {
			return word2Compare;
		}

		return 0;
	}
}
