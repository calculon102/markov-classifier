/*
 * Tagger.java
 *
 *  Input format is plain text , words and interpunctuation marks seperated by spaces like in this comment .
 *  When inputting text with e.g. " e.g. " , this will be treated as one word .
 *  The tagger processes one line at the time , which is supposed to contain one sentence .
 */

package de.uni_leipzig.asv.toolbox.viterbitagger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.uni_leipzig.asv.utils.tokenizer.ImprovedWordTokenizer;

/**
 * 
 * @author BIEMANN
 */
public class Tagger {
	boolean d = !true; // debugging

	private final boolean e = false; // display results while evaluating, only
	// accessible via code here

	private final boolean eval; // evaluation statistics

	private final boolean taggedOut; // write tagged text to console

	protected boolean transform = !true; // transforms ","->_KOM_, ".!?"->
											// _ESENT[ESF]_ or more strange
											// things ;) // for de_old!!

	private final boolean fast; // if true uses fast transitions that waste tons of memory

	private final Lexicon lexicon;

	private final TagList taglist;

	private Transitions transitions;

	private Evaluator evaluator;

	private CondProbs condprobs;

	private final int beamwidth = 5; // 5 is a safe choice, as 3 is usually
										// suficient.

	private String evalResult;

	private StringBuffer tagResult;

	public int finished = 0;

	private boolean useInternalTok = false;

	private boolean replaceNumbers, appendStar = true;

	/** Creates a new instance of Tagger */
	/*
	 * public Tagger(String taglistfile, String lexiconfile, String
	 * transitionfile, boolean use_eval) { this(taglistfile, lexiconfile,
	 * transitionfile, use_eval); }
	 */

	public void setAppendStar(final boolean appendStar) {
		this.appendStar = appendStar;
	}

	/** Creates a new instance of Tagger */
	public Tagger(final InputStream taglistfile, final InputStream lexiconfile, final InputStream transitionfile, final InputStream condprobsfile, final boolean use_eval) {
		this.fast = false;
		this.eval = use_eval;
		this.taggedOut = !this.eval; // either evaluate or tagit

		// load taglist
		this.taglist = new TagList(taglistfile);

		if (this.d) {
			System.out.println("Taglist loaded:" + this.taglist.getNrOfTags());
		}

		this.lexicon = new Lexicon_ram(lexiconfile, false, true);

		if (this.d) {
			System.out.println("Lexicon loaded");
		}

		if (this.fast) {
			this.transitions = new Transitions_ram_fast(transitionfile, this.taglist);
		} else {
			this.transitions = new Transitions_ram(transitionfile, this.taglist);
		}

		if (this.d) {
			System.out.println("Transitions loaded");
		}

		// load conditional Probabilities
		if (condprobsfile != null) {
			this.condprobs = new CondProbs_ram(condprobsfile);

			if (this.d) {
				System.out.println("Conditional propabilities loaded");
			}
		}

		// Evaluator
		if (this.eval) {
			this.evaluator = new Evaluator();
		}
	}

