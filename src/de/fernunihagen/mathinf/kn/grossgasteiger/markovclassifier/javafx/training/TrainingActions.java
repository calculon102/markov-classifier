package de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.javafx.training;

import javafx.event.ActionEvent;

public interface TrainingActions {

	/**
	 * Opens a new session after disposing the current.
	 * 
	 * @param event
	 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
	 */
	void onNewTraining(ActionEvent event);

	/**
	 * Enables the user to choose a previously saved session. If done, the current session will be discarded.
	 * 
	 * @param event
	 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
	 */
	void onOpenTraining(ActionEvent event);

	/**
	 * The user chooses a folder with training-files in text form. It is assmued, that these files are placed into folders which names give the category of the
	 * texts.
	 * 
	 * @param event
	 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
	 */
	void onImportTrainingData(ActionEvent event);

	/**
	 * The current session is saved to a previously choosen file. If no file was chosen previously (via "Open..." or "Save as..." the user must choose a
	 * filename to save.
	 * 
	 * @param event
	 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
	 */
	void onSaveTraining(ActionEvent event);

	/**
	 * The user must choose a target to save a new session.
	 * 
	 * @param event
	 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
	 */
	void onSaveAsTraining(ActionEvent event);

}