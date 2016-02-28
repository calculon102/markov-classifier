package de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.javafx;

import java.io.IOException;
import java.util.logging.Logger;

import de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.javafx.about.AboutController;
import de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.javafx.analysis.AnalysisActions;
import de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.javafx.analysis.AnalysisController;
import de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.javafx.common.AbstractController;
import de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.javafx.tasks.TasksController;
import de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.javafx.training.TrainingActions;
import de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.javafx.training.TrainingController;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 *
 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
 */
public final class MainController extends AbstractController implements TrainingActions, AnalysisActions {
	/** Logger-Instance */
	private static final Logger LOGGER = Logger.getLogger(MainController.class.getName());

	/** Reference to the main-tab-pane. */
	@FXML
	private TabPane tabPane;
	/** Reference to the training-tab. */
	@FXML
	private Tab trainingTab;
	/** Reference to the analysis-tab. */
	@FXML
	private Tab analysisTab;

	/** Reference to the analysis-controller to forward menu-actions. */
	@FXML
	private AnalysisController analysisController;
	/** Reference to the training-controller to forward menu-actions. */
	@FXML
	private TrainingController trainingController;
	/** Reference to the tasks-controller. */
	@FXML
	private TasksController tasksController;

	/** hostServices HostServices of parent application. Given shortly after loading of FXML. */
	private HostServices hostServices;

	@FXML
	public void initialize() {
		analysisController.setTrainingController(trainingController);
		analysisController.setTasksController(tasksController);
		analysisController.setTabPabe(tabPane);

		trainingController.setTasksController(tasksController);
	}

	@Override
	public void onNewTraining(final ActionEvent event) {
		tabPane.getSelectionModel().select(trainingTab);
		trainingController.onNewTraining(event);
	}

	@Override
	public void onOpenTraining(final ActionEvent event) {
		tabPane.getSelectionModel().select(trainingTab);
		trainingController.onOpenTraining(event);
	}

	@Override
	public void onImportTrainingData(final ActionEvent event) {
		tabPane.getSelectionModel().select(trainingTab);
		trainingController.onImportTrainingData(event);
	}

	@Override
	public void onSaveTraining(final ActionEvent event) {
		tabPane.getSelectionModel().select(trainingTab);
		trainingController.onSaveTraining(event);
	}

	@Override
	public void onSaveAsTraining(final ActionEvent event) {
		tabPane.getSelectionModel().select(trainingTab);
		trainingController.onSaveAsTraining(event);
	}

	@Override
	public void onAnalyzeInputData(final ActionEvent event) {
		tabPane.getSelectionModel().select(analysisTab);
		analysisController.onAnalyzeInputData(event);
	}

	@Override
	public void onClearAnalyzeResults(final ActionEvent event) {
		tabPane.getSelectionModel().select(analysisTab);
		analysisController.onClearAnalyzeResults(event);
	}

	@Override
	public void onOpenAnalysis(final ActionEvent event) {
		tabPane.getSelectionModel().select(analysisTab);
		analysisController.onOpenAnalysis(event);
	}

	@Override
	public void onSaveAsAnalysis(final ActionEvent event) {
		tabPane.getSelectionModel().select(analysisTab);
		analysisController.onSaveAsAnalysis(event);
	}

	@Override
	public void onExportAnalysis(final ActionEvent event) {
		tabPane.getSelectionModel().select(analysisTab);
		analysisController.onExportAnalysis(event);
	}

	@Override
	public void onAnalyzeSeries(final ActionEvent event) {
		tabPane.getSelectionModel().select(analysisTab);
		analysisController.onAnalyzeSeries(event);
	}

	/**
	 * Shows small "about..." info dialog.
	 * @param event
	 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
	 * @throws IOException
	 */
	@FXML
	private void onAbout(final ActionEvent event) throws IOException {
		final Dialog<Void> dialog = new Dialog<>();
		dialog.setTitle("About this software"); // TRANSLATE
		dialog.setHeaderText(null);

		final FXMLLoader loader = new FXMLLoader(getClass().getResource("about.fxml"));
		final VBox page = (VBox) loader.load();
		final AboutController controller = (AboutController) loader.getController();
		controller.setHostServices(hostServices);

		dialog.getDialogPane().setContent(page);
		dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
		dialog.setResizable(true);
		dialog.showAndWait();
	}

	/**
	 * The program will quit.
	 *
	 * @param event
	 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
	 */
	@FXML
	private void onQuit(final ActionEvent event) {
		LOGGER.info("Exit application...");
		Platform.exit();
	}

	@Override
	public void setStage(final Stage primaryStage) {
		super.setStage(primaryStage);
		trainingController.setStage(primaryStage);
		analysisController.setStage(primaryStage);
		tasksController.setStage(primaryStage);
	}

	/**
	 * @param hostServices HostServices of parent application. Given shortly after loading of FXML.
	 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
	 */
	public void setHostServices(final HostServices hostServices) {
		this.hostServices = hostServices;
	}
}
