package de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.textprocessing;

import static org.hamcrest.CoreMatchers.notNullValue;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.training.TaggedWord;

/**
 * Manual test-diver for the viterbi-tagger.
 *
 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
 */
@RunWith(Parameterized.class)
public class ViterbiTaggerTest {
	@Parameters(name = "{index}: [{0}] {1}")
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][] {
				{ Locale.GERMAN, "Mitte Januar prophezeiten zwei Weltraumforscher wieder einmal ein Asteroiden-Desaster für die Erde." },
				{ Locale.GERMAN, "Asteroiden-Fehlalarme gehören in der Astronomie fast zum Alltag." },
				{ Locale.GERMAN, "Eine Konferenz befasst sich jetzt mit dem Vorwarn-Dilemma." },
				{ Locale.GERMAN, "Am 13. Januar müssen sich die Astronomen Clark Chapman und David Morrison gefühlt haben wie Hauptdarsteller in einem Blockbuster." },
				{ Locale.GERMAN, "Der Grund: Ein Asteroid mit einem Durchmesser von etwa 30 Metern war, so schien es, auf Kollisionskurs mit der Erde." },
				{ Locale.GERMAN, "Ein Objekt dieser Größe hätte zwar kaum die Menschheit vernichtet, aber gravierende Schäden anrichten können." },
				{ Locale.GERMAN, "Chapman und Morrison griffen trotzdem nicht zum roten Telefon - zu Recht, wie sich herausstellte." },
				{ Locale.GERMAN, "Ein Teleskop in Mexiko hatte einen Asteroiden in Erdnähe entdeckt, der Kurs des Objekts war noch unklar." },
				{ Locale.GERMAN, "Ein Teleskop in Mexiko hatte einen Asteroiden in Erdnähe entdeckt, der Kurs des Objekts war noch unklar." },
				{ Locale.GERMAN, "Den Wissenschaftlern standen nur vier einzelne Beobachtungen zur Verfügung - zu wenig, um eine präzise Flugbahn berechnen zu können." },
				{ Locale.GERMAN, "Die Helligkeit des Brockens schien sich jedoch ständig zu vergrößern, ein Indiz für schnelle Annäherung." },
				{ Locale.GERMAN, "Wie der Online-Nachrichtendienst der BBC berichtet, geriet die internationale Astronomen-Szene in helle Aufregung." },
				{ Locale.GERMAN, "Steven Chesley von der Nasa verschickte eine E-Mail, in der er dem Asteroiden eine Chance von 25 Prozent einräumte, die Erde zu treffen." },
				{ Locale.GERMAN, "Seine Berechnungen brachten Chapman und Morrison dazu, über einen Anruf im Weißen Haus nachzudenken." },
				{ Locale.GERMAN, "Ein Anruf, der nach Ansicht einiger ihrer Kollegen verfrüht und falsch gewesen wäre." },
				{ Locale.GERMAN, "Auch andere Kollegen kritisierten Chapman und Morrison als Panikmacher." },
				{ Locale.GERMAN, "Kurz nach Chesleys E-Mail machte ein Amateur-Astronom eine Beobachtung, die für Entwarnung sorgte: Er blickte durch sein Teleskop und sah, in einer an diesem Tag seltenen Wolkenlücke - nichts." },
				{ Locale.GERMAN, "Hätte sich der Asteroid 2004 AS1 tatsächlich auf Kollisionskurs befunden, der Hobby-Sterngucker hätte ihn im Sucher haben müssen." },
				{ Locale.GERMAN, "Große Erleichterung auf allen Seiten war die Folge." },
				{ Locale.GERMAN, "Nun wird, zum wiederholten Mal, diskutiert, wie mit derartigen Situationen umgegangen werden soll." },
				{ Locale.GERMAN, "Das Dilemma: Der Weltuntergang verkauft sich gut." },
				{ Locale.GERMAN, "Nichts ist so unterhaltsam wie Todesangst, hat Theater-Schocker Christoph Schlingensief einmal festgestellt." },
				{ Locale.GERMAN, "Ein Asteroid, der auf die Erde zurast, die Menschheit vor der Vernichtung - was ergäbe eine aufmerksamkeitsträchtigere Schlagzeile?" },
				{ Locale.GERMAN, "In der Vergangenheit gab es immer wieder falsche Alarme, die ein schlechtes Licht auf die Zunft der Astronomen werfen." },
				{ Locale.GERMAN, "Kritiker Marsden weiß, wovon er spricht: Im März 1998 veröffentlichte er selbst eine vorsichtige Asteroidenwarnung - und machte weltweit Schlagzeilen." },
				{ Locale.GERMAN, "Astronom Benny Peiser, der Chapman und Morrison ebenfalls Panikmache vorwarf, ist mit Schuld am Problem: 1999 verbreitete er eine Asteroidenwarnung in einem Newsletter." },
				{ Locale.GERMAN, "Er schätzte die Trefferwahrscheinlichkeit als extrem gering ein, erhob jedoch Vertuschungsvorwürfe, so die Astronomie-Nachrichtenseite Space.com." },
				{ Locale.GERMAN, "Verschiedene Zeitungen sprangen auf den Zug auf, beschuldigten Astronomen, Herrschaftswissen für sich zu behalten." },
				{ Locale.GERMAN, "Nach einer erneuten überflüssigen Warnung im September 2003 befragte Space.com acht Experten, die alle zu dem gleichen Ergebnis kamen: Fehlalarme sind unvermeidlich." },
				{ Locale.GERMAN, "Junge Wissenschaftler müssten diesen Fehler wohl einmal selbst machen, vermutete Brian Marsden damals." },
				{ Locale.GERMAN, "Chapman und Morrison lagen mit ihrer Zurückhaltung richtig, 2004 AS1 erwies sich im Endeffekt als harmloser Geselle." },
				{ Locale.GERMAN, "Zwar war der Asteroid deutlich größer als zunächst angenommen, nämlich etwa einen halben Kilometer dick." },
				{ Locale.GERMAN, "Er flog jedoch zwölf Millionen Kilometer weit an der Erde vorbei - das entspricht ungefähr dem 32-fachen Abstand zwischen Erde und Mond." },
				{ Locale.GERMAN, "Planetary Defense Conference: Wie schützen wir uns vor Asteroiden?" },
		});
	}

	private final ViterbiTagger taggerToTest;
	private final String sentenceToTest;

	public ViterbiTaggerTest(final Locale locale, final String sentenceToTag) {
		taggerToTest = ViterbiTagger.forLocale(locale);
		sentenceToTest = sentenceToTag;
	}

	/**
	 * Prints out all tagging-results.
	 * 
	 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
	 */
	@Test
	public void testTaggingResults() {
		final String taggedSentences = taggerToTest.tagSentences(sentenceToTest);
		Assert.assertThat(taggedSentences, notNullValue());
		//
		System.out.println("With punctuation: " + taggedSentences);
	}

	/**
	 * Prints out all tagging-results. Removes all punctuation form the sentece
	 * before tagging.
	 * 
	 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
	 */
	@Test
	public void testTaggingResultsWithoutPunctuation() {
		final String taggedSentences = taggerToTest.tagSentences(sentenceToTest.replaceAll("[^a-zA-ZäöüÄÖÜß ]", ""));
		Assert.assertThat(taggedSentences, notNullValue());
		//
		System.out.println("Without punctuation: " + taggedSentences);
	}

	/**
	 * Prints out all tagging-results. Removes all punctuation form the sentece
	 * and converts to TaggedWords before tagging.
	 * 
	 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
	 */
	@Test
	public void testTaggingResultsAsTaggedWords() {
		final List<TaggedWord> tagSentencesSplitWords = taggerToTest.tagSentencesSplitWords(sentenceToTest.replaceAll("[^a-zA-ZäöüÄÖÜß ]", ""));
		Assert.assertThat(tagSentencesSplitWords, notNullValue());
		//
		System.out.print("As Taggedwords: ");
		for (final TaggedWord taggedWord : tagSentencesSplitWords) {
			System.out.print(taggedWord + ", ");
		}
		System.out.print(System.lineSeparator());
	}
}
