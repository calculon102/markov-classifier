package de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.textprocessing;

import static java.util.Objects.requireNonNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.training.TaggedWord;

/**
 * Takes a string splits it with whitespace and removes all found stopwords.
 *
 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
 */
public final class StopWordRemover {
	private static final Logger LOGGER = Logger.getLogger(StopWordRemover.class.getName());
	//
	private static final Set<Locale> supportedLanguages = new HashSet<>(Arrays.asList(Locale.GERMAN, Locale.ENGLISH));
	//
	private final Map<Locale, Set<String>> stopwordsByLocale = new HashMap<>(supportedLanguages.size());

	public StopWordRemover() {
		stopwordsByLocale.put(Locale.GERMAN, readStopwords("destopp.txt"));
		stopwordsByLocale.put(Locale.ENGLISH, readStopwords("enstopp.txt"));
	}

	/**
	 * Remove stopwords from sentence from all known languages.
	 * @param sentence Sentence to filter stopwords from.
	 * @return Filtered sentence.
	 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
	 */
	public List<TaggedWord> removeStopWords(final List<TaggedWord> sentence) {
		List<TaggedWord> result = sentence;

		for (final Locale lang : supportedLanguages) {
			result = removeStopWords(result, lang);
		}

		return result;
	}

	/**
	 * Removes all found stopwords from the given sentence in form of an ordered list.
	 * 
	 * @param sentence Sentence to filter as an ordered list of tagged words.
	 * @param lang Language to filter stopwords from. Supported Locales are GERMAN and ENGLISH.
	 * @return Sentence with all stopwords removed.
	 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
	 */
	public List<TaggedWord> removeStopWords(final List<TaggedWord> sentence, final Locale lang) {
		requireNonNull(sentence);
		requireNonNull(lang);
		//
		LOGGER.fine("Removing stopwords from sentence '" + sentence + "' ...");
		//
		//
		if (!supportedLanguages.contains(lang)) {
			LOGGER.warning("Given locale " + lang + " not supported. Remove only language independent stopwords.");
			return sentence;
		}
		//
		int foundStopwords = 0;
		//
		final Set<String> stopwords = stopwordsByLocale.get(lang);
		final List<TaggedWord> filteredWords = new ArrayList<>(sentence.size());
		for (final TaggedWord word : sentence) {
			if (stopwords.contains(word.getName())) {
				LOGGER.fine("Removing stopword " + word);
				foundStopwords += 1;
				continue;
			}
			//
			filteredWords.add(word);
		}
		//
		if (LOGGER.isLoggable(Level.FINE)) {
			LOGGER.fine("Removed " + foundStopwords + " stopwords. Result: '" + filteredWords + "'");
		}
		//
		return filteredWords;
	}

	/**
	 * Reads the given stopword-file line-wise. Must be in the same package.
	 * 
	 * @param stopwordFilename Name of the stopword-file in this package.
	 * @return Set with found stopwords.
	 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
	 */
	private static Set<String> readStopwords(final String stopwordFilename) {
		final Set<String> stopwords = new HashSet<>();
		try (
				final InputStream resource = StopWordRemover.class.getResourceAsStream(stopwordFilename);
				final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(resource))) {
			String line = null;
			while ((line = bufferedReader.readLine()) != null) {
				if (!line.trim().isEmpty()) {
					stopwords.add(line.trim().toLowerCase());
				}
			}
		} catch (final IOException e) {
			e.printStackTrace();
		}

		return stopwords;
	}
}
