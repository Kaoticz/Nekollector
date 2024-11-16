package com.github.kaoticz.nekollector;

import com.github.kaoticz.nekollector.api.models.ApiResult;
import com.github.kaoticz.nekollector.api.nekosia.services.NekosiaService;
import com.github.kaoticz.nekollector.common.Statics;
import com.github.kaoticz.nekollector.common.Utilities;
import com.github.kaoticz.nekollector.config.SettingsManager;
import com.github.kaoticz.nekollector.services.ApiCoordinator;
import com.github.kaoticz.nekollector.services.FavoritesManager;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class MainController {
    private final SettingsManager settingsManager = new SettingsManager();

    private final FavoritesManager favoritesManager = new FavoritesManager(settingsManager);

    private final ApiCoordinator apiCoordinator = new ApiCoordinator(
            new NekosiaService()
    );

    private boolean isLoading;

    @FXML
    private TextField titleBar;

    @FXML
    private VBox sideBarContainer;

    @FXML
    private StackPane imageContainer;

    @FXML
    private ImageView imageView;

    @FXML
    private Button favoriteButton;

    @FXML
    private Button previousButton;

    @FXML
    private Button downloadButton;

    @FXML
    private Button nextButton;

    /**
     * This method is run when the window is loaded for the first time.
     */
    @FXML
    public void initialize() {
        Platform.runLater(() -> {
            var window = this.imageView.getScene().getWindow();

            window.widthProperty().addListener((_, _, _) -> {
                if (!isLoading) {
                    this.imageView.setFitWidth(this.imageContainer.getWidth() - 10);
                }
            });
            window.heightProperty().addListener((_, _, _) -> {
                if (!isLoading) {
                    this.imageView.setFitHeight(this.imageContainer.getHeight() - 10);
                }
            });

            loadNextImage();
            populateFavoriteButtons();
        });
    }

    /**
     * Populates the sidebar with buttons for the favorites saved in the settings file.
     */
    private void populateFavoriteButtons() {
        // Create a copy of the favorites to avoid concurrency issues
        var favorites = Map.copyOf(this.settingsManager.getSettings().getFavorites());
        var counter = 0;

        for (var favorite : favorites.entrySet()) {
            var mockApiResult = new ApiResult(favorite.getValue(), Statics.LOADING_IMAGE);
            var stackPane = this.favoritesManager.createFavoriteContainer(
                    mockApiResult,
                    this.sideBarContainer,
                    this.imageContainer,
                    this.imageView,
                    this.titleBar,
                    this.favoriteButton,
                    this.downloadButton
            );
            Utilities.getFavoriteButton(stackPane).setDisable(true);
            this.sideBarContainer.getChildren().add(stackPane);
            var innerCounter = counter++;

            CompletableFuture.runAsync(() -> {
                var apiResult = new ApiResult(favorite.getValue(), new Image(favorite.getKey()));
                this.favoritesManager.addFavorite(apiResult);

                // Set the view components in a JavaFX thread.
                Platform.runLater(() -> {
                    var updatedStackPanel = this.favoritesManager.createFavoriteContainer(
                            apiResult,
                            this.sideBarContainer,
                            this.imageContainer,
                            this.imageView,
                            this.titleBar,
                            this.favoriteButton,
                            this.downloadButton
                    );
                    Utilities.getFavoriteButton(updatedStackPanel).setDisable(false);
                    this.sideBarContainer.getChildren().set(innerCounter, updatedStackPanel);
                });
            });
        }
    }

    /**
     * Adds the current image as a favorite.
     * @param ignoredEvent The event arguments.
     */
    @FXML
    public void handleFavoriteButton(@NotNull ActionEvent ignoredEvent) {
        var imageUrl = this.imageView.getImage().getUrl();

        if (this.favoritesManager.isFavorite(imageUrl)) {
            // Remove favorite button
            this.sideBarContainer.getChildren()
                    .stream()
                    .filter(node -> node instanceof StackPane)
                    .map(node -> (StackPane)node)
                    .filter(stackPane -> stackPane.getChildren().stream().anyMatch(node -> node instanceof ImageView image && image.getImage().getUrl().equals(imageUrl)))
                    .findFirst()
                    .ifPresent(stackPane -> this.sideBarContainer.getChildren().remove(stackPane));

            this.favoritesManager.removeFavorite(imageUrl);
        } else {
            // Add favorite button
            var apiResult = new ApiResult(this.titleBar.getText(), this.imageView.getImage());
            this.favoritesManager.addFavorite(apiResult);

            var stackPane = this.favoritesManager.createFavoriteContainer(
                    apiResult,
                    this.sideBarContainer,
                    this.imageContainer,
                    this.imageView,
                    this.titleBar,
                    this.favoriteButton,
                    this.downloadButton
            );
            this.sideBarContainer.getChildren().add(stackPane);
        }

        this.favoriteButton.setText(getFavoriteButtonText(imageUrl));
        this.titleBar.setDisable(!this.favoritesManager.isFavorite(imageUrl));
    }

    /**
     * Loads the previous image in the view.
     * @param ignoredEvent The event arguments.
     */
    @FXML
    public void moveToPreviousImage(@NotNull ActionEvent ignoredEvent) {
        System.out.println("moveToPreviousImage press!");

        toggleAllButtons(false, true);
        var apiResult = this.apiCoordinator.getPreviousImage();


        setTitleBarText(apiResult);
        this.favoriteButton.setText(getFavoriteButtonText(apiResult.apiImage().getUrl()));
        Utilities.resizeImage(this.imageContainer, this.imageView, apiResult.apiImage());
        Utilities.deselectFavoriteButton(this.sideBarContainer);
    }

    /**
     * Loads the next image in the view.
     * @param ignoredEvent The event arguments.
     */
    @FXML
    public void moveToNextImage(@NotNull ActionEvent ignoredEvent) {
        System.out.println("moveToNextImage press!");
        loadNextImage();
    }

    /**
     * Saves the current image to the file system.
     * @param ignoredEvent The event arguments.
     */
    @FXML
    public void downloadImage(@NotNull ActionEvent ignoredEvent) {
        System.out.println("downloadImage press!");
    }

    @FXML
    public void updateFavoriteName(@NotNull KeyEvent ignoredEvent) {
        // Yes, there is no event for when the text field is selected or deselected, so
        // this will result in the settings file being serialized for every key stroke.
        // Too bad!
        var apiResult = new ApiResult(titleBar.getText(), imageView.getImage());
        this.favoritesManager.updateFavorite(imageView.getImage().getUrl(), apiResult);
    }

    /**
     * Prepares the view for an image change and loads the next image.
     */
    private void loadNextImage() {
        isLoading = true;
        this.imageView.setImage(Statics.LOADING_IMAGE);
        this.imageView.setFitHeight(Statics.LOADING_IMAGE.getHeight());
        this.titleBar.setText("...");
        this.titleBar.setDisable(true);
        toggleAllButtons(true, false);
        Utilities.deselectFavoriteButton(this.sideBarContainer);

        this.apiCoordinator.getNextImageAsync()
                .handle((apiResult, ex) -> {
                    if (ex == null) {
                        setTitleBarText(apiResult);
                        Utilities.resizeImage(this.imageContainer, this.imageView, apiResult.apiImage());
                        toggleAllButtons(false, true);

                        // The text for buttons can only be set by a JavaFX thread
                        Platform.runLater(() -> this.favoriteButton.setText(getFavoriteButtonText(apiResult.apiImage().getUrl())));
                    } else {
                        var errorCause = ex.fillInStackTrace().getCause();
                        var errorReason = (errorCause.getMessage() == null)
                                ? "Operation has timed out"
                                : errorCause.getMessage();

                        System.out.println(errorReason);
                        this.titleBar.setText("Request has failed: " + errorReason);
                        this.titleBar.setDisable(true);
                        this.nextButton.setDisable(false);

                        if (this.apiCoordinator.currentIndex() > 1) {
                            this.previousButton.setDisable(false);
                        }

                        // TODO: set error image here
                    }

                    Utilities.deselectFavoriteButton(this.sideBarContainer);
                    isLoading = false;

                    return apiResult;
                });
    }

    /**
     * Toggles all buttons in the view to the specified value.
     * @param disable Whether the buttons should be disabled or not.
     * @param protectPreviousButton Whether the "Previous" button should be toggled so it can be safely used.
     */
    private void toggleAllButtons(boolean disable, boolean protectPreviousButton) {
        this.favoriteButton.setDisable(disable);
        this.previousButton.setDisable(disable);
        this.downloadButton.setDisable(disable);
        this.nextButton.setDisable(disable);

        if (protectPreviousButton && !disable && apiCoordinator.currentIndex() > 1) {
            this.previousButton.setDisable(false);
        } else if (protectPreviousButton && apiCoordinator.currentIndex() < 1) {
            this.previousButton.setDisable(true);
        }
    }

    /**
     * Gets the appropriate text for the favorite button.
     * @param imageUrl The URL to the currently set image.
     * @return The string to display on the favorite button.
     */
    private String getFavoriteButtonText(String imageUrl) {
        return (this.favoritesManager.isFavorite(imageUrl))
                ? Statics.FAVORITE_BUTTON_TEXT
                : Statics.NOT_FAVORITE_BUTTON_TEXT;
    }

    private void setTitleBarText(@NotNull ApiResult apiResult) {
        var imageUrl = apiResult.apiImage().getUrl();
        var isFavorite = this.favoritesManager.isFavorite(imageUrl);
        this.titleBar.setDisable(!isFavorite);
        this.titleBar.setText(
                (isFavorite)
                        ? this.favoritesManager.getCachedFavorite(imageUrl).serviceName()
                        : apiResult.serviceName()
        );
    }
}