package com.github.kaoticz.nekollector.services;

import com.github.kaoticz.nekollector.api.models.ApiResult;
import com.github.kaoticz.nekollector.config.ConfigManager;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Paint;
import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;

public class FavoritesManager {
    private final HashMap<String, ApiResult> favoritesCache = new HashMap<>();

    private final ConfigManager configManager;

    public FavoritesManager(ConfigManager configManager) {
        this.configManager = Objects.requireNonNull(configManager);
    }

    public boolean isFavorite(String imageUrl) {
        return favoritesCache.containsKey(imageUrl);
    }

    public ApiResult getCachedFavorite(String imageUrl) {
        return Objects.requireNonNull(favoritesCache.get(imageUrl));
    }

    public boolean updateFavorite(String imageUrl, ApiResult apiResult) {
        if (!favoritesCache.containsKey(imageUrl)) {
            return false;
        }

        try {
            configManager.saveSettings(settings -> settings.getFavorites().put(imageUrl, apiResult.serviceName()));
        } catch (IOException e) {
            return false;
        }

        favoritesCache.put(imageUrl, apiResult);
        return true;
    }

    public boolean addFavorite(ApiResult apiResult) {
        var imageUrl = apiResult.apiImage().getUrl();

        if (isFavorite(imageUrl)) {
            return false;
        }

        try {
            configManager.saveSettings(settings -> settings.getFavorites().put(imageUrl, apiResult.serviceName()));
        } catch (IOException e) {
            return false;
        }

        favoritesCache.put(imageUrl, apiResult);
        return true;
    }

    public boolean removeFavorite(String imageUrl) {
        if (!isFavorite(imageUrl)) {
            return false;
        }

        try {
            configManager.saveSettings(settings -> settings.getFavorites().remove(imageUrl));
        } catch (IOException e) {
            return false;
        }

        favoritesCache.remove(imageUrl);
        return true;
    }

    public StackPane createFavoriteContainer(ApiResult apiResult) {
        var stackPane = new StackPane();

        var thumbnail = new ImageView(apiResult.apiImage());
        thumbnail.setFitHeight(100);
        thumbnail.setFitWidth(100);
        thumbnail.setPreserveRatio(true);

        var button = new Button();
        button.backgroundProperty().set(new Background(new BackgroundFill(Paint.valueOf("transparent"), CornerRadii.EMPTY, Insets.EMPTY)));
        button.setBorder(new Border(new BorderStroke(Paint.valueOf("transparent"), BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderStroke.MEDIUM)));
        button.setDisable(true);
        button.setPrefWidth(100);
        button.setPrefHeight(thumbnail.getFitHeight());

        stackPane.getChildren().add(thumbnail);
        stackPane.getChildren().add(button);

        return stackPane;
    }
}