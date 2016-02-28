package de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.javafx.training;

import static java.util.Objects.requireNonNull;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.javafx.common.AbstractController;
import de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.javafx.common.ChooseDirectoryAction;
import de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.javafx.common.ChooseFileAction;
import de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.javafx.common.NumberTextField;
import de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.javafx.tasks.TasksController;
import de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.training.TrainingConfig;
import de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.training.TrainingSession;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Window;

/**
 * 
 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
 */
public final class TrainingController extends AbstractController implements TrainingActions {
	/** Logger-Instance */
	private static final Logger LOGGER = Logger.getLogger(TrainingController.class.getName());

	private static final int DEFAULT_SIG_COOC_THRESHOLD = 2;

	/** Reference to the Main-Window. Mainly to disable. */
	@FXML
	private BorderPane trainingPane;
	/** TreeTableView with already imported and analyzed classes. */
	@FXML
	private TreeTableView<SignificantCooccurrence> coocsByClassTreeTable;
	/** TreeTableView with word statistics of imported classes. */
	@FXML
	private TreeTableView<WordStatistics> wordsTreeTable;
	/** Button too start import of trainings-data. Referenced here to get the stage within the action-method. */
	@FXML
	private Button importButton;
	/** Slider to configure the significance-threshold of detected coorcurences. */
	@FXML
	private NumberTextField coocsThresholdSlider;
	/** Label for counting words int the wordsTreeTable. */
	@FXML
	private Label wordCountLabel;
	/** Maximum number of files to import. */
	@FXML
	private NumberTextField maxFilesField;

	/** Reference to current tasks-controller to start tasks. */
	@FXML
	private TasksController tasksController;

	/** Locale-specific resources. */
	@FXML
	private ResourceBundle resources;

	/** Reference to the Markov-Classification-Model. */
	private TrainingSession trainingSession = new TrainingSession();
	/** Widget for user to choose directory. (JavaFX-Directory-Chooser with remmeber-feature) */
	private ChooseDirectoryAction importDirChooseAction;
	/** Widget for user to choose directory. (JavaFX-Directory-Chooser with remmeber-feature) */
	private ChooseFileAction chooseSessionFileAction;

	@FXML
	public void initialize() {
		importDirChooseAction = new ChooseDirectoryAction(resources.getString("training.import.choose.title"));
		chooseSessionFileAction = new ChooseFileAction(resources.getString("training.session.select.file"), ".training");
		//
		final String countLabel = resources.getString("training.words.count");
		wordsTreeTable.setRoot(new TreeItem<>(WordStatistics.asWordRow("root")));
		wordsTreeTable.getRoot().getChildren().addListener(new InvalidationListener() {
			@Override
			public void invalidated(final Observable observable) {
				if (observable instanceof ObservableList<?>) {
					final ObservableList<?> list = (ObservableList<?>) observable;
					Platform.runLater(() -> wordCountLabel.setText(countLabel + " " + Integer.valueOf(list.size()).toString()));
				}
			}
		});
	}

	/**
	 * Tells this controller of the session to manipulate and tell of the actions.
	 * 
	 * @param session The current session-object.
	 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
	 */
	public void setTrainingSession(final TrainingSession session) {
		requireNonNull(session);
		//
		LOGGER.info("Replacing training-session in model und view.");
		//
		this.trainingSession = session;
		//
		final Set<String> classes = trainingSession.getClasses();
		final Map<String, List<SignificantCooccurrence>> coocBeansByName = new HashMap<>(classes.size());
		for (final String className : classes) {
			coocBeansByName.put(className, CoocsTreeTable.convertCoocsToBeans(trainingSession.getCooccurrencesReader(className)));
		}
		//
		Platform.runLater(() -> {
			if (coocsByClassTreeTable != null) {
				CoocsTreeTable.clear(coocsByClassTreeTable);
				CoocsTreeTable.loadFromSession(coocsByClassTreeTable, wordsTreeTable, coocBeansByName, trainingSession, resources);
			}
			if (coocsThresholdSlider != null) {
				coocsThresholdSlider.setText(String.valueOf(DEFAULT_SIG_COOC_THRESHOLD));
			}
		});
	}

	/**
	 * @return Current training-session-object.
	 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
	 */
	public TrainingSession getTrainingSession() {
		return trainingSession;
	}

	/**
	 * @see de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.javafx.training.TrainingActions#onNewTraining(javafx.event.ActionEvent)
	 */
	@Override
	public void onNewTraining(final ActionEvent event) {
		final Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.setTitle(resources.getString("training.new.confirm.title"));
		alert.setHeaderText(resources.getString("training.new.confirm.title"));
		final Label contentText = new Label(resources.getString("training.new.confirm.desc"));
		contentText.setWrapText(true);
		alert.getDialogPane().setContent(contentText);

		final Optional<ButtonType> result = alert.showAndWait();
		if (result.orElse(ButtonType.CANCEL) != ButtonType.OK) {
			return;
		}
		//
		LOGGER.info("Clearing training-data.");
		//
		// Reset session-object
		this.trainingSession = new TrainingSession();
		//
		// Reset view
		CoocsTreeTable.clear(coocsByClassTreeTable);
		//
		wordsTreeTable.getRoot().getChildren().clear();
		//
		getStage().setTitle(resources.getString("app.title") + " - " + resources.getString("training.session.unsaved.title"));
	}

