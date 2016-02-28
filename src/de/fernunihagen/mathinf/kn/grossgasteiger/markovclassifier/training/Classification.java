package de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.training;

import static java.util.Objects.requireNonNull;

import java.io.File;
import java.io.Serializable;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.common.ProgressMonitor;

/**
 * Represents the classification of text-corpus. Contains the whole text-corpus and methods to do explicit analyzation of cooccurences.
 * 
 * Is identified only by its classname(!), as do hashcode and equals!!!
 * 
 * Assumes the uniqueness of a text by its file. So this is completely file-based.
 *
 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
 */
final class Classification implements Serializable {
	/** SERIAL-ID */
	private static final long serialVersionUID = 20150814L;
	/** Logger-Instance */
	private static final Logger LOGGER = Logger.getLogger(Classification.class.getName());

	/** The unique name of this classification. */
	private final String name;
	/** Contains all given files with their (externally normalized) contents. In form of ordered sentences with their ordered words. */
	private final Map<File, List<List<TaggedWord>>> sentencesByFile = new LinkedHashMap<>();
	/** Hold the simple count-statistics per word. */
	private final Map<String, WordStatistic> statisticsByWord = new HashMap<>();

	/** Holds all known cooccurecnes. First time initialization in {@link #searchSiginficantCoocs(double, ProgressMonitor)}. */
	private Cooccurrences cooccurrences;

	/**
	 * Creations an emppty classification with given name.
	 * 
	 * @param name
	 */
	public Classification(final String name) {
		requireNonNull(name);
		//
		this.name = name;
	}

	/**
	 * @return The name of this classification.
	 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
	 */
	public String getName() {
		return name;
	}

	/**
	 * Is a file with given path already imported.
	 * 
	 * @param file Unique path of file.
	 * @return <code>true</code>, if it was formerly imported.
	 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
	 */
	public boolean hasFile(final Path file) {
		requireNonNull(file);
		//
		return sentencesByFile.containsKey(file.toFile());
	}

	/**
	 * Adds the given file with its content.
	 * 
	 * @param path Unique path of file.
	 * @param baseformSentences Content of file in form of ordered sentences.
	 * @return <code>true</code> if added. <code>false</code> if not, i.e. if already added before.
	 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
	 */
	public boolean addFile(final Path path, final List<List<TaggedWord>> baseformSentences) {
		requireNonNull(path);
		requireNonNull(baseformSentences);
		//
		if (hasFile(path)) {
			return false;
		}
		//
		sentencesByFile.put(path.toFile(), baseformSentences);
		//
		return true;
	}

	/**
	 * Searches for significant cooccurences within the previously add files and their contents.
	 * 
	 * @param cooccurrenceSignificanceThreshold
	 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
	 * @param progressMonitor
	 */
	public void searchSiginficantCoocs(final double cooccurrenceSignificanceThreshold, final ProgressMonitor progressMonitor) {
		//
		// Bring all sentences into one big list
		final List<List<TaggedWord>> corpus = new ArrayList<>();
		sentencesByFile.values().forEach(sentences -> corpus.addAll(sentences));
		//
		// Gather statistics per word
		statisticsByWord.putAll(WordStatistic.calculate(corpus));
		//
		// Calculate cooccurrences
		final List<WordStatistic> wordStatistics = statisticsByWord.values().stream().sorted().collect(Collectors.toList());
		final List<WordStatistic> wordStatisticsToCompare = new ArrayList<>(wordStatistics);

		cooccurrences = new Cooccurrences(wordStatistics.size());

		final int courpusSize = corpus.size();
		final int maxProgress = wordStatistics.size() * 2;
		int count = 0;
		//
		final ResourceBundle labels = progressMonitor.labels();
		final String guiMsg = labels.getString("training.import.progress.analyzeWord");

		for (int i = 0; i < wordStatistics.size(); i++) {
			final WordStatistic word1 = wordStatistics.get(i);
			wordStatisticsToCompare.remove(word1);
			//
			for (int j = 0; j < wordStatisticsToCompare.size(); j++) {
				final WordStatistic word2 = wordStatisticsToCompare.get(j);
				final double significance = cooccurrences.calculateAndAdd(word1, word2, courpusSize, cooccurrenceSignificanceThreshold);
				if (significance > cooccurrenceSignificanceThreshold && LOGGER.isLoggable(Level.FINE)) {
					LOGGER.log(Level.FINE, "Significant cooccurrence in {0}: {1}, {2}, {3}", new Object[] { name, word1, word2, significance });
				}
			}
			//
			count += 1;
			//
			if (count % 10 == 0) {
				final String message = MessageFormat.format(guiMsg, count, wordStatistics.size(), word1);
				progressMonitor.update(message);
				progressMonitor.update(wordStatistics.size() + count, maxProgress);
			}
		}
	}

	/**
	 * @return All detected significant cooccurrences by word.
	 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
	 */
	public Map<WordStatistic, Map<WordStatistic, Double>> getCooccurencesByWord() {
		return cooccurrences.coocsByWord();
	}

	/**
	 * @return Access to cooccurrence statisticts.
	 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
	 */
	public CooccurrencesReader getCooccurrences() {
		return cooccurrences;
	}

	/**
	 * @return The full count of all words in this classification. Is not buffered!
	 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
	 */
	public long getWordCount() {
		long result = 0;
		for (final List<List<TaggedWord>> sentenceOfFile : sentencesByFile.values()) {
			for (final List<TaggedWord> wordsOfSentence : sentenceOfFile) {
				result += wordsOfSentence.size();
			}
		}
		return result;
	}

	/**
	 * @return The full count of sentences in this classficiation. Is not buffered!
	 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
	 */
	public long getSentenceCount() {
		long result = 0;
		for (final List<List<TaggedWord>> sentenceOfFile : sentencesByFile.values()) {
			result += sentenceOfFile.size();
		}
		return result;
	}

	/**
	 * @return Statistics by word known in this classification
	 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
	 */
	public Map<String, WordStatistic> getStatisticsByWord() {
		return statisticsByWord;
	}

	@Override
	public int hashCode() {
		return name.hashCode();
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
		final Classification other = (Classification) obj;
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		return true;
	}
}
