/*
 * Lexicon.java
 *
 * Created on 1. Dezember 2005, 20:34
 *
 * Lexicon stores the lexicon in a matrix
 * word1 p(t1) p(t2)
 * word2 p(t1) p(t2) ..
 * ...
 *
 * as given in input file.
 */
package de.uni_leipzig.asv.toolbox.viterbitagger;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import de.uni_leipzig.asv.utils.Pretree;

/**
 * 
 * @author wittig
 */
public abstract class Lexicon {
	boolean d = !true; // debugging

	protected Hashtable<String, InfoFrequencyRow> wordInfo;

	protected String[] rowToWord;

	protected int rows;

	protected int columns;

	protected boolean smooth;

	protected boolean pretree;

	protected int sumFrequency;

	protected double SMOOTHVAL;

	protected double SMOOTHPRETREEVAL = 0.0001;

	// pretree
	protected Pretree guessTagFront;

	protected Pretree guessTagRev;

	public int getRows() {
		return rows;
	}

	public int getColumns() {
		return columns;
	}

	/** Creates a new instance of Lexicon */
	public Lexicon(final InputStream filename, final boolean setSmooth, final boolean setPretree) {
		init(setSmooth, setPretree);

		try {
			load(filename);
		} catch (final Exception e) {
			System.err.println("File IO problem with " + filename + " :"
					+ e.getMessage());
		}

		if (this.d) {
			printMatrix();
		}
	} // end constructor

	private void init(final boolean setSmooth, final boolean setPretree) {
		this.smooth = setSmooth;
		this.pretree = setPretree;

		this.sumFrequency = 0;
		this.wordInfo = new Hashtable<String, InfoFrequencyRow>();

		this.guessTagRev = new Pretree();
		this.guessTagRev.setReverse(true); // reverse
		this.guessTagRev.setIgnoreCase(false); // reverse

		this.guessTagFront = new Pretree();
		this.guessTagFront.setReverse(false); // from front
		this.guessTagFront.setIgnoreCase(false); // reverse
	}

	// Loads matrix from file
	protected abstract void load(InputStream filename) throws IOException,
			FileNotFoundException;

	protected boolean loadPretree(final InputStream filename) {
		return false;
	}

	protected void preparePretree(final String filename) throws IOException {
		this.guessTagFront.prune();
		this.guessTagFront.save(filename + ".front.pretree");

		this.guessTagRev.prune();
		this.guessTagRev.save(filename + ".rev.pretree");

		this.guessTagFront = new Pretree();
		this.guessTagFront.load(filename + ".front.pretree");

		this.guessTagRev = new Pretree();
		this.guessTagRev.load(filename + ".rev.pretree");
	}

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
			lexFileReader.close();
			this.rows = linecount + 1;
		}
	}

	protected double[][] loadNow(final InputStream filename, final boolean fillLexmatrix,
			double[][] lexmatrix, final boolean trainPretree)
					throws FileNotFoundException, NumberFormatException, IOException {
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
			this.SMOOTHVAL = 1.0;
			while ((line = lexFileReader.readLine()) != null) {
				linearray = line.split("\t");

				final int freq = (new Integer(linearray[0])).intValue();
				this.sumFrequency += freq;

				final String newword = linearray[1];
				this.wordInfo.put(newword, new InfoFrequencyRow(freq, linecount));
				this.rowToWord[linecount] = newword;

				// if (wordToRow.size()%10000 == 0) {System.out.println("Lex size:
				// "+wordToRow.size());}

				if (fillLexmatrix) {
					Double actPos;
					double doubleActPos;
					int tagFreq;
					for (int p = 0; p < linearray.length - 2; p++) {
						actPos = new Double(linearray[p + 2]);
						doubleActPos = actPos.doubleValue();
						lexmatrix[linecount][p] = doubleActPos;
						if ((doubleActPos > 0.0) && (doubleActPos < this.SMOOTHVAL)) {
							this.SMOOTHVAL = doubleActPos;
						} // fi
							// insert in pretree

						tagFreq = (int) (0.0001 + (freq) * doubleActPos);

						if (tagFreq > 0) {
							if (this.pretree && (trainPretree)) {
								this.guessTagFront.train(newword, "" + p, tagFreq);
								this.guessTagRev.train(newword, "" + p, tagFreq);
							} // fi pretree
						} // fi tagfreq
					} // rof
				} // fi fillLexmatrix

				linecount++;
			} // elihw

		}
		return lexmatrix;
	}

	/* returns word prob without context - approximated by 1+freq/sumfreq */
	public double getWordProb(final String word) {
		int freq = 0;
		if (this.wordInfo.containsKey(word)) {
			freq = (this.wordInfo.get(word)).getFrequency() + 1;
		} else {
			freq = 1;
		}

		return ((double) freq) / (double) this.sumFrequency;
	}

	public double[] getTagDistribution(final String word) {
		final double[] retVal = new double[this.columns];

		if (this.wordInfo.containsKey(word)) {
			for (int i = 0; i < this.columns; i++) {
				retVal[i] = getMatrixValue((this.wordInfo.get(word)).getRow(),
						i)
						+ this.SMOOTHVAL;
			}

			return retVal;
		}

		if (this.pretree) {
			final List<String> reasonF = this.guessTagFront.classDistribution(word);
			final Map<String, String> rfhash = new HashMap<>(reasonF.size());
			//
			int rfsum = 0;
			for (final String elem : reasonF) {
				final String a[] = elem.split("=");
				rfhash.put(a[0], a[1]);
				rfsum += (new Integer(a[1])).intValue();
			} // rof enum e

			final List<String> reasonR = this.guessTagRev.classDistribution(word);
			final Map<String, String> rrhash = new HashMap<>(reasonR.size());
			//
			int rrsum = 0;
			for (final String elem : reasonR) {
				final String a[] = elem.split("=");
				rfhash.put(a[0], a[1]);
				rrsum += (new Integer(a[1])).intValue();
			} // rof enum e

			for (int i = 0; i < this.columns; i++) {
				double frval = this.SMOOTHPRETREEVAL;
				double rrval = this.SMOOTHPRETREEVAL;
				final String key = new Integer(i).toString();
				final String rfValue = rfhash.get(key);

				if (rfValue != null) {
					frval += (new Double(rfValue))
							.doubleValue()
							/ (rfsum);
				}

				final String rrValue = rrhash.get(key);
				if (rrValue != null) {
					rrval += (new Double(rrValue))
							.doubleValue()
							/ (rrsum);
				}
				retVal[i] = frval * rrval;
			} // rof i
		} else {
			for (int i = 0; i < this.columns; i++) {
				retVal[i] = 1.0; // if word is not in lexicon, return max
									// probability for all tags
			}
		} // esle fi pretree

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
