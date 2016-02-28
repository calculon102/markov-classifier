package de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.javafx.training;

import java.io.File;
import java.io.IOException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.common.SerializableSession;
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
public final class SaveTrainingSessionTask extends AbstractTask<TrainingSession> {
	/** Logger-Instance */
	private static final Logger LOGGER = Logger.getLogger(SaveTrainingSessionTask.class.getName());

	/** File-handle to save training-session to. */
	private final File trainingFileToSave;
	/** Parent window to disable while loading. */
	private final Node parent;
	/** ResourceBundle with localilzed labels. */
	private final ResourceBundle labels;
	/** Holds reference to the javafx-stage */
	private final TrainingController trainingController;

	public SaveTrainingSessionTask(final File trainingFileToSave, final Node parent, final TrainingController trainingController, final ResourceBundle labels) {
		super("Load training-session from " + trainingFileToSave.getName()); // TRANSLATE
		//
		this.trainingFileToSave = trainingFileToSave;
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
	protected TrainingSession call() throws Exception {
		try {
			Platform.runLater(() -> {
				parent.getScene().setCursor(Cursor.WAIT);
				parent.setDisable(true);
			});
			//
			updateMessage("Saving..."); // TRANSLATE
			//
			final SerializableSession<TrainingSession> serializableSession = new SerializableSession<>(trainingController.getTrainingSession());
			serializableSession.save(trainingFileToSave, this);
			//
			if (!isCancelled()) {
				Platform.runLater(() -> trainingController.getStage().setTitle(labels.getString("app.title") + " - " + trainingFileToSave.getName()));
			}
		} catch (final IOException e) {
			final Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle(labels.getString("training.session.save.error"));
			alert.setHeaderText(labels.getString("training.session.save.error"));
			alert.setContentText(e.getLocalizedMessage());
			alert.showAndWait();
			//
			LOGGER.log(Level.SEVERE, labels.getString("training.session.save.error"), e);
		} finally {
			Platform.runLater(() -> {
				parent.getScene().setCursor(Cursor.DEFAULT);
				parent.setDisable(false);
			});
		}
		//
		return null;
	}

}
