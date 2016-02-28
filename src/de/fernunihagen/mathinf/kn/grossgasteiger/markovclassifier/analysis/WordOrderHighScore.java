package de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.analysis;

import static java.util.Objects.requireNonNull;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Top-scored class with its score.
 *
 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
 */
public final class WordOrderHighScore implements Serializable {
	/** SERIAL-ID */
	private static final long serialVersionUID = 20150823L;

	private final String className;
	private final long score;
	private final double percentage;
	private final List<Entry<String, Long>> scoreList;

	/**
	 * Constructs highscore out of given score.
	 * @param score score to base highscore of.
	 * @return HighScore.
	 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
	 */
	public static WordOrderHighScore of(final WordOrderScores score) {
		requireNonNull(score);

		final Map<String, Long> scores = score.map();
		if (scores.isEmpty()) {
			throw new IllegalArgumentException("woScores is empty!");
		}

		final List<Entry<String, Long>> scoreList = new ArrayList<>(scores.entrySet());
		Collections.sort(scoreList, new Comparator<Entry<String, Long>>() {
			@Override
			public int compare(final Entry<String, Long> o1, final Entry<String, Long> o2) {
				return o1.getValue().compareTo(o2.getValue()) * -1;
			}
		});

		final String topClassName = scoreList.get(0).getKey();
		final long topScore = scoreList.get(0).getValue();
		long scoreSum = 0;

		final List<Entry<String, Long>> serializableScoreList = new ArrayList<>(scoreList.size());
		for (final Entry<String, Long> entry : scoreList) {
			scoreSum += entry.getValue();
			serializableScoreList.add(new AbstractMap.SimpleEntry<String, Long>(entry.getKey(), entry.getValue()));
		}
		//
		final double percentage = BigDecimal.valueOf(((double) topScore / (double) scoreSum) * 100).setScale(2, RoundingMode.HALF_UP).doubleValue();
		//
		return new WordOrderHighScore(topClassName, topScore, percentage, serializableScoreList);
	}

	private WordOrderHighScore(final String topClassName, final long topScore, final double percentage, final List<Entry<String, Long>> scoreList) {
		this.className = topClassName;
		this.score = topScore;
		this.percentage = percentage;
		this.scoreList = scoreList;
	}

	/**
	 * @return Name of class in highscore.
	 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
	 */
	public String className() {
		return className;
	}

	/**
	 * @return Actual score.
	 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
	 */
	public long score() {
		return score;
	}

	/**
	 * @return Percentage of this score to the sum of all scores.
	 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
	 */
	public double percentage() {
		return percentage;
	}

	/**
	 * @return Sorted List of Score-entries by name of class, beginning with highest.
	 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
	 */
	public List<Entry<String, Long>> scoreList() {
		return Collections.unmodifiableList(scoreList);
	}
}
