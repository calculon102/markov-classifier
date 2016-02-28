package de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.javafx.common;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import javafx.util.StringConverter;

/**
 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
 */
public class LocalTimeStringConverter extends StringConverter<LocalTime> {

	private final DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm:ss");

	public LocalTimeStringConverter() {
		// NOP
	}

	@Override
	public String toString(final LocalTime value) {
		return value == null ? "" : value.format(timeFormat);
	}

	@Override
	public LocalTime fromString(final String value) {
		return LocalTime.parse(value, timeFormat);
	}
}
