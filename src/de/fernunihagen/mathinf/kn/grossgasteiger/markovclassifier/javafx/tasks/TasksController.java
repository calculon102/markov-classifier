package de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.javafx.tasks;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.javafx.common.AbstractController;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TableView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;

/**
 * Controller for the tasks-pane.
 *
 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
 */
public final class TasksController extends AbstractController {
	/** Logger-Instance */
	private static final Logger LOGGER = Logger.getLogger(TasksController.class.getName());

	/** Access of items in the task-table is ansycnhronous. This leads to weird effects on the underlying collection. Better synchronize add and remove here. */
	private static final Object TABLE_MUTEX = new Object();

	@FXML
	private BorderPane pane;
	@FXML
	private TableView<AbstractTask<?>> table;
	@FXML
	private Label taskCounter;
	@FXML
	private Slider maxThreads;
	@FXML
	private ResourceBundle resources;

	/** Executor-Service for analyzing-threads. Initially set in {@link #initialize(URL, ResourceBundle)}. */
	private ExecutorService executor;

	@FXML
	public void initialize() {

		// Initialize Max-Executor-Threads-Slider
		final int availableProcessors = Runtime.getRuntime().availableProcessors();
		setMaxExecutorThreads(availableProcessors);
		maxThreads.setValue(availableProcessors);
		//
		maxThreads.valueProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(final ObservableValue<? extends Number> observable, final Number oldValue, final Number newValue) {
				if (maxThreads.isValueChanging()) {
					return; // Only when finished
				}
				//
				setMaxExecutorThreads(newValue.intValue());
			}
		});

		final String countLabel = resources.getString("tasks.table.count");
		table.getItems().addListener(new InvalidationListener() {
			@Override
			public void invalidated(final Observable observable) {
				if (observable instanceof ObservableList<?>) {
					final ObservableList<?> list = (ObservableList<?>) observable;
					Platform.runLater(() -> taskCounter.setText(countLabel + " " + Integer.valueOf(list.size()).toString()));
				}
			}
		});
	}

	@FXML
	private void onKeyPressedTable(final KeyEvent event) {
		if (!event.getCode().equals(KeyCode.DELETE)) {
			return;
		}

		final AbstractTask<?> task = table.getSelectionModel().getSelectedItem();
		if (task == null) {
			return;
		}

		LOGGER.log(Level.INFO, "Removing/cancel task {0}.", task.nameProperty().get());

		task.cancel();

		synchronized (TABLE_MUTEX) {
			table.getItems().remove(task);
		}
	}

	/**
	 * Queue the given task to run in the thread-pool of the executor according to the user-set max-thread-count. Will add the given task to the task-table to
	 * be monitored by the user.
	 * @param task Task to run.
	 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
	 */
	public void run(final AbstractTask<?> task) {
		LOGGER.log(Level.INFO, "Executing task {0}.", task.nameProperty().get());
		//
		synchronized (TABLE_MUTEX) {
			table.getItems().add(task);
		}
		//
		task.setOnSucceeded(event -> {
			synchronized (TABLE_MUTEX) {
				table.getItems().remove(task);
			}
		});
		//
		task.setOnFailed(event -> {
			synchronized (TABLE_MUTEX) {
				table.getItems().remove(task);
			}
		});
		//
		executor.execute(task);
	}

	/**
	 * Resets the instance of the analyzeExecutor to an instance with given max Thread-count.
	 * @param nexMaxThreads
	 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
	 */
	protected void setMaxExecutorThreads(final int nexMaxThreads) {
		if (nexMaxThreads < 1) {
			throw new IllegalArgumentException("Max executor-threads must be positve!");
		}
		//
		if (executor != null) {
			executor.shutdown();
		}
		//
		executor = Executors.newFixedThreadPool(nexMaxThreads, new ThreadFactory() {
			@Override
			public Thread newThread(final Runnable r) {
				final Thread t = new Thread(r);
				t.setDaemon(true);
				return t;
			}
		});
		//
		LOGGER.log(Level.INFO, "Max executor-threads set to {0}.", nexMaxThreads);
	}
}
