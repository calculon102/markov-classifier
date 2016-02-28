package de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.javafx.common;

import java.text.NumberFormat;

import javafx.util.StringConverter;

/**
 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
 */
public class DoubleSig2StringConverter extends StringConverter<Double> {
	public static final DoubleSig2StringConverter INSTANCE = new DoubleSig2StringConverter();

	private final NumberFormat nf = NumberFormat.getNumberInstance();

	public DoubleSig2StringConverter() {
		nf.setMaximumFractionDigits(2);
	}

	@Override
	public String toString(final Double value) {
		return value == null || value.equals(Double.NaN) ? "" : nf.format(value);
	}

	@Override
	public Double fromString(final String value) {
		return Double.valueOf(value);
	}
}