	public String tagSentence(String sentence) {
		this.transform = false;

		if (this.d) {
			System.out.println("Tagging sentence: " + sentence);
		}
		if (this.useInternalTok) {
			final ImprovedWordTokenizer tok = new ImprovedWordTokenizer();
			tok.setBoolReplaceNumbers(this.replaceNumbers);
			sentence = tok.execute(sentence);
		} else {
			if (this.replaceNumbers) {
				final ImprovedWordTokenizer tok = new ImprovedWordTokenizer();
				sentence = tok.processNumber(sentence);
			}
		}

		sentence = "<BOS> <BOS> " + sentence; // add dummy words
												// BeginOfSentence

		final String[] items = sentence.split(" +");
		final int nr_of_words = items.length; // # of words in current sentence

		final String[] words = new String[nr_of_words];

		for (int i = 0; i < nr_of_words; i++) {
			words[i] = items[i];
			if (this.transform) {
				if (words[i].equals(",")) {
					words[i] = "_KOM_";
				}
				if (words[i].equals(".")) {
					words[i] = "_ESENTS_";
				}
				if (words[i].equals("!")) {
					words[i] = "_ESENTE_";
				}
				if (words[i].equals("?")) {
					words[i] = "_ESENTF_";
				}
				if (words[i].equals("\"")) {
					words[i] = "_QUOT_";
				}
				if (words[i].equals("(")) {
					words[i] = "_OPENBRAC_";
				}
				if (words[i].equals(")")) {
					words[i] = "_CLOSEBRAC_";
				}
				if (words[i].equals(":")) {
					words[i] = "_COL_";
				}
				if (words[i].equals(";")) {
					words[i] = "_SEMICOL_";
				}
				if (this.d) {
					System.out.println("Transformed:" + words[i]);
				}
			} // fi transform

		} // rof

		// initialise tagmaxprob
		final List<DoubleAndIntValue> tagmaxprob = new ArrayList<>();
		final int blankTagCode = this.taglist.getCodeForTag("-");

		for (int i = 0; i < this.taglist.getNrOfTags(); i++) {
			double inval = 0.0;

			if (i == this.taglist.getCodeForTag("-")) {
				inval = inval + 1.0; // for upranking of BPOS tag in
										// initialisation
			}
			tagmaxprob.add(new DoubleAndIntValue(inval, i));
		} // rof i

		// initialize maxprob and backpointer matrix

		// maxprob stores the maximal sequence probabilities for every word to
		// have some tag
		final double[][] maxprob = new double[nr_of_words][this.taglist.getNrOfTags()];

		// backpoint stores the tag of the last word that lead to this max prob.
		final int[][] backpoint = new int[nr_of_words][this.taglist.getNrOfTags()];

		// initialize maxprob and backpoint for the two dummy words
		for (int i = 0; i < this.taglist.getNrOfTags(); i++) {
			if (i == blankTagCode) {
				maxprob[0][i] = 1.0;
				maxprob[1][i] = 1.0;
				backpoint[0][i] = blankTagCode;
				backpoint[1][i] = blankTagCode;
			} else {
				maxprob[0][i] = 0;
				maxprob[1][i] = 0;
				backpoint[0][i] = blankTagCode;
				backpoint[1][i] = blankTagCode;
			} // esle fi.
		} // rof i

		if (this.d) {
			System.out.println("Initialized matrices for " + nr_of_words
					+ " words and " + this.taglist.getNrOfTags() + " tags");
		}

		// loop over words and fill matrices
		for (int pos = 2; pos < nr_of_words; pos++) {

			// obtain probabilities per tag for current word
			final double[] allLexProbs = this.lexicon.getTagDistribution(words[pos]);
			Collections.sort(tagmaxprob);
			final List<Integer> acceptTags = new ArrayList<>(tagmaxprob.size());
			int count = 0;

			double absmaxprob = 0.0; // for multiplication in case of
										// underflow
			for (final DoubleAndIntValue current : tagmaxprob) {
				if (count < this.beamwidth) {
					count++;
					if (current.getProbability() > 0.0) {
						acceptTags.add(new Integer(current.getTagNr()));
						if (this.d) {
							System.out.println("accepted: tagnr "
									+ current.getTagNr() + " with p="
									+ current.getProbability());
						}
						if (absmaxprob < current.getProbability()) {
							absmaxprob = current.getProbability();
						}
					} // fi prob>0
				} // fi beamwidth
			} // rof

			// underflow management: multiply with 1E100 if maxprob<1E-200. This
			// is a machine precision hack
			if (absmaxprob < 1E-200) {
				for (int i = 0; i < this.taglist.getNrOfTags(); i++) {
					maxprob[pos - 1][i] = maxprob[pos - 1][i] * 1E100;
				} // rof
			} // fi underflow

			tagmaxprob.clear();

			for (int tag = 0; tag < this.taglist.getNrOfTags(); tag++) {

				if (this.d) {
					System.out.println("Processing tag for word " + tag + " "
							+ words[pos]);
				}

				/*
				 * for current tag, compute all probabilities p(tag|T1,T2) =
				 * sequence probability fot tag taking way T1 T2 =
				 * maxprob[pos-1][T2] * sequence probability ending on T2 *
				 * transition(tag|T1,T2) * probability of tag given last 2 tags *
				 * lex(tag|word). probability of tag, given word
				 * 
				 * This is iterated over all possible T2s. The maximal prob and
				 * the respective T2 is saved in matrices
				 */

				double curr_maxprob = -1.0;
				int curr_backpoint = -1;
				final double lexprob = allLexProbs[tag];

				for (final Integer prevtag : acceptTags) {
					final int prevprevtag = backpoint[pos - 1][prevtag]; // code of
					// T1
					final double prev_sequence_prob = maxprob[pos - 1][prevtag]; // maxprob
					// of
					// T2
					final double trans_prob = this.transitions.getTransProb(
							prevprevtag, prevtag, tag);
					final double curr_prob = prev_sequence_prob * trans_prob
							* lexprob;

					if ((this.d) && (curr_prob > 0.0)) {
						System.out.println("word: " + words[pos]
								+ "   Tag seq: " + prevprevtag + " " + prevtag
								+ " " + tag + "  prob: " + curr_prob);
					}

					if (curr_prob > curr_maxprob) {
						curr_maxprob = curr_prob;
						curr_backpoint = prevtag;
					} // fi. curr>max
				} // rof prevtag

				// save in matrices
				maxprob[pos][tag] = curr_maxprob;
				backpoint[pos][tag] = curr_backpoint;
				if (this.d) {
					System.out.println("Tag and backpointer: " + tag + " "
							+ curr_backpoint);
				}

				tagmaxprob.add(new DoubleAndIntValue(curr_maxprob, tag));

			} // rof tag

		} // rof pos

		// all max paths have been determined. Chose and output tag sequence
		final String tagSequence[] = new String[nr_of_words];

		int maxtag = 0;
		double maxtag_prob = -1;
		for (int tag = 0; tag < this.taglist.getNrOfTags(); tag++) {
			if (maxprob[nr_of_words - 1][tag] > maxtag_prob) {
				maxtag = tag;
				maxtag_prob = maxprob[nr_of_words - 1][tag];
			} // fi. maxprob
		} // rof tag

		for (int pos = nr_of_words - 1; pos >= 0; pos--) {
			tagSequence[pos] = this.taglist.getTagForCode(maxtag);
			maxtag = backpoint[pos][maxtag];
		} // rof pos

		if (this.d) {
			System.out.println(" ----- Sentence result: ------- ");
		}

		String taggedSentence = "";

		// pos starts with 2 to omit begin-of-sentence words
		for (int pos = 2; pos < nr_of_words; pos++) {
			String guess = "";
			final boolean inLex = this.lexicon.containsWord(words[pos]);
			if (!inLex && appendStar) {
				guess = "*";
			}
			taggedSentence += " " + words[pos] + "|" + tagSequence[pos] + guess;
		} // rof pos
		if (this.d) {
			System.out.println(taggedSentence);
		}

		return taggedSentence;
	}

