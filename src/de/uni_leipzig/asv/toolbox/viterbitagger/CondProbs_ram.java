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
 * @author not attributable
 * @version 1.0
 */
public class CondProbs_ram extends CondProbs {
	private double[][] lexmatrix;

	/** Creates a new instance of Lexicon */
	public CondProbs_ram(final InputStream condprobsfile) {
		super(condprobsfile);
	} // end constructor

	// Loads matrix from file
	@Override
	protected void load(final InputStream filename) throws IOException, FileNotFoundException {
		determinDimension(filename);
		filename.reset();

		this.lexmatrix = loadNow(filename, true, this.lexmatrix);
	} // end private void load

	@Override
	protected double getMatrixValue(final int row, final int column) {
		return this.lexmatrix[row][column];
	}
}
