package de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.javafx.common;

import java.text.NumberFormat;

import javafx.util.StringConverter;

/**
 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
 */
public class LongStringConverter extends StringConverter<Long> {
	public static final LongStringConverter INSTANCE = new LongStringConverter();

	private final NumberFormat nf = NumberFormat.getNumberInstance();

	public LongStringConverter() {
		nf.setMaximumFractionDigits(0);
	}

	@Override
	public String toString(final Long value) {
		return value == null || value == Long.MIN_VALUE ? "" : nf.format(value);
	}

	@Override
	public Long fromString(final String value) {
		return Long.valueOf(value);
	}
}
