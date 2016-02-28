package de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.javafx.analysis;

import static java.util.Objects.requireNonNull;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.analysis.AnalysisSeriesSettings;
import de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.analysis.AnalysisSession;
import de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.analysis.AnalysisSettings;
import de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.javafx.common.AbstractController;
import de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.javafx.common.ChooseDirectoryAction;
import de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.javafx.common.ChooseFileAction;
import de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.javafx.common.NumberTextField;
import de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.javafx.tasks.TasksController;
import de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.javafx.training.TrainingController;
import de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.training.TrainingSession;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Slider;
import javafx.scene.control.TabPane;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Window;

/**
 * Controller for the analysis-tab in GUI.
 *
 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
 */
public final class AnalysisController extends AbstractController implements AnalysisActions {
	/** Logger-Instance */
	private static final Logger LOGGER = Logger.getLogger(AnalysisController.class.getName());

	/** Reference to the Main-Window. Mainly to disable. */
	@FXML
	private BorderPane analysisPane;
	/** Table with the results of the last input to analyze. */
	@FXML
	private TreeTableView<AnalysisResultBean> resultsTable;
	/** Slider to set the significance-value of gaps. */
	@FXML
	private Slider voidSignificanceSlider;
	/** Slider to set the significance of known emissions not-existent in specific states. */
	@FXML
	private Slider pseudoCountSlider;
	/** Maximum number of files to import. */
	@FXML
	private NumberTextField maxFilesField;

	/** Resources set in {@link #initialize(URL, ResourceBundle)}. */
	@FXML
	private ResourceBundle resources;

	/** Dialog to configure analysis-series and start them. Remembers previous values. */
	private AnalysisSeriesDialog analysisSeriesDialog;
	/** Widget for user to choose directory. (JavaFX-Directory-Chooser with remmeber-feature) */
	private ChooseDirectoryAction dataDirChooseAction;
	/** Widget for user to choose directory. (JavaFX-Directory-Chooser with remmeber-feature) */
	private ChooseFileAction chooseAnalysisFileAction;

	/** Current analysis-session. */
	private final AnalysisSession analysisSession = new AnalysisSession();

	/** Reference to current training-controller to get the current training-session. */
	private TrainingController trainingController;
	/** Reference to current tasks-controller to start tasks. */
	private TasksController tasksController;
	/** Reference to the parent tab-pane to add analysis-results on demand. */
	private TabPane tabPane;

