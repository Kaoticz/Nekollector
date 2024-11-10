package com.github.kaoticz.nekollector;

import com.github.kaoticz.nekollector.api.nekosia.services.NekosiaService;
import com.github.kaoticz.nekollector.common.Statics;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class MainController {

    private static int favoriteCounter = 1;

    @FXML
    private VBox sideBarContainer;

    @FXML
    private ImageView imageView;

    @FXML
    private StackPane imageContainer;

    @FXML
    public void initialize() {
        imageView.setImage(Statics.LOADING_IMAGE);
        Platform.runLater(() -> {
            var stage = imageView.getScene().getWindow();

            stage.widthProperty().addListener((_, _, _) -> imageView.setFitWidth(imageContainer.getWidth() - 10));
            stage.heightProperty().addListener((_, _, _) -> imageView.setFitHeight(imageContainer.getHeight() - 10));
        });
    }

    @FXML
    public void addFavoriteButton(ActionEvent ignoredEvent) {
        var button = new Button("Favorite " + favoriteCounter++);
        this.sideBarContainer.getChildren().add(button);
    }

    @FXML
    public void moveToPreviousImage(ActionEvent ignoredEvent) {
        System.out.println("moveToPreviousImage press!");
    }

    @FXML
    public void moveToNextImage(ActionEvent ignoredEvent) {
        System.out.println("moveToNextImage press!");
        imageView.setImage(Statics.LOADING_IMAGE);

        var service = new NekosiaService();
        try {
            service.getRandomCatgirlAsync().thenAccept(imageView::setImage);
        }
        catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void downloadImage(ActionEvent ignoredEvent) {
        System.out.println("downloadImage press!");
    }
}