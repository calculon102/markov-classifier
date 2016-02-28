package de.uni_leipzig.asv.toolbox.viterbitagger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

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
 * Copyright: Copyright (c) 2005
 * </p>
 * 
 * <p>
 * Company:
 * </p>
 * 
 * @author wittig
 * @version 1.0
 */
public class Config {
	// Daten in <TrainingsdatenFilename>.xml
	private int lexmatrixColumns = -1, lexmatrixRows = -1,
			condProbmatrixColumns = -1, condProbmatrixRows = -1, nrOfTags = -1;

	// sonstige Daten
	private final File configDatei;

	public Config(final String filename) {
		this.configDatei = new File(filename + ".xml");
		configEinlesen();
	}

	public boolean lexmatrixColumnsExists() {
		return this.lexmatrixColumns > 0;
	}

	public boolean lexmatrixRowsExists() {
		return this.lexmatrixRows > 0;
	}

	public boolean condprobmatrixColumnsExists() {
		return this.condProbmatrixColumns > 0;
	}

	public boolean condprobmatrixRowsExists() {
		return this.condProbmatrixRows > 0;
	}

	public boolean nrOfTagsExists() {
		return this.nrOfTags > 0;
	}

	public int getLexmatrixColumns() {
		return this.lexmatrixColumns;
	}

	public int getLexmatrixRows() {
		return this.lexmatrixRows;
	}

	public int getNrOfTags() {
		return this.nrOfTags;
	}

	public int getCondProbmatrixColumns() {
		return this.condProbmatrixColumns;
	}

	public int getCondProbmatrixRows() {
		return this.condProbmatrixRows;
	}

	public void setLexmatrixColumns(final int lexmatrixColumns) {
		this.lexmatrixColumns = lexmatrixColumns;
		configSpeichern();
	}

	public void setLexmatrixRows(final int lexmatrixRows) {
		this.lexmatrixRows = lexmatrixRows;
		configSpeichern();
	}

	public void setCondProbmatrixColumns(final int condProbmatrixColumns) {
		this.condProbmatrixColumns = condProbmatrixColumns;
		configSpeichern();
	}

	public void setCondProbmatrixRows(final int condProbmatrixRows) {
		this.condProbmatrixRows = condProbmatrixRows;
		configSpeichern();
	}

	public void setNrOfTags(final int nrOfTags) {
		this.nrOfTags = nrOfTags;
		configSpeichern();
	}

	public void configEinlesen() {
		BufferedReader configDateiReader = null;
		String zeile;

		try {
			configDateiReader = new BufferedReader(new FileReader(
					this.configDatei));

			while ((zeile = configDateiReader.readLine()) != null) {
				if (zeile.contains("<lexmatrix-columns>")) {
					zeile = zeile.substring(zeile.indexOf(">") + 1);
					this.lexmatrixColumns = new Integer(zeile.substring(0,
							zeile.indexOf("<"))).intValue();
				}
				if (zeile.contains("<lexmatrix-rows>")) {
					zeile = zeile.substring(zeile.indexOf(">") + 1);
					this.lexmatrixRows = new Integer(zeile.substring(0, zeile
							.indexOf("<"))).intValue();
				}
				if (zeile.contains("<condprobmatrix-columns>")) {
					zeile = zeile.substring(zeile.indexOf(">") + 1);
					this.condProbmatrixColumns = new Integer(zeile.substring(0,
							zeile.indexOf("<"))).intValue();
				}
				if (zeile.contains("<condprobmatrix-rows>")) {
					zeile = zeile.substring(zeile.indexOf(">") + 1);
					this.condProbmatrixRows = new Integer(zeile.substring(0,
							zeile.indexOf("<"))).intValue();
				}
				if (zeile.contains("<nrOfTags>")) {
					zeile = zeile.substring(zeile.indexOf(">") + 1);
					this.nrOfTags = new Integer(zeile.substring(0, zeile
							.indexOf("<"))).intValue();
				}
			}
			configDateiReader.close();
		} catch (final IOException ex) {
		}
	}

	public void configSpeichern() {
		/*
		 * new File(this.configDatei.getAbsolutePath()).delete(); PrintWriter
		 * configDateiWriter = null;
		 *  // neue config-Datei anlegen try { configDateiWriter = new
		 * PrintWriter(new BufferedWriter(new FileWriter(this.configDatei)));
		 * configDateiWriter.println("<?xml version=\"1.0\"
		 * encoding=\"UTF-8\"?>"); configDateiWriter.println("<lexmatrix-columns>" +
		 * this.lexmatrixColumns + "</lexmatrix-columns>");
		 * configDateiWriter.println("<lexmatrix-rows>" + this.lexmatrixRows + "</lexmatrix-rows>");
		 * configDateiWriter.println("<condprobmatrix-columns>" +
		 * this.condProbmatrixColumns + "</condprobmatrix-columns>");
		 * configDateiWriter.println("<condprobmatrix-rows>" +
		 * this.condProbmatrixRows + "</condprobmatrix-rows>");
		 * configDateiWriter.println("<nrOfTags>" + this.nrOfTags + "</nrOfTags>");
		 * configDateiWriter.flush(); configDateiWriter.close(); } catch
		 * (IOException ex) { ex.printStackTrace(); }
		 */
	}

	@Override
	public String toString() {
		String s = "Config:\n";
		s += "  lexmatrixColumns: " + this.lexmatrixColumns + "\n";
		s += "  lexmatrixRows: " + this.lexmatrixRows + "\n";
		s += "  condProbmatrixColumns: " + this.condProbmatrixColumns + "\n";
		s += "  condProbmatrixRows: " + this.condProbmatrixRows + "\n";
		s += "  nrOfTags: " + this.nrOfTags + "\n";
		return s;
	}
}
