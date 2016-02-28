package de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.javafx.training;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.training.CooccurrencesReader;
import de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.training.TrainingSession;
import de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.training.WordStatistic;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableView;

/**
 * Static-methods to modify the state of a given treetable that holds infos about imported classes and their significant coocs.
 *
 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
 */
public final class CoocsTreeTable {
	/** Logger-Instance */
	private static final Logger LOGGER = Logger.getLogger(CoocsTreeTable.class.getName());

	/**
	 * Updates the UI-Element to show a newly imported list of files for a given class. Must run in JavaFX-UI-Thread!
	 * 
	 * @param tableInstance The treetable to update.
	 * @param className Name of the class to update in tree-table.
	 * @param coocBeans precalculated beans of the coocs. Enables calculation outside of javaFx-Thread. Use {@link #convertCoocsToBeans(CooccurrencesReader)}.
	 * @param trainingSession Reference to the trainingSession.
	 * @param labels Localized resource-bundle to use for messages
	 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
	 */
	public static void updateImportedClasses(final TreeTableView<SignificantCooccurrence> tableInstance, final String className, final List<SignificantCooccurrence> coocBeans, final TrainingSession trainingSession, final ResourceBundle labels) {
		requireNonNull(tableInstance);
		requireNonNull(className);
		requireNonNull(coocBeans);
		requireNonNull(trainingSession);
		requireNonNull(labels);
		//
		final CooccurrencesReader significantCoocs = trainingSession.getCooccurrencesReader(className);
		final List<SignificantCooccurrence> cooccurencesToAdd = convertCoocsToBeans(significantCoocs);
		//
		TreeItem<SignificantCooccurrence> root = tableInstance.getRoot();
		if (root == null) {
			root = new TreeItem<SignificantCooccurrence>(SignificantCooccurrence.createRoot(labels.getString("training.classes.root.name")));
			tableInstance.setRoot(root);
		}
		//
		final ObservableList<TreeItem<SignificantCooccurrence>> children = root.getChildren();
		for (final TreeItem<SignificantCooccurrence> treeItem : children) {
			final boolean isClass = treeItem.getValue().word1Property().get().equalsIgnoreCase(className);
			if (isClass) {
				final ObservableList<TreeItem<SignificantCooccurrence>> classChildren = treeItem.getChildren();
				//
				for (final SignificantCooccurrence coocToAdd : cooccurencesToAdd) {
					final TreeItem<SignificantCooccurrence> newTreeItem = new TreeItem<SignificantCooccurrence>(coocToAdd);
					classChildren.add(newTreeItem);
				}
				//
				return;
			}
		}
		//
		final SignificantCooccurrence classBean = SignificantCooccurrence.createClass(className, (long) significantCoocs.uniqueCoocsByWord().size(), significantCoocs.meanSignificance(), significantCoocs.significanceSum(), trainingSession.getWordCount(className),
				trainingSession.getSentenceCount(className));
		final TreeItem<SignificantCooccurrence> newClassItem = new TreeItem<>(classBean);
		for (final SignificantCooccurrence coocToAdd : cooccurencesToAdd) {
			final TreeItem<SignificantCooccurrence> newTreeItem = new TreeItem<>(coocToAdd);
			newClassItem.getChildren().add(newTreeItem);
		}
		//
		newClassItem.setExpanded(false);
		root.getChildren().add(newClassItem);
	}

