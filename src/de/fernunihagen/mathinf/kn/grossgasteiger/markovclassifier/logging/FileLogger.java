package de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.logging;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Static class to initialize Java-Logging-mechanisms.
 *
 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
 */
public final class FileLogger {
	/** Simple unmodified log-format. */
	private static final SingleLineFormatter SINLGE_LINE_TXT = new SingleLineFormatter();
	/** File for current logging. */
	private static FileHandler fileTxt;

	static public synchronized void setup(final Level logLevel) {
		// Set global Log-Level
		Logger logger = Logger.getLogger("");
		logger.setLevel(logLevel);

		// Remove all existing handlers.
		final Handler[] handlers = logger.getHandlers();
		for (Handler handler : handlers) {
			logger.removeHandler(handler);
		}

		// Create console-logger
		final ConsoleHandler consoleHandler = new ConsoleHandler();
		consoleHandler.setFormatter(SINLGE_LINE_TXT);
		logger.addHandler(consoleHandler);

		// Create file-logger
		try {
			final DateTimeFormatter datePattern = DateTimeFormatter.ofPattern("yyyyMMdd");
			final LocalDate now = LocalDate.now();
			final String dateString = now.format(datePattern);
			fileTxt = new FileHandler("MarkovTextClassifier_Log_" + dateString + ".txt", true);
		} catch (SecurityException | IOException e) {
			e.printStackTrace();
			return;
		}

		fileTxt.setFormatter(SINLGE_LINE_TXT);
		logger.addHandler(fileTxt);
	}
}
