package de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.javafx.common;

import java.io.File;

import javafx.stage.DirectoryChooser;
import javafx.stage.Window;

/**
 * Represents the user interaction to choose a directory. Remembers the last chosen directory if not cancelled.
 *
 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
 */
public final class ChooseDirectoryAction {

	private final String title;

	private File lastDirectory;

	public ChooseDirectoryAction(final String title) {
		this.title = title;
	}

	/**
	 * User chooses a directory. Starting from the last not-cancelled selection.
	 * 
	 * @param parent parent Window for modality.
	 * @return File-reference to directory or <code>null</code>.
	 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
	 */
	public File start(final Window parent) {
		final DirectoryChooser dirChooser = new DirectoryChooser();
		dirChooser.setTitle(title);
		if (lastDirectory != null) {
			if (lastDirectory.getParentFile() != null) {
				dirChooser.setInitialDirectory(lastDirectory.getParentFile());
			} else {
				dirChooser.setInitialDirectory(lastDirectory);
			}
		}
		//
		final File importDir = dirChooser.showDialog(parent);
		//
		if (importDir != null) {
			lastDirectory = importDir;
		}
		//
		return importDir;
	}
}
