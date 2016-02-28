package de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.analysis;

import static java.util.Objects.requireNonNull;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.common.ProgressMonitor;
import de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.math.MathExt;
import de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.training.TrainingSession;
import de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.training.WordStatistic;

/**
 * The complete Emission-Alphabet of the HMM.
 *
 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
 */
public final class AnalysisMarkovModell {
	/** Creates logarithmic probalities log_2(p) instead of (p) which must be summed and not multiplied and are easier to represent. */
	public static final boolean USE_LOG_PROBABILITES = true;

	/** All emissions of the Markov-Modell. */
	private final Set<WordPair> emissionAlphabet;
	/** Probabilites of switching initially to a specific state. */
	private final Map<String, BigDecimal> initialStateProbabilties;
	/** Probabilites of switching to a specific state. */
	private final Map<String, Map<String, BigDecimal>> stateChangeProbabiltites;
	/** Emission-probablities by state. */
	private final Map<String, Map<WordPair, BigDecimal>> emissionProbabilitiesByState;

	/**
	 * Calculates the properties of a markov-model-instances and creates an imuutbale-version of it.
	 * @param session Reference to the training-data to use for generation.
	 * @param voidSignificance Significance of void-emissions.
	 * @param pseudoCountSignificance Signifiances of emissions in state where those emissions are unknown.
	 * @param progressMonitor Progress is reported to this instance. Will set the progress zo zero initially and to 1/2 when finished for further tasks to
	 *        happen.
	 * @return A fixed and final calculated markov-model.
	 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
	 */
	public static AnalysisMarkovModell ofTrainingSession(final TrainingSession session, final double voidSignificance, final double pseudoCountSignificance, final ProgressMonitor progressMonitor) {
		requireNonNull(session);
		//
		final Map<String, Map<WordStatistic, Map<WordStatistic, Double>>> allSignificantCooccurrences = session.getAllSignificantCooccurrences();
		//
		final Set<WordPair> emissionAlphabet = new HashSet<>();
		final Map<String, Map<WordPair, ClassCooccurrence>> knownEmissionsOfClass = new HashMap<>();
		//
		progressMonitor.update(0, 4);
		progressMonitor.update(progressMonitor.labels().getString("analysis.markov.task.emissionAlphabet"));
		fillEmissionAlphabet(allSignificantCooccurrences, emissionAlphabet, knownEmissionsOfClass, voidSignificance <= 0 ? 0.1 : voidSignificance);
		//
		progressMonitor.update(1, 4);
		progressMonitor.update(progressMonitor.labels().getString("analysis.markov.task.emissionAlphabet"));
		final Map<String, BigDecimal> initialProbablitiesByState = new HashMap<>(knownEmissionsOfClass.size());
		final Map<String, Map<String, BigDecimal>> probablitiesByState = new HashMap<>(knownEmissionsOfClass.size());
		fillProbabilitiesByState(emissionAlphabet, knownEmissionsOfClass, initialProbablitiesByState, probablitiesByState);
		//
		progressMonitor.update(2, 4);
		progressMonitor.update(progressMonitor.labels().getString("analysis.markov.task.emissionProbabilities"));
		final Map<String, Map<WordPair, BigDecimal>> emissionProbabilitiesByState = createProbabilitiesForEmissionsByState(emissionAlphabet, knownEmissionsOfClass, pseudoCountSignificance <= 0 ? 0.1 : pseudoCountSignificance, progressMonitor);
		//
		return new AnalysisMarkovModell(emissionAlphabet, initialProbablitiesByState, probablitiesByState, emissionProbabilitiesByState);
	}

	private AnalysisMarkovModell(final Set<WordPair> emissionAlphabet, final Map<String, BigDecimal> initialProbabilitiesByState, final Map<String, Map<String, BigDecimal>> probablitiesByState, final Map<String, Map<WordPair, BigDecimal>> emissionProbabilitiesByState) {
		this.emissionAlphabet = emissionAlphabet;
		this.initialStateProbabilties = initialProbabilitiesByState;
		this.stateChangeProbabiltites = probablitiesByState;
		this.emissionProbabilitiesByState = emissionProbabilitiesByState;
	}

