/*
 * TagList.java
 *
 * Maintains the list of tags.
 * "-" Tag is invented for inventing tokens before the beginning of sentences.
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
public class TagList {
	boolean d = !true; // debugging

	private String[] rowToTag;

	private int nr_of_tags;

	private int sumTagFrequency;

	private Map<String, InfoFrequencyRow> tagInfo;

	/** Creates a new instance of TagList */
	public TagList(final InputStream filename) {
		try {
			load(filename);
		} catch (final Exception e) {
			System.err.println("File IO problem with " + filename + " :" + e.getMessage());
		}

		if (this.d) {
			printTagList();
		}
	} // end public TagList

	private void load(final InputStream filename) throws IOException, FileNotFoundException {
		this.tagInfo = new HashMap<String, InfoFrequencyRow>();
		this.sumTagFrequency = 0;
		int linecount = 0;
		String line;

		// determin length of array
		determinNrOfTags(filename);
		filename.reset();

		// and fill it
		try (final BufferedReader lexFileReader2 = new BufferedReader(new InputStreamReader(filename))) {
			linecount = 0;
			while ((line = lexFileReader2.readLine()) != null) {
				final String[] items = line.split("\t");
				final String newtag = items[0];
				if (items.length > 1) {
					this.tagInfo.put(newtag, new InfoFrequencyRow(new Integer(
							items[1]).intValue(), linecount));
					this.sumTagFrequency += (new Integer(items[1])).intValue();
				} else {
					this.tagInfo.put(newtag, new InfoFrequencyRow(1, linecount));
					this.sumTagFrequency += 1;
				}
				this.rowToTag[linecount] = newtag;
				linecount++;
			} // elihw
		}

		// invented dummy tag
		this.tagInfo.put("-", new InfoFrequencyRow(-1, linecount));
		this.rowToTag[linecount] = "-";

	} // end private void load

	protected void determinNrOfTags(final InputStream filename) throws FileNotFoundException, IOException {
		// Config einlesen
		int linecount = 0;

		try (final BufferedReader lexFileReader = new BufferedReader(new InputStreamReader(filename))) {
			// Count lines for array dimensioning
			while (lexFileReader.readLine() != null) {
				linecount++;
			} // elihw
		}
		this.nr_of_tags = linecount + 1;

		// Initialize
		this.rowToTag = new String[this.nr_of_tags];
	}

	public int getNrOfTags() {
		return this.nr_of_tags;
	} // end public int getNrOfTags()

	public int getCodeForTag(final String tag) {
		int retval;

		if (this.tagInfo.containsKey(tag)) {
			retval = (this.tagInfo.get(tag)).getRow();
		} else {
			retval = -1;
		} // esle fi-
		return retval;
	} // end public int getCodeForTag(String tag)

	public String getTagForCode(final int code) {
		String retStr = new String();
		if (code < this.nr_of_tags) {
			retStr = this.rowToTag[code];
		} else {
			retStr = "undefined";
		} // esle fi-
		return retStr;
	} // end public String getTagForCode(int code)

	/* returns tag probability without context */
	public double getTagProb(final String tag) {
		double ret;
		if (this.tagInfo.containsKey(tag)) {
			ret = ((this.tagInfo.get(tag)).getFrequency() + 1.0)
					/ this.sumTagFrequency;
		} else {
			ret = 0;
		} // esle fi-
		return ret;
	} // end get Tasg prob

	public void printTagList() {
		System.out.println("---The " + this.nr_of_tags
				+ " tags and their internal code ---");
		for (int r = 0; r < this.nr_of_tags; r++) {
			System.out.println(this.rowToTag[r] + "\t" + r);
		} // rod// roff
	} // end printmatrix

} // end public class TagList
