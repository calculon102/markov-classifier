package de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.javafx.analysis;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

import de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.analysis.AnalysisResult;
import de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.analysis.AnalysisSettings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Simple Bean to represent a single result of an analysis of an input file against the HMM of a class.
 *
 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
 */
public final class AnalysisResultBean implements Serializable {
	private static final long serialVersionUID = 20151129L;

	public static enum TYPE {
		ROOT, GROUP, RESULT
	}

	/** Name of the file to analyze. */
	private String filename;
	/** Name of the class for this analyisis. */
	private String className;
	/** Percent of given text to be of given class. */
	private BigDecimal score;
	/** Count of all emissions in this analyisis */
	private Long emissionCount;
	/** Best class by word-order-analysis. */
	private String wordOrderClass;
	/** Percentage of best word-order class. */
	private Double wordOrderPercentage;

	/** File-count for groups. */
	private Integer fileCount;
	/** Time-string for groups. */
	private String timeString;

	/** Type of this result-bean. */
	private final TYPE type;
	/** The concrete result. */
	private AnalysisResult result;
	/** All classes of the training-data. Sorted. Only in groups. */
	private List<String> trainingClasses;
	/** Statistics for a group-bean, which settings where used to generate this. */
	private AnalysisSettings analysisSettings;

	// Properties for JavaFX-TreeTable - May not be final becaus auf custom de-serialization.
	transient private StringProperty filenameProperty = new SimpleStringProperty("");
	transient private StringProperty classNameProperty = new SimpleStringProperty("");
	transient private ObjectProperty<BigDecimal> scoreProperty = new SimpleObjectProperty<>();
	transient private LongProperty emissionCountProperty = new SimpleLongProperty(Long.MIN_VALUE);
	transient private StringProperty wordOrderClassProperty = new SimpleStringProperty("");
	transient private DoubleProperty wordOrderPercentageProperty = new SimpleDoubleProperty(Double.NaN);
	transient private StringProperty minSigSettingsProperty = new SimpleStringProperty("");
	transient private StringProperty voidSigSettingsProperty = new SimpleStringProperty("");
	transient private StringProperty pseudoSigSettingsProperty = new SimpleStringProperty("");
	transient private LongProperty fileCountProperty = new SimpleLongProperty(Long.MIN_VALUE);
	transient private StringProperty timeStringProperty = new SimpleStringProperty("");

	public AnalysisResultBean(final String filename, final TYPE type) {
		this.filenameProperty().set(filename);
		this.type = type;
	}

	public static AnalysisResultBean createRoot(final String name) {
		return new AnalysisResultBean(name, TYPE.ROOT);
	}

	public static AnalysisResultBean createGroup(final String name, final Integer fileCount, final String timeString, final List<String> classes, final AnalysisSettings analysisSettings) {
		final AnalysisResultBean result = new AnalysisResultBean(name, TYPE.GROUP)
				.setAnalysisSettings(analysisSettings)
				.setTrainingClasses(classes);

		result.fileCountProperty.set(fileCount);
		result.timeStringProperty.set(timeString);

		return result;
	}

	public static AnalysisResultBean createGroup(final AnalysisResultBean existingGroup, final String className, final BigDecimal newScore) {
		final AnalysisResultBean result = new AnalysisResultBean(existingGroup.filenameProperty().get(), TYPE.GROUP)
				.setAnalysisSettings(existingGroup.analysisSettings)
				.setTrainingClasses(existingGroup.trainingClasses);

		result.classNameProperty.set(existingGroup.classNameProperty().get());
		result.fileCountProperty.set(existingGroup.fileCountProperty().get());
		result.scoreProperty.set(existingGroup.scoreProperty().get());
		result.timeStringProperty.set(existingGroup.timeStringProperty().get());

		return result;
	}

	public boolean isGroup() {
		return type == TYPE.GROUP;
	}

	public boolean isResult() {
		return type == TYPE.RESULT;
	}

	public boolean hasDetails() {
		return type == TYPE.RESULT && filenameProperty().get() != null && !filenameProperty().get().isEmpty() && classNameProperty().get() != null && result != null && result.emissions() != null && !result.emissions().isEmpty();
	}

	public AnalysisResult getResult() {
		return result;
	}

	public AnalysisResultBean setResult(final AnalysisResult result) {
		this.result = result;
		return this;
	}

