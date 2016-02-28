package de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.javafx.analysis;

import static java.util.Objects.requireNonNull;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.analysis.AnalysisResult;
import de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.analysis.ViterbiValue;
import de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.analysis.WordPair;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * Tab to display a single analysis-result. Imperative JavaFX to have a usage-comparison to FXML.
 *
 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
 */
public class AnalysisDetailsTab extends Tab {
	/** Logger-Instance */
	private static final Logger LOGGER = Logger.getLogger(AnalysisDetailsTab.class.getName());

	/** Function to convert a given word-order-score to a bean to be used in a table. */
	private static final Function<Entry<String, Long>, WordOrderElementBean> CONVERT_SCORE_TO_BEAN = new Function<Entry<String, Long>, WordOrderElementBean>() {
		@Override
		public WordOrderElementBean apply(final Entry<String, Long> t) {
			return new WordOrderElementBean(t.getKey(), t.getValue());
		}
	};

	public AnalysisDetailsTab(final AnalysisResultBean resultBean, final ResourceBundle labels) {
		requireNonNull(resultBean);
		requireNonNull(labels);
		//
		LOGGER.log(Level.FINE, "Creating detail-tab for {0}.", resultBean.toString());
		//
		this.setText("Result " + resultBean.filenameProperty().get()); // TRANSLATE
		//
		final VBox contentBox = new VBox(9);
		contentBox.setPadding(new Insets(24));
		//
		final Label headerLabel = new Label(labels.getString("analysis.details.head") + " " + resultBean.filenameProperty().get());
		headerLabel.getStyleClass().add("subheader");
		//
		final Label fileLabel = new Label();
		fileLabel.setWrapText(true);
		fileLabel.setText(labels.getString("analysis.details.path") + System.lineSeparator() + resultBean.getResult().file());

		final Label markovClassLabel = new Label();
		markovClassLabel.setWrapText(true);
		markovClassLabel.setText(labels.getString("analysis.details.markovClass")
				+ " " + System.lineSeparator()
				+ resultBean.classNameProperty().get()
				+ " (" + resultBean.scoreProperty().get().multiply(new BigDecimal(100)).setScale(2, RoundingMode.HALF_UP) + "%)");

		final Label wordorderClassLabel = new Label();
		wordorderClassLabel.setWrapText(true);
		wordorderClassLabel.setText(labels.getString("analysis.details.wordorderClass")
				+ " " + System.lineSeparator()
				+ resultBean.getResult().wordOrderHighscore().className()
				+ " (" + (new BigDecimal(resultBean.getResult().wordOrderHighscore().percentage())).setScale(2, RoundingMode.HALF_UP) + "%)");

		final Label viterbiPathLabel = new Label(labels.getString("analysis.details.viterbiPath"));
		viterbiPathLabel.getStyleClass().add("subheader");

		final TableView<ViterbiPathElementBean> viterbiPathTable = createViterbiPathTable(resultBean, labels);
		final Label emissionCountLabel = new Label(labels.getString("analysis.details.emissions.count") + " " + resultBean.emissionCountProperty().get());
		final VBox viterbiDetails = new VBox(9);
		viterbiDetails.setPrefWidth(600);
		viterbiDetails.getChildren().add(viterbiPathLabel);
		viterbiDetails.getChildren().add(viterbiPathTable);
		viterbiDetails.getChildren().add(emissionCountLabel);

		final Label wordOrderLabel = new Label(labels.getString("analysis.details.wordorder"));
		wordOrderLabel.getStyleClass().add("subheader");

		final TableView<WordOrderElementBean> wordOrderTable = createWordOrderTable(resultBean, labels);

		final VBox wordOrderDetails = new VBox(9);
		wordOrderDetails.setPrefWidth(600);
		wordOrderDetails.getChildren().add(wordOrderLabel);
		wordOrderDetails.getChildren().add(wordOrderTable);

		final HBox details = new HBox(9);
		details.getChildren().add(viterbiDetails);
		details.getChildren().add(wordOrderDetails);

		contentBox.getChildren().add(headerLabel);
		contentBox.getChildren().add(fileLabel);
		contentBox.getChildren().add(markovClassLabel);
		contentBox.getChildren().add(wordorderClassLabel);
		contentBox.getChildren().add(details);

		this.setContent(contentBox);
	}