	/**
	 * @return Count of all known emissions.
	 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
	 */
	public int getEmissionCount() {
		return emissionAlphabet.size();
	}

	/**
	 * @return List of all known Emissions including VOID.
	 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
	 */
	public Set<WordPair> getAllEmissions() {
		return Collections.unmodifiableSet(emissionAlphabet);
	}

	/**
	 * @return All known Classes / States, including UNKNOWN
	 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
	 */
	public Set<String> getKnownClasses() {
		return initialStateProbabilties.keySet();
	}

	/**
	 * @param className Name of class to look-up-probability.
	 * @return Chance between 0 and 1 that state of model changes to given class.
	 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
	 */
	public BigDecimal getInitialStateProbability(final String className) {
		return initialStateProbabilties.getOrDefault(className, BigDecimal.ZERO);
	}

	/**
	 * @param fromClass Name of the current class.
	 * @param toClass Name of the probable next class.
	 * @return Chance between 0 and 1 that state of model changes from class to given class.
	 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
	 */
	public BigDecimal getStateChangeProbability(final String fromClass, final String toClass) {
		final Map<String, BigDecimal> fromProps = stateChangeProbabiltites.get(fromClass);
		if (fromProps == null) {
			throw new IllegalArgumentException("Given class " + fromClass + " unknown!");
		}
		//
		final BigDecimal prop = fromProps.get(toClass);
		if (prop == null) {
			throw new IllegalArgumentException("Given class " + toClass + " unknown!");
		}
		//
		return prop;
	}

	/**
	 * @param emission Emission to look-up probabiblity of.
	 * @param className Name of class to look-up-probability.
	 * @return Chance between 0 and 1 that given emission is within given class/state.
	 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
	 */
	public BigDecimal getEmissionProbability(final WordPair emission, final String className) {
		final Map<WordPair, BigDecimal> emissionProbabilitesOfClass = emissionProbabilitiesByState.get(className);
		if (emissionProbabilitesOfClass == null) {
			throw new IllegalArgumentException("Given class " + className + " unknown!");
		}
		//
		return emissionProbabilitesOfClass.getOrDefault(emission, BigDecimal.ZERO);
	}

	/**
	 * @param inSignificantCooccurrencesByClass All significant coocurrences sorted by its classes.
	 * @param outEmissionAlphabet The complete alphabet of all unqique coccurences by emissions.
	 * @param outKnownEmissionsOfClass All emissions, which are significant occurrences of the given class.
	 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
	 */
	private static void fillEmissionAlphabet(final Map<String, Map<WordStatistic, Map<WordStatistic, Double>>> inSignificantCooccurrencesByClass, final Set<WordPair> outEmissionAlphabet, final Map<String, Map<WordPair, ClassCooccurrence>> outKnownEmissionsOfClass, final double voidSignificance) {
		//
		for (final Entry<String, Map<WordStatistic, Map<WordStatistic, Double>>> sigCoocEntry : inSignificantCooccurrencesByClass.entrySet()) {
			final String className = sigCoocEntry.getKey();
			final Map<WordPair, ClassCooccurrence> knownEmissionsOfClass = new HashMap<>();
			outKnownEmissionsOfClass.put(className, knownEmissionsOfClass);
			//
			final Map<WordStatistic, Map<WordStatistic, Double>> sigCoocsOfClass = sigCoocEntry.getValue();
			final Set<Entry<WordStatistic, Map<WordStatistic, Double>>> sigCoocsOfClassEntrySet = sigCoocsOfClass.entrySet();
			//
			for (final Entry<WordStatistic, Map<WordStatistic, Double>> sigCoocs : sigCoocsOfClassEntrySet) {
				final String word1 = sigCoocs.getKey().getName();
				final Set<Entry<WordStatistic, Double>> entrySet = sigCoocs.getValue().entrySet();
				for (final Entry<WordStatistic, Double> entry : entrySet) {
					final String word2 = entry.getKey().getName();
					final WordPair cooc = WordPair.of(word1, word2);
					outEmissionAlphabet.add(cooc);
					final ClassCooccurrence sigCooc = ClassCooccurrence.of(cooc, className, entry.getValue());
					knownEmissionsOfClass.put(cooc, sigCooc);
				}
			}
			//
			final ClassCooccurrence sigCooc = ClassCooccurrence.of(WordPair.VOID, className, voidSignificance);
			knownEmissionsOfClass.put(WordPair.VOID, sigCooc);
		}
		//
		// TOLOOK Configurable?
		//		final AnalysisEmission sigCooc = AnalysisEmission.of(AnalysisCooccurrence.VOID, "Unknown", voidSignificance);
		//		outKnownEmissionsOfClass.put("Unknown", Collections.singletonMap(AnalysisCooccurrence.VOID, sigCooc));
		//
		outEmissionAlphabet.add(WordPair.VOID);
	}

