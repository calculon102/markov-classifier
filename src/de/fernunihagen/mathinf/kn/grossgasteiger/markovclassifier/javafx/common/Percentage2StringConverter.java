package de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.javafx.common;

import java.math.BigDecimal;
import java.math.RoundingMode;

import javafx.util.StringConverter;

public class Percentage2StringConverter extends StringConverter<BigDecimal> {
	public static final Percentage2StringConverter INSTANCE = new Percentage2StringConverter();

	private static final BigDecimal HUNDRED = new BigDecimal(100);

	public Percentage2StringConverter() {
		// NOP
	}

	@Override
	public String toString(final BigDecimal value) {
		return value != null ? value.multiply(HUNDRED).setScale(2, RoundingMode.HALF_UP).toString() : "";
	}

	@Override
	public BigDecimal fromString(final String string) {
		return null; // Nothing, since not editable.
	}
}
