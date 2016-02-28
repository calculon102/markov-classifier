package de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.analysis;

import java.io.File;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Represents a single analysis-result of a file.
 *
 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
 */
public final class AnalysisResult implements Serializable {
	/** SERIAL-ID */
	private static final long serialVersionUID = 20150823L;

	/** The analyzed file. */
	private final File file;
	/** Observed emissions based on the markov-modell. */
	private final CorpusEmissions observedEmissions;
	/** Highest-scoring class in word-order-scoring. */
	private final WordOrderHighScore wordOrderHighscore;
	/** Result of an applied Viterbi-Algorithm on emissions and a Markov-Modell. */
	private final ViterbiAlgorithmResult viterbiAlgorithmResult;

	/**
	 * 
	 * Represents a single analysis-result of a file.
	 * @param file The analyzed file.
	 * @param emissions Emissions of the analysis
	 * @param wordOrderHighscore Highscore of the word-order-analysis.
	 * @param viterbiAlgorithmResult Result of applied viterbi-algortihm on markov-modell.
	 */
	public AnalysisResult(final File file, final CorpusEmissions emissions, final WordOrderHighScore wordOrderHighscore, final ViterbiAlgorithmResult viterbiAlgorithmResult) {
		this.file = file;
		this.observedEmissions = emissions;
		this.wordOrderHighscore = wordOrderHighscore;
		this.viterbiAlgorithmResult = viterbiAlgorithmResult;
	}

	public File file() {
		return file;
	}

	public WordOrderHighScore wordOrderHighscore() {
		return wordOrderHighscore;
	}

	public Map<String, Double> classPropabilities() {
		return viterbiAlgorithmResult.probabilityByClass();
	}

	public List<WordPair> emissions() {
		return observedEmissions.list();
	}

	public List<ViterbiValue> probablePath() {
		return viterbiAlgorithmResult.probablePath();
	}

	public long sentenceCount() {
		return observedEmissions.sentenceCount();
	}

	public long wordCount() {
		return observedEmissions.wordCount();
	}
}
