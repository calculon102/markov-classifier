package de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.javafx.training;

import static java.util.Objects.requireNonNull;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Bean for the TreeTable of word-statistics.
 *
 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
 */
public final class WordStatistics implements Comparable<WordStatistics> {
	private final StringProperty word = new SimpleStringProperty("");
	private final StringProperty className = new SimpleStringProperty("");
	private final StringProperty otherWord = new SimpleStringProperty("");
	private final DoubleProperty significance = new SimpleDoubleProperty(Double.NaN);
	private final LongProperty count = new SimpleLongProperty(Long.MIN_VALUE);
	private final LongProperty classesCount = new SimpleLongProperty(Long.MIN_VALUE);;
	private final LongProperty otherWordsCount = new SimpleLongProperty(Long.MIN_VALUE);

	private WordStatistics() {
		// NOP - Use static factories.
	}

	public StringProperty wordProperty() {
		return word;
	}

	public StringProperty classNameProperty() {
		return className;
	}

	public StringProperty otherWordProperty() {
		return otherWord;
	}

	public DoubleProperty significanceProperty() {
		return significance;
	}

	public LongProperty countProperty() {
		return count;
	}

	public LongProperty classesCountProperty() {
		return classesCount;
	}

	public LongProperty otherWordsCountProperty() {
		return otherWordsCount;
	}

	@Override
	public int compareTo(final WordStatistics o) {
		final int wordCompare = word.get().compareTo(o.word.get());
		if (wordCompare != 0) {
			return wordCompare;
		}
		//
		final int classNameCompare = className.get().compareTo(o.className.get());
		if (classNameCompare != 0) {
			return classNameCompare;
		}
		//
		final int otherWordCompare = otherWord.get().compareTo(o.otherWord.get());
		if (otherWordCompare != 0) {
			return otherWordCompare;
		}
		//
		return 0;
	}

	/**
	 * @param wordName
	 * @return TreeTable-value for root-row of a word.
	 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
	 */
	public static WordStatistics asWordRow(final String wordName) {
		requireNonNull(wordName);
		//
		final WordStatistics wordStatistics = new WordStatistics();
		wordStatistics.word.set(wordName);
		return wordStatistics;
	}

	/**
	 * @param className
	 * @return TreeTable-value for a row of a class for a word.
	 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
	 */
	public static WordStatistics asClassRow(final String className) {
		requireNonNull(className);
		//
		final WordStatistics wordStatistics = new WordStatistics();
		wordStatistics.className.set(className);
		return wordStatistics;
	}

	/**
	 * 
	 * @param otherWord
	 * @param count
	 * @param significance
	 * @return TreeTable-value for another word belonging to the class of a word.
	 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
	 */
	public static WordStatistics asOtherWordRow(final String otherWord, final Long count, final Double significance) {
		requireNonNull(otherWord);
		requireNonNull(count);
		requireNonNull(significance);
		//
		final WordStatistics wordStatistics = new WordStatistics();
		wordStatistics.otherWord.set(otherWord);
		wordStatistics.count.set(count);
		wordStatistics.significance.set(significance);
		return wordStatistics;
	}
}