	/**
	 * Updates the UI-Element to show word-statisistics with the data of the training-session for a class. Must run in JavaFX-UI-Thread!
	 * 
	 * @param tableInstance The treetable to update.
	 * @param className Name of the class to update in tree-table.
	 * @param trainingSession Reference to the trainingSession.
	 * @param labels Localized resource-bundle to use for messages
	 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
	 */
	public static void updateWordStatistics(final TreeTableView<WordStatistics> tableInstance, final String className, final TrainingSession trainingSession, final ResourceBundle labels) {
		requireNonNull(tableInstance);
		requireNonNull(className);
		requireNonNull(trainingSession);
		requireNonNull(labels);
		//
		final TreeItem<WordStatistics> currentRoot = tableInstance.getRoot();
		if (currentRoot == null) {
			final TreeItem<WordStatistics> newRoot = new TreeItem<WordStatistics>(WordStatistics.asWordRow("root"));
			tableInstance.setRoot(newRoot);
		}
		//
		final TreeItem<WordStatistics> root = tableInstance.getRoot();
		//
		final CooccurrencesReader cooccurrencesReader = trainingSession.getCooccurrencesReader(className);
		final Map<String, WordStatistic> statisticsByWord = trainingSession.getStatisticsByWord(className);
		//
		// -- Get or create word item in table
		final Set<Entry<String, WordStatistic>> statisticsEntrySet = statisticsByWord.entrySet();
		final List<TreeItem<WordStatistics>> wordItemsToAdd = new ArrayList<>(statisticsEntrySet.size());
		final Map<String, TreeItem<WordStatistics>> treeItemsByWord = new HashMap<>(); // Buffers once found or created treeitems for a word in the table. Prevents repeated searches
		//
		for (final Entry<String, WordStatistic> entry : statisticsEntrySet) {
			final String word = entry.getKey();
			final WordStatistic statistics = entry.getValue();
			final Map<WordStatistic, Double> coocsOfWord = cooccurrencesReader.coocsByWord().getOrDefault(entry.getValue(), Collections.emptyMap());
			//
			// -- check other words for coocs or relevant count
			final Map<String, TreeItem<WordStatistics>> otherWordItemsBuffer = new HashMap<>(1);
			final Map<String, Long> nextNeighbours = statistics.getNextNeighbours();
			final Set<Entry<String, Long>> nextNeighboursentrySet = nextNeighbours.entrySet();
			for (final Entry<String, Long> nextNeighbour : nextNeighboursentrySet) {
				final String otherWord = nextNeighbour.getKey();
				final Long value = nextNeighbour.getValue();
				if (value == null || value < 2) {
					continue;
				}
				//
				final Optional<Entry<WordStatistic, Double>> coocEntry = coocsOfWord.entrySet().stream().filter(e -> e.getKey().getName().equals(otherWord)).findFirst();
				final Double coocSig = coocEntry.isPresent() ? coocEntry.get().getValue() : null;
				//
				if (coocSig != null) {
					final TreeItem<WordStatistics> otherWordItem = new TreeItem<WordStatistics>(WordStatistics.asOtherWordRow(otherWord, nextNeighbour.getValue(), coocSig));
					otherWordItemsBuffer.put(otherWord, otherWordItem);
				}
			}
			//
			if (otherWordItemsBuffer.isEmpty()) {
				continue;
			}
			//
			TreeItem<WordStatistics> wordItem = treeItemsByWord.get(word);
			if (wordItem == null) {
				for (int i = 0; i < root.getChildren().size(); i++) {
					final TreeItem<WordStatistics> child = root.getChildren().get(i);
					if (word.equals(child.getValue().wordProperty().get())) {
						wordItem = child;
						break;
					}
				}
				//
				if (wordItem == null) {
					wordItem = new TreeItem<WordStatistics>(WordStatistics.asWordRow(word));
					wordItemsToAdd.add(wordItem);
				}
				//
				treeItemsByWord.put(word, wordItem);
			}
			//
			// -- Get or create class item in table
			TreeItem<WordStatistics> classItem = null;
			for (final TreeItem<WordStatistics> child : wordItem.getChildren()) {
				if (className.equals(child.getValue().classNameProperty().get())) {
					classItem = child;
					break;
				}
			}
			//
			if (classItem == null) {
				classItem = new TreeItem<WordStatistics>(WordStatistics.asClassRow(word));
			}
			//
			classItem.getChildren().clear(); // We start new for this class

			classItem.getChildren().addAll(otherWordItemsBuffer.values());
			wordItem.getChildren().add(classItem);
			//
			final WordStatistics value = wordItem.getValue();
			value.classesCountProperty().set(wordItem.getChildren().size());
			if (value.otherWordsCountProperty().get() < 0) {
				value.otherWordsCountProperty().set(0);
			}
			value.otherWordsCountProperty().set(classItem.getChildren().size() + value.otherWordsCountProperty().get());
		}
		//
		root.getChildren().addAll(wordItemsToAdd);
	}

	/**
	 * Empties the table with coocs by class
	 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
	 */
	public static void clear(final TreeTableView<SignificantCooccurrence> tableToClear) {
		// Reset view-state of training-elements
		final TreeItem<SignificantCooccurrence> root = tableToClear.getRoot();
		if (root != null) {
			tableToClear.getRoot().getChildren().clear();
			tableToClear.setRoot(null);
		}
	}

	/**
	 * Fills the given table with the training-data from the given session.
	 * @param coocsByClassTreeTable Table to fill.
	 * @param session Session to take training-data from.
	 * @param labels Localized resource-bundle to use for messages
	 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
	 * @param coocBeansByClass
	 */
	public static void loadFromSession(final TreeTableView<SignificantCooccurrence> coocsByClassTreeTable, final TreeTableView<WordStatistics> wordStatisticsTreeTable, final Map<String, List<SignificantCooccurrence>> coocBeansByClass, final TrainingSession session, final ResourceBundle labels) {
		requireNonNull(coocsByClassTreeTable);
		requireNonNull(session);
		//
		final Set<String> classNames = session.getClasses();
		if (classNames.isEmpty()) {
			return;
		}
		//
		final List<String> classNamesList = new ArrayList<String>(classNames);
		Collections.sort(classNamesList);
		//
		for (final String className : classNamesList) {
			final List<SignificantCooccurrence> coocBeans = coocBeansByClass.get(className);
			//
			final long coocTableStart = System.currentTimeMillis();
			updateImportedClasses(coocsByClassTreeTable, className, coocBeans, session, labels);
			final long coocTableTime = System.currentTimeMillis() - coocTableStart;
			//
			final long wordTableStart = System.currentTimeMillis();
			updateWordStatistics(wordStatisticsTreeTable, className, session, labels);
			final long wordTableTime = System.currentTimeMillis() - wordTableStart;
			//
			LOGGER.log(Level.FINE, "Update-Times for class {0}: coocTable={1}, wordTable={2}", new Object[] { className, coocTableTime, wordTableTime });
		}
	}

	/**
	 * Does the heavy-loaded conversion of coocs to beans for table-display.
	 * @param coocs Coocs to convert to table-beans.
	 * @return Sorted list of table-beans.
	 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
	 */
	public static List<SignificantCooccurrence> convertCoocsToBeans(final CooccurrencesReader coocs) {

		final Map<WordStatistic, Map<WordStatistic, Double>> cooccurencesByWord = coocs.coocsByWord();
		//
		if (cooccurencesByWord.isEmpty()) {
			return Collections.emptyList();
		}
		//
		final List<SignificantCooccurrence> cooccurencesToAdd = new ArrayList<>(cooccurencesByWord.size());
		for (final Entry<WordStatistic, Map<WordStatistic, Double>> coocsOfWord : cooccurencesByWord.entrySet()) {
			final String word1 = coocsOfWord.getKey().getName();
			coocsOfWord.getValue().entrySet().forEach(
					v -> cooccurencesToAdd.add(SignificantCooccurrence.createCooc(word1, v.getKey().getName(), v.getValue())));
		}
		//
		Collections.sort(cooccurencesToAdd);
		return cooccurencesToAdd;
	}
}
