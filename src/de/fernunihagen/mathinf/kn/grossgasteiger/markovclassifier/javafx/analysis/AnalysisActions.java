package de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.javafx.analysis;

import javafx.event.ActionEvent;

public interface AnalysisActions {

	/**
	 * The user chooses a set of input files which are separately testet against the Hidden-Markov-Models of all known classes. The results are put into the
	 * result-Table.
	 * 
	 * @param event
	 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
	 */
	void onAnalyzeInputData(ActionEvent event);

	/**
	 * The results-table is emptied.
	 * 
	 * @param event
	 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
	 */
	void onClearAnalyzeResults(ActionEvent event);

	/**
	 * A previously save (serialized) analysis-session is loaded. The current results are emptied!
	 * @param event
	 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
	 */
	void onOpenAnalysis(ActionEvent event);

	/**
	 * The current analysis-session is saved.
	 * @param event
	 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
	 */
	void onSaveAsAnalysis(ActionEvent event);

	/**
	 * The current-analysis session is exported to clipboard.
	 * @param event
	 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
	 */
	void onExportAnalysis(ActionEvent event);

	/**
	 * May start a series of analyzations via dialog.
	 * @param event
	 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
	 */
	void onAnalyzeSeries(ActionEvent event);
}