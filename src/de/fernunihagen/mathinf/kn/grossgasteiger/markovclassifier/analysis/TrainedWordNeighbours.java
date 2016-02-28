package de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.analysis;

import static java.util.Objects.requireNonNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.training.TrainingSession;
import de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.training.WordStatistic;

/**
 * Parameter-object for analysis of word-orders based on training-data.
 *
 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
 */
public final class TrainedWordNeighbours {
	/** Statistics of words in a specific corpus. */
	private final Map<String, Map<String, WordStatistic>> statisticsByWordOfClass;
	/** et of names of all known classed of this training data. */
	private final Set<String> classes;

	private TrainedWordNeighbours(final Map<String, Map<String, WordStatistic>> statisticsByWordOfClass, final Set<String> classes) {
		this.statisticsByWordOfClass = statisticsByWordOfClass;
		this.classes = classes;
	}

	/**
	 * @param className Name of the class to fetch word-statistics of.
	 * @return Word-statistics of given class.
	 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
	 */
	public Map<String, WordStatistic> getStatisticsOfClass(final String className) {
		return statisticsByWordOfClass.getOrDefault(className, Collections.emptyMap());
	}

	/**
	 * @return Set of names of all known classed of this training data.
	 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
	 */
	public Set<String> classes() {
		return Collections.unmodifiableSet(classes);
	}

	/**
	 * Factory-Method.
	 * @param trainingSession Training-sessions with class-data.
	 * @return New AnalysisParameter for WordOrder-Analysis.
	 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
	 */
	public static TrainedWordNeighbours ofTrainingSession(final TrainingSession trainingSession) {
		requireNonNull(trainingSession);

		final Set<String> classes = trainingSession.getClasses();
		final Map<String, Map<String, WordStatistic>> statistics = new HashMap<>(classes.size());

		for (final String className : classes) {
			statistics.put(className, trainingSession.getStatisticsByWord(className));
		}

		return new TrainedWordNeighbours(statistics, classes);
	}
}
