package de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.javafx.analysis;

import java.io.File;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.common.SavedSession;
import de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.common.SerializableTreeItem;
import de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.javafx.tasks.AbstractTask;
import javafx.application.Platform;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableView;

public class LoadAnalysisSessionTask extends AbstractTask<Void> {
	/** Logger-Instance */
	private static final Logger LOGGER = Logger.getLogger(LoadAnalysisSessionTask.class.getName());

	/** File-handle to load analysis-session from. */
	private final File sessionFileToLoad;
	/** Parent window to disable while loading. */
	private final Node parent;
	/** Table to truncate and insert analysis-data into. */
	private final TreeTableView<AnalysisResultBean> analysisTable;
	/** ResourceBundle with localilzed labels. */
	private final ResourceBundle labels;

	/**
	 * @param sessionFileToLoad File-handle to load analysis-session from.
	 * @param parent Parent window to disable while loading.
	 * @param analysisTable Table to truncate and insert analysis-data into.
	 * @param labels ResourceBundle with localilzed labels.
	 */
	public LoadAnalysisSessionTask(final File sessionFileToLoad, final Node parent, final TreeTableView<AnalysisResultBean> analysisTable, final ResourceBundle labels) { //
		super("Load analysis-session from " + sessionFileToLoad.getName()); // TRANSLATE
		//
		this.sessionFileToLoad = sessionFileToLoad;
		this.parent = parent;
		this.analysisTable = analysisTable;
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
	protected Void call() throws Exception {
		try {
			Platform.runLater(() -> {
				parent.getScene().setCursor(Cursor.WAIT);
				parent.setDisable(true);
			});
			//
			updateMessage("Loading..."); // TRANSLATE
			//
			LOGGER.log(Level.FINE, "Deserializing result-data from file {0}.", sessionFileToLoad.getAbsolutePath());
			//
			final SavedSession<SerializableTreeItem<AnalysisResultBean>> savedSession = new SavedSession<>(sessionFileToLoad);
			final Optional<SerializableTreeItem<AnalysisResultBean>> loadedSessionTree = savedSession.load(this);
			//
			if (!loadedSessionTree.isPresent()) { // Was cancelled
				return null;
			}
			//
			final TreeItem<AnalysisResultBean> treeItem = SerializableTreeItem.toTreeItem(loadedSessionTree.get());
			Platform.runLater(() -> analysisTable.setRoot(treeItem));
		} catch (final Exception e) {
			final Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle(labels.getString("analysis.session.open.error"));
			alert.setHeaderText(e.getLocalizedMessage());
			alert.showAndWait();
			//
			LOGGER.log(Level.SEVERE, labels.getString("analysis.session.open.error"), e);
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
