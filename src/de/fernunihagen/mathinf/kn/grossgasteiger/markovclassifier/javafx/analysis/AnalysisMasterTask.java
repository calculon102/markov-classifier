package de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.javafx.analysis;

import java.io.File;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.analysis.AnalysisMarkovModell;
import de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.analysis.AnalysisSession;
import de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.analysis.AnalysisSettings;
import de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.analysis.TrainedWordNeighbours;
import de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.javafx.tasks.AbstractTask;
import de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.javafx.tasks.TasksController;
import de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.training.TrainingSession;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;

/**
 * Master task-creates markov-modell und further analysis-tasks based on it.
 *
 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
 */
public class AnalysisMasterTask extends AbstractTask<Void> {
	/** Logger-Instance */
	private static final Logger LOGGER = Logger.getLogger(AnalysisMasterTask.class.getName());

	/** Fixed state of a training-session to base the generation of the markov-modell on. */
	private final TrainingSession trainingSession;
	/** Significance-threshold that was used to generate the training-data. Is only used for statistics. */
	private final double minSignificance;
	/** Sentences without any known cooccurence will be taken as "void"-emission. This is their fixed significance value. */
	private final double voidSignificance;
	/** Known cooccurrence will not be significant for all known states. This is their pseudo-count significance to avoid zero-probabilites. */
	private final double pseudoCountSignificance;
	/** List of files to analyze. */
	private final File folderToAnalyze;
	/** Reference to the GUI-element holding the analysis results. Will be feeded by the sub-tasks. */
	private final TreeItem<AnalysisResultBean> treetableRoot;
	/** Current state/reference to the analysis session. */
	private final AnalysisSession analysisSession;
	/** Reference to the task-controller to create sub-tasks. */
	private final TasksController tasksController;
	/** Language-specific labels. */
	private final ResourceBundle labels;
	/** Maximum number of files to read per directory. */
	private final int fileLimit;
	/** Task to execute when this one succeeded. May be null. */
	private AbstractTask<?> nextTask = null;

	/**
	 * Creates a task to create a markov-modell based on given training data und initiates sub-tasks für every single file give.
	 * @param trainingSession Current state of the training-session.
	 * @param minSignificance Significance-threshold that was used to generate the training-data. Is only used for statistics.
	 * @param voidSignificance Sentences without any known cooccurence will be taken as "void"-emission. This is their fixed significance value.
	 * @param pseudoCountSignificance Known cooccurrence will not be significant for all known states. This is their pseudo-count significance to avoid
	 *        zero-probabilites.
	 * @param folderToAnalyze Folder to search text-files in.
	 * @param fileLimit maximum count of files to analyze in this directory.
	 * @param treetableRoot Reference to the GUI-element holding the analysis results. Will be feeded by the sub-tasks.
	 * @param analysisSession Current state/reference to the analysis session.
	 * @param tasksController Reference to the task-controller to create sub-tasks.
	 * @param labels Language-specific labels.
	 */
	public AnalysisMasterTask(final TrainingSession trainingSession, final AnalysisSettings settings, final File folderToAnalyze, final int fileLimit, final TreeItem<AnalysisResultBean> treetableRoot, final AnalysisSession analysisSession,
			final TasksController tasksController,
			final ResourceBundle labels) {
		super("analyze folder " + folderToAnalyze.getAbsolutePath()); // TRANSLATE
		this.trainingSession = trainingSession;
		this.fileLimit = fileLimit;
		this.minSignificance = settings.minSig();
		this.voidSignificance = settings.voidSig();
		this.pseudoCountSignificance = settings.pseudoSig();
		this.folderToAnalyze = folderToAnalyze;
		this.treetableRoot = treetableRoot;
		this.analysisSession = analysisSession;
		this.tasksController = tasksController;
		this.labels = labels;
	}

	@Override
	public ResourceBundle labels() {
		return labels;
	}

	@Override
	protected Void call() throws Exception {
		try {
			updateProgress(-1, 1);
			if (!folderToAnalyze.isDirectory() || !folderToAnalyze.exists()) {
				throw new IllegalArgumentException("Selected directory must be a directory and exist!");
			}
			//
			final List<Path> textFiles = Files.walk(folderToAnalyze.toPath(), 1, FileVisitOption.FOLLOW_LINKS).filter(p -> {
				final String name = p.toFile().getName();
				return name.endsWith(".txt")
						|| name.endsWith(".html")
						|| name.endsWith(".xml")
						|| name.endsWith(".xhtml")
						|| name.endsWith(".rtf")
						|| name.endsWith(".pdf");
			}).limit(fileLimit).collect(Collectors.toList());
			//
			LOGGER.log(Level.INFO, "Task {0} found {1} files to analyze.", new Object[] { nameProperty().get(), textFiles.size() });
			//
			if (textFiles.isEmpty()) {
				LOGGER.fine("onAnalyzeInputData-action: Selected directory " + folderToAnalyze + " contains no valid-text-files.");
				return null;
			}
			//
			final TreeItem<AnalysisResultBean> groupRoot = createGroupRoot();
			//
			LOGGER.log(Level.INFO, "Task {0} creates markov-model.", nameProperty().get());
			final AnalysisMarkovModell emissionAlphabet = AnalysisMarkovModell.ofTrainingSession(trainingSession, voidSignificance, pseudoCountSignificance, this);
			//
			LOGGER.log(Level.INFO, "Task {0} trains word-order-model.", nameProperty().get());
			update(labels.getString("analysis.progress.prepareWordOrder"));
			final TrainedWordNeighbours wordOrderAnalysisParameter = TrainedWordNeighbours.ofTrainingSession(trainingSession);
			updateProgress(3, 4);
			//
			LOGGER.log(Level.INFO, "Task {0} creates {0} sub-tasks to analyze each file.", textFiles.size());
			update(labels.getString("analysis.progress.creatingTasks"));
			for (final Path textFile : textFiles) {
				final AnalysisTask analyzeTask = new AnalysisTask(groupRoot, textFile.toFile(), emissionAlphabet, wordOrderAnalysisParameter, analysisSession, voidSignificance > 0, labels);
				tasksController.run(analyzeTask);
			}
			updateProgress(4, 4);
			//
			return null;
		} catch (final Exception ex) {
			LOGGER.log(Level.SEVERE, "Exception while processing creating markov-modell!");
			LOGGER.log(Level.SEVERE, "Exception:", ex);
			ex.printStackTrace();
			//
			updateProgress(1, 1);
			updateMessage("Failed!"); // TRANSLATE
			//
			throw ex;
		}
	}

