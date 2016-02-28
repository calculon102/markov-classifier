package de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.javafx.common;

import static java.util.Objects.requireNonNull;
import javafx.stage.Stage;

/**
 * Abstract base-class for all my controllers.
 *
 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
 */
public abstract class AbstractController implements StageHolding {
	/** Reference to the primary-stage of this app. */
	private Stage primaryStage = null;

	@Override
	public Stage getStage() {
		return primaryStage;
	}

	@Override
	public void setStage(final Stage primaryStage) {
		requireNonNull(primaryStage);
		this.primaryStage = primaryStage;
	}

}
