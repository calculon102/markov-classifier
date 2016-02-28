package de.uni_leipzig.asv.toolbox.viterbitagger;

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
 * @author unbekannt
 * @version 1.0
 */
public class InfoFrequencyRow {
	private int frequency;

	private int row;

	public InfoFrequencyRow(int frequency, int row) {
		this.frequency = frequency;
		this.row = row;
	}

	public int getFrequency() {
		return this.frequency;
	}

	public int getRow() {
		return this.row;
	}
}
