package de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.javafx.training;

import java.io.File;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.common.SavedSession;
import de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.javafx.tasks.AbstractTask;
import de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.training.TrainingSession;
import javafx.application.Platform;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

/**
 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
 */
public final class LoadTrainingSessionTask extends AbstractTask<Optional<TrainingSession>> {
	/** Logger-Instance */
	private static final Logger LOGGER = Logger.getLogger(LoadTrainingSessionTask.class.getName());

	/** File-handle to load training-session from. */
	private final File trainingFileToLoad;
	/** Parent window to disable while loading. */
	private final Node parent;
	/** ResourceBundle with localilzed labels. */
	private final ResourceBundle labels;
	/** Holds reference to the javafx-stage */
	private final TrainingController trainingController;

	public LoadTrainingSessionTask(final File trainingFileToLoad, final Node parent, final TrainingController trainingController, final ResourceBundle labels) {
		super("Load training-session from " + trainingFileToLoad.getName()); // TRANSLATE
		//
		this.trainingFileToLoad = trainingFileToLoad;
		this.parent = parent;
		this.trainingController = trainingController;
		this.labels = labels;
		//
		this.setOnCancelled(e -> {
			parent.getScene().setCursor(Cursor.DEFAULT);
			parent.setDisable(false);
		});
	}

	@Override
	public ResourceBundle labels() {
		return labels;
	}

	@Override
	protected Optional<TrainingSession> call() throws Exception {
		try {
			Platform.runLater(() -> {
				parent.getScene().setCursor(Cursor.WAIT);
				parent.setDisable(true);
			});
			//
			updateMessage("Loading..."); // TRANSLATE
			final SavedSession<TrainingSession> savedSession = new SavedSession<>(trainingFileToLoad);
			final Optional<TrainingSession> loadedSession = savedSession.load(this);
			//
			if (!loadedSession.isPresent()) { // Was cancelled
				return loadedSession;
			}
			//
			updateMessage("Building tables (may stall UI)..."); // TRANSLATE
			Thread.sleep(100); // Ensure message is updated before invoking heavy operations on tables...
			trainingController.setTrainingSession(loadedSession.get());
			Platform.runLater(() -> trainingController.getStage().setTitle(labels.getString("app.title") + " - " + trainingFileToLoad.getName()));
			//
			return loadedSession;
		} catch (final Exception e) {
			final Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle(labels.getString("training.session.open.error"));
			alert.setHeaderText(e.getLocalizedMessage());
			alert.showAndWait();
		} finally {
			Platform.runLater(() -> {
				parent.getScene().setCursor(Cursor.DEFAULT);
				parent.setDisable(false);
			});
		}
		//
		LOGGER.info("Loaded training-data-from file " + trainingFileToLoad);
		//
		return Optional.empty();
	}

}
