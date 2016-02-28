package de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.analysis;

import static java.util.Objects.requireNonNull;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

final class ViterbiAlgorithm {

	/**
	 * Applies the viterbi-algorithm to the given list of emissions with the known markov-modell. TODO Cut down huge method into smaller ones.
	 * @param observationResults List of emissions to analyze.
	 * @param markovModell Makrov-Modell to use.
	 * @return Result of the algorithm.
	 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
	 */
	public static ViterbiAlgorithmResult apply(final CorpusEmissions emissions, final AnalysisMarkovModell markovModell) {
		requireNonNull(emissions);
		requireNonNull(markovModell);
		//
		final List<WordPair> observationResults = emissions.list();
		//
		// -- Initialize field
		final List<String> classNames = new ArrayList<>(markovModell.getKnownClasses());
		final int classNamesCount = classNames.size();
		final int emissionCount = observationResults.size();
		final ViterbiValue[][] viterbiField = new ViterbiValue[emissionCount][classNamesCount];
		for (int observationIndex = 0; observationIndex < observationResults.size(); observationIndex++) {
			for (int stateIndex = 0; stateIndex < classNamesCount; stateIndex++) {
				final String className = classNames.get(stateIndex);
				viterbiField[observationIndex][stateIndex] = new ViterbiValue(className);
			}
		}

		// -- Compute columns 1
		final WordPair firstObservedEmission = observationResults.get(0);
		for (int stateIndex = 0; stateIndex < classNamesCount; stateIndex++) {
			final String className = classNames.get(stateIndex);
			final BigDecimal initialStateProbability = markovModell.getInitialStateProbability(className);
			final BigDecimal emissionProbability = markovModell.getEmissionProbability(firstObservedEmission, className);
			final BigDecimal newViterbiValue;
			if (AnalysisMarkovModell.USE_LOG_PROBABILITES) {
				newViterbiValue = initialStateProbability.add(emissionProbability);
			} else {
				newViterbiValue = initialStateProbability.multiply(emissionProbability);
			}
			//
			viterbiField[0][stateIndex].setViterbiValue(newViterbiValue);
		}

		// -- Compute rest columns
		for (int observationIndex = 1; observationIndex < observationResults.size(); observationIndex++) {

			final WordPair observedEmission = observationResults.get(observationIndex);

			for (int stateIndex = 0; stateIndex < classNamesCount; stateIndex++) {
				final String currentState = classNames.get(stateIndex);
				//
				final BigDecimal emissionProbability = markovModell.getEmissionProbability(observedEmission, currentState);

				for (int stateIndex2 = 0; stateIndex2 < classNamesCount; stateIndex2++) {
					final String prevState = classNames.get(stateIndex2);
					final BigDecimal stateChangeProbability = markovModell.getStateChangeProbability(prevState, currentState);
					final BigDecimal prevViterbiValue = viterbiField[observationIndex - 1][stateIndex2].getViterbiValue();
					final BigDecimal currentViterbiValue = viterbiField[observationIndex][stateIndex].getViterbiValue();

					final BigDecimal newViterbiValue;
					if (AnalysisMarkovModell.USE_LOG_PROBABILITES) {
						newViterbiValue = prevViterbiValue.add(stateChangeProbability).add(emissionProbability);
					} else {
						newViterbiValue = prevViterbiValue.multiply(stateChangeProbability).multiply(emissionProbability);
					}

					if (newViterbiValue.compareTo(currentViterbiValue) > 0 || (AnalysisMarkovModell.USE_LOG_PROBABILITES && currentViterbiValue == BigDecimal.ZERO)) {
						viterbiField[observationIndex][stateIndex].setViterbiValue(newViterbiValue);
					}
				}
			}
		}

		// -- Find Viterbi-score
		final int lastEmissionIndex = emissionCount - 1;
		BigDecimal highestViterbiValue = BigDecimal.ZERO;
		ViterbiValue viterbiScore = null;
		for (int stateIndex = 0; stateIndex < classNamesCount; stateIndex++) {
			final ViterbiValue currentViterbiValue = viterbiField[lastEmissionIndex][stateIndex];
			final BigDecimal viterbiValue = currentViterbiValue.getViterbiValue();
			if (viterbiValue.compareTo(highestViterbiValue) > 0 || viterbiScore == null) {
				viterbiScore = currentViterbiValue;
				highestViterbiValue = viterbiValue;
			}
		}
		//
		if (viterbiScore == null) {
			throw new IllegalStateException("viterbiScore is null!? Something went bollocks!");
		}

		// Apply Viterbi-algorithm - backtracking part.
		final List<ViterbiValue> mostProbablePath = new ArrayList<>(emissionCount);
		for (int i = 0; i < emissionCount; i++) {
			mostProbablePath.add(null);
		}

		// -- Begin at the end
		mostProbablePath.set(emissionCount - 1, viterbiScore);

		// -- Iterate backwards
		final Map<String, Integer> mostProbableClassCount = new HashMap<>(markovModell.getKnownClasses().size());
		mostProbableClassCount.put(viterbiScore.getClassName(), 1);
		//
		for (int observationIndex = emissionCount - 2; observationIndex >= 0; observationIndex--) {
			//
			ViterbiValue argmaxViterbiValue = null;
			BigDecimal hightestProbability = BigDecimal.ZERO;
			final ViterbiValue nextValue = mostProbablePath.get(observationIndex + 1);
			final BigDecimal nextValueProbability = markovModell.getInitialStateProbability(nextValue.getClassName());
			//
			for (int stateIndex = 0; stateIndex < classNamesCount; stateIndex++) {
				final BigDecimal currentValue;
				if (AnalysisMarkovModell.USE_LOG_PROBABILITES) {
					currentValue = viterbiField[observationIndex][stateIndex].getViterbiValue().add(nextValueProbability);
				} else {
					currentValue = viterbiField[observationIndex][stateIndex].getViterbiValue().multiply(nextValueProbability);
				}
				//
				if (currentValue.compareTo(hightestProbability) > 0 || argmaxViterbiValue == null) {
					argmaxViterbiValue = viterbiField[observationIndex][stateIndex];
					hightestProbability = currentValue;
				}
			}
			//
			mostProbablePath.set(observationIndex, argmaxViterbiValue);
			if (argmaxViterbiValue == null) {
				throw new IllegalStateException("argmaxViterbiValue still null!?");
			}
			mostProbableClassCount.compute(argmaxViterbiValue.getClassName(), (k, v) -> (v == null ? 1 : v + 1));
		}
		//
		// Calculate probability by class
		final Map<String, Double> probabilitesByClass = new HashMap<>();
		final Set<Entry<String, Integer>> emissionCountByClassEntrySet = mostProbableClassCount.entrySet();
		for (final Entry<String, Integer> entry : emissionCountByClassEntrySet) {
			final double probability = (double) entry.getValue() / emissionCount;
			probabilitesByClass.put(entry.getKey(), probability);
		}
		//
		return createResult(probabilitesByClass, mostProbablePath);
	}

	/**
	 * @param probabilitesByClass
	 * @param mostProbablePath
	 * @return New unmodifiable instance of a result-object.
	 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
	 */
	private static ViterbiAlgorithmResult createResult(final Map<String, Double> probabilitesByClass, final List<ViterbiValue> mostProbablePath) {
		return new ViterbiAlgorithmResult() {
			private static final long serialVersionUID = 20151129L;

			@Override
			public Map<String, Double> probabilityByClass() {
				return probabilitesByClass;
			}

			@Override
			public List<ViterbiValue> probablePath() {
				return Collections.unmodifiableList(mostProbablePath);
			}
		};
	}
}
