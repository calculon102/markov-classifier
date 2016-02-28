package de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.analysis;

import java.io.File;
import java.util.List;

public final class AnalysisSeriesSettings {
	private final File baseDirectory;
	private final List<AnalysisSettings> settings;

	public AnalysisSeriesSettings(final File baseDirectory, final List<AnalysisSettings> settings) {
		this.baseDirectory = baseDirectory;
		this.settings = settings;
	}

	public File baseDirectory() {
		return baseDirectory;
	}

	public List<AnalysisSettings> settings() {
		return settings;
	}
}
