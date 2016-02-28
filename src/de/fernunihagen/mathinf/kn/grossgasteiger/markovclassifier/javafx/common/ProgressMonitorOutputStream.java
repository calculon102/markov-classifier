package de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.javafx.common;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.OutputStream;

import de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.common.ProgressMonitor;

/**
 * Custom filterstream to report current progress to a {@link ProgressMonitor}, if full-size of input-stream is available.
 *
 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
 */
public final class ProgressMonitorOutputStream extends FilterOutputStream {

	private final ProgressMonitor monitor;
	private int nread = 0;
	private long size = 0;

	public ProgressMonitorOutputStream(final OutputStream out, final long serializedSize, final ProgressMonitor monitor) {
		super(out);
		this.monitor = monitor;
		this.size = serializedSize < 0 ? -1 : serializedSize;
	}

	@Override
	public void write(final int b) throws IOException {
		super.write(b);
		monitor.update(++nread, size);
		if (monitor.isCancelled()) {
			throw new InterruptedIOException("Stream cancelled while writing to output.");
		}
	}

	@Override
	public void write(final byte[] b) throws IOException {
		super.write(b);
		nread += b.length;
		monitor.update(nread, size);
		if (monitor.isCancelled()) {
			throw new InterruptedIOException("Stream cancelled while writing to output.");
		}
	}

	@Override
	public void write(final byte[] b, final int off, final int len) throws IOException {
		super.write(b, off, len);
		nread += len;
		monitor.update(nread, size);
		if (monitor.isCancelled()) {
			throw new InterruptedIOException("Stream cancelled while writing to output.");
		}
	}

}
