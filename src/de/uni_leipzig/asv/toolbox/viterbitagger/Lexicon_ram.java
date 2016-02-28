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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

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
 * @author BIEMANN
 * @version 1.0
 */
public class Lexicon_ram extends Lexicon {
	private double[][] lexmatrix;

	/** Creates a new instance of Lexicon */
	public Lexicon_ram(final InputStream lexiconfile, final boolean setSmooth, final boolean setPretree) {
		super(lexiconfile, setSmooth, setPretree);
	} // end constructor

	// Loads matrix from file
	@Override
	protected void load(final InputStream filename) throws IOException, FileNotFoundException {
		final boolean pretreePrepared = loadPretree(filename);

		determinDimension(filename);
		filename.reset();

		this.lexmatrix = loadNow(filename, true, this.lexmatrix, !pretreePrepared);

		if (this.smooth) {
			this.SMOOTHVAL = this.SMOOTHVAL / 2;
		} else {
			this.SMOOTHVAL = 0;
		}
	} // end private void load

	@Override
	protected double getMatrixValue(final int row, final int column) {
		return this.lexmatrix[row][column];
	}
} // end public class Lexicon
