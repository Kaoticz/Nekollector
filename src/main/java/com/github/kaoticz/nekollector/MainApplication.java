package com.github.kaoticz.nekollector;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Objects;

/**
 * The entry point of the application.
 */
public class MainApplication extends Application {

    /**
     * The entry point for JavaFX.
     * @param stage the primary stage for this application, onto which
     * the application scene can be set.
     * Applications may create other stages, if needed, but they will not be
     * primary stages.
     * @throws IOException Occurs when loading the view fails.
     */
    @Override
    public void start(@NotNull Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("main-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 320, 240);
        stage.setTitle("Nekollector");
        stage.getIcons().add(new Image(Objects.requireNonNull(MainApplication.class.getResourceAsStream("/assets/images/program_icon.png"))));
        stage.setMinHeight(300);
        stage.setMinWidth(400);
        stage.setHeight(700);
        stage.setWidth(800);
        stage.setScene(scene);
        stage.show();
    }

    /**
     * The entry point of the application.
     * @param args Unused command-line arguments.
     */
    public static void main(String[] args) {
        launch();
    }
}