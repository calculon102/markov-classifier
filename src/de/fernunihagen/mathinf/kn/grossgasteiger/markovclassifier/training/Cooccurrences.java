package de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.training;

import static de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.math.MathExt.factorial;
import static de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.math.MathExt.log2;
import static java.util.Objects.requireNonNull;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Represents a cooccurence of two word-statistics.
 *
 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
 */
final class Cooccurrences implements Serializable, CooccurrencesReader {
	/** SERIAL-ID */
	private static final long serialVersionUID = 20150828L;

	/** All known and non-unqiue cooccurrences with their significances. ° */
	private final Map<WordStatistic, Map<WordStatistic, Double>> cooccurencesByWord;
	/** All known and cooccurrences with their significances. All pairs of key-Value are unique. */
	private final Map<WordStatistic, Map<WordStatistic, Double>> uniqueCooccurencesByWord;
	/** Sum of all significant cooccurrences. */
	private double significanceCount;

	/**
	 * @param size Initial word-count for internal data-structure.
	 */
	public Cooccurrences(final int size) {
		cooccurencesByWord = new HashMap<>(size);
		uniqueCooccurencesByWord = new HashMap<>(size);
	}

	/**
	 * @see de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.training.CooccurrencesReader#coocsByWord()
	 */
	@Override
	public Map<WordStatistic, Map<WordStatistic, Double>> coocsByWord() {
		return Collections.unmodifiableMap(cooccurencesByWord);
	}

	/**
	 * @see de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.training.CooccurrencesReader#uniqueCoocsByWord()
	 */
	@Override
	public Map<WordStatistic, Map<WordStatistic, Double>> uniqueCoocsByWord() {
		return Collections.unmodifiableMap(uniqueCooccurencesByWord);
	}

	/**
	 * @see de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.training.CooccurrencesReader#significanceSum()
	 */
	@Override
	public double significanceSum() {
		return significanceCount;
	}

	/**
	 * @see de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.training.CooccurrencesReader#meanSignificance()
	 */
	@Override
	public double meanSignificance() {
		return uniqueCooccurencesByWord.isEmpty() ? 0 : significanceCount / uniqueCooccurencesByWord.size();
	}

	/**
	 * Creates a new coocurrence-instance based on given statistics.<br>
	 * Based on formulars in "Text Mining: Wissensrohstoff Text", page 139.
	 * 
	 * @param word1 The first word of this cooccurence
	 * @param word2 The second word of htis coocurrence
	 * @param countAllSentences Count of all sentences of the relevant corpus.
	 * @param cooccurrenceSignificanceThreshold
	 * @return The Cooccurrence-instance.
	 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
	 */
	public double calculateAndAdd(final WordStatistic word1, final WordStatistic word2, final long countAllSentences, final double cooccurrenceSignificanceThreshold) {
		requireNonNull(word1);
		requireNonNull(word2);
		//
		final Set<Long> occurencesWord1 = word1.getOccurencesInLineIndex();
		final Set<Long> occurencesWord2 = word2.getOccurencesInLineIndex();
		//
		final double lambda = ((double) (occurencesWord1.size() * occurencesWord2.size())) / (double) countAllSentences;

		final int commonOccurences = countIntersections(occurencesWord1, occurencesWord2);
		final double commonOccurencesDouble = commonOccurences;
		//
		//		final double relevance = (commonOccurencesDouble + 1.0d) / lambda;
		//		if (relevance < 2.5) {
		//			return 0.0d;
		//		}

		final double significance;
		if (commonOccurences > 10) {
			significance = (commonOccurencesDouble * (log2(commonOccurences) - log2(lambda) - 1.0d)) / log2(countAllSentences);
		} else {
			significance = (lambda - commonOccurencesDouble - log2(lambda) + log2(factorial(commonOccurences))) / log2(countAllSentences);
		}
		//
		if (significance <= cooccurrenceSignificanceThreshold) {
			return significance;
		}
		//
		significanceCount += significance;
		//
		Map<WordStatistic, Double> word1Cooccurrences = cooccurencesByWord.get(word1);
		if (word1Cooccurrences == null) {
			word1Cooccurrences = new HashMap<>(1);
			cooccurencesByWord.put(word1, word1Cooccurrences);
			uniqueCooccurencesByWord.put(word1, word1Cooccurrences);
		}
		word1Cooccurrences.put(word2, significance);
		//
		Map<WordStatistic, Double> word2Cooccurrences = cooccurencesByWord.get(word2);
		if (word2Cooccurrences == null) {
			word2Cooccurrences = new HashMap<>(1);
			cooccurencesByWord.put(word2, word2Cooccurrences);
		}
		word2Cooccurrences.put(word1, significance);
		//
		return significance;
	}

	@Override
	public String toString() {
		return cooccurencesByWord.size() + " cooccurrences";
	}

	/**
	 * Halfway performant way to calculate intersection-count between two sets of longs. Much more efficient than retailAll and intersection-methods in
	 * common-libs.
	 * @param set1
	 * @param set2
	 * @return count of intersections.
	 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
	 */
	public static int countIntersections(final Set<Long> set1, final Set<Long> set2) {
		Set<Long> a;
		Set<Long> b;
		if (set1.size() <= set2.size()) {
			a = set1;
			b = set2;
		} else {
			a = set2;
			b = set1;
		}
		int count = 0;
		for (final Long e : a) {
			if (b.contains(e)) {
				count++;
			}
		}
		return count;
	}
}