	/**
	 * @see de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.javafx.training.TrainingActions#onOpenTraining(javafx.event.ActionEvent)
	 */
	@Override
	public void onOpenTraining(final ActionEvent event) {
		final Window window = trainingPane.getScene().getWindow();
		final File fileToOpen = chooseSessionFileAction.open(window);
		//
		if (fileToOpen == null) {
			return;
		}
		//
		final LoadTrainingSessionTask loadTrainingSessionTask = new LoadTrainingSessionTask(fileToOpen, trainingPane, this, resources);
		tasksController.run(loadTrainingSessionTask);
	}

	/**
	 * @see de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.javafx.training.TrainingActions#onSaveTraining(javafx.event.ActionEvent)
	 */
	@Override
	public void onSaveTraining(final ActionEvent event) {
		final File lastChoosenFile = chooseSessionFileAction.getLastFile();
		//
		if (lastChoosenFile == null) {
			onSaveAsTraining(event);
			return;
		}
		//
		saveCurrentTrainingSessionToFile(lastChoosenFile);
	}

	/**
	 * @see de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.javafx.training.TrainingActions#onSaveAsTraining(javafx.event.ActionEvent)
	 */
	@Override
	public void onSaveAsTraining(final ActionEvent event) {
		final Window window = trainingPane.getScene().getWindow();
		final File fileToSave = chooseSessionFileAction.save(window);
		//
		if (fileToSave == null) {
			return;
		}
		//
		saveCurrentTrainingSessionToFile(fileToSave);
	}

	/**
	 * @see de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.javafx.training.TrainingActions#onImportTrainingData(javafx.event.ActionEvent)
	 */
	@Override
	public void onImportTrainingData(final ActionEvent event) {
		final Window window = trainingPane.getScene().getWindow();
		final File importDir = importDirChooseAction.start(window);
		//
		if (importDir == null) {
			return;
		}
		//
		LOGGER.log(Level.INFO, "Load training-session from file {0}.", importDir.getAbsolutePath());
		//
		final TrainingConfig config = new TrainingConfig() {
			@Override
			public double getCoocSignificanceThreshold() {
				return coocsThresholdSlider.getValue();
			}

			@Override
			public int getMaxFiles() {
				return Integer.valueOf(maxFilesField.getText());
			}

			@Override
			public File getImportDir() {
				return importDir;
			}
		};
		//
		ImportDataAction.start(config, trainingSession, coocsByClassTreeTable, wordsTreeTable, tasksController, resources);
	}

	@FXML
	private void onKeyPressedCoocTable(final KeyEvent event) {
		if (!event.getCode().equals(KeyCode.DELETE)) {
			return;
		}

		final TreeItem<SignificantCooccurrence> item = coocsByClassTreeTable.getSelectionModel().getSelectedItem();
		if (item == null || item.getParent() == null) {
			return;
		}
		//
		final String classToRemove = item.getValue().classNameProperty().get();
		//
		LOGGER.log(Level.INFO, "Remove class {0} from training.", classToRemove);
		//
		trainingSession.removeClassification(classToRemove);
		//
		// Preserve TreeItem-Beans to avoid recalculation.
		final ObservableList<TreeItem<SignificantCooccurrence>> classItems = coocsByClassTreeTable.getRoot().getChildren();
		final Map<String, List<SignificantCooccurrence>> coocBeansByName = new HashMap<>(classItems.size());
		for (final TreeItem<SignificantCooccurrence> treeItem : classItems) {
			final String className = treeItem.getValue().classNameProperty().get();
			if (className.equals(classToRemove)) {
				continue;
			}
			//
			final ObservableList<TreeItem<SignificantCooccurrence>> coocItems = treeItem.getChildren();
			final List<SignificantCooccurrence> coocBeans = new ArrayList<>(coocItems.size());
			for (final TreeItem<SignificantCooccurrence> coocItem : coocItems) {
				coocBeans.add(coocItem.getValue());
			}
			coocBeansByName.put(className, coocBeans);
		}
		//
		coocsByClassTreeTable.getRoot().getChildren().clear();
		wordsTreeTable.getRoot().getChildren().clear();
		//
		CoocsTreeTable.loadFromSession(coocsByClassTreeTable, wordsTreeTable, coocBeansByName, trainingSession, resources);
	}

	/**
	 * Gives the current session in state to persistence-layer for saving.
	 * @param fileToSave File-Path to save
	 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
	 */
	private void saveCurrentTrainingSessionToFile(final File fileToSave) {
		requireNonNull(fileToSave);
		//
		LOGGER.log(Level.INFO, "Save training-session to file {0}.", fileToSave.getAbsolutePath());
		final SaveTrainingSessionTask saveTrainingSessionTask = new SaveTrainingSessionTask(fileToSave, trainingPane, this, resources);
		tasksController.run(saveTrainingSessionTask);
	}

	/**
	 * @param tasksController Reference to current tasks-controller start tasks.
	 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
	 */
	public void setTasksController(final TasksController tasksController) {
		requireNonNull(tasksController);
		this.tasksController = tasksController;
	}

	public double getSignifianceThreshold() {
		return coocsThresholdSlider.getValue();
	}
}
