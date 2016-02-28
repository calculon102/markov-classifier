package de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.training;

import java.io.File;

public interface TrainingConfig {
	/**
	 * @return Min-value of a cooccurrence to be significant.
	 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
	 */
	double getCoocSignificanceThreshold();

	/**
	 * @return Maximum files of a directory to analyze.
	 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
	 */
	int getMaxFiles();

	/**
	 * @return Handle to directory to import files from.
	 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
	 */
	File getImportDir();
}
