/*
 * Evaluator.java
 *
 * Created on 5. Dezember 2005, 16:02
 *
 * Evaluates non-matching Tagsets for Tagger
 */

package de.uni_leipzig.asv.toolbox.viterbitagger;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * 
 * @author BIEMANN
 */
public class Evaluator {

	static boolean d = false;

	private int tot_wordcount = 0;

	private int known_wordcount = 0;

	private int unknown_wordcount = 0;

	private final Map<String, Integer> tot_goldtags = new HashMap<>(); // for storing gold standard tags

	private final Map<String, Integer> tot_lextags = new HashMap<>(); // for storing tags used here

	private final Map<String, Integer> tot_confusionTable = new HashMap<>(); // for storing combinations gold/used tags

	// known
	private final Map<String, Integer> known_goldtags = new HashMap<>(); // for storing gold standard tags

	private final Map<String, Integer> known_lextags = new HashMap<>(); // for storing tags used here

	private final Map<String, Integer> known_confusionTable = new HashMap<>(); // for storing combinations gold/used tags

	// unknown
	private final Map<String, Integer> unknown_goldtags = new HashMap<>(); // for storing gold standard tags

	private final Map<String, Integer> unknown_lextags = new HashMap<>(); // for storing tags used here

	private final Map<String, Integer> unknown_confusionTable = new HashMap<>(); // for storing combinations gold/used tags

	// Holes statistics
	private int tot_holes = 0;

	private final int[] holes_length = new int[MAXHOLES];

	private int holecounter = 0;

	private static final int MAXHOLES = 10;

	private String statistics;

	public void add(final String goldtag, final String lextag, final boolean known) {

		// holes
		if (known) {
			if (holecounter > 0) {
				tot_holes++;
				if (holecounter >= MAXHOLES) {
					holes_length[0]++;

				} else {
					holes_length[holecounter]++;
				}
				holecounter = 0;
			}
		} else {
			holecounter++;
		}

		// statistics for total
		tot_wordcount++;

		if (tot_goldtags.containsKey(goldtag)) {
			tot_goldtags.put(goldtag, new Integer(tot_goldtags
					.get(goldtag).intValue() + 1));
		} else {
			tot_goldtags.put(goldtag, new Integer(1));
		}

		if (tot_lextags.containsKey(lextag)) {
			tot_lextags.put(lextag, new Integer(tot_lextags
					.get(lextag).intValue() + 1));
		} else {
			tot_lextags.put(lextag, new Integer(1));
		}

		if (tot_confusionTable.containsKey(goldtag + "|" + lextag)) {
			tot_confusionTable.put(goldtag + "|" + lextag, new Integer(
					tot_confusionTable.get(goldtag + "|" + lextag)
							.intValue() + 1));
		} else {
			tot_confusionTable.put(goldtag + "|" + lextag, new Integer(1));
		}

		if (known) {
			known_wordcount++;
			// statistics for known
			if (known_goldtags.containsKey(goldtag)) {
				known_goldtags
						.put(goldtag, new Integer(known_goldtags
								.get(goldtag).intValue() + 1));
			} else {
				known_goldtags.put(goldtag, new Integer(1));
			}

			if (known_lextags.containsKey(lextag)) {
				known_lextags.put(lextag, new Integer(known_lextags
						.get(lextag).intValue() + 1));
			} else {
				known_lextags.put(lextag, new Integer(1));
			}

			if (known_confusionTable.containsKey(goldtag + "|" + lextag)) {
				known_confusionTable.put(goldtag + "|" + lextag, new Integer(
						known_confusionTable.get(goldtag + "|"
								+ lextag).intValue() + 1));
			} else {
				known_confusionTable
						.put(goldtag + "|" + lextag, new Integer(1));
			}

		} else {
			// statistics for unknown
			unknown_wordcount++;
			if (unknown_goldtags.containsKey(goldtag)) {
				unknown_goldtags.put(goldtag,
						new Integer(unknown_goldtags.get(goldtag)
								.intValue() + 1));
			} else {
				unknown_goldtags.put(goldtag, new Integer(1));
			}

			if (unknown_lextags.containsKey(lextag)) {
				unknown_lextags
						.put(lextag, new Integer(unknown_lextags
								.get(lextag).intValue() + 1));
			} else {
				unknown_lextags.put(lextag, new Integer(1));
			}

			if (unknown_confusionTable.containsKey(goldtag + "|" + lextag)) {
				unknown_confusionTable.put(goldtag + "|" + lextag, new Integer(
						unknown_confusionTable.get(goldtag + "|"
								+ lextag).intValue() + 1));
			} else {
				unknown_confusionTable.put(goldtag + "|" + lextag, new Integer(
						1));
			}
		} // esle fi known
	} // end public void add(

