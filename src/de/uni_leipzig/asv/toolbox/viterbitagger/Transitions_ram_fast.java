/*
 * Transitions.java
 *
 * Created on 1. Dezember 2005, 20:38
 *
 *
 *   * Format is
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

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * 
 * @author BIEMANN
 */
public class Transitions_ram_fast extends Transitions {
	private final double[][][] transmatrix;

	/** Creates a new instance of Transistions */
	public Transitions_ram_fast(final InputStream transitionfile, final TagList tl) {
		super(tl);

		this.transmatrix = new double[this.dimension][this.dimension][this.dimension];

		if (this.d) {
			System.out.print("Initializing transition matrix with "
					+ this.dimension + "^3=" + this.dimension * this.dimension
					* this.dimension + " zeros..");
		}

		for (int i = 0; i < this.dimension; i++) {
			for (int j = 0; j < this.dimension; j++) {
				for (int k = 0; k < this.dimension; k++) {
					this.transmatrix[i][j][k] = 0;
				} // rof k
			} // rof j
		} // rof i

		if (this.d) {
			System.out.println("..done.");
		}

		try {
			load(transitionfile);
		} catch (final Exception e) {
			System.err.println("File IO problem with " + transitionfile + " :"
					+ e.getMessage());
		}

	} // end public Transitions(String filename, TagList tl) {

	protected void load(final InputStream transitionfile) throws IOException,
			FileNotFoundException {
		String line;
		String[] linearray;
		String[] tags;
		int tag1code, tag2code;

		try (final BufferedReader FileReader = new BufferedReader(new InputStreamReader(transitionfile))) {
			while ((line = FileReader.readLine()) != null) {
				linearray = line.split("\t");
				tags = linearray[0].split("\\|");
				tag1code = this.taglist.getCodeForTag(tags[0]);
				tag2code = this.taglist.getCodeForTag(tags[1]);

				if (this.d) {
					System.out.println("prefix " + linearray[0] + " split into "
							+ tags[0] + " and " + tags[1] + ", codes " + tag1code
							+ " and " + tag2code);
				}

				for (int p = 0; p < linearray.length - 1; p++) {
					final Double actPos = new Double(linearray[p + 1]);
					final double doubleActPos = actPos.doubleValue();
					this.transmatrix[tag1code][tag2code][p] = doubleActPos;

					if ((doubleActPos > 0.00000000000)
							&& (doubleActPos < this.SMOOTHVAL)) {
						this.SMOOTHVAL = doubleActPos;
					}
				} // rof

			} // elihw
		}

		// smoothing value = sampling error. This is +0.5 smoothing.
		this.SMOOTHVAL = this.SMOOTHVAL / 2;

		if (this.d) {
			System.out.println("Smoothing value: " + this.SMOOTHVAL);
		}

	} // end private void load

	@Override
	public double getTransProb(final int x, final int y, final int z) {
		double retVal;
		if ((x < this.dimension) && (y < this.dimension)
				&& (z < this.dimension)) {
			retVal = this.transmatrix[x][y][z] + this.SMOOTHVAL;
		} else {
			retVal = -1;
		} // esle fi.

		return retVal;
	} // end public double getTransProb

} // end public class Transitions
