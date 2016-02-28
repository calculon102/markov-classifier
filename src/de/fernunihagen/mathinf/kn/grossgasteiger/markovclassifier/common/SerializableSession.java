package de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.common;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.javafx.common.ProgressMonitorOutputStream;

/**
 * Holds an arbritary session-object ready to be serialized to a file.
 *
 * @param <T>
 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
 */
public final class SerializableSession<T extends Serializable> {
	/** Logger-Instance */
	private static final Logger LOGGER = Logger.getLogger(SerializableSession.class.getName());

	/** Reference to the serializable session-object. */
	private final T session;

	/**
	 * Holds an arbritary session-object ready to be serialized to a file.
	 * @param session Reference to the serializable session-object.
	 */
	public SerializableSession(final T session) {
		this.session = session;
	}

	/**
	 * Saves this session to the given file-path
	 * @param file File-Reference to write the session to.
	 * @param monitor Monitors progress for task-view in UI.
	 * @throws IOException Error while writing file.
	 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
	 */
	public void save(final File file, final ProgressMonitor monitor) throws IOException {
		if (file == null || (file.exists() && !file.isFile())) {
			throw new IllegalArgumentException(file + " is not a file!");
		}
		//
		final long serializedSize = CheckSerializedSize.getSerializedSize(session);
		//
		try (final OutputStream fileStream = new FileOutputStream(file);
				final OutputStream bufferedStream = new BufferedOutputStream(fileStream);
				final OutputStream monitoredStream = new ProgressMonitorOutputStream(bufferedStream, serializedSize, monitor);
				final ObjectOutput outputStream = new ObjectOutputStream(monitoredStream);) {
			//
			outputStream.writeObject(session);
			//
		} catch (final InterruptedIOException ex) {
			LOGGER.log(Level.WARNING, "Interrupted while writing session to " + file + ".", ex);
			return;
		} catch (final IOException ex) {
			LOGGER.log(Level.SEVERE, "Exception while writing session!", ex);
			throw ex;
		}
	}
}
