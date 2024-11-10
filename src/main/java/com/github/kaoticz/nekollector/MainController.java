package com.github.kaoticz.nekollector;

import com.github.kaoticz.nekollector.api.abstractions.ApiService;
import com.github.kaoticz.nekollector.api.nekosia.services.NekosiaService;
import com.github.kaoticz.nekollector.common.Statics;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import java.util.Random;

public class MainController {

    private static int favoriteCounter = 1;

    private static final Random RANDOM = new Random();

    private static final ApiService[] API_SERVICES = new ApiService[] {
            new NekosiaService()
    };

    @FXML
    private TextField titleBar;

    @FXML
    private VBox sideBarContainer;

    @FXML
    private ImageView imageView;

    @FXML
    private StackPane imageContainer;

    @FXML
    public void initialize() {
        Platform.runLater(() -> {
            loadNextImage();
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
        loadNextImage();
    }

    @FXML
    public void downloadImage(ActionEvent ignoredEvent) {
        System.out.println("downloadImage press!");
    }

    private void loadNextImage() {
        imageView.setImage(Statics.LOADING_IMAGE);
        imageView.setFitHeight(Statics.LOADING_IMAGE.getHeight());
        titleBar.setText("...");

        var randomApiService = API_SERVICES[RANDOM.nextInt(API_SERVICES.length)];
        randomApiService.getImageAsync()
                .thenAccept(imageView::setImage)
                .thenAccept(_ -> imageView.setFitHeight(imageContainer.getHeight()))
                .handle((result, ex) -> {
                    if (ex == null) {
                        titleBar.setText(randomApiService.getServiceName());
                    } else {
                        titleBar.setText("Request to " + randomApiService.getServiceName() + " has failed");
                        System.out.println(ex.fillInStackTrace());
                    }

                    return result;
                });
    }
}