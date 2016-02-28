package de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.training;

import static java.util.Objects.requireNonNull;

import java.io.File;
import java.io.Serializable;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.common.ProgressMonitor;
import de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.textprocessing.BaseformConverter;
import de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.textprocessing.CorpusFile;
import de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.textprocessing.StopWordRemover;
import de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.textprocessing.ViterbiTagger;

/**
 * Respresents the main entry-point to the a markov-classification Session.
 *
 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
 */
public final class TrainingSession implements Serializable {
	/** SERIAL-ID */
	private static final long serialVersionUID = 20151101L;
	/** Logger-Instance */
	private static final Logger LOGGER = Logger.getLogger(TrainingSession.class.getName());

	/** Known text-classifications. */
	private final Map<String, Classification> classifications = new HashMap<>();
	/** Buffers already imported files. */
	private final Set<File> alreadyImportedData = new HashSet<>();

	/** Current set language to work with. */
	private final Locale currentLanguage = Locale.GERMAN;

	/**
	 * @return The current set language of this session for text-processing.
	 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
	 */
	public Locale getCurrentLanguage() {
		return currentLanguage;
	}

	/**
	 * Takes all direct subdirs of the given dir as classes and reads all text-data as training data for theses classes. Does the conversion of all sentences,
	 * removing stopwords and bringing all known-words to their baseforms.<br>
	 * <br>
	 * But does not(!) analyzing significant cooccurences or Markov-Models!
	 *
	 * @param className The directory to read new data from.
	 * @param textPath Progressbar to update.
	 * @param viterbiTagger External initialiazed Viterbi-tagger since creation is expensive. But these are not thread-safe, so there must be one per thread!
	 * @return Map where keys are all found classes and the value is the list of found files.
	 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
	 */
	public boolean importFileToClass(final String className, final Path textPath, final ViterbiTagger viterbiTagger, final StopWordRemover stopWordRemover, final BaseformConverter baseFormConverter) {
		requireNonNull(className);
		requireNonNull(textPath);
		requireNonNull(viterbiTagger);
		requireNonNull(stopWordRemover);
		requireNonNull(baseFormConverter);
		//
		final File textFile = textPath.toFile();
		//
		if (!textFile.isFile() || alreadyImportedData.contains(textFile)) {
			return false;
		}
		//
		// 0. Class and/or file known? Remember...
		Classification classification = classifications.get(className);
		if (classification == null) {
			synchronized (this) {
				classification = classifications.get(className);
				if (classification == null) {
					classification = new Classification(className);
					classifications.put(className, classification);
				}
			}
		}
		//
		// Convert input to text
		final CorpusFile corpusFile = new CorpusFile(textPath);
		final List<String> sentences = corpusFile.sentences();
		if (sentences.isEmpty()) {
			return false;
		}
		//
		final List<List<TaggedWord>> normalizedCorpus = new ArrayList<>(sentences.size());
		for (final String sentence : sentences) {
			final List<TaggedWord> tagged = viterbiTagger.tagSentencesSplitWords(sentence);
			final List<TaggedWord> filtered = stopWordRemover.removeStopWords(tagged);
			final List<TaggedWord> baseforms = baseFormConverter.convertTaggedWords(filtered);
			normalizedCorpus.add(baseforms);
		}

		// Remember content by file.
		classification.addFile(textPath, normalizedCorpus);

		alreadyImportedData.add(textFile);

		return true;
	}

	/**
	 * Explicitaly analyze the known class for siginificant cooccurrences.
	 *
	 * @param className Name of the classification.
	 * @param significanceThreshold
	 * @param progressMonitor External monitor. Assuming progress is already half-finished!
	 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
	 * @return
	 */
	public CooccurrencesReader searchSignificantCoocs(final String className, final double significanceThreshold, final ProgressMonitor progressMonitor) {
		requireNonNull(className);
		//
		progressMonitor.update(progressMonitor.labels().getString("training.import.progress.searchingCoocs") + " " + className);
		//
		final Classification classification = classifications.get(className);
		if (classification == null) {
			LOGGER.warning("Classname " + className + " unknown! Cannot search for significant coocs.");
			return null;
		}
		//
		final long startTime = System.currentTimeMillis();
		//
		classification.searchSiginficantCoocs(significanceThreshold, progressMonitor);
		//
		LOGGER.info("Searching significant coocs in class " + className + " took " + (System.currentTimeMillis() - startTime) + "ms");
		//
		return classification.getCooccurrences();
	}

	/**
	 * @return All known class-names of this training-session.
	 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
	 */
	public Set<String> getClasses() {
		return Collections.unmodifiableSet(classifications.keySet());
	}

	/**
	 * @param className Name of the class with the cooccurences. Must exist!
	 * @return All detected significant cooccurrences by word of the given class.
	 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
	 */
	public Map<WordStatistic, Map<WordStatistic, Double>> getSignificantCooccurrences(final String className) {
		final Classification classification = classifications.get(className);
		if (classification == null) {
			LOGGER.warning("Classname " + className + " is unknown!");
			return Collections.emptyMap();
		}
		//
		return classification.getCooccurencesByWord();
	}

	/**
	 * @param className Name of the class with the cooccurences. Must exist!
	 * @return All detected significant cooccurrences by word of the given class.
	 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
	 */
	public CooccurrencesReader getCooccurrencesReader(final String className) {
		final Classification classification = classifications.get(className);
		if (classification == null) {
			throw new IllegalArgumentException("Classname " + className + " is unknown!");
		}
		//
		return classification.getCooccurrences();
	}

	/**
	 * @return The full count of all words in this classification. Is not buffered!
	 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
	 */
	public long getWordCount(final String className) {
		final Classification classification = classifications.get(className);
		if (classification == null) {
			throw new IllegalArgumentException("Classname " + className + " is unknown!");
		}
		//
		return classification.getWordCount();
	}

	/**
	 * @return The full count of sentences in this classficiation. Is not buffered!
	 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
	 */
	public long getSentenceCount(final String className) {
		final Classification classification = classifications.get(className);
		if (classification == null) {
			throw new IllegalArgumentException("Classname " + className + " is unknown!");
		}
		//
		return classification.getSentenceCount();
	}

	/**
	 * @param className Name of the class with the cooccurences. Must exist!
	 * @return All detected significant cooccurrences by word of the given class.
	 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
	 */
	public Map<String, Map<WordStatistic, Map<WordStatistic, Double>>> getAllSignificantCooccurrences() {
		final Map<String, Map<WordStatistic, Map<WordStatistic, Double>>> result = new HashMap<>(classifications.size());
		//
		final Set<String> classNames = classifications.keySet();
		for (final String className : classNames) {
			final Classification classification = classifications.get(className);
			result.put(className, classification.getCooccurencesByWord());
		}
		//
		return result;
	}

	/**
	 * @return <code>true</code> if there is no training-data present.
	 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
	 */
	public boolean isEmpty() {
		return classifications.isEmpty();
	}

	/**
	 * @param className Name of class to look-up statistics for.
	 * @return Statistics by word in given class.
	 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
	 */
	public Map<String, WordStatistic> getStatisticsByWord(final String className) {
		return classifications.get(className).getStatisticsByWord();
	}

	/**
	 * Remove trained classficiation by given name.
	 * @param name Name of classification.
	 * @return <code>true</code> if classification by name existed and was deleted. <code>false</code> otherwise.
	 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
	 */
	public boolean removeClassification(final String name) {
		LOGGER.log(Level.INFO, "Remove trained class {0} from session.", name);
		//
		return classifications.remove(name) != null;
	}
}
