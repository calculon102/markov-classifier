package de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.analysis;

import java.util.Collections;
import java.util.List;
import java.util.Map;

enum UnknownViterbiAlgorithmResult implements ViterbiAlgorithmResult {
	INSTANCE;

	private final List<ViterbiValue> probablePath = Collections.emptyList();
	private final Map<String, Double> probabilityByClass = Collections.singletonMap("Unknown", 0.0d); // TRANSLATE

	@Override
	public List<ViterbiValue> probablePath() {
		return probablePath;
	}

	@Override
	public Map<String, Double> probabilityByClass() {
		return probabilityByClass;
	}

}