	public void tagFile_alt(final String textfile) throws IOException,
			FileNotFoundException {
		final BufferedReader textFileReader = new BufferedReader(new FileReader(
				textfile));
		String line;
		finished = 0;
		int correct = 0; // for eval
		int total = 0; // for eval
		int totalsen = 0; // for total sentences count

		// Evaluator
		if (this.eval) {
			this.evaluator = new Evaluator();
		}

		// loop over lines=sentences
		while ((line = textFileReader.readLine()) != null) {
			totalsen++;
			finished += (line.length());
			if (this.d) {
				System.out.println("Tagging line:" + line);
			}

			if (this.eval) {
				line = "<BOS>|- <BOS>|- " + line; // add dummy words
													// BeginOfSentence
			} else {
				line = "<BOS> <BOS> " + line; // add dummy words
												// BeginOfSentence
			}

			final String[] items = line.split(" +");
			final int nr_of_words = items.length; // # of words in current sentence

			final String[] words = new String[nr_of_words];
			final String[] condtags = new String[nr_of_words];
			final String[] gold_tags = new String[nr_of_words];

			for (int i = 0; i < nr_of_words; i++) {

				if (this.condprobs != null) {
					final String[] parts = items[i].split("\\|");
					if (this.d) {
						String s = "Split " + items[i] + " into " + parts[0];
						for (int index = 1; index < parts.length; index++) {
							s += " and " + parts[index];
						}
						System.out.println(s);
					}
					words[i] = parts[0];
					if (parts.length > 1) {
						condtags[i] = parts[1];
					} else {
						condtags[i] = "unknown";
					}
					if (this.eval) {
						if (parts.length > 2) {
							gold_tags[i] = parts[2];
						} else {
							gold_tags[i] = "unknown";
						}
					}
				} else {
					if (this.eval) {
						final String[] parts = items[i].split("\\|");
						if (parts.length > 1) {
							if (this.d) {
								System.out.println("Split " + items[i]
										+ " into " + parts[0] + " and "
										+ parts[1]);
							}
							words[i] = parts[0];
							gold_tags[i] = parts[1];
						} else {
							words[i] = items[i];
							gold_tags[i] = "unknown";
						} // esle fi
					} else {
						words[i] = items[i];
					} // else fi eval
				} // esle fi withCondprobs

				if (this.transform) {
					if (words[i].equals(",")) {
						words[i] = "_KOM_";
					}
					if (words[i].equals(".")) {
						words[i] = "_ESENTS_";
					}
					if (words[i].equals("!")) {
						words[i] = "_ESENTE_";
					}
					if (words[i].equals("?")) {
						words[i] = "_ESENTF_";
					}
					if (words[i].equals("\"")) {
						words[i] = "_QUOT_";
					}
					if (words[i].equals("(")) {
						words[i] = "_OPENBRAC_";
					}
					if (words[i].equals(")")) {
						words[i] = "_CLOSEBRAC_";
					}
					if (words[i].equals(":")) {
						words[i] = "_COL_";
					}
					if (words[i].equals(";")) {
						words[i] = "_SEMICOL_";
					}
					if (this.d) {
						System.out.println("Transformed:" + words[i]);
					}
				} // fi transform

			} // rof

			// initialise tagmaxprob
			final List<DoubleAndIntValue> tagmaxprob = new ArrayList<>();

			for (int i = 0; i < this.taglist.getNrOfTags(); i++) {
				double inval = 0.0;

				if (i == this.taglist.getCodeForTag("-")) {
					inval = inval + 1.0; // for upranking of BPOS tag in
											// initialisation
				}
				tagmaxprob.add(new DoubleAndIntValue(inval, i));
			} // rof i

			// initialize maxprob and backpointer matrix

			// maxprob stores the maximal sequence probabilities for every word
			// to have some tag
			final double[][] maxprob = new double[nr_of_words][this.taglist
					.getNrOfTags()];

			// backpoint stores the tag of the last word that lead to this max
			// prob.
			final int[][] backpoint = new int[nr_of_words][this.taglist.getNrOfTags()];

			// initialize maxprob and backpoint for the two dummy words
			for (int i = 0; i < this.taglist.getNrOfTags(); i++) {
				if (i == this.taglist.getCodeForTag("-")) {
					maxprob[0][i] = 1.0;
					maxprob[1][i] = 1.0;
					backpoint[0][i] = this.taglist.getCodeForTag("-");
					backpoint[1][i] = this.taglist.getCodeForTag("-");
				} else {
					maxprob[0][i] = 0;
					maxprob[1][i] = 0;
					backpoint[0][i] = this.taglist.getCodeForTag("-");
					backpoint[1][i] = this.taglist.getCodeForTag("-");
				} // esle fi.
			} // rof i

			if (this.d) {
				System.out.println("Initialized matrices for " + nr_of_words
						+ " words and " + this.taglist.getNrOfTags() + " tags");
			}

			// loop over words and fill matrices
			for (int pos = 2; pos < nr_of_words; pos++) {

				// obtain probabilities per tag for current word
				final double[] allLexProbs = this.lexicon
						.getTagDistribution(words[pos]);

				// obtain conditional probabilities per tag for current condtag
				double[] allCondProbs = null;
				if (this.condprobs != null) {
					allCondProbs = this.condprobs
							.getTagDistribution(condtags[pos]);
				}

				Collections.sort(tagmaxprob);
				final List<Integer> acceptTags = new ArrayList<>(tagmaxprob.size());
				int count = 0;

				double absmaxprob = 0.0; // for multiplication in case of
											// underflow
				for (final DoubleAndIntValue current : tagmaxprob) {
					if (count < this.beamwidth) {
						count++;
						if (current.getProbability() > 0.0) {
							acceptTags.add(new Integer(current.getTagNr()));
							if (this.d) {
								System.out.println("accepted: tagnr "
										+ current.getTagNr() + " with p="
										+ current.getProbability());
							}
							if (absmaxprob < current.getProbability()) {
								absmaxprob = current.getProbability();
							}
						} // fi prob>0
						else {
							break; // e ist sortiert: gr��er als 0.0 wird's
									// nicht mehr
						}
					} // fi beamwidth
					else {
						break; // maximale Zahl an ber�cksichtigten items
								// erreicht
					}
				} // rof

				// underflow management: multiply with 1E100 if maxprob<1E-200.
				// This is a machine precision hack
				if (absmaxprob < 1E-200) {
					for (int i = 0; i < this.taglist.getNrOfTags(); i++) {
						maxprob[pos - 1][i] = maxprob[pos - 1][i] * 1E100;
					} // rof
				} // fi underflow

				tagmaxprob.clear();

				for (int tag = 0; tag < this.taglist.getNrOfTags(); tag++) {

					if (this.d) {
						System.out.println("Processing tag number " + tag
								+ " for word \"" + words[pos] + "\"");
					}

					/*
					 * for current tag, compute all probabilities p(tag|T1,T2) =
					 * sequence probability fot tag taking way T1 T2 =
					 * maxprob[pos-1][T2] * sequence probability ending on T2 *
					 * transition(tag|T1,T2) * probability of tag given last 2
					 * tags * lex(tag|word). probability of tag, given word
					 * 
					 * This is iterated over all possible T2s. The maximal prob
					 * and the respective T2 is saved in matrices
					 */

					double curr_maxprob = -1.0;
					int curr_backpoint = -1;
					final double lexprob = allLexProbs[tag];

					double condProb = 1;
					if (allCondProbs != null) {
						condProb = allCondProbs[tag];
					}

					for (final Integer prevtag : acceptTags) {

						final int prevprevtag = backpoint[pos - 1][prevtag]; // code
						// of T1
						final double prev_sequence_prob = maxprob[pos - 1][prevtag]; // maxprob
						// of
						// T2
						final double trans_prob = this.transitions.getTransProb(
								prevprevtag, prevtag, tag);
						final double curr_prob = prev_sequence_prob * trans_prob
								* lexprob * condProb;

						if ((this.d) && (curr_prob > 0.0)) {
							System.out.println("word: " + words[pos]
									+ "   Tag seq: " + prevprevtag + " "
									+ prevtag + " " + tag + "  prob: "
									+ curr_prob);
						}

						if (curr_prob > curr_maxprob) {
							curr_maxprob = curr_prob;
							curr_backpoint = prevtag;
						} // fi. curr>max
					} // rof prevtag

					// save in matrices
					maxprob[pos][tag] = curr_maxprob;
					backpoint[pos][tag] = curr_backpoint;
					if (this.d) {
						System.out.println("Tag and backpointer: " + tag + " "
								+ curr_backpoint);
					}

					tagmaxprob.add(new DoubleAndIntValue(curr_maxprob, tag));

				} // rof tag

			} // rof pos

			// all max paths have been determined. Chose and output tag sequence
			final String tagSequence[] = new String[nr_of_words];

			int maxtag = 0;
			double maxtag_prob = -1;
			for (int tag = 0; tag < this.taglist.getNrOfTags(); tag++) {
				if (maxprob[nr_of_words - 1][tag] > maxtag_prob) {
					maxtag = tag;
					maxtag_prob = maxprob[nr_of_words - 1][tag];
				} // fi. maxprob
			} // rof tag

			for (int pos = nr_of_words - 1; pos >= 0; pos--) {
				tagSequence[pos] = this.taglist.getTagForCode(maxtag);
				maxtag = backpoint[pos][maxtag];
			} // rof pos

			if (this.d) {
				System.out.println(" ----- Sentence result: ------- ");
			}

			if ((this.eval) && ((totalsen % 100) == 0)) {
				System.out.println("sentences tagged:" + totalsen);
			}

			// pos starts with 2 to omit begin-of-sentence words
			for (int pos = 2; pos < nr_of_words; pos++) {
				if (this.eval) {
					final boolean inLex = this.lexicon.containsWord(words[pos]);
					String guess = "";
					if (!inLex) {
						guess = "*";
					}
					if (this.e) {
						System.out.println(words[pos] + "\t" + gold_tags[pos]
								+ "\t" + tagSequence[pos] + guess);
					}
					if (gold_tags[pos].equals(tagSequence[pos])) {
						correct++;
					}
					if (!(gold_tags[pos].equals("unknown"))) {
						total++;
					}

					// send it to Evaluator
					this.evaluator.add(gold_tags[pos], tagSequence[pos], inLex);
				} // fi eval
				if (this.taggedOut) {
					String guess = "";
					final boolean inLex = this.lexicon.containsWord(words[pos]);
					if (!inLex) {
						guess = "*";
					}
					System.out.print(" " + words[pos] + "|" + tagSequence[pos]
							+ guess);
				}
			} // rof pos
			if (this.taggedOut) {
				System.out.println();
			}

		} // elihw loop over sentences

		if (this.eval) {
			final double percent = (double) correct / (double) total;
			evalResult = "Overall Accuracy: " + percent + " (" + correct + "/"
					+ total + ")\n";
			this.evaluator.printStatistics();
			evalResult += evaluator.getStatistics();
		} // fi eval
		finished = (int) (new File(textfile)).length();
		textFileReader.close();

	} // end void tagFile();

