package de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.analysis;

import static java.util.Objects.requireNonNull;

import java.io.File;
import java.io.Serializable;
import java.util.Locale;
import java.util.ResourceBundle;

import de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.common.ProgressMonitor;

/**
 * Represents a single session of data-analysis by HMMs
 *
 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
 */
public final class AnalysisSession implements Serializable {
	/** SERIAL-ID */
	private static final long serialVersionUID = 20150919L;

	/** Current set language to work with. */
	private final Locale currentLanguage = Locale.GERMAN;

	/**
	 * Analyzes the file for class-probabilities based on the given Emission-Alphabet.
	 * @param file Text-File to analyze.
	 * @param markovModell Emission-Alphabet to use. Based on training-data.
	 * @param trainedWordNeighbours Parameter to do additional word-order-analysis.
	 * @param insertVoidEmissions Should void emissions be inserted?
	 * @param progressMonitor Progress-monitor this method reports to.
	 * @param labels Locale-spezcific-labels.
	 * @return The final analytation result.
	 * @throws Exception
	 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
	 */
	public AnalysisResult analyzeFile(final File file, final AnalysisMarkovModell markovModell, final TrainedWordNeighbours trainedWordNeighbours, final boolean insertVoidEmissions, final ProgressMonitor progressMonitor, final ResourceBundle labels) throws Exception {
		requireNonNull(file);
		requireNonNull(markovModell);
		requireNonNull(progressMonitor);
		requireNonNull(labels);
		//
		if (!file.isFile() || !file.exists()) {
			throw new IllegalArgumentException("Given file must exist!");
		}
		//
		progressMonitor.update(ProgressMonitor.INDETERMINATE_PROGRESS, 1);
		//
		final Locale localeToUse;
		if (file.getParent().equalsIgnoreCase("en")) {
			localeToUse = Locale.ENGLISH;
		} else if (file.getParent().equalsIgnoreCase("en")) {
			localeToUse = Locale.GERMAN;
		} else {
			localeToUse = currentLanguage;
		}
		//
		// Parse file for structured content
		final NormalizedCorpus corpus = NormalizedCorpus.ofFile(file, progressMonitor, labels, localeToUse);
		//
		// Word-order analysis
		progressMonitor.update(labels.getString("analysis.progress.wordOrder"));
		final WordOrderScores wos = new WordOrderScores(corpus, trainedWordNeighbours);
		final WordOrderHighScore wohs = WordOrderHighScore.of(wos);
		//
		// Locate emissions in content
		progressMonitor.update(labels.getString("analysis.progress.analyzing"));
		final CorpusEmissions corpusEmissions = CorpusEmissions.of(corpus, markovModell.getAllEmissions(), insertVoidEmissions, progressMonitor); // TODO Make VOID-Parameter configurable
		if (corpusEmissions.none()) {
			return new AnalysisResult(file, corpusEmissions, wohs, UnknownViterbiAlgorithmResult.INSTANCE);
		}
		//
		// Apply viterbi-algorithm to find viterbi-path.
		progressMonitor.update(labels.getString("analysis.progress.viterbi"));
		final ViterbiAlgorithmResult viterbiResult = ViterbiAlgorithm.apply(corpusEmissions, markovModell);

		final AnalysisResult analysisResult = new AnalysisResult(file, corpusEmissions, wohs, viterbiResult);

		progressMonitor.update(labels.getString("analysis.progress.done"));
		progressMonitor.update(1, 1);

		return analysisResult;
	}
}