	/**
	 * Calculcates the probablity of a state/class based on the count of emissions by significant cooccurences compared to all emission.
	 * @param emissionAlphabet
	 * @param knownEmissionsOfClass
	 * @param initialProbablitiesByState Map will be filled with the initial probablities to switch to a state.
	 * @param stateChangeProbabilities Map will be filled with the chances to switch from a state to other states.
	 * @return Probability of state-change to class of given name.
	 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
	 */
	private static void fillProbabilitiesByState(final Set<WordPair> emissionAlphabet, final Map<String, Map<WordPair, ClassCooccurrence>> knownEmissionsOfClass, final Map<String, BigDecimal> initialProbablitiesByState,
			final Map<String, Map<String, BigDecimal>> stateChangeProbabilities) {
		//
		knownEmissionsOfClass.entrySet().forEach((classEmissions) -> {
			final String state = classEmissions.getKey();
			final BigDecimal initialProbability;
			if (USE_LOG_PROBABILITES) {
				initialProbability = BigDecimal.valueOf(MathExt.log2(1.0 / knownEmissionsOfClass.size())).setScale(10, RoundingMode.HALF_UP);
			} else {
				initialProbability = BigDecimal.valueOf(1.0 / knownEmissionsOfClass.size()).setScale(10, RoundingMode.HALF_UP);
			}

			initialProbablitiesByState.put(state, initialProbability);

			final Map<String, BigDecimal> furtherProbabilites = new HashMap<>(knownEmissionsOfClass.size());
			stateChangeProbabilities.put(state, furtherProbabilites);

			final BigDecimal ownChance;
			final BigDecimal otherChance;

			if (USE_LOG_PROBABILITES) {
				// TOLOOK Equal-chances or what!?
				//				final double ownChanceDouble = 1.0d / (MathExt.log2(knownEmissionsOfClass.size()));
				final double ownChanceDouble = 1.0d / knownEmissionsOfClass.size();
				//				final double otherChanceDouble = Math.abs(1.0d - ownChanceDouble) / (knownEmissionsOfClass.size() - 1.0d);
				final double otherChanceDouble = 1.0d / knownEmissionsOfClass.size();
				ownChance = BigDecimal.valueOf(MathExt.log2(ownChanceDouble));
				otherChance = BigDecimal.valueOf(MathExt.log2(otherChanceDouble));
			} else {
				ownChance = BigDecimal.valueOf(1.0d / (MathExt.log2(knownEmissionsOfClass.size())));
				otherChance = BigDecimal.ONE.subtract(ownChance).divide(new BigDecimal(knownEmissionsOfClass.size() - 1), 10, RoundingMode.HALF_UP);
			}

			knownEmissionsOfClass.entrySet().forEach((classEmissions2) -> {
				final String otherState = classEmissions2.getKey();

				if (state.equals(otherState)) {
					furtherProbabilites.put(otherState, ownChance);
				} else {
					furtherProbabilites.put(otherState, otherChance);
				}

			});
		});
	}

