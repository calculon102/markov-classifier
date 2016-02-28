package de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.textprocessing;

import static java.util.Objects.requireNonNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

/**
 * Util-class to check for abbrevations.
 *
 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
 */
public final class Abbrevations {
	private static final Abbrevations INSTANCE = new Abbrevations();

	/** Containes the known abbrevations. */
	private final Set<String> abbrevations = new HashSet<>();

	/**
	 * Intits the instance with all abbrevations given in abbrev.txt.
	 */
	private Abbrevations() {
		final InputStream resource = this.getClass().getResourceAsStream("abbrev.txt");
		try (final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(resource))) {
			String line;
			while (null != (line = bufferedReader.readLine())) {
				abbrevations.add(line);
			}
		} catch (final IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	/**
	 * Gets an instance with all abbrevations.
	 * 
	 * @return An Abbrevations-instance.
	 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
	 */
	public static Abbrevations getInstance() {
		return INSTANCE;
	}

	/**
	 * Checks if the ending of the given String is an abbrevation. Include sentence-ending-markers like '.', '!' and '?'.
	 * 
	 * @param sentence The String which ending is to check.
	 * @return <code>true</code> if the sentence ends with an abbrevation.
	 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
	 */
	public boolean endsWithAbbrevation(final String sentence) {
		requireNonNull(sentence);
		//
		final String trimmedSentence = sentence.trim();
		if (trimmedSentence.isEmpty()) {
			return false;
		}
		//
		int startIndex = trimmedSentence.lastIndexOf(" ");
		startIndex = startIndex < 0 ? 0 : startIndex;
		//
		int endIndex = trimmedSentence.length() - 1;
		if (trimmedSentence.endsWith(".") || trimmedSentence.endsWith("!") || trimmedSentence.endsWith("?") || trimmedSentence.endsWith(":") || trimmedSentence.endsWith(";")) {
			endIndex -= 1;
		}
		//
		final String lastWord = trimmedSentence.substring(startIndex, endIndex + 1);
		//
		return abbrevations.contains(lastWord.trim());
	}

}
