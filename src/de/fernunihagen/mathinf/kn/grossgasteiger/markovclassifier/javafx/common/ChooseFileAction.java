package de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.javafx.common;

import java.io.File;

import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Window;

/**
 * Represents the user interaction to choose a session-file. Remembers the last chosen directory if not cancelled.
 *
 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
 */
public final class ChooseFileAction {
	private final String title;
	private final String ending;

	private File lastFile;

	public ChooseFileAction(final String title, final String ending) {
		this.title = title;
		this.ending = ending;
	}

	/**
	 * User chooses a directory. Starting from the last not-cancelled selection.
	 * 
	 * @param parent parent Window for modality.
	 * @return File-reference to directory or <code>null</code>.
	 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
	 */
	public File open(final Window parent) {
		final FileChooser fileChooser = createFileChooser();
		final File fileToOpen = fileChooser.showOpenDialog(parent);
		//
		if (fileToOpen != null) {
			lastFile = fileToOpen;
		}
		//
		return fileToOpen;
	}

	/**
	 * User chooses a file-destination to save the current session.
	 * 
	 * @param parent parent Window for modality.
	 * @return File-reference to directory or <code>null</code>.
	 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
	 */
	public File save(final Window parent) {
		final FileChooser fileChooser = createFileChooser();
		final File fileToSave = fileChooser.showSaveDialog(parent);
		//
		if (fileToSave == null) {
			return null;
		}
		//
		lastFile = fileToSave;
		//
		if (!fileToSave.exists() && !fileToSave.getName().endsWith(ending)) {
			return new File(fileToSave.getAbsolutePath() + ending);
		}
		//
		return fileToSave;
	}

	/**
	 * @return The latest chosen file.
	 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
	 */
	public File getLastFile() {
		return lastFile;
	}

	/**
	 * Internal state-based factory-method.
	 * 
	 * @return A file-chooser based on title and last-directory.
	 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
	 */
	private FileChooser createFileChooser() {
		final FileChooser fileChooser = new FileChooser();
		fileChooser.getExtensionFilters().addAll(new ExtensionFilter("Session-data (*" + ending + ")", "*" + ending + "")); // TRANSLATE
		//
		fileChooser.setTitle(title);
		if (lastFile != null) {
			fileChooser.setInitialDirectory(lastFile.getParentFile());
			fileChooser.setInitialFileName(lastFile.getName());
		}
		//
		return fileChooser;
	}
}
