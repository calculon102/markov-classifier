package de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.javafx.common;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;

import de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.common.ProgressMonitor;

/**
 * Custom filterstream to report current progress to a {@link ProgressMonitor}, if full-size of input-stream is available.
 *
 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
 */
public final class ProgressMonitorInputStream extends FilterInputStream {

	private final ProgressMonitor monitor;
	private int nread = 0;
	private int size = 0;

	public ProgressMonitorInputStream(final InputStream in, final ProgressMonitor monitor) {
		super(in);
		this.monitor = monitor;

		try {
			size = in.available();
		} catch (final IOException ioe) {
			size = 0;
		}
	}

	@Override
	public int read() throws IOException {
		final int read = in.read();
		monitor.update(++nread, size);
		if (monitor.isCancelled()) {
			throw new InterruptedIOException("Cancelled while streaming from input.");
		}
		return read;
	}

	@Override
	public int read(final byte[] b) throws IOException {
		final int read = in.read(b);
		nread += read;
		monitor.update(nread, size);
		if (monitor.isCancelled()) {
			throw new InterruptedIOException("Cancelled while streaming from input.");
		}
		return read;
	}

	@Override
	public int read(final byte[] b, final int off, final int len) throws IOException {
		final int read = in.read(b, off, len);
		nread += read;
		monitor.update(nread, size);
		if (monitor.isCancelled()) {
			throw new InterruptedIOException("Stream cancelled while reading from input.");
		}
		return read;
	}

	@Override
	public long skip(final long n) throws IOException {
		final long nr = in.skip(n);
		nread += nr;
		if (nr > 0) {
			monitor.update(nread, size);
			if (monitor.isCancelled()) {
				throw new InterruptedIOException("Stream cancelled while reading from input.");
			}
		}
		return nr;
	}

	@Override
	public synchronized void reset() throws IOException {
		monitor.update(-1, size);
		if (monitor.isCancelled()) {
			throw new InterruptedIOException("Stream cancelled while reading from input.");
		}
		super.reset();
	}

}
