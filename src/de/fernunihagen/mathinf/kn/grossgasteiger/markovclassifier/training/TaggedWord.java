package de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.training;

import static java.util.Objects.requireNonNull;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a single word with syntactic informations.
 *
 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
 */
public final class TaggedWord implements Serializable {
	/** SERIAL-ID */
	private static final long serialVersionUID = 20150814L;
	/** Buffer of created instances. */
	private static final Map<String, TaggedWord> BUFFER = new HashMap<>();

	/** The word itself. */
	private final String name;
	/** Viterbi-tag. */
	private final String tag;

	private TaggedWord(final String name, final String tag) {
		this.name = name;
		this.tag = tag;
	}

	/**
	 * @return The word itself.
	 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return The Viterbi-Tag.
	 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
	 */
	public String getTag() {
		return tag;
	}

	/**
	 * @return <code>true</code> if the tag of this word indicates a noun "NN"
	 *         or "NN*".
	 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
	 */
	public boolean isNoun() {
		return tag.startsWith("NN");
	}

	/**
	 * Creates a new tagged word by given informations.
	 * 
	 * @param name
	 *            The word itself.
	 * @param tag
	 *            The viterbi-tag.
	 * @return An unique instance for the combination of name and tag.
	 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
	 */
	public static TaggedWord ofNameAndTag(final String name, final String tag) {
		requireNonNull(name);
		requireNonNull(tag);
		//
		return BUFFER.computeIfAbsent(name + "|" + tag, k -> new TaggedWord(name.toLowerCase(), tag));
	}

	/**
	 * Creates a word by a pipe-separated String "[name]|[tag]".
	 * 
	 * @param taggedString
	 * @return An unique instance for the combination of name and tag.
	 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
	 */
	public static TaggedWord ofTaggedString(final String taggedString) {
		final String[] split = taggedString.trim().split("\\|");
		if (split.length < 2) {
			return new TaggedWord(split[0], "?");
		}
		//
		return BUFFER.computeIfAbsent(taggedString, k -> new TaggedWord(split[0].toLowerCase(), split[1]));
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((tag == null) ? 0 : tag.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final TaggedWord other = (TaggedWord) obj;
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		if (tag == null) {
			if (other.tag != null) {
				return false;
			}
		} else if (!tag.equals(other.tag)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return name + " [" + tag + "]";
	}
}
