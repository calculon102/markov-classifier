package de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.analysis;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

interface ViterbiAlgorithmResult extends Serializable {

	/**
	 * @return The sorted most probable path of classnames with its viterbi-value.
	 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
	 */
	List<ViterbiValue> probablePath();

	/**
	 * @return The normalized probability of each known state/classname. The sum of all values must be 1.
	 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
	 */
	Map<String, Double> probabilityByClass();
}
