package de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.textprocessing;

import static java.util.Objects.requireNonNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.tika.io.IOUtils;

import de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.training.TaggedWord;
import de.uni_leipzig.asv.toolbox.viterbitagger.Tagger;

/**
 * Tags sentences with Viterbi-model for germon or english.
 *
 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
 */
public final class ViterbiTagger {

	/**
	 * Static memory of files names for tagger-models. Value must be a List with 4 entries in order of Taglist, Lecicon, Transtions and Model.
	 */
	private static final Map<Locale, List<String>> TAGGER_FILES_BY_LOCALE = new HashMap<>(2);

	static {
		TAGGER_FILES_BY_LOCALE.put(Locale.GERMAN, Arrays.asList(
				"taggermodel/deTaggerModel.taglist",
				"taggermodel/deTaggerModel.lexicon",
				"taggermodel/deTaggerModel.transitions",
				"taggermodel/deTaggerModel.model"));
		TAGGER_FILES_BY_LOCALE.put(Locale.ENGLISH, Arrays.asList(
				"taggermodel/en_bnc-taggermodel.taglist",
				"taggermodel/en_bnc-taggermodel.lexicon",
				"taggermodel/en_bnc-taggermodel.transitions",
				"taggermodel/english.model"));
	}

	private static final Map<Locale, ViterbiTagger> INSTANCES_BY_LOCALE = new ConcurrentHashMap<>(2);

	/**
	 * Get a (buffered) instance.
	 * 
	 * @param locale
	 * @return
	 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
	 */
	public static ViterbiTagger forLocale(final Locale locale) {
		return INSTANCES_BY_LOCALE.computeIfAbsent(locale, key -> new ViterbiTagger(key));
	}

	/** Instance of the external ASV-Tagger for the workload. */
	private final Tagger tagger;

	private ViterbiTagger(final Locale localeToUse) {
		requireNonNull(localeToUse);
		//
		final List<String> fileNames = TAGGER_FILES_BY_LOCALE.get(localeToUse);
		if (fileNames == null) {
			throw new IllegalStateException("Language " + localeToUse + " not available for ViterbiTagger!");
		}
		//
		try (
				final InputStream tagList = new ByteArrayInputStream(IOUtils.toByteArray(this.getClass().getResourceAsStream(fileNames.get(0))));
				final InputStream lexicon = new ByteArrayInputStream(IOUtils.toByteArray(this.getClass().getResourceAsStream(fileNames.get(1))));
				final InputStream transitions = new ByteArrayInputStream(IOUtils.toByteArray(this.getClass().getResourceAsStream(fileNames.get(2))))) {
			tagger = new Tagger(tagList, lexicon, transitions, null, false);
		} catch (final IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}

	}

	/**
	 * Tags the given sentence.
	 * 
	 * @param sentence sentenceToTag.
	 * @return Tagged where every token is appened by an |(Tagname).
	 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
	 */
	public String tagSentences(final String sentence) {
		return tagger.tagSentence(sentence);
	}

	/**
	 * Tags the given sentence.
	 * 
	 * @param sentence sentenceToTag.
	 * @return Tagged where every token is appened by an |(Tagname).
	 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
	 */
	public List<TaggedWord> tagSentencesSplitWords(final String sentence) {
		final String tagSentences = tagSentences(sentence);
		final String[] words = tagSentences.split(" ");
		//
		final List<TaggedWord> taggedWords = new ArrayList<>(words.length);
		for (final String word : words) {
			if (!word.trim().isEmpty()) {
				taggedWords.add(TaggedWord.ofTaggedString(word));
			}
		}
		//
		return taggedWords;
	}
}
