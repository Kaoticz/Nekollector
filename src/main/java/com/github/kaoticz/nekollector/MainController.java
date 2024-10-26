package com.github.kaoticz.nekollector;

import com.github.kaoticz.nekollector.common.Statics;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

public class MainController {

    private static int favoriteCounter = 1;

    @FXML
    private VBox sideBarContainer;

    @FXML
    private ImageView imageView;

    @FXML
    public void initialize() {
        imageView.setImage(Statics.LOADING_IMAGE);
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
    }

    @FXML
    public void downloadImage(ActionEvent ignoredEvent) {
        System.out.println("downloadImage press!");
    }
}