	public String getEvalResult() {
		return evalResult;
	}

	public void tagFile(final String textfile, final int spalteAusgangskategorie,
			final int spalteZielkategorie, final int spalteZwischenkategorie,
			final String eingabeformat, final String ausgabeformat,
			final boolean transitionsOverSentenceTagsAndGaps) throws IOException,
			FileNotFoundException {

		try (final BufferedReader textFileReader = new BufferedReader(new FileReader(textfile))) {
			tagResult = new StringBuffer();
			finished = 0;
			// Evaluator
			if (this.eval) {
				this.evaluator = new Evaluator();
			}

			finished = 0;
			String inputline;
			// StringBuffer tagResult = new StringBuffer();
			final StringBuilder taggedSentences = new StringBuilder();
			// loop over inputlines=sentences or words
			while ((inputline = textFileReader.readLine()) != null) {
				taggedSentences.append(this.tagSentence(inputline));
				taggedSentences.append(System.lineSeparator());
				// System.out.println(tagResult.toString());
				finished += inputline.length();
			}
		}
	} // end void tagFile();

	public StringBuffer getTagResult() {
		return tagResult;
	}

	protected boolean getEval() {
		return this.eval;
	}

	protected boolean getE() {
		return this.e;
	}

	protected boolean getTaggedOut() {
		return this.taggedOut;
	}

