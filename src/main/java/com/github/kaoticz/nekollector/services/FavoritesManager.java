package com.github.kaoticz.nekollector.services;

import com.github.kaoticz.nekollector.api.models.ApiResult;
import com.github.kaoticz.nekollector.common.Statics;
import com.github.kaoticz.nekollector.common.Utilities;
import com.github.kaoticz.nekollector.config.SettingsManager;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Paint;
import org.jetbrains.annotations.NotNull;
import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;

/**
 * Handles addition and removal of favorite images.
 */
public class FavoritesManager {
    private final HashMap<String, ApiResult> favoritesCache = new HashMap<>();

    private final SettingsManager settingsManager;

    /**
     * Initializes an object that handles addition and removal of favorite images.
     * @param settingsManager The settings manager.
     */
    public FavoritesManager(@NotNull SettingsManager settingsManager) {
        this.settingsManager = Objects.requireNonNull(settingsManager);
    }

    /**
     * Determines whether the provided URL is from a favorite image.
     * @param imageUrl The URL to the image.
     * @return True if the image is a favorite, false otherwise.
     */
    public boolean isFavorite(String imageUrl) {
        return imageUrl != null && this.favoritesCache.containsKey(imageUrl);
    }

    /**
     * Gets the favorite for the provided URL.
     * @param imageUrl The URL to the image.
     * @return The cached favorite.
     */
    public @NotNull ApiResult getCachedFavorite(@NotNull String imageUrl) {
        return Objects.requireNonNull(this.favoritesCache.get(imageUrl));
    }

    /**
     * Updates the favorite with the specified URL.
     * @param imageUrl The URL of the image.
     * @param apiResult The new value of the favorite.
     * @return True if the favorite got updated successfully, false otherwise.
     */
    public boolean updateFavorite(@NotNull String imageUrl, @NotNull ApiResult apiResult) {
        if (!this.favoritesCache.containsKey(imageUrl)) {
            return false;
        }

        try {
            this.settingsManager.saveSettings(settings -> settings.getFavorites().put(imageUrl, apiResult.serviceName()));
        } catch (IOException e) {
            return false;
        }

        this.favoritesCache.put(imageUrl, apiResult);
        return true;
    }

    /**
     * Adds a favorite.
     * @param apiResult The favorite to be added.
     * @return True if the favorite got added successfully, false otherwise.
     */
    public boolean addFavorite(@NotNull ApiResult apiResult) {
        var imageUrl = apiResult.apiImage().getUrl();

        if (isFavorite(imageUrl)) {
            return false;
        }

        try {
            this.settingsManager.saveSettings(settings -> settings.getFavorites().put(imageUrl, apiResult.serviceName()));
        } catch (IOException e) {
            return false;
        }

        this.favoritesCache.put(imageUrl, apiResult);
        return true;
    }

    /**
     * Removes the favorite with the specified URL.
     * @param imageUrl The URL to the image.
     * @return True if the favorite got removed successfully, false otherwise.
     */
    public boolean removeFavorite(@NotNull String imageUrl) {
        if (!isFavorite(imageUrl)) {
            return false;
        }

        try {
            this.settingsManager.saveSettings(settings -> settings.getFavorites().remove(imageUrl));
        } catch (IOException e) {
            return false;
        }

        this.favoritesCache.remove(imageUrl);
        return true;
    }

    /**
     * Creates a favorite entry for the program's sidebar.
     * @param apiResult The favorite to be displayed.
     * @param sideBarContainer The master container of the sidebar.
     * @param mainImageContainer The container that holds the image view.
     * @param mainImageView The image view that renders the main image.
     * @param titleBar The title bar of the application.
     * @return The favorite view entry.
     */
    public @NotNull StackPane createFavoriteContainer(
            @NotNull ApiResult apiResult,
            @NotNull VBox sideBarContainer,
            @NotNull StackPane mainImageContainer,
            @NotNull ImageView mainImageView,
            @NotNull TextField titleBar,
            @NotNull Button addFavoriteButton,
            @NotNull Button downloadButton
    ) {
        var stackPane = new StackPane();
        stackPane.setBorder(Statics.DESELECTION_BORDER);
        stackPane.setPrefHeight(100);
        stackPane.setPrefWidth(100);

        var thumbnail = new ImageView(apiResult.apiImage());
        thumbnail.setFitHeight(93);
        thumbnail.setFitWidth(93);
        thumbnail.setPreserveRatio(true);

        var button = new Button();
        button.setBackground(new Background(new BackgroundFill(Paint.valueOf("717171"), Statics.SMOOTH_CORNER, Insets.EMPTY)));
        button.setOpacity(0.08);
        button.setCursor(Cursor.HAND);
        button.setDisable(false);
        button.setPrefWidth(100);
        button.setPrefHeight(100);

        setFavoriteButtonAction(
                sideBarContainer,
                mainImageContainer,
                mainImageView,
                titleBar,
                addFavoriteButton,
                downloadButton,
                button,
                thumbnail,
                apiResult.apiImage().getUrl()
        );

        stackPane.getChildren().add(thumbnail);
        stackPane.getChildren().add(button);

        return stackPane;
    }

    /**
     * Defines the behavior of the favorite button.
     * @param sideBarContainer The master container of the sidebar.
     * @param mainImageContainer The container that holds the image view.
     * @param mainImageView The image view that renders the main image.
     * @param titleBar The title bar of the application.
     * @param addFavoriteButton The button that adds or removes favorites.
     * @param downloadButton The button that locally saves the current image.
     * @param button The favorite button.
     * @param thumbnail The thumbnail for the favorite image.
     * @param imageUrl The URL to the image.
     */
    private void setFavoriteButtonAction(
            @NotNull VBox sideBarContainer,
            @NotNull StackPane mainImageContainer,
            @NotNull ImageView mainImageView,
            @NotNull TextField titleBar,
            @NotNull Button addFavoriteButton,
            @NotNull Button downloadButton,
            @NotNull Button button,
            @NotNull ImageView thumbnail,
            String imageUrl
    ) {
        button.setOnMousePressed(_ -> {
            thumbnail.setFitWidth(thumbnail.getFitWidth() - 1);
            thumbnail.setFitHeight(thumbnail.getFitHeight() - 1);
        });
        button.setOnMouseReleased(_ -> {
            thumbnail.setFitWidth(thumbnail.getFitWidth() + 1);
            thumbnail.setFitHeight(thumbnail.getFitHeight() + 1);
        });
        button.setOnMouseClicked(_ -> {
            Utilities.deselectFavoriteButton(sideBarContainer);

            // Apply a border to the container of the selected button
            ((Pane)button.getParent()).setBorder(Statics.SELECTION_BORDER);

            // Enable the favorite and download buttons
            addFavoriteButton.setText(Statics.FAVORITE_BUTTON_TEXT);
            addFavoriteButton.setDisable(false);
            downloadButton.setDisable(false);

            // Display the selected image
            var favorite = getCachedFavorite(imageUrl);
            titleBar.setText(favorite.serviceName());
            titleBar.setDisable(!isFavorite(favorite.apiImage().getUrl()));
            Utilities.resizeImageToContainer(mainImageContainer, mainImageView, favorite.apiImage());
        });
    }
}