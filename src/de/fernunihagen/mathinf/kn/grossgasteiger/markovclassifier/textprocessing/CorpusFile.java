package de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.textprocessing;

import static java.util.Objects.requireNonNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.html.HtmlParser;
import org.apache.tika.parser.pdf.PDFParser;
import org.apache.tika.parser.rtf.RTFParser;
import org.apache.tika.parser.txt.TXTParser;
import org.apache.tika.parser.xml.XMLParser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * Reference to a file representing a parsable plain-text corpus.
 *
 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
 */
public final class CorpusFile {

	/** Logger-instance. */
	private static final Logger LOGGER = Logger.getLogger(CorpusFile.class.getName());
	/** Regular expression to split a text into groups divided by '.', '!' or '?'. */
	private static final Pattern SENTENCE_SPLIT = Pattern.compile("([^.!?]+[.!?]{1})");
	/** Regular expression to remove all non-letters. */
	private static final Pattern GLYPH_REMOVE = Pattern.compile("[^a-zA-ZäöüÄÖÜß ]");

	/** Path to the corupus file. */
	private final Path path;

	/**
	 * @param pathToFile File to be parsed as plain-text. Possibly already plain-text, HTML, XML, RTF oder PDF.
	 */
	public CorpusFile(final Path pathToFile) {
		this.path = pathToFile;
	}

	/**
	 * @return The sentences of the corpus as an ordered list. If without content or not parsable, an empty list. Check SEVERE-log for possible errors and
	 *         exceptions.
	 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
	 */
	public List<String> sentences() {
		return splitTextIntoSentences(convertFileToText(path));
	}

	/**
	 * Stateless method to convert given files to plain-text. Assumes given text-files as plain-text itself or markup like XML or HTML.
	 *
	 * @param path The text-file to read.
	 * @return Plain-text content of the file.
	 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
	 */
	private static String convertFileToText(final Path path) {
		requireNonNull(path);

		final File file = path.toFile();
		final String filename = file.getName();
		final String absolutePath = file.getAbsolutePath();

		LOGGER.log(Level.FINE, "Parsing text-content of file: {0}", absolutePath);

		final Parser parser;
		if (filename.endsWith(".txt")) {
			parser = new TXTParser();
		} else if (filename.endsWith(".html")
				|| filename.endsWith(".xtml")) {
			parser = new HtmlParser();
		} else if (filename.endsWith(".xml")) {
			parser = new XMLParser();
		} else if (filename.endsWith(".rtf")) {
			parser = new RTFParser();
		} else if (filename.endsWith(".pdf")) {
			parser = new PDFParser();
		} else {
			parser = new AutoDetectParser();
		}
		//
		final ContentHandler contentHandler = new BodyContentHandler(10 * 1024 * 1024); // TOLOOK
		final Metadata metadata = new Metadata();
		final ParseContext parseContext = new ParseContext();
		//
		try (final InputStream inputStream = new FileInputStream(file)) {
			parser.parse(inputStream, contentHandler, metadata, parseContext);
		} catch (IOException | SAXException | TikaException e) {
			LOGGER.log(Level.SEVERE, "Error while parsing text.", e);
			return "";
		}
		//
		final String fileContent = contentHandler.toString();
		LOGGER.log(Level.FINE, "Content: {0}", fileContent);
		//
		return fileContent;
	}

	/**
	 * Splits the given text into sentences, preserving abbrevations defined in abbr.txt
	 *
	 * @param text The text to split.
	 * @return The sorted list of recognized sentences.
	 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
	 */
	private static List<String> splitTextIntoSentences(final String text) {
		requireNonNull(text);

		if (text.isEmpty()) {
			return Collections.emptyList();
		}

		final int loggingEndIndex = text.length() < 20 ? text.length() : 20;
		if (LOGGER.isLoggable(Level.FINE)) {
			LOGGER.fine("Split text into senteces beginning with: '" + text.substring(0, loggingEndIndex) + "' (...)");
		}

		final Matcher matcher = SENTENCE_SPLIT.matcher(text);
		final List<String> intermediateResult = new ArrayList<>();

		int groupCount = 0;
		while (matcher.find()) {
			final String sentence = matcher.group(1);
			final String sentenceWithoutPunctuations = GLYPH_REMOVE.matcher(sentence).replaceAll("");
			intermediateResult.add(sentenceWithoutPunctuations);
			groupCount += 1;
		}

		LOGGER.log(Level.FINE, "Detected {0} sentences. Merging false detected abbrevations...", groupCount);

		// Merge false detected abbrevation-endings
		final Abbrevations abbrevations = Abbrevations.getInstance();
		final List<String> result = new ArrayList<>(intermediateResult.size());
		for (int i = 0; i < intermediateResult.size(); i++) {
			final String sentence = intermediateResult.get(i);
			final boolean endsWithAbbrevation = abbrevations.endsWithAbbrevation(sentence);
			if (!endsWithAbbrevation) {
				result.add(sentence);
			}
			//
			int j = 0;
			while (intermediateResult.size() < i + j) {
				j += 1;
				//
				final String nextSentence = intermediateResult.get(i + j);
				final String mergedSentece = sentence + " " + nextSentence;
				result.add(mergedSentece);
				//
				if (!abbrevations.endsWithAbbrevation(mergedSentece) || intermediateResult.size() >= i + j) {
					i += j;
					break;
				}
			}
		}

		LOGGER.log(Level.FINE, "Result with {0} sentences after merging.", result.size());

		return result;
	}
}
