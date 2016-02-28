package de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.javafx.tasks;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.common.ProgressMonitor;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Modality;

/**
 * Base-class for all tasks within this app. Additional with Name, status-text and a start-time.
 *
 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
 * @param <V>
 */
public abstract class AbstractTask<V> extends Task<V> implements ProgressMonitor {
	private static final Logger LOGGER = Logger.getLogger(AbstractTask.class.getName());

	/** Name of this task */
	private final StringProperty name = new SimpleStringProperty();
	/** Start-time of this task. */
	private final ObjectProperty<LocalTime> startTime = new SimpleObjectProperty<>(LocalTime.now());
	/** Start-date of this task. */
	private final ObjectProperty<LocalDate> startDate = new SimpleObjectProperty<>(LocalDate.now());

	public AbstractTask(final String name) {
		super();
		this.name.set(name);
		this.update("Pending"); // TRANSLATE
		//
		setOnFailed(e -> Platform.runLater(() -> {
			final Throwable ex = AbstractTask.this.getException();
			final Alert alert;
			if (ex != null) {
				LOGGER.log(Level.SEVERE, "Exception on task '" + name + "'!", ex);
				ex.printStackTrace();
				alert = new Alert(AlertType.ERROR, "Exception on task: '" + name + "'." + ex.toString());
			} else {
				alert = new Alert(AlertType.ERROR, "Task '" + name + "' failed for unknown reasons...");
			}
			alert.initModality(Modality.APPLICATION_MODAL);
			alert.showAndWait();
		}));
	}

	public final ReadOnlyStringProperty nameProperty() {
		return name;
	}

	public final ReadOnlyObjectProperty<LocalTime> startTimeProperty() {
		return startTime;
	}

	public final ReadOnlyObjectProperty<LocalDate> startDateProperty() {
		return startDate;
	}

	@Override
	public void update(final double workDone, final double max) {
		updateProgress(workDone, max);
	}

	@Override
	public void update(final String message) {
		updateMessage(message);
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + ": " + this.name.get() + " (" + this.getState() + ")";
	}
}
