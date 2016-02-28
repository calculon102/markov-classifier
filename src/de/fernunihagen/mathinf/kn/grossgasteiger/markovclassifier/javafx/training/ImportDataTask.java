package de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.javafx.training;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.common.ProgressMonitor;
import de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.javafx.tasks.AbstractTask;
import de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.textprocessing.BaseformConverter;
import de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.textprocessing.StopWordRemover;
import de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.textprocessing.ViterbiTagger;
import de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.training.CooccurrencesReader;
import de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.training.TrainingConfig;
import de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.training.TrainingSession;
import javafx.application.Platform;
import javafx.scene.control.TreeTableView;

/**
 * Controller-Task to import files from a directory structure and update the UI afertwards.
 *
 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
 */
final class ImportDataTask extends AbstractTask<Void> {
	private static final Logger LOGGER = Logger.getLogger(ImportDataTask.class.getName());

	/** Reference to the TreeTableView to update. */
	private final TreeTableView<SignificantCooccurrence> importedClassesTreeTable;
	/** Reference to the analyzing session of Markov-Models. */
	private final TrainingSession trainingSession;
	/** BaseDir to import from. */
	private final File importDir;
	/** Localized resource-bundle. */
	private final ResourceBundle labels;
	/** TreeTable with word-statistics by class. */
	private final TreeTableView<WordStatistics> wordsTreeTable;
	/** Config for importing training files. */
	private final TrainingConfig config;

	/**
	 * @param importdir Directory to import files from.
	 * @param session Reference to the session-instance of markov-analyzation
	 * @param resultsTable Reference of the TreeTable to update with imported text-classes.
	 * @param labels Localized resource-bundle.
	 */
	public ImportDataTask(final TrainingConfig config, final TrainingSession session, final TreeTableView<SignificantCooccurrence> resultsTable, final TreeTableView<WordStatistics> wordsTreeTable, final ResourceBundle labels) {
		super("Train class " + config.getImportDir().getName());

		this.config = config;
		this.importedClassesTreeTable = resultsTable;
		this.wordsTreeTable = wordsTreeTable;
		this.trainingSession = session;
		this.importDir = config.getImportDir();
		this.labels = labels;
	}

	@Override
	protected Void call() throws Exception {
		LOGGER.log(Level.INFO, "Starting import of training-data from dir {0}", importDir.getAbsolutePath());
		final long timeStart = System.currentTimeMillis();
		//
		try {
			final List<Path> textFilesList = searchFilesSubTask();
			if (textFilesList.isEmpty()) {
				updateMessage(labels.getString("training.import.progress.noFiles"));
				LOGGER.log(Level.INFO, "Directory {0} contains no text-files to import", importDir.getAbsolutePath());
				return null;
			}
			//
			final String className = importFilesSubTask(textFilesList);
			//
			final CooccurrencesReader coocs = trainingSession.searchSignificantCoocs(className, config.getCoocSignificanceThreshold(), this);
			update("Creating cooc-beans..."); // TRANSLATE
			final List<SignificantCooccurrence> coocBeans = CoocsTreeTable.convertCoocsToBeans(coocs);
			//
			Platform.runLater(() -> {
				CoocsTreeTable.updateImportedClasses(importedClassesTreeTable, className, coocBeans, trainingSession, labels);
				CoocsTreeTable.updateWordStatistics(wordsTreeTable, className, trainingSession, labels);
			});
			//
		} catch (final Exception ex) {
			LOGGER.log(Level.SEVERE, "Exception while importing from dir {0}", importDir.getAbsolutePath());
			LOGGER.log(Level.SEVERE, "Exception:", ex);
			ex.printStackTrace();
			//
			updateProgress(1, 1);
			updateMessage(labels.getString("training.import.progress.error") + " " + ex.getMessage());
			//
			throw ex;
		} finally {
			final long timeUsed = System.currentTimeMillis() - timeStart;
			LOGGER.log(Level.INFO, "Import of training-data from {0} done. Took {1} seconds", new Object[] { importDir.getAbsolutePath(), (double) (timeUsed / 1000) });
		}
		//
		updateMessage(labels.getString("training.import.progress.done"));
		return null;
	}

	/**
	 * @return All files within the current set {@link #importDir} with endings of txt, xml, html, xthml, rtf or pdf.
	 * @throws IOException Error on file-system-level.
	 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
	 */
	private List<Path> searchFilesSubTask() throws IOException {
		updateMessage(labels.getString("training.import.progress.scanning"));
		updateProgress(ProgressMonitor.INDETERMINATE_PROGRESS, 1);
		//
		// 1. Search relevant files in dir
		//
		if (isCancelled()) {
			return null;
		}
		//
		return Files
				.walk(importDir.toPath(), 1, FileVisitOption.FOLLOW_LINKS)
				.filter((final Path p) -> p.toFile().getName().endsWith(".txt")
						|| p.toFile().getName().endsWith(".xml")
						|| p.toFile().getName().endsWith(".html")
						|| p.toFile().getName().endsWith(".xhtml")
						|| p.toFile().getName().endsWith(".rtf")
						|| p.toFile().getName().endsWith(".pdf"))
				.limit(config.getMaxFiles())
				.collect(Collectors.toList());
	}

	/**
	 * Imports / Trains with the given list of files and adds them to the class specified by the name of the current {@link #importDir}.
	 * @param textFilesList Text-Files to import.
	 * @return Name of the class the files are imported to.
	 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
	 */
	private String importFilesSubTask(final List<Path> textFilesList) {
		final String className = importDir.getName();
		final int maxProgress = textFilesList.size() * 2; // File-Parsing is only the first half.
		int finishedFileCount = 0;
		//
		// TOFIX Configure if language is taken by classname/dir
		final Locale localeForTagger;
		if (className.equalsIgnoreCase("en")) {
			localeForTagger = Locale.ENGLISH;
		} else if (className.equalsIgnoreCase("en")) {
			localeForTagger = Locale.GERMAN;
		} else {
			localeForTagger = trainingSession.getCurrentLanguage();
		}
		//
		final ViterbiTagger viterbiTagger = ViterbiTagger.forLocale(localeForTagger);
		final StopWordRemover stopwordRemover = new StopWordRemover();
		final BaseformConverter baseformConverter = new BaseformConverter();
		//
		final String parseFileMessageTemplate = labels.getString("training.import.progress.parsingFile");
		for (final Path textFile : textFilesList) {
			if (isCancelled()) {
				return null;
			}
			//
			final String message = MessageFormat.format(parseFileMessageTemplate, finishedFileCount, textFilesList.size(), textFile.toFile().getName());
			updateMessage(message);
			//
			trainingSession.importFileToClass(className, textFile, viterbiTagger, stopwordRemover, baseformConverter);
			finishedFileCount += 1;
			updateProgress(finishedFileCount, maxProgress);
		}
		//
		return className;
	}

	@Override
	public ResourceBundle labels() {
		return labels;
	}
}
