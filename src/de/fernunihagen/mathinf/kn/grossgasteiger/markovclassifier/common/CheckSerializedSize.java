package de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.common;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;

public final class CheckSerializedSize extends OutputStream {

	/** Serialize obj and count the bytes */
	public static long getSerializedSize(final Serializable obj) {
		try {
			final CheckSerializedSize counter = new CheckSerializedSize();
			final ObjectOutputStream objectOutputStream = new ObjectOutputStream(counter);
			objectOutputStream.writeObject(obj);
			objectOutputStream.close();
			return counter.getNBytes();
		} catch (final Exception e) {
			// Serialization failed
			return -1;
		}
	}

	private long nBytes = 0;

	private CheckSerializedSize() {
	}

	@Override
	public void write(final int b) throws IOException {
		++nBytes;
	}

	@Override
	public void write(final byte[] b, final int off, final int len) throws IOException {
		nBytes += len;
	}

	public long getNBytes() {
		return nBytes;
	}
}