	protected TagList getTaglist() {
		return this.taglist;
	}

	public Lexicon getLexicon() {
		return this.lexicon;
	}

	protected CondProbs getCondProbs() {
		return this.condprobs;
	}

	protected Transitions getTransitions() {
		return this.transitions;
	}

	protected Evaluator getEvaluator() {
		return this.evaluator;
	}

	protected int getBeamwidth() {
		return this.beamwidth;
	}

	public void setUseInternalTok(final boolean useInternalTok) {
		this.useInternalTok = useInternalTok;
	}

	public void setReplaceNumbers(final boolean replaceNumbers) {
		this.replaceNumbers = replaceNumbers;
	}
} // end public class Tagger

// for comparing and sorting tag numbers with probabilities, necessary for beam
// search
class DoubleAndIntValue implements Comparable<DoubleAndIntValue> {
	private final double probability;

	private final int tagnumber;

	/**
	 * Creates new Object.
	 * 
	 */
	public DoubleAndIntValue(final double _prob, final int _tag) {
		this.probability = _prob;
		this.tagnumber = _tag;
	}

	@Override
	public int compareTo(final DoubleAndIntValue o) {
		if (o.getProbability() > this.probability) {
			return 1;
		} else if (o.getProbability() < this.probability) {
			return -1;
		} else {
			return 0;
		}
	}

	public int getTagNr() {
		return this.tagnumber;
	}

	public double getProbability() {
		return this.probability;
	}
}
