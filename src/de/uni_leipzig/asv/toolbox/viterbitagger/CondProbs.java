package de.uni_leipzig.asv.toolbox.viterbitagger;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Hashtable;

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
 * @author not attributable
 * @version 1.0
 */
public abstract class CondProbs {
	boolean d = !true; // debugging

	protected Hashtable<String, InfoFrequencyRow> wordInfo;

	protected String[] rowToWord;

	protected int rows;

	protected int columns;

	protected double SMOOTHVAL = 0.000001;

	/** Creates a new instance of Lexicon */
	public CondProbs(final InputStream condprobsfile) {
		init();

		try {
			load(condprobsfile);
		} catch (final Exception e) {
			System.err.println("File IO problem with " + condprobsfile + " :"
					+ e.getMessage());
		}

		if (this.d) {
			printMatrix();
		}
	} // end constructor

	private void init() {
		this.wordInfo = new Hashtable<String, InfoFrequencyRow>();
	}

	// Loads matrix from file
	protected abstract void load(InputStream condprobsfile) throws IOException,
			FileNotFoundException;

	protected void determinDimension(final InputStream filename) throws FileNotFoundException, IOException {

		try (final BufferedReader lexFileReader = new BufferedReader(new InputStreamReader(filename))) {
			String line;
			int linecount = 0;

			// Count lines for array dimensioning
			line = lexFileReader.readLine();
			if (line == null) {
				throw new IllegalArgumentException("Given file " + filename + " does not contain a single line!");
			}

			final String linearray[] = line.split("\t");
			this.columns = linearray.length - 1;

			while (lexFileReader.readLine() != null) {
				linecount++;
			} // elihw

			this.rows = linecount + 1;
		}
	}

	protected double[][] loadNow(final InputStream filename, final boolean fillLexmatrix,
			double[][] lexmatrix) throws FileNotFoundException,
					NumberFormatException, IOException {
		int linecount = 0;
		String line;
		String[] linearray;

		// Initialize matrix
		if (fillLexmatrix) {
			lexmatrix = new double[this.rows][this.columns];
		}
		this.rowToWord = new String[this.rows];

		// and fill it
		try (final BufferedReader lexFileReader = new BufferedReader(new InputStreamReader(filename))) {
			linecount = 0;
			while ((line = lexFileReader.readLine()) != null) {
				linearray = line.split("\t");

				final int freq = (new Integer(linearray[0])).intValue();

				final String newword = linearray[1];
				this.wordInfo.put(newword, new InfoFrequencyRow(freq, linecount));
				this.rowToWord[linecount] = newword;

				if (fillLexmatrix) {
					Double actPos;
					double doubleActPos;
					for (int p = 0; p < linearray.length - 2; p++) {
						actPos = new Double(linearray[p + 2]);
						doubleActPos = actPos.doubleValue();
						lexmatrix[linecount][p] = doubleActPos;
					} // rof
				} // fi fillLexmatrix

				linecount++;
			} // elihw

			lexFileReader.close();
		}

		return lexmatrix;
	}

	public double[] getTagDistribution(final String word) {
		final double[] retVal = new double[this.columns];

		if (this.wordInfo.containsKey(word)) {
			for (int i = 0; i < this.columns; i++) {
				retVal[i] = getMatrixValue((this.wordInfo.get(word)).getRow(),
						i)
						+ this.SMOOTHVAL;
			}
		} else {
			for (int i = 0; i < this.columns; i++) {
				retVal[i] = 1.0; // if word is not in lexicon, return max
									// probability for all tags
			}
		}

		return retVal;
	} // end public double tagDistribution

	// returns whether word is in lexicon or not
	public boolean containsWord(final String word) {
		return this.wordInfo.containsKey(word);
	} // end public double containsWord(String word)

	public void printMatrix() {
		System.out.println(" ---- " + this.rows + "x" + this.columns
				+ " matrix in the lexicon: ---- ");
		for (int r = 0; r < this.rows; r++) {
			System.out.print("\n" + this.rowToWord[r]);
			for (int c = 0; c < this.columns; c++) {
				System.out.print("\t" + getMatrixValue(r, c));
			} // rof
		} // rod// roff
	} // end printmatrix

	protected abstract double getMatrixValue(int row, int column);
} // end public abstract class Lexicon
