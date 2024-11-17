package com.github.kaoticz.nekollector.common;

import com.github.kaoticz.nekollector.api.models.ApiResult;
import com.github.kaoticz.nekollector.services.FavoritesManager;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Stream;

/**
 * Collection of helper methods.
 */
public class Utilities {
    /**
     * Resizes the specified image to the size of the container.
     * @param imageContainer The container of the image to be resized.
     * @param imageView The image view of the image to be resized.
     * @param image The image to be resized.
     */
    public static void resizeImage(@NotNull Pane imageContainer, @NotNull ImageView imageView, @NotNull Image image) {
        imageView.setImage(image);
        imageView.setFitWidth(imageContainer.getWidth());
        imageView.setFitHeight(imageContainer.getHeight());
    }

    /**
     * Gets all favorite buttons.
     * @param sideBarContainer The sidebar container.
     * @return A stream of all favorite buttons.
     */
    public static Stream<Button> getFavoriteButtons(@NotNull VBox sideBarContainer) {
        return sideBarContainer.getChildren()
                .stream()
                .filter(node -> node instanceof StackPane)
                .map(node -> (StackPane)node)
                .flatMap(node -> node.getChildren().stream())
                .filter(node -> node instanceof Button)
                .map(node -> (Button)node);
    }

    /**
     * Gets the button from the specified favorite container.
     * @param favoriteContainer The favorite container.
     * @return The favorite button.
     */
    public static Button getFavoriteButton(@NotNull StackPane favoriteContainer) {
        return (Button)favoriteContainer.getChildren()
                .filtered(node -> node instanceof Button)
                .getFirst();
    }

    /**
     * Deselect the currently selected button.
     * @param sideBarContainer The sidebar container.
     */
    public static void deselectFavoriteButton(@NotNull VBox sideBarContainer) {
        // Find the selected button and deselect it
        // Obs: If the user selects favorites too quickly, there might be a possibility
        // of more than one button being selected at a time, so we iterate through all
        // of them
        getFavoriteButtons(sideBarContainer)
                .filter(button -> button.getBorder() != Statics.TRANSPARENT_BORDER)
                .forEach(selectedButton -> selectedButton.setBorder(Statics.TRANSPARENT_BORDER));
    }

    /**
     * Sets the appropriate status and text to the title bar.
     * @param apiResult The currently set name and image.
     * @param favoritesManager The favorites' manager.
     * @param titleBar The title bar.
     */
    public static void setTitleBarText(@NotNull ApiResult apiResult, @NotNull FavoritesManager favoritesManager, @NotNull TextField titleBar) {
        var imageUrl = apiResult.apiImage().getUrl();
        var isFavorite = favoritesManager.isFavorite(imageUrl);
        titleBar.setDisable(!isFavorite);
        titleBar.setText(
                (isFavorite)
                        ? favoritesManager.getCachedFavorite(imageUrl).serviceName()
                        : apiResult.serviceName()
        );
    }
}