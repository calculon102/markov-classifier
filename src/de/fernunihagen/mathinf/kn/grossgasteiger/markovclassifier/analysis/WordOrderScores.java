package de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.analysis;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.training.WordStatistic;

/**
 * Analysis of the word-order of given text-corpus based on given trainings-data, represented by a score-value. The score for a class is the sum of the
 * occurrence-count of each word-order multplied by the word-order within the class.
 *
 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
 */
final class WordOrderScores {

	private final NormalizedCorpus corpus;
	private final TrainedWordNeighbours trainedWordNeighbours;

	/**
	 * Analysis of the word-order of given text-corpus based on given trainings-data, represented by a score-value. The score for a class is the sum of the
	 * occurrence-count of each word-order multplied by the word-order within the class.
	 * @param corpus
	 * @param wordOrderAnalysisParameter
	 */
	public WordOrderScores(final NormalizedCorpus corpus, final TrainedWordNeighbours wordOrderAnalysisParameter) {
		this.corpus = corpus;
		this.trainedWordNeighbours = wordOrderAnalysisParameter;
	}

	/**
	 * @return The uncached(!) score of each class of the word-order-analysis.
	 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
	 */
	public Map<String, Long> map() {
		final Map<String, Map<String, Integer>> observedWordOrders = oberserveWordOrder();
		final Map<String, Long> classScores = computeClassScores(observedWordOrders);

		return classScores;
	}

	/**
	 * @return Map of Words with all their dircet neighbours as keys with and the occurerences of the neighbourship.
	 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
	 */
	private Map<String, Map<String, Integer>> oberserveWordOrder() {
		requireNonNull(corpus);

		final List<String> mergedWordList = new ArrayList<>();
		corpus.sentences().forEach(l -> l.forEach(w -> mergedWordList.add(w.getName())));

		final Map<String, Map<String, Integer>> observedWordOrders = new HashMap<>();
		for (int i = 0; i < mergedWordList.size(); i++) {
			if (i + 1 == mergedWordList.size()) {
				break;
			}

			final String word = mergedWordList.get(i);
			final String nextWord = mergedWordList.get(i + 1);

			Map<String, Integer> nextWordCounts = observedWordOrders.get(word);
			if (nextWordCounts == null) {
				nextWordCounts = new HashMap<>(1);
				observedWordOrders.put(word, nextWordCounts);
			}

			nextWordCounts.compute(nextWord, (k, v) -> v == null ? 1 : v + 1);
		}

		return observedWordOrders;
	}

	/**
	 * Calculates the score per class basaed on the given word-order-occurrences.
	 * @param observedWordOrders Observed word-orders and their occurrences.
	 * @return Score by className.
	 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
	 */
	private Map<String, Long> computeClassScores(final Map<String, Map<String, Integer>> observedWordOrders) {
		requireNonNull(trainedWordNeighbours);

		final Set<String> classes = trainedWordNeighbours.classes();
		final Map<String, Long> classScores = new HashMap<>(classes.size());
		classes.forEach(c -> classScores.put(c, 0l));

		for (final String className : classes) {
			final Map<String, WordStatistic> statisticsOfClass = trainedWordNeighbours.getStatisticsOfClass(className);
			final Set<Entry<String, WordStatistic>> entrySet = statisticsOfClass.entrySet();

			for (final Entry<String, WordStatistic> entry : entrySet) {
				final String word1 = entry.getKey();
				final Set<Entry<String, Long>> nextNeighbours = entry.getValue().getNextNeighbours().entrySet();

				for (final Entry<String, Long> nextNeighbour : nextNeighbours) {
					final String word2 = nextNeighbour.getKey();
					final long score = nextNeighbour.getValue() * observedWordOrders.getOrDefault(word1, Collections.emptyMap()).getOrDefault(word2, 0);

					if (score > 0) {
						classScores.compute(className, (k, v) -> v + score);
					}
				}
			}
		}

		return classScores;
	}
}
