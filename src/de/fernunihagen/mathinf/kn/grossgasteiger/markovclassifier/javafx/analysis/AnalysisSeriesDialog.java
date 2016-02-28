package de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.javafx.analysis;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.analysis.AnalysisSeriesSettings;
import de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.analysis.AnalysisSettings;
import de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.javafx.common.ChooseDirectoryAction;
import de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.javafx.common.NumberTextField;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Window;
import javafx.util.Callback;

final class AnalysisSeriesDialog extends Dialog<AnalysisSeriesSettings> {
	private final ChooseDirectoryAction dataDirChooseAction;

	AnalysisSeriesDialog(final Window parent, final ResourceBundle labels) {
		dataDirChooseAction = new ChooseDirectoryAction(labels.getString("analysis.input.choose.title"));

		setTitle("Configure analyze-series"); // TRANSLATE
		setHeaderText("Given analyze-parameters are tested in all nine combinations against all direct sub-directories of the given parent directory."); // TRANSLATE
		setResizable(true);

		final TextField directory = new TextField();
		directory.setEditable(false);
		final Button dirChooseButton = new Button("...");
		dirChooseButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent event) {
				final File chosenDir = dataDirChooseAction.start(parent);
				if (chosenDir != null) {
					directory.setText(chosenDir.getAbsolutePath());
				}
			}
		});

		final NumberTextField minSig = new NumberTextField().setValue(2);
		final NumberTextField voidSig1 = new NumberTextField().setValue(2);
		final NumberTextField voidSig2 = new NumberTextField().setValue(6);
		final NumberTextField voidSig3 = new NumberTextField().setValue(10);
		final NumberTextField pseudoSig1 = new NumberTextField().setValue(0);
		final NumberTextField pseudoSig2 = new NumberTextField().setValue(2);
		final NumberTextField pseudoSig3 = new NumberTextField().setValue(6);

		final GridPane grid = new GridPane();
		grid.add(new Label("Base-directory:"), 1, 1); // TRANSLATE
		grid.add(directory, 2, 1);
		grid.add(dirChooseButton, 3, 1);
		grid.add(new Label("Sig.-threshold of training:"), 1, 2); // TRANSLATE
		grid.add(minSig, 2, 2);
		grid.add(new Label("void-Significance #1:"), 1, 3); // TRANSLATE
		grid.add(voidSig1, 2, 3);
		grid.add(new Label("void-Significance #2:"), 1, 4); // TRANSLATE
		grid.add(voidSig2, 2, 4);
		grid.add(new Label("void-Significance #3:"), 1, 5); // TRANSLATE
		grid.add(voidSig3, 2, 5);
		grid.add(new Label("pseudo-Significance #1:"), 1, 6); // TRANSLATE
		grid.add(pseudoSig1, 2, 6);
		grid.add(new Label("pseudo-Significance #2:"), 1, 7); // TRANSLATE
		grid.add(pseudoSig2, 2, 7);
		grid.add(new Label("pseudo-Significance #3:"), 1, 8); // TRANSLATE
		grid.add(pseudoSig3, 2, 8);

		getDialogPane().setContent(grid);

		final ButtonType buttonTypeOk = new ButtonType("OK", ButtonData.OK_DONE); // TRANSLATE
		getDialogPane().getButtonTypes().add(buttonTypeOk);
		final ButtonType buttonTypeCancel = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE); // TRANSLATE
		getDialogPane().getButtonTypes().add(buttonTypeCancel);

		setResultConverter(new Callback<ButtonType, AnalysisSeriesSettings>() {

			@Override
			public AnalysisSeriesSettings call(final ButtonType param) {
				if (param != buttonTypeOk) {
					return null;
				}

				final File file = new File(directory.getText());
				if (!file.exists() || !file.isDirectory()) {
					final Alert alert = new Alert(AlertType.ERROR, "Given base-path does not exist or is not a directory!"); // TRANSLATE
					alert.showAndWait();
					return null;
				}

				final List<AnalysisSettings> settings = new ArrayList<>(9);
				settings.add(new AnalysisSettings(minSig.getValue(), voidSig1.getValue(), pseudoSig1.getValue()));
				settings.add(new AnalysisSettings(minSig.getValue(), voidSig1.getValue(), pseudoSig2.getValue()));
				settings.add(new AnalysisSettings(minSig.getValue(), voidSig1.getValue(), pseudoSig3.getValue()));
				settings.add(new AnalysisSettings(minSig.getValue(), voidSig2.getValue(), pseudoSig1.getValue()));
				settings.add(new AnalysisSettings(minSig.getValue(), voidSig2.getValue(), pseudoSig2.getValue()));
				settings.add(new AnalysisSettings(minSig.getValue(), voidSig2.getValue(), pseudoSig3.getValue()));
				settings.add(new AnalysisSettings(minSig.getValue(), voidSig3.getValue(), pseudoSig1.getValue()));
				settings.add(new AnalysisSettings(minSig.getValue(), voidSig3.getValue(), pseudoSig2.getValue()));
				settings.add(new AnalysisSettings(minSig.getValue(), voidSig3.getValue(), pseudoSig3.getValue()));

				return new AnalysisSeriesSettings(file, settings);
			}
		});
	}
}
