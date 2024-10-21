package com.github.kaoticz.nekollector;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

public class MainController {

    private static int favoriteCounter = 1;

    @FXML
    private VBox sideBarContainer;

    @FXML
    public void addFavoriteButton(ActionEvent ignoredEvent) {
        var button = new Button("Favorite " + favoriteCounter++);
        sideBarContainer.getChildren().add(button);
    }
}