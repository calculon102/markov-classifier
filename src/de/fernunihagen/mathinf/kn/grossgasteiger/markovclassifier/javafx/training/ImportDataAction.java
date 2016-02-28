package de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.javafx.training;

import static java.util.Objects.requireNonNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.javafx.tasks.TasksController;
import de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.training.TrainingConfig;
import de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.training.TrainingSession;
import javafx.scene.control.TreeTableView;

/**
 * Creates worker threads for importing training-data.
 *
 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
 */
public class ImportDataAction {
	/** Logger-Instance */
	private static final Logger LOGGER = Logger.getLogger(ImportDataAction.class.getName());

	/**
	 * Parses the given directory for further dirctories to use as classes and creates an import-thread for each.
	 * @param config Base-dir to look for further directories.
	 * @param trainingSession Current Training-Session to enhance.
	 * @param coocsTable Table to insert the resulting significant cooccurrences.
	 * @param wordsTable Table to insert the result of word-order-analysis.
	 * @param tasksController Controller to give created tasks to.
	 * @param labels Localized labels.
	 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
	 */
	public static void start(final TrainingConfig config, final TrainingSession trainingSession, final TreeTableView<SignificantCooccurrence> coocsTable, final TreeTableView<WordStatistics> wordsTable, final TasksController tasksController, final ResourceBundle labels) {
		requireNonNull(config);
		requireNonNull(trainingSession);
		//
		try {
			final List<Path> subDirList = Files
					.walk(config.getImportDir().toPath(), 1, FileVisitOption.FOLLOW_LINKS)
					.filter(p -> p.toFile().isDirectory())
					.collect(Collectors.toList());
			//
			if (subDirList.isEmpty()) {
				LOGGER.log(Level.INFO, "No subdirectories in selected directory {0} to parse as classes.", config.getImportDir().getAbsolutePath());
				return;
			}
			//
			for (final Path classDir : subDirList) {
				if (classDir.toFile().equals(config.getImportDir())) {
					continue;
				}
				final TrainingConfig taskConfig = changeConfig(classDir.toFile(), config);
				final ImportDataTask importDataTask = new ImportDataTask(taskConfig, trainingSession, coocsTable, wordsTable, labels);
				tasksController.run(importDataTask);
			}

		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Creates a new config-instance based on the given, only changing the directory.
	 * @param newImportDir The new directory to use as import dir.
	 * @param config The config to base the new on on.
	 * @return New TrainingConfig.
	 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
	 */
	private static TrainingConfig changeConfig(final File newImportDir, final TrainingConfig config) {
		return new TrainingConfig() {
			@Override
			public double getCoocSignificanceThreshold() {
				return config.getCoocSignificanceThreshold();
			}

			@Override
			public int getMaxFiles() {
				return config.getMaxFiles();
			}

			@Override
			public File getImportDir() {
				return newImportDir;
			}
		};
	}

}
