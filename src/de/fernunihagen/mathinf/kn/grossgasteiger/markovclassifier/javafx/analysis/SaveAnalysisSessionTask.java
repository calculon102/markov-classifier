package de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.javafx.analysis;

import java.io.File;
import java.io.IOException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.common.SerializableSession;
import de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.common.SerializableTreeItem;
import de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.javafx.tasks.AbstractTask;
import javafx.application.Platform;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableView;

public class SaveAnalysisSessionTask extends AbstractTask<Void> {
	/** Logger-Instance */
	private static final Logger LOGGER = Logger.getLogger(SaveAnalysisSessionTask.class.getName());

	/** File-handle to load analysis-session from. */
	private final File fileToSave;
	/** Parent window to disable while loading. */
	private final Node parent;
	/** Table to truncate and insert analysis-data into. */
	private final TreeTableView<AnalysisResultBean> analysisTable;
	/** ResourceBundle with localilzed labels. */
	private final ResourceBundle labels;

	/**
	 * @param sessionFileToSave File-handle to save analysis-session to.
	 * @param parent Parent window to disable while saving.
	 * @param analysisTable Table to truncate and insert analysis-data into.
	 * @param labels ResourceBundle with localilzed labels.
	 */
	public SaveAnalysisSessionTask(final File sessionFileToSave, final Node parent, final TreeTableView<AnalysisResultBean> analysisTable, final ResourceBundle labels) { //
		super("Save analysis-session from " + sessionFileToSave.getName()); // TRANSLATE
		//
		this.fileToSave = sessionFileToSave;
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
			LOGGER.log(Level.FINE, "Serializing result-data to file {0}.", fileToSave.getAbsolutePath());
			//
			updateMessage("Saving..."); // TRANSLATE
			//
			final TreeItem<AnalysisResultBean> root = analysisTable.getRoot();
			final SerializableTreeItem<AnalysisResultBean> analysisResultTree = SerializableTreeItem.ofTreeItem(root);
			//
			final SerializableSession<SerializableTreeItem<AnalysisResultBean>> serializableSession = new SerializableSession<>(analysisResultTree);
			serializableSession.save(fileToSave, this);
		} catch (final IOException e) {
			final Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle(labels.getString("analysis.session.save.error"));
			alert.setHeaderText(labels.getString("analysis.session.save.error"));
			alert.setContentText(e.getLocalizedMessage());
			alert.showAndWait();
			//
			LOGGER.log(Level.SEVERE, labels.getString("analysis.session.save.error"), e);
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
