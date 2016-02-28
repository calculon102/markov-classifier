package de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.training;

import java.util.Map;

/**
 * Read-only access to existing cooc-statistics.
 *
 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
 */
public interface CooccurrencesReader {

	/**
	 * @return All detected significant cooccurrences by word.
	 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
	 */
	Map<WordStatistic, Map<WordStatistic, Double>> coocsByWord();

	/**
	 * Nearly the same as {@link #coocsByWord()}, but this time all pairs of WordStatistics are unique. So all WordStatistic-Key-Value pairs are
	 * checked and the corresponding pair is omitted.
	 * @return
	 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
	 */
	Map<WordStatistic, Map<WordStatistic, Double>> uniqueCoocsByWord();

	/**
	 * @return Sum of all significant cooccurrences.
	 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
	 */
	double significanceSum();

	/**
	 * @return Mean-value of significance of known cooccurrences.
	 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
	 */
	double meanSignificance();

}