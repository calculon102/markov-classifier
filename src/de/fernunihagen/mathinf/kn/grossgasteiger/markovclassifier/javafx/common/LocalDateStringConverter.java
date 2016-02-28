package de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.javafx.common;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import javafx.util.StringConverter;

/**
 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
 */
public class LocalDateStringConverter extends StringConverter<LocalDate> {

	private final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd.MM.yyyy");

	public LocalDateStringConverter() {
		// NOP
	}

	@Override
	public String toString(final LocalDate value) {
		return value == null ? "" : value.format(dateFormat);
	}

	@Override
	public LocalDate fromString(final String value) {
		return LocalDate.parse(value, dateFormat);
	}
}
