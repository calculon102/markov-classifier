package de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.javafx;

import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.logging.FileLogger;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * Entry-point for JavaFX-Application.
 *
 * @author Frank Großgasteiger <frank.grossgasteiger@fernuni-hagen.de>
 */
public final class Main extends Application {
	/** Logger-Instance */
	private static final Logger LOGGER = Logger.getLogger(Application.class.getName());

	@Override
	public void start(final Stage primaryStage) throws Exception {
		final ResourceBundle resourceBundle = ResourceBundle.getBundle("de.fernunihagen.mathinf.kn.grossgasteiger.markovclassifier.javafx.MainWindow");
		//
		primaryStage.setTitle(resourceBundle.getString("app.title") + " - " + resourceBundle.getString("training.session.unsaved.title"));
		//
		final FXMLLoader mainWindowLoader = new FXMLLoader(getClass().getResource("MainWindow.fxml"), resourceBundle);
		final Scene scene = mainWindowLoader.load();
		primaryStage.setScene(scene);
		//
		primaryStage.getIcons().addAll(
				new Image(Main.class.getResourceAsStream("icons/app.svg")),
				new Image(Main.class.getResourceAsStream("icons/app_16.png")),
				new Image(Main.class.getResourceAsStream("icons/app_24.png")),
				new Image(Main.class.getResourceAsStream("icons/app_32.png")),
				new Image(Main.class.getResourceAsStream("icons/app_64.png")));
		//
		primaryStage.show();
		//
		final MainController mainController = mainWindowLoader.getController();
		mainController.setStage(primaryStage);
		mainController.setHostServices(getHostServices());
	}

	public static void main(final String[] args) {
		FileLogger.setup(Level.INFO);
		LOGGER.info("Starting application.");
		//
		launch(args);
	}
}