	@Override
	protected void succeeded() {
		if (nextTask != null) {
			tasksController.run(nextTask);
		}
		//
		super.succeeded();
	}

	/**
	 * @param nextTask Task to execute when this one succeeded. May be <code>null</code>.
	 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
	 */
	public void setNextTask(final AbstractTask<?> nextTask) {
		this.nextTask = nextTask;
	}

	/**
	 * @return The group-treenode for the whole analysis job.
	 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
	 */
	private TreeItem<AnalysisResultBean> createGroupRoot() {
		final String groupName = "/" + folderToAnalyze.getName() + "/*";
		//
		final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd.MM.yyyy");
		final DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm:ss");
		final String timeString = LocalDate.now().format(dateFormat) + ", " + LocalTime.now().format(timeFormat);
		//
		final List<String> classes = new ArrayList<>(trainingSession.getClasses());
		Collections.sort(classes);
		//
		final AnalysisSettings analysisSettings = new AnalysisSettings(minSignificance, voidSignificance, pseudoCountSignificance);
		//
		final AnalysisResultBean groupBean = AnalysisResultBean.createGroup(groupName, fileLimit, timeString, classes, analysisSettings);
		final TreeItem<AnalysisResultBean> groupRoot = new TreeItem<AnalysisResultBean>(groupBean);
		treetableRoot.getChildren().add(groupRoot);
		//
		groupRoot.getChildren().addListener(new InvalidationListener() {
			@Override
			public void invalidated(final Observable observable) {
				if (observable instanceof ObservableList<?>) {
					@SuppressWarnings("unchecked")
					final ObservableList<TreeItem<AnalysisResultBean>> observableList = (ObservableList<TreeItem<AnalysisResultBean>>) observable;
					final Map<String, Integer> coocClassNameCount = new HashMap<>(observableList.size());
					final Map<String, Integer> woClassNameCount = new HashMap<>(observableList.size());
					observableList.forEach(treeItem -> {
						coocClassNameCount.compute(treeItem.getValue().classNameProperty().get(), (k, v) -> v == null ? 1 : v + 1);
						woClassNameCount.compute(treeItem.getValue().wordOrderClassProperty().get(), (k, v) -> v == null ? 1 : v + 1);
					});
					//
					Entry<String, Integer> highestCoocEntry = null;
					for (final Entry<String, Integer> entry : coocClassNameCount.entrySet()) {
						if (highestCoocEntry == null || entry.getValue() > highestCoocEntry.getValue()) {
							highestCoocEntry = entry;
						}
					}

					Entry<String, Integer> highestWordOrderEntry = null;
					for (final Entry<String, Integer> entry : woClassNameCount.entrySet()) {
						if (highestWordOrderEntry == null || entry.getValue() > highestWordOrderEntry.getValue()) {
							highestWordOrderEntry = entry;
						}
					}
					//
					if (highestCoocEntry == null) {
						throw new IllegalStateException("highestEntry is null!");
					}
					if (highestWordOrderEntry == null) {
						throw new IllegalStateException("highestWordOrderEntry is null!");
					}
					//
					// Update bean-values of group
					final AnalysisResultBean groupBean = groupRoot.getValue();
					final BigDecimal newScore = new BigDecimal((double) highestCoocEntry.getValue() / (double) observableList.size(), MathContext.DECIMAL32);
					groupBean.classNameProperty().set(highestCoocEntry.getKey());
					groupBean.scoreProperty().set(newScore);
					groupBean.wordOrderClassProperty().set(highestWordOrderEntry.getKey());
					final BigDecimal newWordOrderPercentage = new BigDecimal(((double) highestWordOrderEntry.getValue() / (double) observableList.size()) * 100).setScale(2, RoundingMode.HALF_UP);
					groupBean.wordOrderPercentageProperty().set(newWordOrderPercentage.doubleValue());
				}
			}
		});
		//
		return groupRoot;
	}

}
