package de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.analysis;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.common.ProgressMonitor;
import de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.training.TaggedWord;

/**
 * Found emissions of a given corpus.
 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
 */
final class CorpusEmissions implements Serializable {
	/** SERIAL-ID */
	private static final long serialVersionUID = 20150920L;

	private final List<WordPair> emissions;
	private final long sentenceCount;
	private final long wordCount;

	private CorpusEmissions(final List<WordPair> emissions, final long sentenceCount, final long wordCount) {
		this.emissions = emissions;
		this.sentenceCount = sentenceCount;
		this.wordCount = wordCount;
	}

	/**
	 * Evaluates the given corpus with all known emissions and returns all found emissions in an ordered way.
	 * @return Results of the observations.
	 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
	 */
	public static CorpusEmissions of(final NormalizedCorpus corpus, final Set<WordPair> knownEmissions, final boolean insertVoidEmissions, final ProgressMonitor progressMonitor) {
		final List<WordPair> observedEmissions = new LinkedList<>(); // Estimation...
		long sentenceCount = 0;
		long wordCount = 0;

		final List<List<TaggedWord>> sentences = corpus.sentences();
		for (final List<TaggedWord> words : sentences) {
			//
			final Set<WordPair> coocs = createCoocsOfWords(words);
			boolean foundSignifianctCoocInSentence = false;
			//
			for (final WordPair cooc : coocs) {
				final boolean isCoocKnown = knownEmissions.contains(cooc);
				if (isCoocKnown) {
					foundSignifianctCoocInSentence = true;
					observedEmissions.add(cooc);
				}
			}
			//
			if (insertVoidEmissions && !foundSignifianctCoocInSentence && !observedEmissions.isEmpty()) { // Only add void, if there is already a valid cooc present
				observedEmissions.add(WordPair.VOID);
			}
			//
			sentenceCount += 1;
			wordCount += words.size();
			//
			progressMonitor.update((double) sentenceCount / (double) sentences.size(), 1);
		}
		//
		// Cut away trailing VOID
		if (insertVoidEmissions) {
			for (int i = observedEmissions.size() - 1; i >= 0; i--) {
				if (observedEmissions.get(i) == WordPair.VOID) {
					observedEmissions.remove(i);
					continue;
				}
				//
				break;
			}
		}

		final long resultWordCount = wordCount;
		final long resultSentenceCount = sentenceCount;

		return new CorpusEmissions(observedEmissions, resultSentenceCount, resultWordCount);
	}

	/**
	 * @return Sorted List of observed emissions.
	 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
	 */
	List<WordPair> list() {
		return Collections.unmodifiableList(emissions);
	}

	/**
	 * @return Count of all obvserved words.
	 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
	 */
	long wordCount() {
		return wordCount;
	}

	/**
	 * @return Count of all observed sentences.
	 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
	 */
	long sentenceCount() {
		return sentenceCount;
	}

	/**
	 * @return <code>true</code> if there are zero oberved emissions in this object.
	 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
	 */
	boolean none() {
		return emissions.isEmpty();
	}

	/**
	 * @param words Words to get all coccurrence-combinations from.
	 * @return All coccurrence-combinations of the given words.
	 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
	 */
	private static Set<WordPair> createCoocsOfWords(final List<TaggedWord> words) {
		//
		final List<String> wordList1 = new ArrayList<>(words.size());
		final List<String> wordList2 = new ArrayList<>(words.size());
		//
		for (final TaggedWord taggedWord : words) {
			wordList1.add(taggedWord.getName());
			wordList2.add(taggedWord.getName());
		}
		//
		final Set<WordPair> result = new HashSet<>(wordList1.size() * wordList2.size());
		for (final String word1 : wordList1) {
			wordList2.remove(word1);
			//
			for (final String word2 : wordList2) {
				if (word1.equals(word2)) {
					continue;
				}
				//
				result.add(WordPair.of(word1, word2));
			}
		}
		//
		return result;
	}
}
