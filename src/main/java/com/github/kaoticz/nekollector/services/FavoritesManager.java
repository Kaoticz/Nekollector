package com.github.kaoticz.nekollector.services;

import com.github.kaoticz.nekollector.api.models.ApiResult;
import com.github.kaoticz.nekollector.config.ConfigManager;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;

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

    public StackPane createFavoriteButton(ApiResult apiResult) {
        var stackPane = new StackPane();
        stackPane.setAlignment(Pos.BASELINE_CENTER);

        var thumbnail = new ImageView(apiResult.apiImage());
        thumbnail.setFitWidth(100);
        thumbnail.setPreserveRatio(true);

        var backgroundImage = new BackgroundImage(apiResult.apiImage(), BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, BackgroundSize.DEFAULT);
        var button = new Button();
        button.backgroundProperty().set(new Background(backgroundImage));
        button.setAlignment(Pos.BASELINE_CENTER);
        button.setPrefWidth(100);

        stackPane.getChildren().add(thumbnail);
        stackPane.getChildren().add(button);

        return stackPane;
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
}