	private static TableView<ViterbiPathElementBean> createViterbiPathTable(final AnalysisResultBean resultBean, final ResourceBundle labels) {
		final AnalysisResult result = resultBean.getResult();

		final List<WordPair> emissionList = result.emissions();
		final List<ViterbiValue> mostProbablePath = result.probablePath();
		final List<ViterbiPathElementBean> rowElements = new ArrayList<>(mostProbablePath.size());
		//
		for (int i = 0; i < emissionList.size(); i++) {
			final WordPair analysisCooccurrence = emissionList.get(i);
			final ViterbiValue viterbiPathElement = mostProbablePath.get(i);
			//
			final ViterbiPathElementBean viterbiPathElementBean = new ViterbiPathElementBean(analysisCooccurrence.toString(), viterbiPathElement.getClassName(), viterbiPathElement.getViterbiValue());
			rowElements.add(viterbiPathElementBean);
		}

		final TableColumn<ViterbiPathElementBean, Integer> indexCol = new TableColumn<>("#");
		indexCol.setCellValueFactory(row -> new ReadOnlyObjectWrapper<Integer>(rowElements.indexOf(row.getValue()) + 1));
		final TableColumn<ViterbiPathElementBean, String> emissionCol = new TableColumn<>(labels.getString("analysis.details.viterbiPath.col.emission"));
		emissionCol.setCellValueFactory(new PropertyValueFactory<>("emission"));
		final TableColumn<ViterbiPathElementBean, String> viterbiStateCol = new TableColumn<>(labels.getString("analysis.details.viterbiPath.col.state"));
		viterbiStateCol.setCellValueFactory(new PropertyValueFactory<>("viterbiState"));
		final TableColumn<ViterbiPathElementBean, BigDecimal> viterbiValueCol = new TableColumn<>(labels.getString("analysis.details.viterbiPath.col.value"));
		viterbiValueCol.setCellValueFactory(new PropertyValueFactory<>("viterbiValue"));

		final TableView<ViterbiPathElementBean> viterbiPathTable = new TableView<>();
		viterbiPathTable.getColumns().add(indexCol);
		viterbiPathTable.getColumns().add(emissionCol);
		viterbiPathTable.getColumns().add(viterbiStateCol);
		viterbiPathTable.getColumns().add(viterbiValueCol);
		viterbiPathTable.getItems().addAll(rowElements);

		return viterbiPathTable;
	}

	private static TableView<WordOrderElementBean> createWordOrderTable(final AnalysisResultBean resultBean, final ResourceBundle labels) {

		final List<Entry<String, Long>> scoreList = resultBean.getResult().wordOrderHighscore().scoreList();
		final List<WordOrderElementBean> rowElements = scoreList.stream().map(CONVERT_SCORE_TO_BEAN).collect(Collectors.toList());

		final TableColumn<WordOrderElementBean, Integer> indexCol = new TableColumn<>("#");
		indexCol.setCellValueFactory(row -> new ReadOnlyObjectWrapper<Integer>(rowElements.indexOf(row.getValue()) + 1));
		final TableColumn<WordOrderElementBean, String> classCol = new TableColumn<>(labels.getString("analysis.details.wordorder.col.class"));
		classCol.setCellValueFactory(new PropertyValueFactory<>("className"));
		final TableColumn<WordOrderElementBean, String> scoreCol = new TableColumn<>(labels.getString("analysis.details.wordorder.col.score"));
		scoreCol.setCellValueFactory(new PropertyValueFactory<>("score"));

		final TableView<WordOrderElementBean> wordOrderTable = new TableView<>();
		wordOrderTable.getColumns().add(indexCol);
		wordOrderTable.getColumns().add(classCol);
		wordOrderTable.getColumns().add(scoreCol);
		wordOrderTable.getItems().addAll(rowElements);

		return wordOrderTable;
	}

	public static final class ViterbiPathElementBean {
		private final String emission;
		private final String viterbiState;
		private final BigDecimal viterbiValue;

		public ViterbiPathElementBean(final String emission, final String viterbiState, final BigDecimal viterbiValue) {
			this.emission = emission;
			this.viterbiState = viterbiState;
			this.viterbiValue = viterbiValue.setScale(10, RoundingMode.HALF_UP);
		}

		public String getEmission() {
			return emission;
		}

		public String getViterbiState() {
			return viterbiState;
		}

		public BigDecimal getViterbiValue() {
			return viterbiValue;
		}
	}

	public static final class WordOrderElementBean {
		private final String className;
		private final Long score;

		public WordOrderElementBean(final String className, final Long score) {
			this.className = className;
			this.score = score;
		}

		public String getClassName() {
			return className;
		}

		public Long getScore() {
			return score;
		}
	}
}