	/**
	 * Calculates the probabilites of all emissions in all classes.
	 * @param emissionAlphabet
	 * @param knownEmissionsByClass
	 * @param pseudoCountSignificance
	 * @param progressMonitor
	 * @return
	 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
	 */
	private static Map<String, Map<WordPair, BigDecimal>> createProbabilitiesForEmissionsByState(final Set<WordPair> emissionAlphabet, final Map<String, Map<WordPair, ClassCooccurrence>> knownEmissionsByClass, final double pseudoCountSignificance,
			final ProgressMonitor progressMonitor) {
		//
		final Map<String, Map<WordPair, BigDecimal>> result = new HashMap<>(knownEmissionsByClass.size());
		//
		final Map<String, Set<ClassCooccurrence>> allEmissionsOfClass = new HashMap<>(knownEmissionsByClass.size());
		final Map<String, BigDecimal> significanceSumByClass = new HashMap<>(knownEmissionsByClass.size());
		//
		// Initialize Maps class-wise.
		final Set<String> classNames = knownEmissionsByClass.keySet();
		final double finalProgress = classNames.size() * 2;
		double currentProgess = classNames.size();

		for (final String className : classNames) {
			final Set<ClassCooccurrence> emissionsOfClass = new HashSet<ClassCooccurrence>(emissionAlphabet.size());
			allEmissionsOfClass.put(className, emissionsOfClass);

			final Map<WordPair, BigDecimal> emissionsProbabilites = new HashMap<>(emissionAlphabet.size());
			result.put(className, emissionsProbabilites);

			significanceSumByClass.put(className, BigDecimal.ZERO);
		}
		//
		// 1. Add pseudo-count significances for all classes and calculate significance sum.
		for (final WordPair cooc : emissionAlphabet) {
			//
			for (final String className : classNames) {
				final Map<WordPair, ClassCooccurrence> knownEmissions = knownEmissionsByClass.get(className);
				final ClassCooccurrence knownEmission = knownEmissions.get(cooc);
				//
				final ClassCooccurrence emissionToAdd;
				if (knownEmission != null) {
					emissionToAdd = knownEmission;
				} else {
					emissionToAdd = ClassCooccurrence.of(cooc, className, pseudoCountSignificance);
				}
				//
				allEmissionsOfClass.get(className).add(emissionToAdd);
				final BigDecimal emissionSignificance = new BigDecimal(emissionToAdd.getSignificance());
				significanceSumByClass.compute(className, (k, v) -> v == null ? emissionSignificance : v.add(emissionSignificance));
			}
		}
		//
		// 2. Calculate probalitites.
		for (final String className : classNames) {
			//
			final Set<ClassCooccurrence> allEmissions = allEmissionsOfClass.get(className);
			final BigDecimal significanceSum = significanceSumByClass.get(className);
			final Map<WordPair, BigDecimal> resultForClass = result.get(className);
			//
			for (final ClassCooccurrence analysisEmission : allEmissions) {
				if (USE_LOG_PROBABILITES) {
					final double nativeProbability = analysisEmission.getSignificance() / significanceSum.doubleValue(); // TOLOOK Hmpf to doubleConversion...
					final BigDecimal probability = new BigDecimal(MathExt.log2(nativeProbability)).setScale(50, RoundingMode.HALF_UP);
					resultForClass.put(analysisEmission.getCooc(), probability);
				} else {
					final BigDecimal probability = new BigDecimal(analysisEmission.getSignificance()).divide(significanceSum, 50, RoundingMode.HALF_UP); // TOLOOK Make precision configurable?
					resultForClass.put(analysisEmission.getCooc(), probability);
				}
			}
			//
			currentProgess += 1.0;
			progressMonitor.update(currentProgess, finalProgress);
		}
		//
		return result;
	}
}
