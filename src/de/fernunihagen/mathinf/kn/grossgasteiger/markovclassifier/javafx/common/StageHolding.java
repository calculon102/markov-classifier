package de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.javafx.common;

import javafx.stage.Stage;

/**
 * Component that holds a JavaFX-Stage.
 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
 */
public interface StageHolding {

	/**
	 * @return The primary JavaFX-Stage.
	 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
	 */
	Stage getStage();

	/**
	 * @param primaryStage The primary JavaFX-Stage.
	 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
	 */
	void setStage(Stage primaryStage);
}
