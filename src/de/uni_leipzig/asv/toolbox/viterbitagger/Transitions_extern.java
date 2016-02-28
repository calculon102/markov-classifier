/*
 * Transitions.java
 *
 * Created on 11. Dezember 2006
 *
 * Format is
 *  T1|T2 p(t1) p(t2) ... p(tn)
 *  T3|T4  ........
 *
 *  that means: after sequence T1 T2, the probability for tag with code 0 is p(t1).
 *
 *  All non-initialized entries are zeroed.
 *
 *
 */

package de.uni_leipzig.asv.toolbox.viterbitagger;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * <p>
 * Title:
 * </p>
 * 
 * <p>
 * Description:
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2006
 * </p>
 * 
 * <p>
 * Company:
 * </p>
 * 
 * @author wittig
 * @version 1.0
 */

public class Transitions_extern extends Transitions {
	private static final String binStr = ".bin";

	private RandomAccessFile dataFile;

	private long dataOffset;

	/** Creates a new instance of Transistions */
	public Transitions_extern(String filename, TagList tl) {
		super(tl);

		// Wenn bin-File noch nicht existiert, dann anlegen
		File binFilename = new File(filename + Transitions_extern.binStr);
		if (!binFilename.exists()) {
			makeBinFile(filename);

			if (this.d) {
				System.out.println("bin-file produced for " + filename);
			}
		} else {
			if (this.d) {
				System.out.println("bin-file already exists");
			}
		}

		// bin-File öffnen, SMOOTHVAL auslesen
		try {
			this.dataFile = new RandomAccessFile(filename
					+ Transitions_extern.binStr, "r");
			this.SMOOTHVAL = this.dataFile.readDouble();
			this.dataOffset = this.dataFile.getFilePointer();
		} catch (Exception e) {
			System.err.println("File IO problem with "
					+ (filename + Transitions_extern.binStr) + " :"
					+ e.getMessage());
		}
	} // end public Transitions(String filename, TagList tl) {

	/**
	 * makeBinFile
	 * 
	 * @param filename
	 *            String
	 */
	private void makeBinFile(String filename) {
		// Matrix initialisieren
		double[][][] transmatrix = new double[this.dimension][this.dimension][this.dimension];

		if (this.d) {
			System.out.print("Initializing transition matrix with "
					+ this.dimension + "^3=" + this.dimension * this.dimension
					* this.dimension + " zeros..");
		}

		for (int i = 0; i < this.dimension; i++) {
			for (int j = 0; j < this.dimension; j++) {
				for (int k = 0; k < this.dimension; k++) {
					transmatrix[i][j][k] = 0;
				} // rof k
			} // rof j
		} // rof i

		if (this.d) {
			System.out.println("..done.");
		}

		// Werte einlesen
		try {
			BufferedReader FileReader = new BufferedReader(new FileReader(
					filename));

			String line;
			String[] linearray;
			String[] tags;
			int tag1code, tag2code;

			while ((line = FileReader.readLine()) != null) {
				linearray = line.split("\t");
				tags = linearray[0].split("\\|");
				tag1code = this.taglist.getCodeForTag(tags[0]);
				tag2code = this.taglist.getCodeForTag(tags[1]);

				if (this.d) {
					System.out.println("prefix " + linearray[0]
							+ " split into " + tags[0] + " and " + tags[1]
							+ ", codes " + tag1code + " and " + tag2code);
				}

				for (int p = 0; p < linearray.length - 1; p++) {
					Double actPos = new Double(linearray[p + 1]);
					double doubleActPos = actPos.doubleValue();
					transmatrix[tag1code][tag2code][p] = doubleActPos;

					if ((doubleActPos > 0.00000000000)
							&& (doubleActPos < this.SMOOTHVAL)) {
						this.SMOOTHVAL = doubleActPos;
					}
				} // rof

			} // elihw
			// smoothing value = sampling error. This is +0.5 smoothing.
			this.SMOOTHVAL = this.SMOOTHVAL / 2;

			if (this.d) {
				System.out.println("Smoothing value: " + this.SMOOTHVAL);
			}

			FileReader.close();
		} catch (Exception e) {
			System.err.println("File IO problem with " + filename + " :"
					+ e.getMessage());
		}

		// Bin-File schreiben
		try {
			DataOutputStream out = new DataOutputStream(
					new BufferedOutputStream(new FileOutputStream(filename
							+ Transitions_extern.binStr)));

			// Meta-Inf: smoothing value
			out.writeDouble(this.SMOOTHVAL);

			// Daten
			for (int i = 0; i < this.dimension; i++) {
				for (int j = 0; j < this.dimension; j++) {
					for (int k = 0; k < this.dimension; k++) {
						out.writeDouble(transmatrix[i][j][k]);
					} // rof k
				} // rof j
			} // rof i

			out.flush();
			out.close();
		} catch (Exception e) {
			System.err.println("File IO problem with "
					+ (filename + Transitions_extern.binStr) + " :"
					+ e.getMessage());
		}
	}

	@Override
	public double getTransProb(int x, int y, int z) {
		double retVal = -1;
		if ((x < this.dimension) && (y < this.dimension)
				&& (z < this.dimension)) {
			long pos = this.dataOffset
					+ (x * this.dimension * this.dimension + y * this.dimension + z)
					* 8;
			try {
				this.dataFile.seek(pos);
				retVal = this.dataFile.readDouble() + this.SMOOTHVAL;
			} catch (IOException ex) {
			}
		}
		return retVal;
	} // end public double getTransProb
}
