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
public abstract class Transitions {
	boolean d = !true; // debugging

	protected double SMOOTHVAL;

	protected TagList taglist;

	protected int dimension;

	/** Creates a new instance of Transistions */
	public Transitions(TagList tl) {
		this.taglist = tl;
		this.dimension = tl.getNrOfTags();
		this.SMOOTHVAL = 1;
	} // end public Transitions(String filename, TagList tl) {

	protected abstract double getTransProb(int x, int y, int z);
}