	@FXML
	public void initialize() {
		TreeItem<AnalysisResultBean> root = resultsTable.getRoot();
		if (root == null) {
			root = new TreeItem<>(AnalysisResultBean.createRoot("root"));
			resultsTable.setRoot(root);
		}

		// Initialize Dir-Chooser with label.
		analysisSeriesDialog = new AnalysisSeriesDialog(getStage(), resources);
		dataDirChooseAction = new ChooseDirectoryAction(resources.getString("analysis.input.choose.title"));
		chooseAnalysisFileAction = new ChooseFileAction(resources.getString("analysis.session.select.file"), ".result");

		voidSignificanceSlider.valueProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(final ObservableValue<? extends Number> observable, final Number oldValue, final Number newValue) {
				LOGGER.info("Set void-significance from " + oldValue + " to " + newValue);
			}
		});

		pseudoCountSlider.valueProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(final ObservableValue<? extends Number> observable, final Number oldValue, final Number newValue) {
				LOGGER.info("Set pseudo-count from " + oldValue + " to " + newValue);
			}
		});
	}

	@Override
	public void onAnalyzeInputData(final ActionEvent event) {
		try {
			LOGGER.fine("onAnalyzeInputData-action startet.");
			//
			final TrainingSession trainingSession = trainingController.getTrainingSession();
			requireNonNull(trainingSession);
			//
			if (trainingSession.isEmpty()) {
				final Alert info = new Alert(AlertType.WARNING);
				info.setTitle(resources.getString("analysis.info.noTrainingData.title"));
				info.setHeaderText(resources.getString("analysis.info.noTrainingData.head"));
				info.showAndWait();
				return;
			}
			//
			final File importDir = dataDirChooseAction.start(analysisPane.getScene().getWindow());
			if (importDir == null) {
				LOGGER.fine("onAnalyzeInputData-action cancelled.");
				return;
			}
			//
			final AnalysisSettings settings = new AnalysisSettings(trainingController.getSignifianceThreshold(), voidSignificanceSlider.getValue(), pseudoCountSlider.getValue());
			analyzeFilesInFolder(trainingSession, importDir, settings);
		} catch (final IOException ex) {
			LOGGER.log(Level.SEVERE, "Error reading analyize-files.", ex);
		}
	}

	@Override
	public void onClearAnalyzeResults(final ActionEvent event) {
		LOGGER.info("Clearing result-table.");
		//
		final TreeItem<AnalysisResultBean> root = this.resultsTable.getRoot();
		if (root != null) {
			this.resultsTable.getRoot().getChildren().clear();
		}
	}

	@Override
	public void onOpenAnalysis(final ActionEvent event) {
		final Window window = analysisPane.getScene().getWindow();
		final File fileToOpen = chooseAnalysisFileAction.open(window);
		//
		if (fileToOpen == null) {
			return;
		}
		//
		LOGGER.info("Load result-data-from file " + fileToOpen);
		final LoadAnalysisSessionTask loadAnalysisSessionTask = new LoadAnalysisSessionTask(fileToOpen, analysisPane, resultsTable, resources);
		tasksController.run(loadAnalysisSessionTask);
	}

	@Override
	public void onSaveAsAnalysis(final ActionEvent event) {
		final Window window = analysisPane.getScene().getWindow();
		final File saveFile = chooseAnalysisFileAction.save(window);
		//
		if (saveFile == null) {
			return;
		}
		//
		LOGGER.info("Save result-data to file " + saveFile);
		//
		final SaveAnalysisSessionTask saveAnalysisSessionTask = new SaveAnalysisSessionTask(saveFile, analysisPane, resultsTable, resources);
		tasksController.run(saveAnalysisSessionTask);
	}

	@Override
	public void onExportAnalysis(final ActionEvent event) {
		LOGGER.log(Level.INFO, "Copy complete result-table to clip-board");
		//
		final StringBuilder clipBoardString = new StringBuilder();
		final ObservableList<TreeItem<AnalysisResultBean>> children = resultsTable.getRoot().getChildren();
		boolean isFirst = true;
		for (final TreeItem<AnalysisResultBean> treeItem : children) {
			if (!isFirst) {
				clipBoardString.append(System.lineSeparator());
			} else {
				isFirst = false;
			}
			clipBoardString.append(createClipboardString(treeItem));
		}

		final Clipboard clipboard = Clipboard.getSystemClipboard();
		final ClipboardContent content = new ClipboardContent();
		content.putString(clipBoardString.toString());
		clipboard.setContent(content);
	}

	@Override
	public void onAnalyzeSeries(final ActionEvent event) {
		// Check training-session // TOFIX DRY
		final TrainingSession trainingSession = trainingController.getTrainingSession();
		requireNonNull(trainingSession);
		//
		if (trainingSession.isEmpty()) {
			final Alert info = new Alert(AlertType.WARNING);
			info.setTitle(resources.getString("analysis.info.noTrainingData.title"));
			info.setHeaderText(resources.getString("analysis.info.noTrainingData.head"));
			info.showAndWait();
			return;
		}
		//
		final Optional<AnalysisSeriesSettings> dialogSettings = analysisSeriesDialog.showAndWait();
		if (!dialogSettings.isPresent()) {
			return;
		}
		//
		// Start master-tasks...
		final List<AnalysisSettings> seriesSettings = dialogSettings.get().settings();
		final File baseDirectory = dialogSettings.get().baseDirectory();
		final List<AnalysisMasterTask> masterTasks = new ArrayList<>(9);
		//
		LOGGER.log(Level.INFO, "Starting series of analyze-tasks on folder {0}.", baseDirectory.getAbsolutePath());
		//
		try {
			final List<Path> folders = Files.walk(baseDirectory.toPath(), 1).filter(p -> !p.toFile().equals(baseDirectory) && p.toFile().isDirectory()).sorted().collect(Collectors.toList());
			for (final AnalysisSettings settings : seriesSettings) {
				if (LOGGER.isLoggable(Level.FINE)) {
					LOGGER.fine("Analyze " + folders.size() + " folders in " + baseDirectory.getAbsolutePath() + " with settings " + settings);
				}
				//
				for (final Path path : folders) {
					masterTasks.add(new AnalysisMasterTask(trainingSession, settings, path.toFile(), maxFilesField.getValue(), resultsTable.getRoot(), analysisSession, tasksController, resources));
				}
			}
			// Cascade tasks logically. Their build-up must not intersect!
			for (int i = 0; i < masterTasks.size(); i++) {
				if (i + 1 < masterTasks.size()) {
					final int nextIndex = i + 1;
					masterTasks.get(i).setNextTask(masterTasks.get(nextIndex));
				}
			}
			//
			tasksController.run(masterTasks.get(0));
		} catch (final IOException ex) {
			LOGGER.log(Level.SEVERE, "Error while analyzing file", ex);
		}
	}

	/**
	 * @param trainingController Reference to current training-controller to get the current training-session.
	 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
	 */
	public void setTrainingController(final TrainingController trainingController) {
		requireNonNull(trainingController);
		this.trainingController = trainingController;
	}

	/**
	 * @param tasksController Reference to current tasks-controller start tasks.
	 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
	 */
	public void setTasksController(final TasksController tasksController) {
		requireNonNull(tasksController);
		this.tasksController = tasksController;
	}

	/**
	 * @param TabPane Reference to the parent tab-pane to add analysis-results on demand.
	 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
	 */
	public void setTabPabe(final TabPane tabPane) {
		requireNonNull(tabPane);
		this.tabPane = tabPane;
	}

	@FXML
	private void onMouseClickedResultTable(final MouseEvent event) {
		if (event.getClickCount() != 2) {
			return;
		}
		//
		final TreeItem<AnalysisResultBean> item = resultsTable.getSelectionModel().getSelectedItem();
		if (!item.getValue().hasDetails()) {
			return;
		}
		//
		LOGGER.log(Level.INFO, "Open detail-tab for row {0} from result-list.", resultsTable.getSelectionModel().getSelectedIndex());
		//
		Platform.runLater(() -> {
			final AnalysisDetailsTab analysisDetailsTab = new AnalysisDetailsTab(item.getValue(), resources);
			tabPane.getTabs().add(analysisDetailsTab);
			tabPane.getSelectionModel().select(analysisDetailsTab);
		});
	}

	@FXML
	private void onKeyPressedResultTable(final KeyEvent event) {
		// Copy current row into clipboard
		if (event.isControlDown() && event.getCode().equals(KeyCode.C)) {
			final TreeItem<AnalysisResultBean> item = resultsTable.getSelectionModel().getSelectedItem();
			if (item == null || item.getValue() == null) {
				return;
			}

			final String clipboardString = createClipboardString(item);
			if (clipboardString == null || clipboardString.isEmpty()) {
				return;
			}
			//
			LOGGER.log(Level.INFO, "Copy row {0} from result-list to clip-board.", resultsTable.getSelectionModel().getSelectedIndex());
			//
			final Clipboard clipboard = Clipboard.getSystemClipboard();
			final ClipboardContent content = new ClipboardContent();
			content.putString(clipboardString);
			clipboard.setContent(content);

			return;
		}

		if (event.getCode().equals(KeyCode.DELETE)) {
			final TreeItem<AnalysisResultBean> item = resultsTable.getSelectionModel().getSelectedItem();
			if (item == null || item.getParent() == null) {
				return;
			}
			//
			LOGGER.log(Level.INFO, "Removing row {0} from result-list.", resultsTable.getSelectionModel().getSelectedIndex());
			//
			item.getParent().getChildren().remove(item);
		}
	}

	/**
	 * Starts analyzation master-task for given directory with current training-session and configuration.
	 * @param trainingSession
	 * @param folderToAnalyze
	 * @param settings
	 * @throws IOException
	 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
	 */
	private void analyzeFilesInFolder(final TrainingSession trainingSession, final File folderToAnalyze, final AnalysisSettings settings) throws IOException {
		LOGGER.log(Level.INFO, "Analyzing up to {0} files in folder {1} with settings {2}", new Object[] { maxFilesField.getValue(), folderToAnalyze.getAbsolutePath(), settings });
		//
		final AnalysisMasterTask analysisMasterTask = new AnalysisMasterTask(trainingSession, settings, folderToAnalyze, maxFilesField.getValue(), resultsTable.getRoot(), analysisSession, tasksController, resources);
		tasksController.run(analysisMasterTask);
	}

	/**
	 * @param treeItem TreeItem-Row to convert.
	 * @return Converts nearly all values of the treeitem to a one-line string, tab-seperated, to by copied to spreadsheet-apps like Excel.
	 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
	 */
	private String createClipboardString(final TreeItem<AnalysisResultBean> treeItem) {
		final AnalysisResultBean groupBean = treeItem.getValue();
		if (!groupBean.isGroup()) {
			return "";
		}

		final NumberFormat numberFormat = NumberFormat.getInstance(Locale.GERMAN); // TODO Locale konfigurierbar machen.

		final ObservableList<TreeItem<AnalysisResultBean>> children = treeItem.getChildren();
		final Map<String, Integer> classWins = new HashMap<>();
		children.forEach(c -> classWins.compute(c.getValue().classNameProperty().get(), (k, v) -> (v == null) ? 1 : v + 1));

		final String filename = groupBean.filenameProperty().get() != null ? groupBean.filenameProperty().get() : "   ";

		final List<String> classes = groupBean.getTrainingClasses();
		final StringBuilder classResults = new StringBuilder();
		for (final String className : classes) {
			final Integer wins = classWins.get(className);
			final double count = wins == null ? 0.0d : (double) wins / children.size();
			classResults.append(numberFormat.format(count)).append("\t");
		}

		return numberFormat.format(groupBean.getAnalysisSettings().minSig()) + "\t"
				+ numberFormat.format(groupBean.getAnalysisSettings().voidSig()) + "\t"
				+ numberFormat.format(groupBean.getAnalysisSettings().pseudoSig()) + "\t"
				+ filename.substring(1, filename.length() - 2) + "\t"
				+ groupBean.classNameProperty().get() + "\t"
				+ numberFormat.format(groupBean.scoreProperty().get()) + "\t"
				+ classResults.toString()
				+ groupBean.wordOrderClassProperty().get() + "\t"
				+ (!Double.isNaN(groupBean.wordOrderPercentageProperty().get()) ? numberFormat.format(groupBean.wordOrderPercentageProperty().get() / 100) : "");
	}
}
