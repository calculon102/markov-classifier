package de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.analysis;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.common.ProgressMonitor;
import de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.textprocessing.BaseformConverter;
import de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.textprocessing.CorpusFile;
import de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.textprocessing.StopWordRemover;
import de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.textprocessing.ViterbiTagger;
import de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.training.TaggedWord;

final class NormalizedCorpus {
	/** Corpus without content. */
	public static NormalizedCorpus EMPTY = new NormalizedCorpus(Collections.emptyList());

	/** Stopword-removing-tool. */
	private static final StopWordRemover stopwordRemover = new StopWordRemover();
	/** Converts words and sentences to their baseforms. */
	private static final BaseformConverter baseformConverter = new BaseformConverter();

	/** List of normalized sentences with orders word-list. */
	private final List<List<TaggedWord>> sentences;

	private NormalizedCorpus(final List<List<TaggedWord>> sentences) {
		this.sentences = sentences;
	}

	/**
	 * @return Ordered List of normalized sentences with orders word-list.
	 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
	 */
	public List<List<TaggedWord>> sentences() {
		return Collections.unmodifiableList(sentences);
	}

	/**
	 * Tries to get all text-content from given file and splits it into sentences and words, tagged by the Viterbi-Tagger.
	 * @param file File to parse.
	 * @param monitor Monitor to report progress to.
	 * @param labels Current language-specific-label instance.
	 * @param language Current language to use for parsing.
	 * @return List of Sentences with tagged words in order.
	 * @throws Exception File-exceptions.
	 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
	 */
	public static NormalizedCorpus ofFile(final File file, final ProgressMonitor monitor, final ResourceBundle labels, final Locale language) throws Exception {
		// Read file content
		monitor.update(labels.getString("analysis.progress.readingFile"));

		final CorpusFile corpus = new CorpusFile(file.toPath());
		final List<String> sentences = corpus.sentences();

		if (sentences.isEmpty()) {
			return NormalizedCorpus.EMPTY;
		}

		monitor.update(labels.getString("analysis.progress.normalizing"));

		// Normalize content
		// TODO Dynamic language detection
		final ViterbiTagger tagger = ViterbiTagger.forLocale(language);
		final List<List<TaggedWord>> normalized = new ArrayList<>(sentences.size());
		for (final String sentence : sentences) {
			final List<TaggedWord> tagged = tagger.tagSentencesSplitWords(sentence);
			final List<TaggedWord> filtered = stopwordRemover.removeStopWords(tagged);
			final List<TaggedWord> converted = baseformConverter.convertTaggedWords(filtered);
			normalized.add(converted);
		}

		return new NormalizedCorpus(normalized);
	}
}
