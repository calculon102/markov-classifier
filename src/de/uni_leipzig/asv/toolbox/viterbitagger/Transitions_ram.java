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
import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author BIEMANN
 */
public class Transitions_ram extends Transitions {
	private final Map<String, Double> transTable = new HashMap<>();

	/** Creates a new instance of Transistions */
	public Transitions_ram(final InputStream transitionfile, final TagList tl) {
		super(tl);

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
		String goaltag;

		final BufferedReader FileReader = new BufferedReader(new InputStreamReader(transitionfile));

		while ((line = FileReader.readLine()) != null) {
			linearray = line.split("\t");
			tags = linearray[0].split("\\|");

			if (this.d) {
				System.out.println("prefix " + linearray[0] + " split into "
						+ tags[0] + " and " + tags[1]);
			}

			for (int p = 0; p < linearray.length - 1; p++) {
				final Double actPos = new Double(linearray[p + 1]);
				goaltag = this.taglist.getTagForCode(p);
				final double doubleActPos = actPos.doubleValue();

				if (!(linearray[p + 1].equals("0"))) {
					this.transTable.put(
							tags[0] + "|" + tags[1] + "|" + goaltag, actPos);
				}

				if ((doubleActPos > 0.0) && (doubleActPos < this.SMOOTHVAL)) {
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

	} // end private void load

	@Override
	public double getTransProb(final int x, final int y, final int z) {
		final String tagstring = this.taglist.getTagForCode(x) + "|"
				+ this.taglist.getTagForCode(y) + "|"
				+ this.taglist.getTagForCode(z);

		final Double transition = this.transTable.get(tagstring);
		if (transition != null) {
			return (transition).doubleValue() + this.SMOOTHVAL;
		}

		return this.SMOOTHVAL;

	} // end public double getTransProb

	/*
	 * public double getTransProb(String t1, String t2, String t3) { double
	 * retVal;
	 * 
	 * String tagstring=t1+"|"+t2+"|"+t3;
	 * 
	 * if(transTable.containsKey(tagstring)) {
	 * retVal=((Double)transTable.get(tagstring)).doubleValue()+SMOOTHVAL; }
	 * else { retVal=SMOOTHVAL; } // esle fi.
	 * 
	 * return retVal; } // end public double getTransProb
	 * 
	 */

} // end public class Transitions
