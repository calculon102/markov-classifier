package de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.javafx.analysis;

import static java.util.Objects.requireNonNull;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.analysis.AnalysisMarkovModell;
import de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.analysis.AnalysisResult;
import de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.analysis.AnalysisSession;
import de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.analysis.TrainedWordNeighbours;
import de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.javafx.analysis.AnalysisResultBean.TYPE;
import de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.javafx.tasks.AbstractTask;
import javafx.application.Platform;
import javafx.scene.control.TreeItem;

/**
 * Analyse a file against all known significant cooccurrences via a HMM.
 *
 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
 */
public final class AnalysisTask extends AbstractTask<AnalysisResult> {
	/** Logger-Instance */
	private static final Logger LOGGER = Logger.getLogger(AnalysisTask.class.getName());

	/** File to analyze. */
	private final File file;
	/** Reference to current training-data for analyzation. */
	private final AnalysisMarkovModell emissionAlphabet;
	/** Current analyis-session for the workload and to update. */
	private final AnalysisSession analysisSession;
	/** Locale-specific labels. */
	private final ResourceBundle labels;
	/** TreeItem to update afterwards. */
	private final TreeItem<AnalysisResultBean> treeItem;
	/** Parameter to do word-order analysis. */
	private final TrainedWordNeighbours wordOrderAnalysisParameter;

	private final boolean insertVoidEmissions;

	/**
	 * @param analysisResultBean Reference to infos represented in GUI.
	 * @param root Root to insert the final treeitem-structure.
	 * @param file File to analyze.
	 * @param emissionAlphabet Reference to current training-data for analyzation.
	 * @param wordOrderAnalysisParameter Parameter to do word-order analysis.
	 * @param analysisSession Current analyis-session for the workload and to update.
	 * @param insertVoidEmissions Gibt an ob überhaupt void-emissionen verwendet werden sollen.
	 * @param labels Locale-specific labels.
	 */
	public AnalysisTask(final TreeItem<AnalysisResultBean> root, final File file, final AnalysisMarkovModell emissionAlphabet, final TrainedWordNeighbours wordOrderAnalysisParameter, final AnalysisSession analysisSession, final boolean insertVoidEmissions, final ResourceBundle labels) {
		super("Analyze file " + file.getName()); // TRANSLATE
		this.treeItem = root;
		this.file = file;
		this.emissionAlphabet = emissionAlphabet;
		this.wordOrderAnalysisParameter = wordOrderAnalysisParameter;
		this.analysisSession = analysisSession;
		this.insertVoidEmissions = insertVoidEmissions;
		this.labels = labels;
	}

	@Override
	protected AnalysisResult call() throws Exception {
		LOGGER.log(Level.INFO, "Analyze file {0}...", file.getName());
		//
		try {
			final AnalysisResult analysisResult = analysisSession.analyzeFile(file, emissionAlphabet, wordOrderAnalysisParameter, this.insertVoidEmissions, this, labels);
			//
			updateTreeItem(treeItem, analysisResult);
			LOGGER.log(Level.INFO, "Done analyzing file {0} in task: {1}", new Object[] { file.getName(), analysisResult.classPropabilities() });
			//
			return analysisResult;
		} catch (final Exception ex) {
			LOGGER.log(Level.SEVERE, "Exception while processing file {0}.", file.getName());
			LOGGER.log(Level.SEVERE, "Exception:", ex);
			ex.printStackTrace();
			//
			updateProgress(1, 1);
			updateMessage("Failed!"); // TRANSLATE
			//
			throw ex;
		}
	}

	/**
	 * Updates the treeitem in GUI with the given analysis-result.
	 * @param root TreeItem to update.
	 * @param result Analysis-result to give into the treeitem.
	 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
	 */
	private static void updateTreeItem(final TreeItem<AnalysisResultBean> root, final AnalysisResult result) {
		requireNonNull(root);
		requireNonNull(result);
		//
		final Map<String, Double> classPropabilities = result.classPropabilities();
		final List<String> classes = new ArrayList<String>(classPropabilities.keySet());
		Collections.sort(classes);

		final List<TreeItem<AnalysisResultBean>> subResults = new ArrayList<>(classes.size());
		//
		Double highestProbability = null;
		String highestProbabilityClass = "None"; // TRANSLATE
		//
		for (final String className : classes) {
			if (className.isEmpty()) {
				continue;
			}
			//
			final Double probability = classPropabilities.get(className);
			final AnalysisResultBean resultBean = new AnalysisResultBean("", TYPE.RESULT);
			resultBean.classNameProperty().set(className);
			resultBean.scoreProperty().set(new BigDecimal(probability));
			resultBean.filenameProperty().set(result.file().getName());
			resultBean.setResult(result);

			final TreeItem<AnalysisResultBean> treeItem = new TreeItem<AnalysisResultBean>(resultBean);
			subResults.add(treeItem);
			//
			if (highestProbability == null || highestProbability < probability) {
				highestProbability = probability;
				highestProbabilityClass = className;
			}
		}
		//
		final BigDecimal highestProbabilityBigDecimal = highestProbability == null ? null : new BigDecimal(highestProbability);
		//
		final AnalysisResultBean bestResultBean = new AnalysisResultBean(result.file().getName(), TYPE.RESULT);
		bestResultBean.classNameProperty().set(highestProbabilityClass);
		bestResultBean.scoreProperty().set(highestProbabilityBigDecimal);
		bestResultBean.setResult(result);
		bestResultBean.emissionCountProperty().set(result.emissions().size());
		bestResultBean.wordOrderClassProperty().set(result.wordOrderHighscore().className());
		bestResultBean.wordOrderPercentageProperty().set(result.wordOrderHighscore().percentage());
		bestResultBean.filenameProperty().set(result.file().getName());
		final TreeItem<AnalysisResultBean> resultItem = new TreeItem<AnalysisResultBean>(bestResultBean);
		//
		final String bestResultClass = highestProbabilityClass;
		final List<TreeItem<AnalysisResultBean>> subResultItemsToAdd = subResults.stream()
				.filter(treeItem -> !bestResultClass.equals(treeItem.getValue().classNameProperty().get()))
				.collect(Collectors.toList());
		//
		Platform.runLater(() -> {
			if (!subResultItemsToAdd.isEmpty()) {
				resultItem.getChildren().addAll(subResultItemsToAdd);
			}
			root.getChildren().add(resultItem);
		});
	}

	@Override
	public ResourceBundle labels() {
		return labels;
	}
}