	public List<String> getTrainingClasses() {
		return trainingClasses;
	}

	public AnalysisResultBean setTrainingClasses(final List<String> trainingClasses) {
		this.trainingClasses = trainingClasses;
		return this;
	}

	public AnalysisSettings getAnalysisSettings() {
		return analysisSettings;
	}

	private AnalysisResultBean setAnalysisSettings(final AnalysisSettings analysisSettings) {
		this.analysisSettings = analysisSettings;

		minSigSettingsProperty.set(String.valueOf(analysisSettings.minSig()));
		voidSigSettingsProperty.set(String.valueOf(analysisSettings.voidSig()));
		pseudoSigSettingsProperty.set(String.valueOf(analysisSettings.pseudoSig()));

		return this;
	}

	@Override
	public String toString() {
		return "AnalysisResultBean [filename=" + filename + ", className=" + className + ", score=" + score + ", fileCount=" + fileCount + ", timeString=" + timeString + ", type=" + type + ", result=" + result + ", emissionCount=" + emissionCount + ", wordOrderClass=" + wordOrderClass + ", wordOrderPercentage="
				+ wordOrderPercentage + ", trainingClasses=" + trainingClasses + ", analysisSettings=" + analysisSettings + "]";
	}

	public StringProperty filenameProperty() {
		return filenameProperty;
	}

	public StringProperty classNameProperty() {
		return classNameProperty;
	}

	public ObjectProperty<BigDecimal> scoreProperty() {
		return scoreProperty;
	}

	public LongProperty emissionCountProperty() {
		return emissionCountProperty;
	}

	public StringProperty wordOrderClassProperty() {
		return wordOrderClassProperty;
	}

	public DoubleProperty wordOrderPercentageProperty() {
		return wordOrderPercentageProperty;
	}

	public StringProperty minSigSettingsProperty() {
		return minSigSettingsProperty;
	}

	public StringProperty voidSigSettingsProperty() {
		return voidSigSettingsProperty;
	}

	public StringProperty pseudoSigSettingsProperty() {
		return pseudoSigSettingsProperty;
	}

	public LongProperty fileCountProperty() {
		return fileCountProperty;
	}

	public StringProperty timeStringProperty() {
		return timeStringProperty;
	}

	/**
	 * Custom serialization. JavaFX-properties are not serializable, so the values are stored in mostly primitve members and simple objects.
	 * @param out stream to write to.
	 * @throws IOException
	 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
	 */
	private void writeObject(final ObjectOutputStream out) throws IOException {
		this.filename = filenameProperty.get();
		this.className = classNameProperty.get();
		this.score = scoreProperty.get();
		this.emissionCount = emissionCountProperty.get();
		this.wordOrderClass = wordOrderClassProperty.get();
		this.wordOrderPercentage = wordOrderPercentageProperty.get();
		this.fileCount = (int) fileCountProperty.get();
		this.timeString = timeStringProperty.get();

		out.defaultWriteObject();
	}

	/**
	 * Custom de-serialization. JavaFX-properties are not serializable, values are stored and read from mostly primitve members and simple objects.
	 * @param in
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
	 */
	private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();

		filenameProperty = new SimpleStringProperty(filename);
		classNameProperty = new SimpleStringProperty(className);
		scoreProperty = new SimpleObjectProperty<>(score);
		emissionCountProperty = new SimpleLongProperty(emissionCount == null ? Long.MIN_VALUE : emissionCount);
		wordOrderClassProperty = new SimpleStringProperty(wordOrderClass == null ? "" : wordOrderClass);
		wordOrderPercentageProperty = new SimpleDoubleProperty(wordOrderPercentage == null ? Double.NaN : wordOrderPercentage);
		minSigSettingsProperty = new SimpleStringProperty(analysisSettings == null ? "" : String.valueOf(analysisSettings.minSig()));
		voidSigSettingsProperty = new SimpleStringProperty(analysisSettings == null ? "" : String.valueOf(analysisSettings.voidSig()));
		pseudoSigSettingsProperty = new SimpleStringProperty(analysisSettings == null ? "" : String.valueOf(analysisSettings.pseudoSig()));
		fileCountProperty = new SimpleLongProperty(fileCount == null ? Long.MIN_VALUE : fileCount);
		timeStringProperty = new SimpleStringProperty(timeString == null ? "" : timeString);
	}
}
