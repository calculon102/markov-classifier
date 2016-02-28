package de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.training;

import static java.util.Objects.requireNonNull;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Data-class to store statistic of a word like count within a text.
 *
 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
 */
public final class WordStatistic implements Comparable<WordStatistic>, Serializable {
	/** SERIAL-ID */
	private static final long serialVersionUID = 20150912L;
	/** Logger-Instance. */
	private static final Logger LOGGER = Logger.getLogger(WordStatistic.class.getName());

	/** Name of the word. */
	private final String name;
	/** Stores all unique occurences of the word within a sentence of an indexed corups. */
	private final Set<Long> occurencesInLineIndex = new HashSet<>(1);
	/** Counts the occurence of following words and their count as value. */
	private final Map<String, Long> nextNeighbours = new HashMap<>(1);

	/** Count within a corups */
	private long count = 0;
	/** Buffered and externally calculated hashcode. */
	private int bufferedHashcode = 0;

	private WordStatistic(final String name) {
		requireNonNull(name);
		//
		this.name = name;
	}

	/**
	 * Calculates a set of Statistics for each unique word in a corpus.
	 * 
	 * @param corpus Ordered corpus of sentences, which are themselves lists of words.
	 * @return A new statistics instance.
	 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
	 */
	public static Map<String, WordStatistic> calculate(final List<List<TaggedWord>> corpus) {
		requireNonNull(corpus);
		//
		final Map<String, WordStatistic> result = new HashMap<>();
		//
		for (int line = 1; line < corpus.size(); line++) {
			final List<TaggedWord> taggedWords = corpus.get(line - 1);
			final long lineAsLong = Integer.toUnsignedLong(line);
			//
			for (int wordIndex = 0; wordIndex < taggedWords.size(); wordIndex++) {
				//
				final String word = taggedWords.get(wordIndex).getName();
				final WordStatistic statistic = result.computeIfAbsent(word, w -> new WordStatistic(w));
				statistic.count += 1;
				statistic.occurencesInLineIndex.add(lineAsLong);
				//
				final int nextIndex = wordIndex + 1;
				if (taggedWords.size() > nextIndex) {
					final String nextWord = taggedWords.get(nextIndex).getName();
					statistic.nextNeighbours.compute(nextWord, (k, v) -> v == null ? 1 : v + 1);
				}
			}
		}
		//
		result.values().forEach(s -> s.calculateHashCode());
		//
		LOGGER.info("Calculated statistics for " + result.size() + " words.");
		//
		return result;
	}

	/**
	 * @return Name of the word.
	 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return Count of word in corpus used in initialization.
	 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
	 */
	public long getCount() {
		return count;
	}

	/**
	 * @return Indexes of all sentences which contained this word in the given corpus. Modifiable for performance-reasons. Beware!
	 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
	 */
	public Set<Long> getOccurencesInLineIndex() {
		return occurencesInLineIndex;
	}

	/**
	 * @return Counts the occurence of following words and their count as value.
	 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
	 */
	public Map<String, Long> getNextNeighbours() {
		return nextNeighbours;
	}

	@Override
	public int hashCode() {
		return bufferedHashcode;
	}

	private void calculateHashCode() {
		final int prime = 31;
		final int result = 1;
		bufferedHashcode = prime * result + ((name == null) ? 0 : name.hashCode());
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
		final WordStatistic other = (WordStatistic) obj;
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return name + " (n=" + count + ")";
	}

	@Override
	public int compareTo(final WordStatistic o) {
		return name.compareTo(o.name);
	}
}
