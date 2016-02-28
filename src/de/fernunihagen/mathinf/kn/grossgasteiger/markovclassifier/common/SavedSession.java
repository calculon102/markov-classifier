package de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.common;

import static java.util.Objects.requireNonNull;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.javafx.common.ProgressMonitorInputStream;

/**
 * Reference to a saved session-file, which reports its lazy loading process to to given monitor.
 *
 * @param <T>
 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
 */
public final class SavedSession<T extends Serializable> {
	/** Logger-Instance */
	private static final Logger LOGGER = Logger.getLogger(SavedSession.class.getName());

	/** The file the session is saved to. */
	private final File file;

	/**
	 * Reference to a saved session-file, which reports its lazy loading process to to given monitor. Thus the monitor and its parent-task must be running for
	 * the whole lifecycle of this object!
	 * @param file The file the session is saved to.
	 * @param monitor Monitor to report loading status to.
	 */
	public SavedSession(final File file) {
		requireNonNull(file);
		//
		this.file = file;
	}

	/**
	 * Loads a session from a given file.
	 * @param monitor Monitor to report loading status to.
	 * @return The loaded session object, maybe empty if IO is interrupted, i.e. by cancelling the task.
	 * @throws Exception Error with file-IO or deserialiaztion.
	 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
	 */
	@SuppressWarnings("unchecked")
	public Optional<T> load(final ProgressMonitor monitor) throws Exception {
		requireNonNull(monitor);
		//
		if (!file.isFile() || !file.exists()) {
			throw new IllegalArgumentException(file + " is not a directory or does not exist!");
		}
		//
		try (final InputStream input = new FileInputStream(file);
				final InputStream progess = new ProgressMonitorInputStream(input, monitor);
				final InputStream buffer = new BufferedInputStream(progess);
				final ObjectInput object = new ObjectInputStream(buffer);) {

			final Object readObject = object.readObject();
			return Optional.of((T) readObject);

		} catch (final InterruptedIOException ex) {
			LOGGER.log(Level.WARNING, "Interrupted while loading session from " + file + ".", ex);
			return Optional.empty();
		} catch (final ClassCastException | ClassNotFoundException | IOException ex) {
			LOGGER.log(Level.SEVERE, "Error while loading session from " + file + ".", ex);
			throw ex;
		}
	}

}
