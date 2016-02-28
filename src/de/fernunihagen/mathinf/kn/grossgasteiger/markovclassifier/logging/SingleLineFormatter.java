package de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.logging;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

final class SingleLineFormatter extends Formatter {
	private static final DateTimeFormatter LOG_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd, HH:mm:ss");

	@Override
	public String format(final LogRecord record) {
		//		final LocalDateTime logTime = LocalDateTime.ofEpochSecond(record.getMillis(), 0, ZoneOffset.UTC);
		final String timeString = LOG_DATE_FORMAT.format(LocalDateTime.now());
		//
		final String message;
		final Object[] parameters = record.getParameters();
		//
		if (parameters == null || parameters.length == 0) {
			message = record.getMessage();
		} else {
			message = formatMessage(record);
		}
		//
		final String logString = "[" + timeString + "] " + record.getLevel() + ": " + message + System.lineSeparator();
		//
		if (record.getThrown() != null) {
			final StringWriter sw = new StringWriter();
			record.getThrown().printStackTrace(new PrintWriter(sw));
			return logString + sw.toString() + System.lineSeparator();
		}
		//
		return logString;
	}
}