	public void printStatistics() {
		int curr_val;
		int corr_sum;
		int total_sum;
		double tot_CP_gold, tot_CP_lex;
		double known_CP_gold, known_CP_lex;
		double unknown_CP_gold, unknown_CP_lex;

		double tot_HC_gold, tot_HC_lex;
		double known_HC_gold, known_HC_lex;
		double unknown_HC_gold, unknown_HC_lex;
		double tot_ICC;
		double known_ICC;
		double unknown_ICC;
		double tot_EP;
		final double known_EP;
		double unknown_EP;
		final double tot_cctp;
		final double known_cctp;
		final double unknown_cctp;

		int max;

		// total items
		if (d) {
			System.out.print("tot CM");
			for (final Entry<String, Integer> lt : tot_lextags.entrySet()) {
				System.out.print("\t" + lt.getKey());
			} // rof enum lt
		}

		// total
		corr_sum = 0;
		total_sum = 0;
		tot_HC_gold = 0;
		tot_HC_lex = 0;
		tot_ICC = 0;

		for (final Entry<String, Integer> gt : tot_goldtags.entrySet()) {
			final String curr_gt = gt.getKey();
			if (d) {
				System.out.print("\n" + curr_gt);
			}

			tot_HC_gold -= (tot_goldtags.get(curr_gt).doubleValue() / tot_wordcount)
					* Math.log(tot_goldtags.get(curr_gt)
							.doubleValue()
							/ tot_wordcount);
			max = 0;
			for (final Entry<String, Integer> lt : tot_lextags.entrySet()) {
				final String curr_lt = lt.getKey();
				if (tot_confusionTable.containsKey(curr_gt + "|" + curr_lt)) {
					curr_val = tot_confusionTable.get(curr_gt + "|"
							+ curr_lt).intValue();

					tot_ICC += (((double) curr_val) / (double) tot_wordcount)
							* Math
									.log((((double) curr_val) / (double) tot_wordcount)
											/ ((tot_goldtags
													.get(curr_gt)
													.doubleValue() / tot_wordcount) * (tot_lextags
													.get(curr_lt)
													.doubleValue() / tot_wordcount)));

				} else {
					curr_val = 0;
				} // esle fi
				if (max < curr_val) {
					max = curr_val;
				}
				if (d) {
					System.out.print("\t" + curr_val);
				}
			} // rof enum lt
				// System.out.println("Max:"+max+"
				// total:"+(Integer)tot_goldtags.get(curr_gt));

			corr_sum += max;
			total_sum += tot_goldtags.get(curr_gt).intValue();
		} // rof enum gt
		tot_CP_gold = (double) corr_sum / (double) total_sum;

		corr_sum = 0;
		total_sum = 0;
		for (final Entry<String, Integer> lt : tot_lextags.entrySet()) {
			final String curr_lt = lt.getKey();
			tot_HC_lex -= (tot_lextags.get(curr_lt).doubleValue() / tot_wordcount)
					* Math.log(tot_lextags.get(curr_lt)
							.doubleValue()
							/ tot_wordcount);

			max = 0;
			for (final Entry<String, Integer> gt : tot_goldtags.entrySet()) {
				final String curr_gt = gt.getKey();
				if (tot_confusionTable.containsKey(curr_gt + "|" + curr_lt)) {
					curr_val = tot_confusionTable.get(curr_gt + "|"
							+ curr_lt).intValue();
				} else {
					curr_val = 0;
				} // esle fi
				if (max < curr_val) {
					max = curr_val;
				}
			} // rof enum lt

			corr_sum += max;
			total_sum += tot_lextags.get(curr_lt).intValue();
		} // rof enum gt
		tot_CP_lex = (double) corr_sum / (double) total_sum;
		tot_EP = tot_ICC / tot_HC_gold;
		// known items
		if (d) {
			System.out.print("\n\nknw CM");
			for (final Entry<String, Integer> lt : known_lextags.entrySet()) {
				System.out.print("\t" + lt.getKey());
			} // rof enum lt
		}

		corr_sum = 0;
		total_sum = 0;
		known_HC_gold = 0;
		known_HC_lex = 0;
		known_ICC = 0;

		for (final Entry<String, Integer> gt : known_goldtags.entrySet()) {
			final String curr_gt = gt.getKey();
			if (d) {
				System.out.print("\n" + curr_gt);
			}

			known_HC_gold -= (known_goldtags.get(curr_gt)
					.doubleValue() / known_wordcount)
					* Math.log(known_goldtags.get(curr_gt)
							.doubleValue()
							/ known_wordcount);
			max = 0;
			for (final Entry<String, Integer> lt : known_lextags.entrySet()) {
				final String curr_lt = lt.getKey();
				if (known_confusionTable.containsKey(curr_gt + "|" + curr_lt)) {
					curr_val = known_confusionTable.get(curr_gt
							+ "|" + curr_lt).intValue();

					known_ICC += (((double) curr_val) / (double) known_wordcount)
							* Math
									.log((((double) curr_val) / (double) known_wordcount)
											/ ((known_goldtags
													.get(curr_gt)
													.doubleValue() / known_wordcount) * (known_lextags
													.get(curr_lt)
													.doubleValue() / known_wordcount)));

				} else {
					curr_val = 0;
				} // esle fi
				if (max < curr_val) {
					max = curr_val;
				}
				if (d) {
					System.out.print("\t" + curr_val);
				}
			} // rof enum lt
				// System.out.println("Max:"+max+"
				// total:"+(Integer)known_goldtags.get(curr_gt));

			corr_sum += max;
			total_sum += known_goldtags.get(curr_gt).intValue();
		} // rof enum gt

		known_CP_gold = (double) corr_sum / (double) total_sum;
		if (total_sum == 0) {
			known_CP_gold = 0;
		}
		corr_sum = 0;
		total_sum = 0;
		for (final Entry<String, Integer> lt : known_lextags.entrySet()) {
			final String curr_lt = lt.getKey();
			known_HC_lex -= (known_lextags.get(curr_lt)
					.doubleValue() / known_wordcount)
					* Math.log(known_lextags.get(curr_lt)
							.doubleValue()
							/ known_wordcount);

			max = 0;
			for (final Entry<String, Integer> gt : known_goldtags.entrySet()) {
				final String curr_gt = gt.getKey();
				if (known_confusionTable.containsKey(curr_gt + "|" + curr_lt)) {
					curr_val = known_confusionTable.get(curr_gt
							+ "|" + curr_lt).intValue();
				} else {
					curr_val = 0;
				} // esle fi
				if (max < curr_val) {
					max = curr_val;
				}
			} // rof enum lt

			corr_sum += max;
			total_sum += known_lextags.get(curr_lt).intValue();
		} // rof enum gt
		known_CP_lex = (double) corr_sum / (double) total_sum;
		known_EP = known_ICC / known_HC_gold;
		// unknown
		if (d) {
			System.out.print("\n\nukn CM");
			for (final Entry<String, Integer> lt : unknown_lextags.entrySet()) {
				System.out.print("\t" + lt.getKey());
			} // rof enum lt
		}

		// total
		corr_sum = 0;
		total_sum = 0;
		unknown_HC_gold = 0;
		unknown_HC_lex = 0;
		unknown_ICC = 0;

		for (final Entry<String, Integer> gt : unknown_goldtags.entrySet()) {
			final String curr_gt = gt.getKey();
			if (d) {
				System.out.print("\n" + curr_gt);
			}

			unknown_HC_gold -= (unknown_goldtags.get(curr_gt)
					.doubleValue() / unknown_wordcount)
					* Math.log(unknown_goldtags.get(curr_gt)
							.doubleValue()
							/ unknown_wordcount);
			max = 0;
			for (final Entry<String, Integer> lt : unknown_lextags.entrySet()) {
				final String curr_lt = lt.getKey();
				if (unknown_confusionTable.containsKey(curr_gt + "|" + curr_lt)) {
					curr_val = unknown_confusionTable.get(curr_gt
							+ "|" + curr_lt).intValue();

					unknown_ICC += (((double) curr_val) / (double) unknown_wordcount)
							* Math
									.log((((double) curr_val) / (double) unknown_wordcount)
											/ ((unknown_goldtags
													.get(curr_gt)
													.doubleValue() / unknown_wordcount) * (unknown_lextags
													.get(curr_lt)
													.doubleValue() / unknown_wordcount)));

				} else {
					curr_val = 0;
				} // esle fi
				if (max < curr_val) {
					max = curr_val;
				}
				if (d) {
					System.out.print("\t" + curr_val);
				}
			} // rof enum lt
				// System.out.println("Max:"+max+"
				// total:"+(Integer)unknown_goldtags.get(curr_gt));

			corr_sum += max;
			total_sum += unknown_goldtags.get(curr_gt).intValue();
		} // rof enum gt
		unknown_CP_gold = (double) corr_sum / (double) total_sum;
		if (total_sum == 0) {
			unknown_CP_gold = 0;
		}
		corr_sum = 0;
		total_sum = 0;
		for (final Entry<String, Integer> lt : unknown_lextags.entrySet()) {
			final String curr_lt = lt.getKey();
			unknown_HC_lex -= (unknown_lextags.get(curr_lt)
					.doubleValue() / unknown_wordcount)
					* Math.log(unknown_lextags.get(curr_lt)
							.doubleValue()
							/ unknown_wordcount);

			max = 0;
			for (final Entry<String, Integer> gt : unknown_goldtags.entrySet()) {
				final String curr_gt = gt.getKey();
				if (unknown_confusionTable.containsKey(curr_gt + "|" + curr_lt)) {
					curr_val = unknown_confusionTable.get(curr_gt
							+ "|" + curr_lt).intValue();
				} else {
					curr_val = 0;
				} // esle fi
				if (max < curr_val) {
					max = curr_val;
				}
			} // rof enum lt

			corr_sum += max;
			total_sum += unknown_lextags.get(curr_lt).intValue();
		} // rof enum gt
		unknown_CP_lex = (double) corr_sum / (double) total_sum;
		if (total_sum == 0) {
			unknown_CP_lex = 0;
		}
		unknown_EP = unknown_ICC / unknown_HC_gold;
		if (unknown_HC_gold == 0) {
			unknown_EP = 0;
		}
		tot_cctp = Math.pow(Math.E, (tot_HC_gold - tot_ICC));
		known_cctp = Math.pow(Math.E, (known_HC_gold - known_ICC));
		unknown_cctp = Math.pow(Math.E, (unknown_HC_gold - unknown_ICC));

		// PRINT IT
		statistics = "";
		statistics += "TOTAL TOKENS:\t" + tot_wordcount + "\n";
		statistics += "\ntotal cluster purity goldtags:\t" + tot_CP_gold + "\n";
		statistics += "total cluster purity lextags: \t" + tot_CP_lex + "\n";
		statistics += "total entropy goldtags: \t" + tot_HC_gold + "\n";
		statistics += "total entropy lextags: \t" + tot_HC_lex + "\n";
		statistics += "total conditional Entropy: \t" + tot_ICC + "\n";
		statistics += "total entropy purity lextags:\t" + tot_EP + "\n";
		// System.out.println("total CLuster distance:\t"+tot_CD);
		statistics += "total cluster-cond. tag perplexity:\t" + tot_cctp + "\n";

		statistics += "\nKNOWN TOKENS:\t" + known_wordcount + " ("
				+ (double) known_wordcount / (double) tot_wordcount * 100
				+ "% )\n";
		statistics += "known cluster purity goldtags: \t" + known_CP_gold
				+ "\n";
		statistics += "known cluster purity lextags: \t" + known_CP_lex + "\n";
		statistics += "known entropy goldtags: \t" + known_HC_gold + "\n";
		statistics += "known entropy lextags: \t" + known_HC_lex + "\n";
		statistics += "known conditional Entropy: \t" + known_ICC + "\n";
		statistics += "known entropy purity lextags:\t" + known_EP + "\n";
		// System.out.println("known CLuster distance:\t"+known_CD);
		statistics += "known cluster-cond. tag perplexity:\t" + known_cctp
				+ "\n";

		statistics += "\nUNKNOWN TOKENS:\t" + unknown_wordcount
				+ "\nunknown percent:\t" + (double) unknown_wordcount
				/ (double) tot_wordcount * 100 + "\n";
		statistics += "unknown cluster purity goldtags: \t" + unknown_CP_gold
				+ "\n";
		statistics += "unknown cluster purity lextags:\t" + unknown_CP_lex
				+ "\n";
		statistics += "unknown entropy goldtags: \t" + unknown_HC_gold + "\n";
		statistics += "unknown entropy lextags: \t" + unknown_HC_lex + "\n";
		statistics += "unknown conditional Entropy: \t" + unknown_ICC + "\n";
		statistics += "unknown entropy purity lextags:\t" + unknown_EP + "\n";
		// System.out.println("unknown CLuster distance:\t"+unknown_CD);
		statistics += "unknown cluster-cond. tag perplexity:\t" + unknown_cctp
				+ "\n";

		statistics += "\nHOLE statistics\ntotal holes: " + tot_holes + "\n";
		for (int i = 1; i < MAXHOLES; i++) {
			if (tot_holes == 0) {
				statistics += " length " + i + ": " + holes_length[i] + "("
						+ (double) 0 + "% )" + "\n";
			} else {
				statistics += " length " + i + ": " + holes_length[i] + "("
						+ (double) holes_length[i] / (double) tot_holes * 100
						+ "% )" + "\n";
			}
		}
		if (tot_holes == 0) {
			statistics += " longer: " + holes_length[0] + "(" + (double) 0
					+ "% )" + "\n";
		} else {
			statistics += " longer: " + holes_length[0] + "("
					+ (double) holes_length[0] / (double) tot_holes * 100
					+ "% )" + "\n";
		}

	} // end void printStatistics

	public String getStatistics() {
		return statistics;
	}

}
