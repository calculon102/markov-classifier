package de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.textprocessing;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.training.TaggedWord;
import de.uni_leipzig.asv.toolbox.baseforms.Zerleger2;

/**
 * Converts all words in a given sentence to its baseforms.
 *
 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
 */
public final class BaseformConverter {
	private static final Logger LOGGER = Logger.getLogger(StopWordRemover.class.getName());

	/** reduce file for splitting */
	private static final String REDUCE_FILE = "trees/grfExt.tree";
	/** forward file */
	private static final String FORWARD_FILE = "trees/kompVVic.tree";
	/** backward file */
	private static final String BACKWARD_FILE = "trees/kompVHic.tree";

	private final Zerleger2 zerleger = new Zerleger2();

	public BaseformConverter() {
		zerleger.init(getClass().getResourceAsStream(FORWARD_FILE),
				getClass().getResourceAsStream(BACKWARD_FILE),
				getClass().getResourceAsStream(REDUCE_FILE));
	}

	/**
	 * Splits the given word into its compounds (at least 1) and converts alls compund-words into its baseforms.
	 * 
	 * @param word The word to convert.
	 * @param isNoun If the word is a noun, only reducation is done. Mo compound-distribution.
	 * @return All compund words in their baseforms.
	 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
	 */
	public List<String> convertToBaseform(final String word, final boolean isNoun) {
		requireNonNull(word);
		//
		if (word.trim().isEmpty()) {
			return Collections.emptyList();
		}
		//
		if (!isNoun) { // TOFIX Additional blacklist of words recognized as nouns by accident or known wrong split, i.e. "betroffene"
			final String baseform = zerleger.grundFormReduktion(word);
			if (baseform.trim().length() > 1) {
				return Collections.singletonList(baseform);
			}
			//
			return Collections.emptyList();
		}
		//
		final List<String> compundWords = zerleger.kZerlegung(word);
		final List<String> baseforms = compundWords.stream()
				.filter(word2 -> word2.trim().length() > 1)
				.collect(Collectors.toList());
		//
		if (LOGGER.isLoggable(Level.FINE)) {
			LOGGER.fine("Converted " + word + " into its baseforms " + baseforms);
		}
		//
		return baseforms;
	}

	/**
	 * Converts all whitespace-seperated words within the given sentence, splits them to their compound words and converts them to their baseforms. All words
	 * are lowercase afterwards!
	 * 
	 * @param sentence The sentence to convert.
	 * @return Converted sentence
	 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
	 */
	public List<String> convertWordsOfSentence(final List<String> sentence) {
		requireNonNull(sentence);
		//
		final List<String> baseformWords = new ArrayList<>(sentence.size() * 2);
		//
		for (final String word : sentence) {
			final List<String> baseforms = convertToBaseform(word.trim(), false); // Unknown...

			if (!baseforms.isEmpty()) {
				baseformWords.addAll(baseforms);
			}
		}
		//
		if (LOGGER.isLoggable(Level.FINE)) {
			LOGGER.log(Level.FINE, "Converting words of sentence '{0}' to their baseforms: {1}", new Object[] { sentence, baseformWords });
		}
		//
		return baseformWords;
	}

	/**
	 * Converts all whitespace-seperated words within the given sentence, splits them to their compound words and converts them to their baseforms. All words
	 * are lowercase afterwards!
	 * 
	 * @param sentence The sentence to convert.
	 * @return Converted sentence
	 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
	 */
	public List<TaggedWord> convertTaggedWords(final List<TaggedWord> sentence) {
		requireNonNull(sentence);
		//
		//
		final List<TaggedWord> baseformWords = new ArrayList<>(sentence.size() * 2);
		//
		for (final TaggedWord word : sentence) {
			final List<String> baseforms = convertToBaseform(word.getName(), word.isNoun());
			//
			if (baseforms.size() == 1 && baseforms.get(0).equals(word.getName())) {
				baseformWords.add(word);
				continue;
			}
			//
			for (final String baseform : baseforms) {
				baseformWords.add(TaggedWord.ofNameAndTag(baseform, word.getTag()));
			}
		}
		//
		if (LOGGER.isLoggable(Level.FINE)) {
			LOGGER.log(Level.FINE, "Converting words of sentence '{0}' to their baseforms: {1}", new Object[] { sentence, baseformWords });
		}
		//
		return baseformWords;
	}
}
